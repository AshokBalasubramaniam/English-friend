'use strict';

const Correction = require('../models/Correction');

const correctionRepository = {
  create(data) {
    return Correction.create(data);
  },

  findByMessage(messageId) {
    return Correction.find({ messageId });
  },

  findByMessageIds(messageIds) {
    // Correction only references messageId directly; callers that need
    // conversation-level corrections should look up message ids first.
    return Correction.find({ messageId: { $in: messageIds } });
  },

  deleteByMessage(messageId) {
    return Correction.deleteMany({ messageId });
  },
};

module.exports = correctionRepository;
