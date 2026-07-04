'use strict';

/**
 * Loads and validates environment variables.
 * Throws a clear error at boot time if a required variable is missing,
 * rather than failing later with a confusing runtime error.
 */

const path = require('path');
require('dotenv').config({ path: path.resolve(process.cwd(), '.env') });

const REQUIRED_VARS = [
  'MONGODB_URI',
  'JWT_ACCESS_SECRET',
  'JWT_REFRESH_SECRET',
  'OPENAI_API_KEY',
];

function requireEnv(name, fallback) {
  const value = process.env[name];
  if (value === undefined || value === '') {
    if (fallback !== undefined) return fallback;
    return undefined;
  }
  return value;
}

function validateEnv() {
  const missing = REQUIRED_VARS.filter((key) => !process.env[key]);
  if (missing.length > 0) {
    // Fail fast and loudly - never silently run with missing secrets.
    // eslint-disable-next-line no-console
    console.error(
      `[env] Missing required environment variables: ${missing.join(', ')}\n` +
        '[env] Copy .env.example to .env and fill in real values before starting the server.'
    );
    throw new Error(`Missing required environment variables: ${missing.join(', ')}`);
  }
}

validateEnv();

const env = {
  NODE_ENV: requireEnv('NODE_ENV', 'development'),
  PORT: parseInt(requireEnv('PORT', '5000'), 10),

  MONGODB_URI: process.env.MONGODB_URI,

  JWT_ACCESS_SECRET: process.env.JWT_ACCESS_SECRET,
  JWT_REFRESH_SECRET: process.env.JWT_REFRESH_SECRET,
  JWT_ACCESS_EXPIRY: requireEnv('JWT_ACCESS_EXPIRY', '15m'),
  JWT_REFRESH_EXPIRY: requireEnv('JWT_REFRESH_EXPIRY', '7d'),

  OPENAI_API_KEY: process.env.OPENAI_API_KEY,
  OPENAI_MODEL: requireEnv('OPENAI_MODEL', 'gpt-4o-mini'),

  GOOGLE_CLIENT_ID: requireEnv('GOOGLE_CLIENT_ID', ''),

  CORS_ORIGIN: requireEnv('CORS_ORIGIN', '*'),

  isProduction: requireEnv('NODE_ENV', 'development') === 'production',
  isTest: requireEnv('NODE_ENV', 'development') === 'test',
};

module.exports = env;
