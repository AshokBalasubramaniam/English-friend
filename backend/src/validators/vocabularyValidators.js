'use strict';

const { param } = require('express-validator');

const vocabularyIdParamValidator = [
  param('id').isMongoId().withMessage('Invalid vocabulary id'),
];

module.exports = { vocabularyIdParamValidator };
