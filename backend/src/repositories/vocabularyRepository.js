'use strict';

const Vocabulary = require('../models/Vocabulary');

const vocabularyRepository = {
  create(data) {
    return Vocabulary.create(data);
  },

  upsert(userId, word, data) {
    return Vocabulary.findOneAndUpdate(
      { userId, word: word.toLowerCase() },
      { $setOnInsert: { userId, word: word.toLowerCase(), ...data } },
      { upsert: true, new: true, setDefaultsOnInsert: true }
    );
  },

  findByUser(userId, { limit = 50, skip = 0 } = {}) {
    return Vocabulary.find({ userId }).sort({ learnedAt: -1 }).skip(skip).limit(limit);
  },

  countByUser(userId) {
    return Vocabulary.countDocuments({ userId });
  },

  deleteById(id) {
    return Vocabulary.findByIdAndDelete(id);
  },
};

module.exports = vocabularyRepository;
