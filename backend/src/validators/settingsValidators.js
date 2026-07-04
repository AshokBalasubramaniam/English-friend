'use strict';

const { body } = require('express-validator');

const updateSettingsValidator = [
  body('darkMode').optional().isBoolean(),
  body('notificationsEnabled').optional().isBoolean(),
  body('languageMode').optional().isIn(['english', 'tamil-english', 'tamil']),
];

module.exports = { updateSettingsValidator };
