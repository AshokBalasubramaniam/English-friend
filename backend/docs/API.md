# English Friend AI - API Reference

Base URL: `/api/v1`

All authenticated endpoints require:

```
Authorization: Bearer <accessToken>
```

Responses use the envelope `{ success: boolean, data?, message?, details? }`.

---

## Auth (`/auth`)

| Method | Path | Auth | Body | Description |
|---|---|---|---|---|
| POST | `/auth/register` | no | `{ email, password, name }` | Create a local account, returns user + tokens |
| POST | `/auth/login` | no | `{ email, password }` | Login with email/password |
| POST | `/auth/google` | no | `{ googleId, email, name }` | Login/register via Google profile (scaffold - see README) |
| POST | `/auth/refresh` | no | `{ refreshToken }` | Exchange a valid refresh token for a new access+refresh pair |
| POST | `/auth/logout` | yes | - | Invalidates the stored refresh token hash |
| GET  | `/auth/me` | yes | - | Returns the current authenticated user |

Auth routes are rate-limited more strictly than the rest of the API (see `middleware/rateLimiter.js`).

---

## Conversations (`/conversations`)

All routes require auth.

| Method | Path | Body / Query | Description |
|---|---|---|---|
| POST | `/conversations` | `{ mode? }` (`english` \| `tamil-english` \| `tamil`) | Start a new conversation |
| GET | `/conversations` | `?limit=&skip=` | List the user's conversations, newest first |
| GET | `/conversations/:conversationId` | - | Get one conversation (must belong to the user) |
| POST | `/conversations/:conversationId/end` | - | Ends the conversation, computes scores, extracts vocabulary |

## Messages (`/conversations/:conversationId/messages`)

| Method | Path | Body / Query | Description |
|---|---|---|---|
| GET | `/conversations/:conversationId/messages` | `?limit=&skip=` | List messages in a conversation |
| POST | `/conversations/:conversationId/messages` | `{ text }` | **Streaming** endpoint (Server-Sent Events). Persists the user's message, streams the AI friend's reply back live, then persists the AI reply (with Tamil translation). |

### SSE event shapes for `POST .../messages`

```
event: chunk
data: { "text": "partial reply text..." }

event: done
data: { "message": { ...persisted AI Message document... } }

event: error
data: { "message": "Failed to generate AI reply" }
```

---

## Vocabulary (`/vocabulary`)

| Method | Path | Description |
|---|---|---|
| GET | `/vocabulary?limit=&skip=` | List vocabulary/idioms learned by the user |
| DELETE | `/vocabulary/:id` | Remove a vocabulary entry |

## Progress (`/progress`)

| Method | Path | Description |
|---|---|---|
| GET | `/progress?limit=&skip=` | List daily progress records |
| GET | `/progress/today` | Get today's progress record (or `null`) |

## Settings (`/settings`)

| Method | Path | Body | Description |
|---|---|---|---|
| GET | `/settings` | - | Get (or lazily create) the user's settings |
| PATCH | `/settings` | `{ darkMode?, notificationsEnabled?, languageMode? }` | Update settings |

## Notifications (`/notifications`)

| Method | Path | Query | Description |
|---|---|---|---|
| GET | `/notifications` | `?limit=&skip=&unreadOnly=true` | List notifications |
| PATCH | `/notifications/:id/read` | - | Mark one notification as read |
| PATCH | `/notifications/read-all` | - | Mark all notifications as read |

## Feedback (`/feedback`)

| Method | Path | Body | Description |
|---|---|---|---|
| POST | `/feedback` | `{ message, rating (1-5) }` | Submit app feedback |
| GET | `/feedback` | - | List the current user's submitted feedback |

---

## Socket.io events

Connect to the same server/port with:

```js
io(URL, { auth: { token: '<accessToken>' } });
```

The handshake is rejected (`connect_error`) if the token is missing, invalid, expired, or belongs to a deleted user.

| Direction | Event | Payload | Description |
|---|---|---|---|
| Client -> Server | `user_message` | `{ conversationId, text }` | Send a chat message in an active conversation |
| Server -> Client | `ai_reply_chunk` | `{ text }` | One streamed token/chunk of the AI's reply (emitted repeatedly) |
| Server -> Client | `ai_reply_done` | `{ message }` | Final event once the full AI reply has been generated and persisted |
| Server -> Client | `ai_reply_error` | `{ message }` | Emitted if generation fails |

---

## Health check

`GET /health` (no `/api/v1` prefix, unauthenticated) - returns `{ success: true, message, uptime }`.
