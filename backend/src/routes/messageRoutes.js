'use strict';

const express = require('express');
const messageController = require('../controllers/messageController');
const validateRequest = require('../middleware/validateRequest');
const { sendMessageValidator } = require('../validators/messageValidators');

// mergeParams so we can read :conversationId from the parent router
// (conversationRoutes mounts this at /:conversationId/messages).
const router = express.Router({ mergeParams: true });

router.get('/', messageController.listMessages);
router.post('/', sendMessageValidator, validateRequest, messageController.sendMessage);

module.exports = router;
