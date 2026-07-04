'use strict';

const { body } = require('express-validator');

const createFeedbackValidator = [
  body('message').trim().isLength({ min: 1, max: 1000 }).withMessage('message is required'),
  body('rating').isInt({ min: 1, max: 5 }).withMessage('rating must be between 1 and 5'),
];

module.exports = { createFeedbackValidator };
