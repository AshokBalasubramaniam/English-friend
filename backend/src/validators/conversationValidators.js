'use strict';

const { body, param } = require('express-validator');

const startConversationValidator = [
  body('mode')
    .optional()
    .isIn(['english', 'tamil-english', 'tamil'])
    .withMessage('mode must be one of english, tamil-english, tamil'),
];

const conversationIdParamValidator = [
  param('conversationId').isMongoId().withMessage('Invalid conversation id'),
];

module.exports = { startConversationValidator, conversationIdParamValidator };
