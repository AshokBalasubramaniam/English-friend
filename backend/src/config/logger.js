'use strict';

const winston = require('winston');
const path = require('path');
const fs = require('fs');

const logsDir = path.join(process.cwd(), 'logs');
if (!fs.existsSync(logsDir)) {
  fs.mkdirSync(logsDir, { recursive: true });
}

const SENSITIVE_KEYS = [
  'password',
  'passwordHash',
  'refreshToken',
  'refreshTokenHash',
  'accessToken',
  'authorization',
  'token',
  'openaiApiKey',
  'apiKey',
];

/**
 * Recursively redacts sensitive fields from log metadata so secrets
 * and credentials never end up in log files or console output.
 */
function redact(obj, seen = new WeakSet()) {
  if (obj === null || typeof obj !== 'object') return obj;
  if (seen.has(obj)) return '[Circular]';
  seen.add(obj);

  if (Array.isArray(obj)) {
    return obj.map((item) => redact(item, seen));
  }

  const result = {};
  for (const [key, value] of Object.entries(obj)) {
    if (SENSITIVE_KEYS.includes(key.toLowerCase())) {
      result[key] = '[REDACTED]';
    } else if (value && typeof value === 'object') {
      result[key] = redact(value, seen);
    } else {
      result[key] = value;
    }
  }
  return result;
}

const redactFormat = winston.format((info) => {
  const { level, message, timestamp, ...meta } = info;
  const redactedMeta = redact(meta);
  return { level, message, timestamp, ...redactedMeta };
});

const logger = winston.createLogger({
  level: process.env.LOG_LEVEL || (process.env.NODE_ENV === 'production' ? 'info' : 'debug'),
  format: winston.format.combine(
    winston.format.timestamp(),
    redactFormat(),
    winston.format.errors({ stack: true }),
    winston.format.json()
  ),
  defaultMeta: { service: 'english-friend-ai-backend' },
  transports: [
    new winston.transports.File({
      filename: path.join(process.cwd(), 'logs', 'error.log'),
      level: 'error',
    }),
    new winston.transports.File({
      filename: path.join(process.cwd(), 'logs', 'combined.log'),
    }),
  ],
});

if (process.env.NODE_ENV !== 'production') {
  logger.add(
    new winston.transports.Console({
      format: winston.format.combine(
        winston.format.colorize(),
        winston.format.simple()
      ),
    })
  );
}

module.exports = logger;
