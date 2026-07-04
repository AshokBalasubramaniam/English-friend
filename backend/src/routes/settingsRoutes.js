'use strict';

const express = require('express');
const settingsController = require('../controllers/settingsController');
const { authenticate } = require('../middleware/authMiddleware');
const validateRequest = require('../middleware/validateRequest');
const { updateSettingsValidator } = require('../validators/settingsValidators');

const router = express.Router();

router.use(authenticate);

router.get('/', settingsController.getSettings);
router.patch('/', updateSettingsValidator, validateRequest, settingsController.updateSettings);

module.exports = router;
