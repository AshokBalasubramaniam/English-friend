'use strict';

const asyncHandler = require('../utils/asyncHandler');
const notificationService = require('../services/notificationService');

const listNotifications = asyncHandler(async (req, res) => {
  const { limit, skip, unreadOnly } = req.query;
  const notifications = await notificationService.listForUser(req.user._id, {
    limit: limit ? Number(limit) : undefined,
    skip: skip ? Number(skip) : undefined,
    unreadOnly: unreadOnly === 'true',
  });
  res.status(200).json({ success: true, data: { notifications } });
});

const markRead = asyncHandler(async (req, res) => {
  const notification = await notificationService.markRead(req.params.id);
  res.status(200).json({ success: true, data: { notification } });
});

const markAllRead = asyncHandler(async (req, res) => {
  await notificationService.markAllRead(req.user._id);
  res.status(200).json({ success: true, message: 'All notifications marked as read' });
});

module.exports = { listNotifications, markRead, markAllRead };
