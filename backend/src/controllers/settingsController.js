'use strict';

const asyncHandler = require('../utils/asyncHandler');
const settingsRepository = require('../repositories/settingsRepository');

const getSettings = asyncHandler(async (req, res) => {
  let settings = await settingsRepository.findByUser(req.user._id);
  if (!settings) {
    settings = await settingsRepository.createDefault(req.user._id);
  }
  res.status(200).json({ success: true, data: { settings } });
});

const updateSettings = asyncHandler(async (req, res) => {
  const { darkMode, notificationsEnabled, languageMode } = req.body;
  const update = {};
  if (darkMode !== undefined) update.darkMode = darkMode;
  if (notificationsEnabled !== undefined) update.notificationsEnabled = notificationsEnabled;
  if (languageMode !== undefined) update.languageMode = languageMode;

  const settings = await settingsRepository.upsert(req.user._id, update);
  res.status(200).json({ success: true, data: { settings } });
});

module.exports = { getSettings, updateSettings };
