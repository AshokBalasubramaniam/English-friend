'use strict';

/**
 * Auth flow tests (register/login happy path + validation failure).
 *
 * These tests use `mongodb-memory-server` to spin up an ephemeral, real
 * MongoDB instance in-process so we don't need a live database or Atlas
 * connection to run the test suite.
 *
 * If `mongodb-memory-server` is not installed in this environment yet,
 * run: npm install --save-dev mongodb-memory-server
 */

process.env.NODE_ENV = 'test';
process.env.JWT_ACCESS_SECRET = process.env.JWT_ACCESS_SECRET || 'test-access-secret';
process.env.JWT_REFRESH_SECRET = process.env.JWT_REFRESH_SECRET || 'test-refresh-secret';
process.env.OPENAI_API_KEY = process.env.OPENAI_API_KEY || 'test-openai-key';

let mongoServer;
let mongoose;
let app;

beforeAll(async () => {
  // TODO: if mongodb-memory-server is unavailable in this environment,
  // replace this block with a connection to a real test MongoDB instance
  // (e.g. process.env.MONGODB_URI_TEST) instead of skipping.
  const { MongoMemoryServer } = require('mongodb-memory-server');
  mongoServer = await MongoMemoryServer.create();
  process.env.MONGODB_URI = mongoServer.getUri();

  mongoose = require('mongoose');
  app = require('../src/app');
  await mongoose.connect(process.env.MONGODB_URI);
});

afterAll(async () => {
  if (mongoose) await mongoose.connection.dropDatabase();
  if (mongoose) await mongoose.disconnect();
  if (mongoServer) await mongoServer.stop();
});

describe('Auth API', () => {
  const request = require('supertest');

  it('registers a new user and returns tokens (happy path)', async () => {
    const res = await request(app).post('/api/v1/auth/register').send({
      email: 'friend@example.com',
      password: 'password123',
      name: 'Test Friend',
    });

    expect(res.statusCode).toBe(201);
    expect(res.body.success).toBe(true);
    expect(res.body.data.user.email).toBe('friend@example.com');
    expect(res.body.data.accessToken).toBeDefined();
    expect(res.body.data.refreshToken).toBeDefined();
  });

  it('logs in an existing user (happy path)', async () => {
    await request(app).post('/api/v1/auth/register').send({
      email: 'login@example.com',
      password: 'password123',
      name: 'Login User',
    });

    const res = await request(app).post('/api/v1/auth/login').send({
      email: 'login@example.com',
      password: 'password123',
    });

    expect(res.statusCode).toBe(200);
    expect(res.body.data.accessToken).toBeDefined();
  });

  it('rejects registration with an invalid email (validation failure)', async () => {
    const res = await request(app).post('/api/v1/auth/register').send({
      email: 'not-an-email',
      password: 'password123',
      name: 'Bad Email',
    });

    expect(res.statusCode).toBe(400);
    expect(res.body.success).toBe(false);
  });

  it('rejects registration with a too-short password (validation failure)', async () => {
    const res = await request(app).post('/api/v1/auth/register').send({
      email: 'shortpass@example.com',
      password: '123',
      name: 'Short Pass',
    });

    expect(res.statusCode).toBe(400);
  });
});
