'use strict';

const mongoose = require('mongoose');
const { Schema } = mongoose;

const analyticsSchema = new Schema(
  {
    userId: { type: Schema.Types.ObjectId, ref: 'User', required: true, index: true },
    event: { type: String, required: true },
    metadata: { type: Schema.Types.Mixed, default: {} },
    createdAt: { type: Date, default: Date.now },
  },
  { timestamps: false }
);

analyticsSchema.index({ userId: 1, event: 1, createdAt: -1 });

module.exports = mongoose.model('Analytics', analyticsSchema);
