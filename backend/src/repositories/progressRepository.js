'use strict';

const Progress = require('../models/Progress');

function startOfDay(date) {
  const d = new Date(date);
  d.setHours(0, 0, 0, 0);
  return d;
}

const progressRepository = {
  findByUserAndDate(userId, date) {
    return Progress.findOne({ userId, date: startOfDay(date) });
  },

  findByUser(userId, { limit = 30, skip = 0 } = {}) {
    return Progress.find({ userId }).sort({ date: -1 }).skip(skip).limit(limit);
  },

  incrementForDate(userId, date, increments) {
    const day = startOfDay(date);
    return Progress.findOneAndUpdate(
      { userId, date: day },
      { $inc: increments, $setOnInsert: { userId, date: day } },
      { upsert: true, new: true, setDefaultsOnInsert: true }
    );
  },

  setStreak(userId, date, streak) {
    return Progress.findOneAndUpdate(
      { userId, date: startOfDay(date) },
      { $set: { streak }, $setOnInsert: { userId, date: startOfDay(date) } },
      { upsert: true, new: true, setDefaultsOnInsert: true }
    );
  },
};

module.exports = progressRepository;
