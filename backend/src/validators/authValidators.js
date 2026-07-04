'use strict';

const { body } = require('express-validator');

const registerValidator = [
  body('email').isEmail().withMessage('A valid email is required').normalizeEmail(),
  body('password')
    .isLength({ min: 8 })
    .withMessage('Password must be at least 8 characters long'),
  body('name').trim().notEmpty().withMessage('Name is required'),
];

const loginValidator = [
  body('email').isEmail().withMessage('A valid email is required').normalizeEmail(),
  body('password').notEmpty().withMessage('Password is required'),
];

const googleLoginValidator = [
  body('idToken').optional().isString(),
  body('googleId').notEmpty().withMessage('googleId is required'),
  body('email').isEmail().withMessage('A valid email is required'),
  body('name').optional().isString(),
];

const refreshTokenValidator = [
  body('refreshToken').notEmpty().withMessage('refreshToken is required'),
];

module.exports = {
  registerValidator,
  loginValidator,
  googleLoginValidator,
  refreshTokenValidator,
};
