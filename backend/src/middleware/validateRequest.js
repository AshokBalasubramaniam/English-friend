'use strict';

const { validationResult } = require('express-validator');

/**
 * Runs after express-validator chains on a route. If any validation
 * failed, responds 400 with the details instead of reaching the controller.
 */
function validateRequest(req, res, next) {
  const errors = validationResult(req);
  if (!errors.isEmpty()) {
    return res.status(400).json({
      success: false,
      message: 'Validation failed',
      details: errors.array().map((e) => ({ field: e.path, message: e.msg })),
    });
  }
  next();
}

module.exports = validateRequest;
