'use strict';

const Message = require('../models/Message');

const messageRepository = {
  create(data) {
    return Message.create(data);
  },

  findById(id) {
    return Message.findById(id);
  },

  findByConversation(conversationId, { limit = 100, skip = 0 } = {}) {
    return Message.find({ conversationId })
      .sort({ createdAt: 1 })
      .skip(skip)
      .limit(limit);
  },

  countByConversation(conversationId) {
    return Message.countDocuments({ conversationId });
  },

  deleteByConversation(conversationId) {
    return Message.deleteMany({ conversationId });
  },
};

module.exports = messageRepository;
