'use strict';

const Settings = require('../models/Settings');

const settingsRepository = {
  findByUser(userId) {
    return Settings.findOne({ userId });
  },

  createDefault(userId) {
    return Settings.create({ userId });
  },

  upsert(userId, update) {
    return Settings.findOneAndUpdate(
      { userId },
      { $set: update, $setOnInsert: { userId } },
      { upsert: true, new: true, setDefaultsOnInsert: true }
    );
  },
};

module.exports = settingsRepository;
