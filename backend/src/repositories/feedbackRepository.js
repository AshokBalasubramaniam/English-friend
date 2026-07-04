'use strict';

const Feedback = require('../models/Feedback');

const feedbackRepository = {
  create(data) {
    return Feedback.create(data);
  },

  findByUser(userId) {
    return Feedback.find({ userId }).sort({ createdAt: -1 });
  },

  findAll({ limit = 50, skip = 0 } = {}) {
    return Feedback.find({}).sort({ createdAt: -1 }).skip(skip).limit(limit);
  },
};

module.exports = feedbackRepository;
