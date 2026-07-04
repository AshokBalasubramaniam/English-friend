'use strict';

const mongoose = require('mongoose');
const { Schema } = mongoose;

const messageSchema = new Schema(
  {
    conversationId: {
      type: Schema.Types.ObjectId,
      ref: 'Conversation',
      required: true,
      index: true,
    },
    sender: { type: String, enum: ['user', 'ai'], required: true },
    englishText: { type: String, required: true },
    tamilTranslation: { type: String, default: '' },
    audioUrl: { type: String, default: null },
    createdAt: { type: Date, default: Date.now },
  },
  { timestamps: false }
);

messageSchema.index({ conversationId: 1, createdAt: 1 });

module.exports = mongoose.model('Message', messageSchema);
