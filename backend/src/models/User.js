'use strict';

const mongoose = require('mongoose');
const { Schema } = mongoose;

const memorySchema = new Schema(
  {
    officeName: { type: String, default: '' },
    friends: { type: [String], default: [] },
    favoriteFood: { type: String, default: '' },
    goals: { type: [String], default: [] },
    routines: { type: [String], default: [] },
  },
  { _id: false }
);

const userSchema = new Schema(
  {
    email: {
      type: String,
      required: true,
      unique: true,
      lowercase: true,
      trim: true,
      index: true,
    },
    passwordHash: {
      type: String,
      // Not required when the user signs in via Google OAuth.
      required: function () {
        return this.authProvider === 'local';
      },
    },
    name: { type: String, required: true, trim: true },
    authProvider: {
      type: String,
      enum: ['local', 'google'],
      default: 'local',
    },
    googleId: { type: String, default: null, index: true, sparse: true },
    englishLevel: {
      type: String,
      enum: ['beginner', 'intermediate', 'advanced'],
      default: 'beginner',
    },
    memory: { type: memorySchema, default: () => ({}) },
    refreshTokenHash: { type: String, default: null, select: false },
  },
  { timestamps: { createdAt: 'createdAt', updatedAt: 'updatedAt' } }
);

module.exports = mongoose.model('User', userSchema);
