'use strict';

const ApiError = require('../utils/ApiError');

/**
 * Catches any request that didn't match a route and forwards a 404
 * to the centralized error handler.
 */
function notFound(req, res, next) {
  next(ApiError.notFound(`Route not found: ${req.method} ${req.originalUrl}`));
}

module.exports = notFound;
