'use strict';

const express = require('express');
const authController = require('../controllers/authController');
const validateRequest = require('../middleware/validateRequest');
const { authenticate } = require('../middleware/authMiddleware');
const { authLimiter } = require('../middleware/rateLimiter');
const {
  registerValidator,
  loginValidator,
  googleLoginValidator,
  refreshTokenValidator,
} = require('../validators/authValidators');

const router = express.Router();

router.post('/register', authLimiter, registerValidator, validateRequest, authController.register);
router.post('/login', authLimiter, loginValidator, validateRequest, authController.login);
router.post(
  '/google',
  authLimiter,
  googleLoginValidator,
  validateRequest,
  authController.googleLogin
);
router.post(
  '/refresh',
  authLimiter,
  refreshTokenValidator,
  validateRequest,
  authController.refresh
);
router.post('/logout', authenticate, authController.logout);
router.get('/me', authenticate, authController.me);

module.exports = router;
