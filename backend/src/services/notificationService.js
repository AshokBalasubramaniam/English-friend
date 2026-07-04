'use strict';

const notificationRepository = require('../repositories/notificationRepository');

/**
 * Creates an in-app reminder/notification record.
 *
 * TODO(production): integrate with Firebase Cloud Messaging (FCM) or APNs
 * to actually push this notification to the user's device. This scaffold
 * only persists the notification so it can be listed via GET /notifications;
 * wiring in real push delivery is out of scope for this pass.
 */
async function createNotification(userId, { type = 'reminder', message }) {
  return notificationRepository.create({ userId, type, message, sentAt: new Date() });
}

async function createDailyPracticeReminder(userId) {
  return createNotification(userId, {
    type: 'reminder',
    message: "Hey! Your English Friend is waiting to chat today. Let's practice for a few minutes!",
  });
}

function listForUser(userId, options) {
  return notificationRepository.findByUser(userId, options);
}

function markRead(notificationId) {
  return notificationRepository.markRead(notificationId);
}

function markAllRead(userId) {
  return notificationRepository.markAllRead(userId);
}

module.exports = {
  createNotification,
  createDailyPracticeReminder,
  listForUser,
  markRead,
  markAllRead,
};
