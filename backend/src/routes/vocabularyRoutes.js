'use strict';

const express = require('express');
const vocabularyController = require('../controllers/vocabularyController');
const { authenticate } = require('../middleware/authMiddleware');
const validateRequest = require('../middleware/validateRequest');
const { vocabularyIdParamValidator } = require('../validators/vocabularyValidators');

const router = express.Router();

router.use(authenticate);

router.get('/', vocabularyController.listVocabulary);
router.delete('/:id', vocabularyIdParamValidator, validateRequest, vocabularyController.deleteVocabulary);

module.exports = router;
