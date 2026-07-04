'use strict';

const express = require('express');
const feedbackController = require('../controllers/feedbackController');
const { authenticate } = require('../middleware/authMiddleware');
const validateRequest = require('../middleware/validateRequest');
const { createFeedbackValidator } = require('../validators/feedbackValidators');

const router = express.Router();

router.use(authenticate);

router.post('/', createFeedbackValidator, validateRequest, feedbackController.createFeedback);
router.get('/', feedbackController.listMyFeedback);

module.exports = router;
