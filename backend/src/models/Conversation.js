'use strict';

const mongoose = require('mongoose');
const { Schema } = mongoose;

const scoresSchema = new Schema(
  {
    grammar: { type: Number, min: 0, max: 100, default: 0 },
    vocabulary: { type: Number, min: 0, max: 100, default: 0 },
    fluency: { type: Number, min: 0, max: 100, default: 0 },
    confidence: { type: Number, min: 0, max: 100, default: 0 },
    pronunciation: { type: Number, min: 0, max: 100, default: 0 },
    overall: { type: Number, min: 0, max: 100, default: 0 },
  },
  { _id: false }
);

const conversationSchema = new Schema(
  {
    userId: { type: Schema.Types.ObjectId, ref: 'User', required: true, index: true },
    mode: {
      type: String,
      enum: ['english', 'tamil-english', 'tamil'],
      default: 'english',
    },
    startedAt: { type: Date, default: Date.now },
    endedAt: { type: Date, default: null },
    scores: { type: scoresSchema, default: () => ({}) },
    isFavorite: { type: Boolean, default: false },
  },
  { timestamps: true }
);

conversationSchema.index({ userId: 1, startedAt: -1 });

module.exports = mongoose.model('Conversation', conversationSchema);
