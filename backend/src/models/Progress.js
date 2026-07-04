'use strict';

const mongoose = require('mongoose');
const { Schema } = mongoose;

const progressSchema = new Schema(
  {
    userId: { type: Schema.Types.ObjectId, ref: 'User', required: true, index: true },
    date: { type: Date, required: true },
    practiceMinutes: { type: Number, default: 0 },
    wordsLearned: { type: Number, default: 0 },
    mistakesFixed: { type: Number, default: 0 },
    streak: { type: Number, default: 0 },
  },
  { timestamps: true }
);

progressSchema.index({ userId: 1, date: 1 }, { unique: true });

module.exports = mongoose.model('Progress', progressSchema);
