'use strict';

const mongoose = require('mongoose');
const { Schema } = mongoose;

const notificationSchema = new Schema(
  {
    userId: { type: Schema.Types.ObjectId, ref: 'User', required: true, index: true },
    type: {
      type: String,
      enum: ['reminder', 'streak', 'achievement', 'system'],
      default: 'reminder',
    },
    message: { type: String, required: true },
    sentAt: { type: Date, default: Date.now },
    read: { type: Boolean, default: false },
  },
  { timestamps: false }
);

module.exports = mongoose.model('Notification', notificationSchema);
