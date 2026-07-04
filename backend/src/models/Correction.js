'use strict';

const mongoose = require('mongoose');
const { Schema } = mongoose;

const correctionSchema = new Schema(
  {
    messageId: { type: Schema.Types.ObjectId, ref: 'Message', required: true, index: true },
    original: { type: String, required: true },
    corrected: { type: String, required: true },
    reason: { type: String, default: '' },
    example: { type: String, default: '' },
    difficultyLevel: {
      type: String,
      enum: ['beginner', 'intermediate', 'advanced'],
      default: 'beginner',
    },
  },
  { timestamps: { createdAt: true, updatedAt: false } }
);

module.exports = mongoose.model('Correction', correctionSchema);
