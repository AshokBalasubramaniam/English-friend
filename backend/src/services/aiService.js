'use strict';

const env = require('../config/env');
const logger = require('../config/logger');

const GEMINI_BASE_URL = 'https://generativelanguage.googleapis.com/v1beta/models';

/**
 * Converts our OpenAI-style {role: 'system'|'user'|'assistant', content} messages into
 * Gemini's request shape: a separate systemInstruction plus a contents[] array using
 * Gemini's role names ('user' / 'model' - Gemini has no 'assistant' or 'system' role here).
 */
function toGeminiRequest(messages) {
  const systemMessages = messages.filter((m) => m.role === 'system');
  const turns = messages.filter((m) => m.role !== 'system');

  return {
    systemInstruction: systemMessages.length
      ? { parts: [{ text: systemMessages.map((m) => m.content).join('\n\n') }] }
      : undefined,
    contents: turns.map((m) => ({
      role: m.role === 'assistant' ? 'model' : 'user',
      parts: [{ text: m.content }],
    })),
  };
}

function extractText(candidateResponse) {
  return candidateResponse?.candidates?.[0]?.content?.parts?.map((p) => p.text || '').join('') || '';
}

function sleep(ms) {
  return new Promise((resolve) => setTimeout(resolve, ms));
}

// Gemini includes a structured retry hint on 429s, e.g. { "retryDelay": "5s" } - prefer that
// over guessing, but only worth honoring if it's short; a 60s wait mid-chat is worse UX than
// just failing fast and letting the user try again themselves.
const MAX_HONORED_RETRY_DELAY_MS = 8000;

function getSuggestedRetryDelayMs(message) {
  const match = message.match(/"retryDelay":\s*"(\d+(?:\.\d+)?)s"/);
  if (!match) return null;
  return Math.ceil(parseFloat(match[1]) * 1000) + 250;
}

function isRateLimitError(message) {
  return message.includes('(429)') || message.includes('RESOURCE_EXHAUSTED');
}

// Gemini's free tier caps requests both per-minute AND per-day, per model (see
// quotaId in the 429 body: "...PerMinute..." vs "...PerDay..."). A per-minute cap is worth
// a short retry; a per-day cap won't clear until tomorrow no matter how long we wait, so
// retrying (or suggesting "try again in a moment") is actively misleading.
function isDailyQuotaError(message) {
  return isRateLimitError(message) && /PerDay/i.test(message);
}

function isTransientServerError(message) {
  return message.includes('(500)') || message.includes('(502)') || message.includes('(503)');
}

/**
 * Retries transient Gemini failures (per-minute rate limits, transient 5xxs) with a short
 * backoff. Daily quota exhaustion is deliberately NOT retried - see isDailyQuotaError.
 * @param {() => Promise<T>} fn
 * @param {{ maxRetries?: number, canRetry?: () => boolean }} [opts] - canRetry lets streamReply
 *   refuse to retry once it has already emitted partial content to the caller, since retrying
 *   from scratch at that point would duplicate/garble what was already streamed out.
 */
async function callWithRetry(fn, opts = {}) {
  const { maxRetries = 2, canRetry = () => true } = opts;
  let lastErr;

  for (let attempt = 0; attempt <= maxRetries; attempt++) {
    try {
      return await fn();
    } catch (err) {
      lastErr = err;
      const retryable =
        (isRateLimitError(err.message) && !isDailyQuotaError(err.message)) ||
        isTransientServerError(err.message);
      if (!retryable || attempt === maxRetries || !canRetry()) throw err;

      const suggestedDelay = getSuggestedRetryDelayMs(err.message);
      if (suggestedDelay !== null && suggestedDelay > MAX_HONORED_RETRY_DELAY_MS) throw err;
      const delayMs = suggestedDelay ?? 500 * Math.pow(2, attempt);

      logger.warn(
        `Gemini call failed (attempt ${attempt + 1}/${maxRetries + 1}), retrying in ${delayMs}ms: ${err.message}`
      );
      await sleep(delayMs);
    }
  }
  throw lastErr;
}

/**
 * Maps a raw Gemini/network error to a short, clean message safe to show the end user -
 * never the raw provider JSON/stack trace. Full details are still logged server-side by the
 * caller before this is used.
 */
function toUserFacingMessage(err) {
  const message = err.message || '';
  if (isDailyQuotaError(message)) {
    return "I've reached my daily conversation limit on the free plan - please come back tomorrow, or ask about upgrading the plan.";
  }
  if (isRateLimitError(message)) {
    return "I'm getting a lot of requests right now - please wait a moment and try again.";
  }
  if (message.includes('(401)') || message.includes('(403)') || message.includes('API key')) {
    return "There's a configuration issue on our end. Please try again later.";
  }
  if (isTransientServerError(message)) {
    return "I'm having trouble connecting right now - please try again in a moment.";
  }
  return 'Something went wrong generating a reply. Please try again.';
}

/**
 * Builds the system prompt that defines "English Friend AI"'s persona:
 * a warm, patient best-friend character who chats naturally, remembers
 * personal details about the user, and gently corrects English mistakes
 * without derailing the conversation. The language mix (English only,
 * Tamil-English code-switching, or mostly Tamil) is driven by `mode`.
 *
 * @param {string} userName - the user's display name, so the AI can address them by name
 * @param {object} userMemory - subset of User.memory (officeName, friends, favoriteFood, goals, routines)
 * @param {string} mode - 'english' | 'tamil-english' | 'tamil'
 * @param {boolean} asksQuestions - whether the AI should proactively ask the user questions
 *   (Settings.aiAsksQuestions) or just respond/chat without prompting
 * @returns {string} system prompt
 */
function buildSystemPrompt(userName, userMemory = {}, mode = 'english', asksQuestions = true) {
  const { officeName, friends = [], favoriteFood, goals = [], routines = [] } = userMemory;

  const memoryLines = [];
  if (officeName) memoryLines.push(`- They work at/study at: ${officeName}`);
  if (friends.length) memoryLines.push(`- Their friends: ${friends.join(', ')}`);
  if (favoriteFood) memoryLines.push(`- Their favorite food: ${favoriteFood}`);
  if (goals.length) memoryLines.push(`- Their goals: ${goals.join(', ')}`);
  if (routines.length) memoryLines.push(`- Their daily routines: ${routines.join(', ')}`);

  const memoryBlock = memoryLines.length
    ? `Here is what you remember about your friend so far:\n${memoryLines.join('\n')}`
    : 'You do not know much about your friend yet.';

  const languageInstruction = {
    english: 'Speak only in simple, natural English. Do not use Tamil.',
    'tamil-english':
      'Speak in a natural Tamil-English code-switched style (Tanglish), the way close Tamil friends casually mix English and Tamil in real conversation. Keep the core teaching content in English.',
    tamil: 'Speak mostly in Tamil (using Tamil script), occasionally introducing the English word or phrase being taught in parentheses.',
  }[mode] || 'Speak in simple, natural English.';

  const questionInstruction = asksQuestions
    ? "- You're curious about their life - ask friendly follow-up questions naturally (about their day, work, food, plans) to keep the conversation going and learn more about them over time."
    : "- Do NOT ask the user questions. Respond warmly and conversationally, but let them lead - never end your reply with a question, and never proactively ask about their day, work, or life unless they bring it up first.";

  return `You are "English Friend AI" - the user's warm, supportive, endlessly patient best friend whose special skill is helping them get comfortable speaking English.

Your friend's name is ${userName || 'friend'} - address them by name naturally sometimes, especially when greeting them.

Personality rules:
- You talk like a real close friend on WhatsApp: casual, warm, encouraging - never like a textbook or a formal tutor.
- You remember personal details they've shared and naturally bring them up ("How did the meeting with Ravi go?").
- ${memoryBlock}
${questionInstruction}
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
 * Streams a chat completion from Gemini, invoking onDelta(text) for each
 * incoming text chunk. Resolves with the full concatenated reply text
 * once the stream completes. Uses Gemini's SSE streaming endpoint directly
 * (no SDK dependency) via Node's built-in fetch.
 *
 * @param {Array<{role: 'system'|'user'|'assistant', content: string}>} messages
 * @param {(chunk: string) => void} onDelta
 * @param {object} [options]
 * @returns {Promise<string>} the full assistant reply
 */
async function streamReply(messages, onDelta, options = {}) {
  const { temperature = 0.8, maxTokens = 400 } = options;
  const { systemInstruction, contents } = toGeminiRequest(messages);
  const url = `${GEMINI_BASE_URL}/${env.GEMINI_MODEL}:streamGenerateContent?alt=sse&key=${env.GEMINI_API_KEY}`;

  let emittedAny = false;

  return callWithRetry(
    async () => {
      try {
        const response = await fetch(url, {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({
            contents,
            systemInstruction,
            generationConfig: {
              temperature,
              maxOutputTokens: maxTokens,
              // Disables Gemini 2.5's extended "thinking" tokens - not worth the added
              // latency for a real-time chat reply.
              thinkingConfig: { thinkingBudget: 0 },
            },
          }),
        });

        if (!response.ok || !response.body) {
          const errorBody = await response.text().catch(() => '');
          throw new Error(`Gemini streamGenerateContent failed (${response.status}): ${errorBody}`);
        }

        const reader = response.body.getReader();
        const decoder = new TextDecoder();
        let buffer = '';
        let fullText = '';

        // Gemini's alt=sse stream sends one JSON object per "data: ..." line, each
        // event separated by a blank line, matching standard Server-Sent Events framing.
        while (true) {
          const { value, done } = await reader.read();
          if (done) break;
          // Gemini sends CRLF line endings (events end in "\r\n\r\n"), not bare "\n\n".
          buffer += decoder.decode(value, { stream: true }).replace(/\r\n/g, '\n');

          let eventEnd;
          while ((eventEnd = buffer.indexOf('\n\n')) !== -1) {
            const rawEvent = buffer.slice(0, eventEnd);
            buffer = buffer.slice(eventEnd + 2);

            const dataLine = rawEvent.split('\n').find((line) => line.startsWith('data: '));
            if (!dataLine) continue;

            const delta = extractText(JSON.parse(dataLine.slice('data: '.length)));
            if (delta) {
              fullText += delta;
              emittedAny = true;
              if (typeof onDelta === 'function') onDelta(delta);
            }
          }
        }

        return fullText;
      } catch (err) {
        logger.error(`Gemini streamReply failed: ${err.message}`);
        throw err;
      }
    },
    // Never retry once we've already streamed partial content to the caller - restarting
    // from scratch at that point would duplicate/garble what the client already received.
    { canRetry: () => !emittedAny }
  );
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
  const { systemInstruction, contents } = toGeminiRequest(messages);
  const url = `${GEMINI_BASE_URL}/${env.GEMINI_MODEL}:generateContent?key=${env.GEMINI_API_KEY}`;

  return callWithRetry(async () => {
    try {
      const response = await fetch(url, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          contents,
          systemInstruction,
          generationConfig: {
            temperature,
            maxOutputTokens: maxTokens,
            responseMimeType: 'application/json',
            thinkingConfig: { thinkingBudget: 0 },
          },
        }),
      });

      if (!response.ok) {
        const errorBody = await response.text().catch(() => '');
        throw new Error(`Gemini generateContent failed (${response.status}): ${errorBody}`);
      }

      return extractText(await response.json()) || '{}';
    } catch (err) {
      logger.error(`Gemini completeJSON failed: ${err.message}`);
      throw err;
    }
  });
}

module.exports = {
  buildSystemPrompt,
  streamReply,
  completeJSON,
  toUserFacingMessage,
};
