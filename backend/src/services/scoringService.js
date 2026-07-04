'use strict';

const messageRepository = require('../repositories/messageRepository');
const correctionRepository = require('../repositories/correctionRepository');
const aiService = require('./aiService');
const logger = require('../config/logger');

/**
 * Computes grammar/vocabulary/fluency/confidence/pronunciation/overall
 * scores (0-100) for a finished conversation.
 *
 * This is a heuristic + AI-assisted approach, clearly separated:
 *  1. Heuristic component (deterministic, cheap, always available):
 *     - grammar: penalized by the ratio of corrections to user messages
 *     - fluency: rewarded by average message length / response cadence
 *     - vocabulary: rewarded by lexical diversity (unique words ratio)
 *     - pronunciation: cannot be measured from text alone in this scaffold,
 *       defaults to a neutral score (TODO: derive from audio/ASR confidence
 *       once voice input is wired up)
 *     - confidence: rewarded by message length and lower correction ratio
 *  2. AI-assisted adjustment: we ask the model for a qualitative 0-100
 *     "overall" impression given a sample of the transcript, and blend it
 *     with the heuristic overall for a more human-feeling score.
 *
 * If the AI call fails, we gracefully fall back to the pure heuristic score.
 */
async function scoreConversation(conversationId) {
  const messages = await messageRepository.findByConversation(conversationId, { limit: 500 });
  const userMessages = messages.filter((m) => m.sender === 'user');

  if (userMessages.length === 0) {
    return {
      grammar: 0,
      vocabulary: 0,
      fluency: 0,
      confidence: 0,
      pronunciation: 0,
      overall: 0,
    };
  }

  const messageIds = userMessages.map((m) => m._id);
  const corrections = await correctionRepository.findByMessageIds(messageIds);

  const correctionRatio = corrections.length / userMessages.length;
  const grammar = clampScore(100 - correctionRatio * 60);

  const allWords = userMessages
    .flatMap((m) => m.englishText.toLowerCase().split(/\s+/))
    .filter(Boolean);
  const uniqueWords = new Set(allWords);
  const lexicalDiversity = allWords.length ? uniqueWords.size / allWords.length : 0;
  const vocabulary = clampScore(lexicalDiversity * 100 + Math.min(uniqueWords.size, 50));

  const avgLength = allWords.length / userMessages.length;
  const fluency = clampScore(40 + avgLength * 6);

  const confidence = clampScore(50 + avgLength * 4 - correctionRatio * 30);

  // Pronunciation cannot be assessed from text in this scaffold - neutral
  // placeholder until audio/ASR pipeline exists.
  const pronunciation = 60;

  const heuristicOverall = clampScore(
    grammar * 0.25 + vocabulary * 0.2 + fluency * 0.2 + confidence * 0.15 + pronunciation * 0.2
  );

  let overall = heuristicOverall;
  try {
    const aiOverall = await getAiImpressionScore(userMessages);
    if (typeof aiOverall === 'number') {
      overall = clampScore(heuristicOverall * 0.6 + aiOverall * 0.4);
    }
  } catch (err) {
    logger.warn(`scoringService: AI-assisted scoring failed, using heuristic only: ${err.message}`);
  }

  return { grammar, vocabulary, fluency, confidence, pronunciation, overall };
}

async function getAiImpressionScore(userMessages) {
  const transcriptSample = userMessages
    .slice(-15)
    .map((m) => m.englishText)
    .join('\n');

  const raw = await aiService.completeJSON(
    [
      {
        role: 'system',
        content:
          'You are an English speaking coach. Given a sample of a learner\'s chat messages, give an overall spoken-English quality score from 0 to 100, considering grammar, vocabulary range, and fluency of expression. Respond ONLY with JSON: { "overall": number }.',
      },
      { role: 'user', content: transcriptSample },
    ],
    { temperature: 0.2, maxTokens: 50 }
  );

  const parsed = JSON.parse(raw);
  return typeof parsed.overall === 'number' ? parsed.overall : null;
}

function clampScore(value) {
  if (Number.isNaN(value)) return 0;
  return Math.max(0, Math.min(100, Math.round(value)));
}

module.exports = { scoreConversation };
