'use strict';

const { body, param } = require('express-validator');

const sendMessageValidator = [
  param('conversationId').isMongoId().withMessage('Invalid conversation id'),
  body('text')
    .trim()
    .isLength({ min: 1, max: 2000 })
    .withMessage('text must be between 1 and 2000 characters'),
];

module.exports = { sendMessageValidator };
