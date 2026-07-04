'use strict';

const jwt = require('jsonwebtoken');
const crypto = require('crypto');
const env = require('../config/env');

/**
 * Signs a short-lived access token embedding the user id.
 */
function signAccessToken(userId) {
  return jwt.sign({ sub: String(userId), type: 'access' }, env.JWT_ACCESS_SECRET, {
    expiresIn: env.JWT_ACCESS_EXPIRY,
  });
}

/**
 * Signs a longer-lived refresh token. The plaintext is only ever sent to
 * the client; the server stores a SHA-256 hash of it (see hashToken).
 */
function signRefreshToken(userId) {
  return jwt.sign({ sub: String(userId), type: 'refresh' }, env.JWT_REFRESH_SECRET, {
    expiresIn: env.JWT_REFRESH_EXPIRY,
  });
}

function verifyAccessToken(token) {
  return jwt.verify(token, env.JWT_ACCESS_SECRET);
}

function verifyRefreshToken(token) {
  return jwt.verify(token, env.JWT_REFRESH_SECRET);
}

/**
 * Refresh tokens are never stored in plaintext - only their SHA-256 hash,
 * so a database leak does not expose usable tokens.
 */
function hashToken(token) {
  return crypto.createHash('sha256').update(token).digest('hex');
}

module.exports = {
  signAccessToken,
  signRefreshToken,
  verifyAccessToken,
  verifyRefreshToken,
  hashToken,
};
