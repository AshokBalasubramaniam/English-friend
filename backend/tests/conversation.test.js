'use strict';

/**
 * Basic test stub for conversation creation. Uses mongodb-memory-server,
 * same approach as auth.test.js.
 */

process.env.NODE_ENV = 'test';
process.env.JWT_ACCESS_SECRET = process.env.JWT_ACCESS_SECRET || 'test-access-secret';
process.env.JWT_REFRESH_SECRET = process.env.JWT_REFRESH_SECRET || 'test-refresh-secret';
process.env.OPENAI_API_KEY = process.env.OPENAI_API_KEY || 'test-openai-key';

let mongoServer;
let mongoose;
let app;

beforeAll(async () => {
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

describe('Conversation API', () => {
  const request = require('supertest');
  let accessToken;

  beforeEach(async () => {
    const res = await request(app).post('/api/v1/auth/register').send({
      email: `user${Date.now()}@example.com`,
      password: 'password123',
      name: 'Conversation Tester',
    });
    accessToken = res.body.data.accessToken;
  });

  it('creates a new conversation for an authenticated user', async () => {
    const res = await request(app)
      .post('/api/v1/conversations')
      .set('Authorization', `Bearer ${accessToken}`)
      .send({ mode: 'english' });

    expect(res.statusCode).toBe(201);
    expect(res.body.success).toBe(true);
    expect(res.body.data.conversation.mode).toBe('english');
    expect(res.body.data.conversation.endedAt).toBeNull();
  });

  it('rejects conversation creation without auth', async () => {
    const res = await request(app).post('/api/v1/conversations').send({ mode: 'english' });
    expect(res.statusCode).toBe(401);
  });

  // TODO: add coverage for POST /messages once a test double / mock for
  // aiService.streamReply is introduced, so tests don't call the real
  // OpenAI API.
});
