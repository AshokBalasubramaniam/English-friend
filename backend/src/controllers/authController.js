'use strict';

const asyncHandler = require('../utils/asyncHandler');
const authService = require('../services/authService');

function serializeUser(user) {
  return {
    id: user._id,
    email: user.email,
    name: user.name,
    authProvider: user.authProvider,
    englishLevel: user.englishLevel,
    memory: user.memory,
    createdAt: user.createdAt,
  };
}

const register = asyncHandler(async (req, res) => {
  const { email, password, name } = req.body;
  const { user, accessToken, refreshToken } = await authService.register({ email, password, name });
  res.status(201).json({ success: true, data: { user: serializeUser(user), accessToken, refreshToken } });
});

const login = asyncHandler(async (req, res) => {
  const { email, password } = req.body;
  const { user, accessToken, refreshToken } = await authService.login({ email, password });
  res.status(200).json({ success: true, data: { user: serializeUser(user), accessToken, refreshToken } });
});

const googleLogin = asyncHandler(async (req, res) => {
  const { googleId, email, name } = req.body;
  const { user, accessToken, refreshToken } = await authService.googleLogin({ googleId, email, name });
  res.status(200).json({ success: true, data: { user: serializeUser(user), accessToken, refreshToken } });
});

const refresh = asyncHandler(async (req, res) => {
  const { refreshToken } = req.body;
  const { user, accessToken, refreshToken: newRefreshToken } = await authService.refreshAccessToken(
    refreshToken
  );
  res
    .status(200)
    .json({ success: true, data: { user: serializeUser(user), accessToken, refreshToken: newRefreshToken } });
});

const logout = asyncHandler(async (req, res) => {
  await authService.logout(req.user._id);
  res.status(200).json({ success: true, message: 'Logged out' });
});

const me = asyncHandler(async (req, res) => {
  res.status(200).json({ success: true, data: { user: serializeUser(req.user) } });
});

module.exports = { register, login, googleLogin, refresh, logout, me };
