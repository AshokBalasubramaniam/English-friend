'use strict';

const Analytics = require('../models/Analytics');

const analyticsRepository = {
  log(userId, event, metadata = {}) {
    return Analytics.create({ userId, event, metadata });
  },

  findByUser(userId, { limit = 100, skip = 0 } = {}) {
    return Analytics.find({ userId }).sort({ createdAt: -1 }).skip(skip).limit(limit);
  },

  findByEvent(event, { limit = 100 } = {}) {
    return Analytics.find({ event }).sort({ createdAt: -1 }).limit(limit);
  },
};

module.exports = analyticsRepository;
