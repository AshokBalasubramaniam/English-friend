# English Friend AI

An AI conversation companion for practicing spoken English, with natural English/Tamil switching, live voice conversation, grammar correction, vocabulary building, and progress tracking.

This repo contains two independent projects:

```
android/   Kotlin + Jetpack Compose app (MVVM, Clean Architecture, Hilt, Room, Retrofit, Socket.io)
backend/   Node.js + Express + MongoDB API (JWT auth, OpenAI streaming, Socket.io)
```

## Status

Both projects are **scaffolds**: correct architecture and working core pieces (auth, chat streaming, navigation, persistence), not the full feature set from the product spec yet. See each project's own README for what's implemented vs. stubbed:

- [android/README.md](android/README.md)
- [backend/README.md](backend/README.md)
- [backend/docs/API.md](backend/docs/API.md)

## Local development

1. Backend:
   ```bash
   cd backend
   cp .env.example .env   # fill in MONGODB_URI, JWT secrets, OPENAI_API_KEY, GOOGLE_CLIENT_ID
   npm install
   npm run dev
   ```
   Or via Docker: `cd backend && docker compose up` (see `backend/docker-compose.yml`).

2. Android:
   - Open `android/` in Android Studio.
   - Add `BASE_URL` to `android/local.properties` pointing at your running backend.
   - Sync Gradle and run on an emulator or device.

## Suggested next steps

1. Wire real credentials into `backend/.env` and confirm `npm run dev` boots and `/health` responds.
2. Open the Android project in Studio, let Gradle sync, fix any first-sync issues (no SDK/AGP was available in the sandbox that generated this scaffold, so this hasn't been compiled yet).
3. Wire `SpeechRecognizerManager` into `ChatViewModel`'s mic button (currently stubbed).
4. Add Google Sign-In token verification server-side (`authService.googleLogin` has a TODO with the exact library call needed).
5. Everything past that (scoring UI/charts, PDF/DOCX export, notifications delivery, admin panel, full test suites, CI/CD, Play Store release config) — build incrementally, one feature at a time, verifying each end-to-end before moving on.
