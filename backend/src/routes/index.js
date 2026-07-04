'use strict';

const express = require('express');

const authRoutes = require('./authRoutes');
const conversationRoutes = require('./conversationRoutes');
const vocabularyRoutes = require('./vocabularyRoutes');
const progressRoutes = require('./progressRoutes');
const settingsRoutes = require('./settingsRoutes');
const notificationRoutes = require('./notificationRoutes');
const feedbackRoutes = require('./feedbackRoutes');

const router = express.Router();

router.use('/auth', authRoutes);
router.use('/conversations', conversationRoutes);
router.use('/vocabulary', vocabularyRoutes);
router.use('/progress', progressRoutes);
router.use('/settings', settingsRoutes);
router.use('/notifications', notificationRoutes);
router.use('/feedback', feedbackRoutes);

module.exports = router;
