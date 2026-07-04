'use strict';

const aiService = require('./aiService');
const logger = require('../config/logger');

/**
 * Translates English -> Tamil using a dedicated, low-temperature prompt.
 * Returns Tamil script text.
 */
async function translateToTamil(englishText) {
  try {
    const raw = await aiService.completeJSON(
      [
        {
          role: 'system',
          content:
            'Translate the given English text into natural, conversational Tamil (Tamil script). Respond ONLY with JSON: { "translation": string }.',
        },
        { role: 'user', content: englishText },
      ],
      { temperature: 0.2, maxTokens: 400 }
    );
    const parsed = JSON.parse(raw);
    return parsed.translation || '';
  } catch (err) {
    logger.error(`translationService.translateToTamil failed: ${err.message}`);
    throw err;
  }
}

/**
 * Translates Tamil -> English using a dedicated, low-temperature prompt.
 */
async function translateToEnglish(tamilText) {
  try {
    const raw = await aiService.completeJSON(
      [
        {
          role: 'system',
          content:
            'Translate the given Tamil text into natural, conversational English. Respond ONLY with JSON: { "translation": string }.',
        },
        { role: 'user', content: tamilText },
      ],
      { temperature: 0.2, maxTokens: 400 }
    );
    const parsed = JSON.parse(raw);
    return parsed.translation || '';
  } catch (err) {
    logger.error(`translationService.translateToEnglish failed: ${err.message}`);
    throw err;
  }
}

module.exports = { translateToTamil, translateToEnglish };
