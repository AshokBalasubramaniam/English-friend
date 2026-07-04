'use strict';

const asyncHandler = require('../utils/asyncHandler');
const progressRepository = require('../repositories/progressRepository');

const listProgress = asyncHandler(async (req, res) => {
  const { limit, skip } = req.query;
  const progress = await progressRepository.findByUser(req.user._id, {
    limit: limit ? Number(limit) : undefined,
    skip: skip ? Number(skip) : undefined,
  });
  res.status(200).json({ success: true, data: { progress } });
});

const getTodayProgress = asyncHandler(async (req, res) => {
  const progress = await progressRepository.findByUserAndDate(req.user._id, new Date());
  res.status(200).json({ success: true, data: { progress: progress || null } });
});

module.exports = { listProgress, getTodayProgress };
