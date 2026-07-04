'use strict';

const express = require('express');
const progressController = require('../controllers/progressController');
const { authenticate } = require('../middleware/authMiddleware');

const router = express.Router();

router.use(authenticate);

router.get('/', progressController.listProgress);
router.get('/today', progressController.getTodayProgress);

module.exports = router;
