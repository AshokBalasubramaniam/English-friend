'use strict';

const asyncHandler = require('../utils/asyncHandler');
const conversationService = require('../services/conversationService');
const messageRepository = require('../repositories/messageRepository');
const aiService = require('../services/aiService');
const translationService = require('../services/translationService');
const logger = require('../config/logger');

const listMessages = asyncHandler(async (req, res) => {
  const { limit, skip } = req.query;
  // Ensure the conversation belongs to the requester before listing.
  await conversationService.getConversation(req.user._id, req.params.conversationId);
  const messages = await messageRepository.findByConversation(req.params.conversationId, {
    limit: limit ? Number(limit) : undefined,
    skip: skip ? Number(skip) : undefined,
  });
  res.status(200).json({ success: true, data: { messages } });
});

/**
 * The core real, working OpenAI-streaming endpoint (HTTP/SSE version -
 * chatSocket.js provides the Socket.io equivalent for the app's live chat
 * UI). Persists the user's message, streams the AI's reply back to the
 * client token-by-token via Server-Sent Events, then persists the full
 * AI reply (with Tamil translation) once the stream completes.
 *
 * POST /api/v1/conversations/:conversationId/messages
 * body: { text: string }
 */
const sendMessage = asyncHandler(async (req, res) => {
  const { conversationId } = req.params;
  const { text } = req.body;

  await conversationService.addUserMessage(req.user._id, conversationId, text);
  const promptMessages = await conversationService.buildPromptMessages(req.user._id, conversationId);

  res.writeHead(200, {
    'Content-Type': 'text/event-stream',
    'Cache-Control': 'no-cache',
    Connection: 'keep-alive',
    'X-Accel-Buffering': 'no',
  });

  const sendEvent = (event, data) => {
    res.write(`event: ${event}\ndata: ${JSON.stringify(data)}\n\n`);
  };

  try {
    const fullReply = await aiService.streamReply(promptMessages, (chunk) => {
      sendEvent('chunk', { text: chunk });
    });

    let tamilTranslation = '';
    try {
      tamilTranslation = await translationService.translateToTamil(fullReply);
    } catch (err) {
      logger.warn(`sendMessage: translation failed, continuing without it: ${err.message}`);
    }

    const aiMessage = await conversationService.addAiMessage(conversationId, fullReply, tamilTranslation);

    sendEvent('done', { message: aiMessage });
    res.end();
  } catch (err) {
    logger.error(`sendMessage streaming failed: ${err.message}`, { stack: err.stack });
    sendEvent('error', { message: `Failed to generate AI reply: ${err.message}` });
    res.end();
  }
});

module.exports = { listMessages, sendMessage };
