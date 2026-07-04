'use strict';

const aiService = require('./aiService');
const vocabularyRepository = require('../repositories/vocabularyRepository');
const logger = require('../config/logger');

/**
 * Extracts new/useful vocabulary and idioms from the AI's replies in a
 * conversation, so the learner can review them later. Uses the AI itself
 * to pick words that are genuinely useful to a learner (not every common
 * word), along with a meaning, Tamil meaning, pronunciation guide, and a
 * usage example.
 *
 * @param {string} userId
 * @param {string[]} aiTexts - the AI's message texts from the conversation
 * @returns {Promise<Array>} the vocabulary entries that were newly stored
 */
async function extractAndStoreVocabulary(userId, aiTexts) {
  if (!aiTexts || aiTexts.length === 0) return [];

  const combinedText = aiTexts.join('\n');

  const raw = await aiService.completeJSON(
    [
      {
        role: 'system',
        content: `You help English learners build vocabulary. From the given AI-friend chat messages, pick up to 5 useful English words or idioms that would be valuable for an English learner to study (skip trivial words like "the", "is").
Respond ONLY with JSON:
{ "items": [ { "word": string, "meaning": string, "tamilMeaning": string, "pronunciation": string, "usageExample": string } ] }`,
      },
      { role: 'user', content: combinedText },
    ],
    { temperature: 0.4, maxTokens: 600 }
  ).catch((err) => {
    logger.error(`vocabularyService: AI extraction failed: ${err.message}`);
    return '{"items": []}';
  });

  let items = [];
  try {
    items = JSON.parse(raw).items || [];
  } catch (err) {
    logger.error(`vocabularyService: failed to parse AI response: ${err.message}`);
    return [];
  }

  const stored = [];
  for (const item of items) {
    if (!item.word) continue;
    // eslint-disable-next-line no-await-in-loop
    const doc = await vocabularyRepository.upsert(userId, item.word, {
      meaning: item.meaning || '',
      tamilMeaning: item.tamilMeaning || '',
      pronunciation: item.pronunciation || '',
      usageExample: item.usageExample || '',
    });
    stored.push(doc);
  }

  return stored;
}

function listForUser(userId, options) {
  return vocabularyRepository.findByUser(userId, options);
}

module.exports = { extractAndStoreVocabulary, listForUser };
