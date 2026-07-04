'use strict';

const { Server } = require('socket.io');
const { verifyAccessToken } = require('../utils/tokenUtils');
const userRepository = require('../repositories/userRepository');
const logger = require('../config/logger');
const env = require('../config/env');
const registerChatSocket = require('./chatSocket');

/**
 * Initializes Socket.io on the given HTTP server and authenticates every
 * connecting socket using the same JWT access token used for REST calls
 * (sent by the client as `auth: { token: '<accessToken>' }`).
 */
function initSockets(httpServer) {
  const io = new Server(httpServer, {
    cors: {
      origin: env.CORS_ORIGIN,
      credentials: true,
    },
  });

  io.use(async (socket, next) => {
    try {
      const token =
        socket.handshake.auth?.token ||
        (socket.handshake.headers?.authorization || '').replace('Bearer ', '');

      if (!token) return next(new Error('Authentication token required'));

      const payload = verifyAccessToken(token);
      if (payload.type !== 'access') return next(new Error('Invalid token type'));

      const user = await userRepository.findById(payload.sub);
      if (!user) return next(new Error('User no longer exists'));

      socket.user = user;
      next();
    } catch (err) {
      next(new Error('Authentication failed'));
    }
  });

  io.on('connection', (socket) => {
    logger.info(`Socket connected: ${socket.id} (user ${socket.user._id})`);
    registerChatSocket(io, socket);

    socket.on('disconnect', (reason) => {
      logger.info(`Socket disconnected: ${socket.id} (${reason})`);
    });
  });

  return io;
}

module.exports = initSockets;
