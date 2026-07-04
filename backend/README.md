# English Friend AI - Backend

Backend API for **English Friend AI**, a WhatsApp-style, English/Tamil bilingual
chat app where an AI "best friend" helps users practice spoken English -
remembering personal details, chatting casually, and correcting mistakes
kindly without breaking the flow of conversation.

## Architecture

Layered / repository pattern - no business logic in route handlers:

```
routes -> controllers -> services -> repositories -> models
```

- **models/** - Mongoose schemas (User, Conversation, Message, Correction, Vocabulary, Progress, Settings, Notification, Feedback, Analytics)
- **repositories/** - thin CRUD/query wrappers over each model
- **services/** - business logic (auth, conversation flow, OpenAI integration, correction, translation, scoring, vocabulary extraction, notifications)
- **controllers/** - thin HTTP glue: parse req, call service, shape res
- **routes/** - Express routers, mounted under `/api/v1`
- **middleware/** - auth, rate limiting, validation, centralized error handling
- **sockets/** - Socket.io real-time chat streaming
- **utils/** - ApiError, asyncHandler, JWT helpers

## Setup

1. Install dependencies:

   ```bash
   npm install
   ```

2. Copy the example env file and fill in real values (never commit `.env`):

   ```bash
   cp .env.example .env
   ```

   You need at minimum: `MONGODB_URI`, `JWT_ACCESS_SECRET`, `JWT_REFRESH_SECRET`,
   and `OPENAI_API_KEY`. The server refuses to start if any required
   variable is missing (see `src/config/env.js`).

3. Run in development (auto-restart via nodemon):

   ```bash
   npm run dev
   ```

4. Run tests:

   ```bash
   npm test
   ```

5. Or run with Docker Compose (spins up the backend + a local MongoDB):

   ```bash
   docker compose up --build
   ```

   When using the bundled `mongo` service, set `MONGODB_URI=mongodb://mongo:27017/english-friend-ai`
   in your `.env`.

## API base path

All REST endpoints are mounted under:

```
/api/v1
```

Health check (unauthenticated): `GET /health`

See [`docs/API.md`](./docs/API.md) for the full list of endpoints and Socket.io events.

## How real-time chat works

There are two equivalent ways to chat with the AI friend and receive a
**streamed** reply (token-by-token, like a typing effect):

1. **REST + Server-Sent Events**: `POST /api/v1/conversations/:conversationId/messages`
   streams `chunk` / `done` / `error` SSE events on the same HTTP response.
2. **Socket.io**: connect with `auth: { token: '<accessToken>' }`, then emit
   `user_message` with `{ conversationId, text }`. The server streams back
   `ai_reply_chunk` events as the AI reply is generated, then a final
   `ai_reply_done` event with the persisted AI message (including its Tamil
   translation).

Both paths share the same underlying logic in `services/conversationService.js`
and `services/aiService.js`, so behavior (persona, memory, corrections) is
identical regardless of transport.

## Deploying to Render

This repo includes a `render.yaml` Blueprint. Steps:

1. Push this `backend/` folder to a GitHub repo (Render deploys from a git repo, not a local folder).
2. In the Render dashboard: **New > Blueprint**, point it at that repo. Render reads `render.yaml` and creates the web service automatically (Node runtime, `npm install` build, `npm start` start command, `/health` health check).
3. Render prompts you to fill in the vars marked `sync: false` — paste in your real `MONGODB_URI`, `JWT_ACCESS_SECRET`, `JWT_REFRESH_SECRET`, `OPENAI_API_KEY`, and `GOOGLE_CLIENT_ID` there. These are stored encrypted in Render, never in the repo.
4. Do **not** set `PORT` yourself — Render injects its own `PORT` env var at runtime, and `server.js` already reads `process.env.PORT`.
5. In MongoDB Atlas, make sure Network Access allows connections from anywhere (`0.0.0.0/0`) or Render's static outbound IPs, otherwise the deployed service can't reach your cluster.
6. Once live, confirm with `GET https://<your-service>.onrender.com/health`.

If you'd rather configure the service manually instead of via Blueprint: Runtime = Node, Build Command = `npm install`, Start Command = `npm start`, then add the same env vars individually under the service's **Environment** tab.

## Notes on scope (this is a scaffold pass)

- `aiService.js`, `correctionService.js`, and `translationService.js` contain
  real, runnable OpenAI integrations.
- `authService.googleLogin` validates a client-supplied Google profile but
  does **not** yet cryptographically verify the Google ID token against
  Google's public keys - see the `TODO(production)` comment in that file.
- `notificationService.js` persists in-app notifications but does not yet
  push to devices (FCM/APNs) - see its `TODO(production)` comment.
- `scoringService.js` uses a documented heuristic + AI-assisted blend;
  pronunciation scoring is a placeholder until an audio/ASR pipeline exists.
