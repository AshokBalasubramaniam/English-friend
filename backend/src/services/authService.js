'use strict';

const bcrypt = require('bcrypt');
const userRepository = require('../repositories/userRepository');
const settingsRepository = require('../repositories/settingsRepository');
const ApiError = require('../utils/ApiError');
const {
  signAccessToken,
  signRefreshToken,
  verifyRefreshToken,
  hashToken,
} = require('../utils/tokenUtils');
const logger = require('../config/logger');

const SALT_ROUNDS = 10;

function hashPassword(password) {
  return bcrypt.hash(password, SALT_ROUNDS);
}

function comparePassword(password, passwordHash) {
  return bcrypt.compare(password, passwordHash);
}

/**
 * Issues a fresh access + refresh token pair for a user, and persists
 * only the hash of the refresh token (never the plaintext) so a DB leak
 * cannot be used to impersonate users.
 */
async function generateTokens(userId) {
  const accessToken = signAccessToken(userId);
  const refreshToken = signRefreshToken(userId);
  await userRepository.setRefreshTokenHash(userId, hashToken(refreshToken));
  return { accessToken, refreshToken };
}

async function register({ email, password, name }) {
  const existing = await userRepository.findByEmail(email);
  if (existing) {
    throw ApiError.conflict('An account with this email already exists');
  }

  const passwordHash = await hashPassword(password);
  const user = await userRepository.create({
    email,
    passwordHash,
    name,
    authProvider: 'local',
  });

  await settingsRepository.createDefault(user._id);

  const tokens = await generateTokens(user._id);
  logger.info(`New user registered: ${user.email}`);

  return { user, ...tokens };
}

async function login({ email, password }) {
  const user = await userRepository.findByEmailWithPassword(email);
  if (!user || user.authProvider !== 'local') {
    throw ApiError.unauthorized('Invalid email or password');
  }

  const isMatch = await comparePassword(password, user.passwordHash);
  if (!isMatch) {
    throw ApiError.unauthorized('Invalid email or password');
  }

  const tokens = await generateTokens(user._id);
  return { user, ...tokens };
}

/**
 * Verifies a Google ID token and logs in / registers the user.
 *
 * NOTE (scaffold stub): full verification against Google's public keys via
 * `google-auth-library` is not wired up yet. This function validates the
 * shape of the decoded payload the client is expected to send and documents
 * exactly where real verification must be added before production use.
 *
 * TODO(production): replace this with:
 *   const { OAuth2Client } = require('google-auth-library');
 *   const client = new OAuth2Client(env.GOOGLE_CLIENT_ID);
 *   const ticket = await client.verifyIdToken({ idToken, audience: env.GOOGLE_CLIENT_ID });
 *   const payload = ticket.getPayload();
 */
async function googleLogin({ googleId, email, name }) {
  if (!googleId || !email) {
    throw ApiError.badRequest('googleId and email are required for Google login');
  }

  let user = await userRepository.findByGoogleId(googleId);

  if (!user) {
    user = await userRepository.findByEmail(email);
    if (user) {
      // Existing local account with the same email - link the Google id.
      user = await userRepository.updateById(user._id, { googleId, authProvider: 'google' });
    } else {
      user = await userRepository.create({
        email,
        name: name || email.split('@')[0],
        authProvider: 'google',
        googleId,
      });
      await settingsRepository.createDefault(user._id);
    }
  }

  const tokens = await generateTokens(user._id);
  return { user, ...tokens };
}

async function refreshAccessToken(refreshToken) {
  if (!refreshToken) throw ApiError.unauthorized('Refresh token required');

  let payload;
  try {
    payload = verifyRefreshToken(refreshToken);
  } catch (err) {
    throw ApiError.unauthorized('Invalid or expired refresh token');
  }

  const user = await userRepository.findByIdWithRefreshToken(payload.sub);
  if (!user || !user.refreshTokenHash) {
    throw ApiError.unauthorized('Invalid refresh token');
  }

  if (user.refreshTokenHash !== hashToken(refreshToken)) {
    // Refresh token has been rotated/revoked or does not match what we issued.
    throw ApiError.unauthorized('Invalid refresh token');
  }

  const tokens = await generateTokens(user._id);
  return { user, ...tokens };
}

async function logout(userId) {
  await userRepository.setRefreshTokenHash(userId, null);
}

module.exports = {
  hashPassword,
  comparePassword,
  generateTokens,
  register,
  login,
  googleLogin,
  refreshAccessToken,
  logout,
};
