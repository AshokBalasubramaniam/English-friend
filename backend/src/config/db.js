'use strict';

const mongoose = require('mongoose');
const env = require('./env');
const logger = require('./logger');

mongoose.set('strictQuery', true);

/**
 * Connects to MongoDB (Atlas or local) using MONGODB_URI.
 * Exits the process on failure to connect at boot, since the app
 * cannot function without a database.
 */
async function connectDB() {
  try {
    logger.info('Connecting to MongoDB...');

    mongoose.connection.on('connected', () => {
      logger.info('MongoDB connection established');
    });

    mongoose.connection.on('error', (err) => {
      logger.error(`MongoDB connection error: ${err.message}`);
    });

    mongoose.connection.on('disconnected', () => {
      logger.warn('MongoDB disconnected');
    });

    await mongoose.connect(env.MONGODB_URI, {
      autoIndex: !env.isProduction,
    });

    return mongoose.connection;
  } catch (err) {
    logger.error(`Failed to connect to MongoDB: ${err.message}`);
    throw err;
  }
}

async function disconnectDB() {
  await mongoose.disconnect();
}

module.exports = { connectDB, disconnectDB, mongoose };
