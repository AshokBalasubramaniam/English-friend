'use strict';

const conversationService = require('../services/conversationService');
const aiService = require('../services/aiService');
const translationService = require('../services/translationService');
const logger = require('../config/logger');

/**
 * Registers the real-time chat handlers for one authenticated socket.
 *
 * Client -> Server: 'user_message' { conversationId, text }
 * Server -> Client: 'ai_reply_chunk' { text }   (streamed, many times)
 * Server -> Client: 'ai_reply_done'  { message } (once, at the end)
 * Server -> Client: 'ai_reply_error' { message }
 */
function registerChatSocket(io, socket) {
  socket.on('user_message', async ({ conversationId, text } = {}) => {
    try {
      if (!conversationId || !text) {
        socket.emit('ai_reply_error', { message: 'conversationId and text are required' });
        return;
      }

      await conversationService.addUserMessage(socket.user._id, conversationId, text);
      const promptMessages = await conversationService.buildPromptMessages(
        socket.user._id,
        conversationId
      );

      const fullReply = await aiService.streamReply(promptMessages, (chunk) => {
        socket.emit('ai_reply_chunk', { text: chunk });
      });

      let tamilTranslation = '';
      try {
        tamilTranslation = await translationService.translateToTamil(fullReply);
      } catch (err) {
        logger.warn(`chatSocket: translation failed, continuing without it: ${err.message}`);
      }

      const aiMessage = await conversationService.addAiMessage(
        conversationId,
        fullReply,
        tamilTranslation
      );

      socket.emit('ai_reply_done', { message: aiMessage });
    } catch (err) {
      logger.error(`chatSocket user_message failed: ${err.message}`);
      socket.emit('ai_reply_error', { message: 'Failed to generate AI reply' });
    }
  });
}

module.exports = registerChatSocket;
