'use strict';

const asyncHandler = require('../utils/asyncHandler');
const conversationService = require('../services/conversationService');
const translationService = require('../services/translationService');
const logger = require('../config/logger');

const startConversation = asyncHandler(async (req, res) => {
  const { mode } = req.body;
  const conversation = await conversationService.startConversation(req.user._id, mode);

  // Generate the AI's opening greeting so the chat never starts on a blank screen - the
  // user shouldn't have to speak first. Best-effort: if this fails, the conversation still
  // starts fine, the app just falls back to its own empty-state placeholder.
  let greeting = null;
  try {
    const greetingText = await conversationService.generateOpeningMessage(req.user._id, conversation._id);
    let tamilTranslation = '';
    try {
      tamilTranslation = await translationService.translateToTamil(greetingText);
    } catch (err) {
      logger.warn(`startConversation: greeting translation failed, continuing without it: ${err.message}`);
    }
    greeting = await conversationService.addAiMessage(conversation._id, greetingText, tamilTranslation);
  } catch (err) {
    logger.error(`startConversation: opening greeting generation failed: ${err.message}`);
  }

  res.status(201).json({ success: true, data: { conversation, greeting } });
});

const listConversations = asyncHandler(async (req, res) => {
  const { limit, skip } = req.query;
  const conversations = await conversationService.listConversations(req.user._id, {
    limit: limit ? Number(limit) : undefined,
    skip: skip ? Number(skip) : undefined,
  });
  res.status(200).json({ success: true, data: { conversations } });
});

const getConversation = asyncHandler(async (req, res) => {
  const conversation = await conversationService.getConversation(req.user._id, req.params.conversationId);
  res.status(200).json({ success: true, data: { conversation } });
});

const endConversation = asyncHandler(async (req, res) => {
  const conversation = await conversationService.endConversation(req.user._id, req.params.conversationId);
  res.status(200).json({ success: true, data: { conversation } });
});

module.exports = { startConversation, listConversations, getConversation, endConversation };
