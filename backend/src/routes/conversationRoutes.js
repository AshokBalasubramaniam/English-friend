'use strict';

const express = require('express');
const conversationController = require('../controllers/conversationController');
const { authenticate } = require('../middleware/authMiddleware');
const validateRequest = require('../middleware/validateRequest');
const {
  startConversationValidator,
  conversationIdParamValidator,
} = require('../validators/conversationValidators');
const messageRoutes = require('./messageRoutes');

const router = express.Router();

router.use(authenticate);

router.post('/', startConversationValidator, validateRequest, conversationController.startConversation);
router.get('/', conversationController.listConversations);
router.get(
  '/:conversationId',
  conversationIdParamValidator,
  validateRequest,
  conversationController.getConversation
);
router.post(
  '/:conversationId/end',
  conversationIdParamValidator,
  validateRequest,
  conversationController.endConversation
);

// Nested: /conversations/:conversationId/messages
router.use('/:conversationId/messages', messageRoutes);

module.exports = router;
