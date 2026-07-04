'use strict';

const mongoose = require('mongoose');
const { Schema } = mongoose;

const vocabularySchema = new Schema(
  {
    userId: { type: Schema.Types.ObjectId, ref: 'User', required: true, index: true },
    word: { type: String, required: true, trim: true },
    meaning: { type: String, required: true },
    tamilMeaning: { type: String, default: '' },
    pronunciation: { type: String, default: '' },
    usageExample: { type: String, default: '' },
    learnedAt: { type: Date, default: Date.now },
  },
  { timestamps: false }
);

vocabularySchema.index({ userId: 1, word: 1 }, { unique: true });

module.exports = mongoose.model('Vocabulary', vocabularySchema);
