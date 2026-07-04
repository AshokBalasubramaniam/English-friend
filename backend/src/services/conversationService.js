'use strict';

const conversationRepository = require('../repositories/conversationRepository');
const messageRepository = require('../repositories/messageRepository');
const userRepository = require('../repositories/userRepository');
const progressRepository = require('../repositories/progressRepository');
const ApiError = require('../utils/ApiError');
const aiService = require('./aiService');
const correctionService = require('./correctionService');
const scoringService = require('./scoringService');
const vocabularyService = require('./vocabularyService');
const logger = require('../config/logger');

async function startConversation(userId, mode = 'english') {
  const conversation = await conversationRepository.create({ userId, mode });
  return conversation;
}

async function getConversation(userId, conversationId) {
  const conversation = await conversationRepository.findById(conversationId);
  if (!conversation) throw ApiError.notFound('Conversation not found');
  if (String(conversation.userId) !== String(userId)) {
    throw ApiError.forbidden('You do not have access to this conversation');
  }
  return conversation;
}

async function listConversations(userId, options) {
  return conversationRepository.findByUser(userId, options);
}

/**
 * Persists the user's message, runs (non-blocking) grammar correction on
 * it, and returns the stored message. The AI reply itself is generated
 * separately by aiService.streamReply (see chatSocket.js / messageController.js)
 * so callers can stream the reply chunk-by-chunk.
 */
async function addUserMessage(userId, conversationId, text) {
  const conversation = await getConversation(userId, conversationId);
  if (conversation.endedAt) throw ApiError.badRequest('Conversation has already ended');

  const message = await messageRepository.create({
    conversationId,
    sender: 'user',
    englishText: text,
  });

  // Fire-and-forget grammar analysis - does not block the chat reply.
  correctionService.correctAndStore(message._id, text).catch((err) => {
    logger.error(`Background correction failed for message ${message._id}: ${err.message}`);
  });

  return message;
}

async function addAiMessage(conversationId, text, tamilTranslation = '') {
  return messageRepository.create({
    conversationId,
    sender: 'ai',
    englishText: text,
    tamilTranslation,
  });
}

/**
 * Builds the message list (system prompt + recent history) to send to
 * OpenAI for generating the AI's next reply in a conversation.
 */
async function buildPromptMessages(userId, conversationId) {
  const user = await userRepository.findById(userId);
  const conversation = await getConversation(userId, conversationId);
  const history = await messageRepository.findByConversation(conversationId, { limit: 30 });

  const systemPrompt = aiService.buildSystemPrompt(user.memory, conversation.mode);

  const chatMessages = history.map((m) => ({
    role: m.sender === 'user' ? 'user' : 'assistant',
    content: m.englishText,
  }));

  return [{ role: 'system', content: systemPrompt }, ...chatMessages];
}

/**
 * Ends a conversation, triggers scoring, and extracts vocabulary from the
 * AI's messages for the user's vocabulary list. Also bumps today's
 * Progress record.
 */
async function endConversation(userId, conversationId) {
  const conversation = await getConversation(userId, conversationId);
  if (conversation.endedAt) return conversation;

  const scores = await scoringService.scoreConversation(conversationId);
  const updated = await conversationRepository.endConversation(conversationId, scores);

  const messages = await messageRepository.findByConversation(conversationId, { limit: 500 });
  const aiTexts = messages.filter((m) => m.sender === 'ai').map((m) => m.englishText);

  vocabularyService.extractAndStoreVocabulary(userId, aiTexts).catch((err) => {
    logger.error(`Vocabulary extraction failed for conversation ${conversationId}: ${err.message}`);
  });

  const practiceMinutes = Math.max(
    1,
    Math.round((Date.now() - new Date(conversation.startedAt).getTime()) / 60000)
  );
  await progressRepository.incrementForDate(userId, new Date(), { practiceMinutes });

  return updated;
}

module.exports = {
  startConversation,
  getConversation,
  listConversations,
  addUserMessage,
  addAiMessage,
  buildPromptMessages,
  endConversation,
};
