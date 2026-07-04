'use strict';

const mongoose = require('mongoose');
const { Schema } = mongoose;

const settingsSchema = new Schema(
  {
    userId: {
      type: Schema.Types.ObjectId,
      ref: 'User',
      required: true,
      unique: true,
      index: true,
    },
    darkMode: { type: Boolean, default: false },
    notificationsEnabled: { type: Boolean, default: true },
    languageMode: {
      type: String,
      enum: ['english', 'tamil-english', 'tamil'],
      default: 'english',
    },
  },
  { timestamps: true }
);

module.exports = mongoose.model('Settings', settingsSchema);
