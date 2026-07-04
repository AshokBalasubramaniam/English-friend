'use strict';

const aiService = require('./aiService');
const correctionRepository = require('../repositories/correctionRepository');
const logger = require('../config/logger');

/**
 * Asks the model for a structured grammar-correction JSON for a single
 * user utterance. Returns null when the utterance had no meaningful
 * mistake worth flagging.
 *
 * @param {string} utterance - the raw text the user said/typed
 * @returns {Promise<{corrected: string, reason: string, example: string, difficultyLevel: string} | null>}
 */
async function analyzeUtterance(utterance) {
  const messages = [
    {
      role: 'system',
      content: `You are an English grammar coach. Given a sentence spoken by an English learner, decide if it has a grammar, tense, word-choice, or preposition mistake.
Respond ONLY with a JSON object with this exact shape:
{
  "hasMistake": boolean,
  "corrected": string,        // the corrected sentence (same as original if hasMistake is false)
  "reason": string,           // one short, friendly sentence explaining the fix (empty string if no mistake)
  "example": string,          // one additional example sentence using the correct pattern (empty string if no mistake)
  "difficultyLevel": string   // one of "beginner", "intermediate", "advanced" - the level of the grammar point involved
}
Do not include any text outside the JSON object.`,
    },
    { role: 'user', content: utterance },
  ];

  try {
    const raw = await aiService.completeJSON(messages, { temperature: 0.2, maxTokens: 300 });
    const parsed = JSON.parse(raw);

    if (!parsed.hasMistake) return null;

    return {
      original: utterance,
      corrected: parsed.corrected || utterance,
      reason: parsed.reason || '',
      example: parsed.example || '',
      difficultyLevel: ['beginner', 'intermediate', 'advanced'].includes(parsed.difficultyLevel)
        ? parsed.difficultyLevel
        : 'beginner',
    };
  } catch (err) {
    logger.error(`correctionService.analyzeUtterance failed: ${err.message}`);
    return null;
  }
}

/**
 * Analyzes an utterance and, if a mistake is found, persists a Correction
 * document linked to the given message.
 */
async function correctAndStore(messageId, utterance) {
  const analysis = await analyzeUtterance(utterance);
  if (!analysis) return null;

  return correctionRepository.create({
    messageId,
    original: analysis.original,
    corrected: analysis.corrected,
    reason: analysis.reason,
    example: analysis.example,
    difficultyLevel: analysis.difficultyLevel,
  });
}

module.exports = { analyzeUtterance, correctAndStore };
