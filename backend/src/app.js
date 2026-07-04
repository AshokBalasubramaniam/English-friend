'use strict';

const express = require('express');
const helmet = require('helmet');
const cors = require('cors');
const env = require('./config/env');
const routes = require('./routes');
const errorMiddleware = require('./middleware/errorMiddleware');
const notFound = require('./middleware/notFound');
const { apiLimiter } = require('./middleware/rateLimiter');

const app = express();

app.use(helmet());
app.use(
  cors({
    origin: env.CORS_ORIGIN === '*' ? true : env.CORS_ORIGIN.split(','),
    credentials: true,
  })
);
app.use(express.json({ limit: '1mb' }));
app.use(express.urlencoded({ extended: true, limit: '1mb' }));

app.get('/health', (req, res) => {
  res.status(200).json({ success: true, message: 'English Friend AI backend is healthy', uptime: process.uptime() });
});

app.use('/api/v1', apiLimiter, routes);

app.use(notFound);
app.use(errorMiddleware);

module.exports = app;
