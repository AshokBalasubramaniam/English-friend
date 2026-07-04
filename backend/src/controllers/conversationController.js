'use strict';

const asyncHandler = require('../utils/asyncHandler');
const conversationService = require('../services/conversationService');

const startConversation = asyncHandler(async (req, res) => {
  const { mode } = req.body;
  const conversation = await conversationService.startConversation(req.user._id, mode);
  res.status(201).json({ success: true, data: { conversation } });
});

const listConversations = asyncHandler(async (req, res) => {
  const { limit, skip } = req.query;
  const conversations = await conversationService.listConversations(req.user._id, {
    limit: limit ? Number(limit) : undefined,
    skip: skip ? Number(skip) : undefined,
  });
  res.status(200).json({ success: true, data: { conversations } });
});

const getConversation = asyncHandler(async (req, res) => {
  const conversation = await conversationService.getConversation(req.user._id, req.params.conversationId);
  res.status(200).json({ success: true, data: { conversation } });
});

const endConversation = asyncHandler(async (req, res) => {
  const conversation = await conversationService.endConversation(req.user._id, req.params.conversationId);
  res.status(200).json({ success: true, data: { conversation } });
});

module.exports = { startConversation, listConversations, getConversation, endConversation };
