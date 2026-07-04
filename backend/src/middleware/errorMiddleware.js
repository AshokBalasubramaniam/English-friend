'use strict';

const ApiError = require('../utils/ApiError');
const logger = require('../config/logger');
const env = require('../config/env');

/**
 * Centralized error handler. Never leaks stack traces (or raw internal
 * error messages) to clients in production.
 */
// eslint-disable-next-line no-unused-vars
function errorMiddleware(err, req, res, next) {
  let { statusCode, message } = err;

  if (!(err instanceof ApiError)) {
    statusCode = statusCode || 500;
    message = env.isProduction ? 'Internal server error' : err.message;
  }

  statusCode = statusCode || 500;

  logger.error(`${req.method} ${req.originalUrl} -> ${statusCode}: ${err.message}`, {
    stack: err.stack,
  });

  const body = {
    success: false,
    message: message || 'Something went wrong',
  };

  if (err.details) body.details = err.details;
  if (!env.isProduction && err.stack) body.stack = err.stack;

  res.status(statusCode).json(body);
}

module.exports = errorMiddleware;
