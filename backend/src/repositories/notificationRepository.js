'use strict';

const Notification = require('../models/Notification');

const notificationRepository = {
  create(data) {
    return Notification.create(data);
  },

  findByUser(userId, { limit = 20, skip = 0, unreadOnly = false } = {}) {
    const query = { userId };
    if (unreadOnly) query.read = false;
    return Notification.find(query).sort({ sentAt: -1 }).skip(skip).limit(limit);
  },

  markRead(id) {
    return Notification.findByIdAndUpdate(id, { read: true }, { new: true });
  },

  markAllRead(userId) {
    return Notification.updateMany({ userId, read: false }, { $set: { read: true } });
  },
};

module.exports = notificationRepository;
