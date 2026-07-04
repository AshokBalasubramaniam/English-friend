'use strict';

const OpenAI = require('openai');
const env = require('../config/env');
const logger = require('../config/logger');

const client = new OpenAI({ apiKey: env.OPENAI_API_KEY });

/**
 * Builds the system prompt that defines "English Friend AI"'s persona:
 * a warm, patient best-friend character who chats naturally, remembers
 * personal details about the user, and gently corrects English mistakes
 * without derailing the conversation. The language mix (English only,
 * Tamil-English code-switching, or mostly Tamil) is driven by `mode`.
 *
 * @param {object} userMemory - subset of User.memory (officeName, friends, favoriteFood, goals, routines)
 * @param {string} mode - 'english' | 'tamil-english' | 'tamil'
 * @returns {string} system prompt
 */
function buildSystemPrompt(userMemory = {}, mode = 'english') {
  const { officeName, friends = [], favoriteFood, goals = [], routines = [] } = userMemory;

  const memoryLines = [];
  if (officeName) memoryLines.push(`- They work at/study at: ${officeName}`);
  if (friends.length) memoryLines.push(`- Their friends: ${friends.join(', ')}`);
  if (favoriteFood) memoryLines.push(`- Their favorite food: ${favoriteFood}`);
  if (goals.length) memoryLines.push(`- Their goals: ${goals.join(', ')}`);
  if (routines.length) memoryLines.push(`- Their daily routines: ${routines.join(', ')}`);

  const memoryBlock = memoryLines.length
    ? `Here is what you remember about your friend so far:\n${memoryLines.join('\n')}`
    : 'You do not know much about your friend yet - ask friendly questions to learn about their life, naturally, over time.';

  const languageInstruction = {
    english: 'Speak only in simple, natural English. Do not use Tamil.',
    'tamil-english':
      'Speak in a natural Tamil-English code-switched style (Tanglish), the way close Tamil friends casually mix English and Tamil in real conversation. Keep the core teaching content in English.',
    tamil: 'Speak mostly in Tamil (using Tamil script), occasionally introducing the English word or phrase being taught in parentheses.',
  }[mode] || 'Speak in simple, natural English.';

  return `You are "English Friend AI" - the user's warm, supportive, endlessly patient best friend whose special skill is helping them get comfortable speaking English.

Personality rules:
- You talk like a real close friend on WhatsApp: casual, warm, encouraging, curious about their day - never like a textbook or a formal tutor.
- You remember personal details they've shared and naturally bring them up ("How did the meeting with Ravi go?").
- ${memoryBlock}
- Keep replies short and conversational (2-4 sentences), like a real chat message, not an essay.

Correction rules:
- When the user makes a grammar or word-choice mistake, do NOT interrupt the flow of conversation or lecture them.
- Weave a brief, kind correction naturally into your reply, e.g. "Nice! By the way, we usually say 'I went to the office' instead of 'I go to office yesterday' - small thing, you're doing great!"
- Never make the user feel bad. Always encourage them to keep talking.
- Do not correct every single sentence - focus on the most useful mistake per turn, or skip correction entirely if the sentence was fine.

Language mode: ${languageInstruction}

Stay in character as their friend at all times. Never mention that you are an AI language model or break the roleplay.`;
}

/**
 * Streams a chat completion from OpenAI, invoking onDelta(text) for each
 * incoming text chunk. Resolves with the full concatenated reply text
 * once the stream completes.
 *
 * @param {Array<{role: 'system'|'user'|'assistant', content: string}>} messages
 * @param {(chunk: string) => void} onDelta
 * @param {object} [options]
 * @returns {Promise<string>} the full assistant reply
 */
async function streamReply(messages, onDelta, options = {}) {
  const { temperature = 0.8, maxTokens = 400 } = options;

  let fullText = '';

  try {
    const stream = await client.chat.completions.create({
      model: env.OPENAI_MODEL,
      messages,
      temperature,
      max_tokens: maxTokens,
      stream: true,
    });

    for await (const part of stream) {
      const delta = part?.choices?.[0]?.delta?.content;
      if (delta) {
        fullText += delta;
        if (typeof onDelta === 'function') onDelta(delta);
      }
    }

    return fullText;
  } catch (err) {
    logger.error(`OpenAI streamReply failed: ${err.message}`);
    throw err;
  }
}

/**
 * Non-streaming helper for structured/JSON tasks (corrections, translations,
 * scoring assistance, vocabulary extraction) where we want the complete
 * response before parsing it, rather than a token stream.
 *
 * @param {Array<{role: string, content: string}>} messages
 * @param {object} [options]
 * @returns {Promise<string>} raw text content of the model's reply
 */
async function completeJSON(messages, options = {}) {
  const { temperature = 0.3, maxTokens = 500 } = options;

  try {
    const response = await client.chat.completions.create({
      model: env.OPENAI_MODEL,
      messages,
      temperature,
      max_tokens: maxTokens,
      response_format: { type: 'json_object' },
    });

    return response.choices[0]?.message?.content || '{}';
  } catch (err) {
    logger.error(`OpenAI completeJSON failed: ${err.message}`);
    throw err;
  }
}

module.exports = {
  buildSystemPrompt,
  streamReply,
  completeJSON,
};
