'use strict';

const User = require('../models/User');

/**
 * Thin data-access wrapper over the User model.
 * No business logic here - that belongs in services.
 */
const userRepository = {
  create(data) {
    return User.create(data);
  },

  findById(id) {
    return User.findById(id);
  },

  findByIdWithRefreshToken(id) {
    return User.findById(id).select('+refreshTokenHash');
  },

  findByEmail(email) {
    return User.findOne({ email: email.toLowerCase() });
  },

  findByEmailWithPassword(email) {
    return User.findOne({ email: email.toLowerCase() }).select('+passwordHash');
  },

  findByGoogleId(googleId) {
    return User.findOne({ googleId });
  },

  updateById(id, update) {
    return User.findByIdAndUpdate(id, update, { new: true });
  },

  setRefreshTokenHash(id, refreshTokenHash) {
    return User.findByIdAndUpdate(id, { refreshTokenHash }, { new: true });
  },

  updateMemory(id, memoryPatch) {
    const set = {};
    Object.keys(memoryPatch || {}).forEach((key) => {
      set[`memory.${key}`] = memoryPatch[key];
    });
    return User.findByIdAndUpdate(id, { $set: set }, { new: true });
  },

  deleteById(id) {
    return User.findByIdAndDelete(id);
  },
};

module.exports = userRepository;
