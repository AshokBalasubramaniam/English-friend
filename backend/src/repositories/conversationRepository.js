'use strict';

const Conversation = require('../models/Conversation');

const conversationRepository = {
  create(data) {
    return Conversation.create(data);
  },

  findById(id) {
    return Conversation.findById(id);
  },

  findByUser(userId, { limit = 20, skip = 0 } = {}) {
    return Conversation.find({ userId }).sort({ startedAt: -1 }).skip(skip).limit(limit);
  },

  findActiveForUser(userId) {
    return Conversation.findOne({ userId, endedAt: null }).sort({ startedAt: -1 });
  },

  updateById(id, update) {
    return Conversation.findByIdAndUpdate(id, update, { new: true });
  },

  endConversation(id, scores) {
    return Conversation.findByIdAndUpdate(
      id,
      { endedAt: new Date(), scores },
      { new: true }
    );
  },

  setFavorite(id, isFavorite) {
    return Conversation.findByIdAndUpdate(id, { isFavorite }, { new: true });
  },

  deleteById(id) {
    return Conversation.findByIdAndDelete(id);
  },
};

module.exports = conversationRepository;
