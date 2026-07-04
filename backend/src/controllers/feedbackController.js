'use strict';

const asyncHandler = require('../utils/asyncHandler');
const feedbackRepository = require('../repositories/feedbackRepository');

const createFeedback = asyncHandler(async (req, res) => {
  const { message, rating } = req.body;
  const feedback = await feedbackRepository.create({ userId: req.user._id, message, rating });
  res.status(201).json({ success: true, data: { feedback } });
});

const listMyFeedback = asyncHandler(async (req, res) => {
  const feedback = await feedbackRepository.findByUser(req.user._id);
  res.status(200).json({ success: true, data: { feedback } });
});

module.exports = { createFeedback, listMyFeedback };
