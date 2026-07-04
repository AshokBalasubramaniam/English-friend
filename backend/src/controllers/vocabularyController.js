'use strict';

const asyncHandler = require('../utils/asyncHandler');
const vocabularyService = require('../services/vocabularyService');
const vocabularyRepository = require('../repositories/vocabularyRepository');

const listVocabulary = asyncHandler(async (req, res) => {
  const { limit, skip } = req.query;
  const items = await vocabularyService.listForUser(req.user._id, {
    limit: limit ? Number(limit) : undefined,
    skip: skip ? Number(skip) : undefined,
  });
  res.status(200).json({ success: true, data: { items } });
});

const deleteVocabulary = asyncHandler(async (req, res) => {
  await vocabularyRepository.deleteById(req.params.id);
  res.status(200).json({ success: true, message: 'Vocabulary entry deleted' });
});

module.exports = { listVocabulary, deleteVocabulary };
