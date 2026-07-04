# English Friend AI — Android

An AI conversation-practice app to help learners improve spoken English, with bilingual
(English / Tamil) support. This repository currently contains an **architecture scaffold**:
the app is wired end-to-end (Compose UI -> ViewModel -> UseCase -> Repository -> Room/Retrofit/
Socket.IO) with real, if simple, screens for Login and Chat, and stubs/TODOs for everything that
depends on a real backend or design assets.

## Tech stack

- Kotlin + Jetpack Compose + Material 3 (with dynamic color)
- MVVM + Clean Architecture (`domain` / `data` / `presentation`)
- Hilt for dependency injection
- Retrofit + OkHttp (auth interceptor, certificate-pinning stub)
- Room, encrypted at rest via SQLCipher (passphrase sealed in AndroidKeystore-backed
  EncryptedSharedPreferences)
- DataStore (Preferences) for session/settings
- Kotlin Coroutines + Flow throughout
- Navigation Compose
- socket.io-client (Java) for realtime streaming chat
- Coil for image loading
- Media3/ExoPlayer for AI voice playback
- WorkManager (+ Hilt WorkerFactory) for a daily practice reminder notification
- androidx.biometric for optional biometric app-lock

## Requirements

- **Android Studio**: Koala (2024.1.1) or newer, bundled with an AGP 8.5+/Gradle 8.7-compatible
  JDK 17.
- **JDK 17** (Android Studio's embedded JDK works fine).
- An Android SDK with **compileSdk 34** / **minSdk 26** installed via the SDK Manager.

This scaffold was authored without a local Android SDK installed, so it has not been built with
a real Gradle/AGP toolchain yet. All Kotlin/XML/Gradle files are syntactically valid, but you
should expect to do a first-build pass (dependency resolution, minor API-level fixes) when you
open it in Android Studio.

## First-time setup

1. **Clone/open the project** in Android Studio, pointing at this `android/` directory as the
   project root.
2. **Create `local.properties`** in this directory (it's git-ignored) with at least your SDK
   path, and optionally a backend URL override:

   ```properties
   sdk.dir=/path/to/Android/sdk
   BASE_URL=https://your-dev-api.example.com/
   ```

   If `BASE_URL` is omitted, the build falls back to the `BASE_URL_DEFAULT` value in
   `gradle.properties`.
3. **(Optional, release builds only) Create `keystore.properties`** in this directory (also
   git-ignored) if you intend to produce a signed release build:

   ```properties
   storeFile=/absolute/path/to/release.jks
   storePassword=...
   keyAlias=...
   keyPassword=...
   ```

   No keystore file is included in this repository. Debug builds work without this file.
4. **Sync Gradle** and let Android Studio download dependencies (the Gradle wrapper jar binary
   itself is intentionally not committed — see "Known omissions" below).
5. **Run** the `app` configuration on an emulator or device running API 26+.

## Known omissions / stubs (read before building further)

- **`gradle/wrapper/gradle-wrapper.jar`** is not committed (binary file). `gradle-wrapper.properties`
  points at Gradle 8.7 — regenerate the jar with `gradle wrapper --gradle-version 8.7` once you
  have a local Gradle install, or let Android Studio regenerate it on first sync.
- **Certificate pinning** (`di/NetworkModule.kt`, `res/xml/network_security_config.xml`) is
  stubbed out with TODOs — no real SPKI pins are set. Add them before a production release.
- **SQLCipher passphrase lifecycle** (`data/local/db/AppDatabase.kt`,
  `core/security/EncryptedPrefsManager.kt`) generates and stores a random passphrase on first
  run; the TODOs there call out install/restore edge cases worth revisiting.
- **Google Sign-In** button on the Login screen is a no-op — wire up the real Google Identity
  Services client and feed its ID token into `LoginViewModel.loginWithGoogle()`.
- **Speech-to-text wiring**: `SpeechRecognizerManager` is implemented but not yet connected to
  `ChatViewModel`'s mic button; `onMicToggle()` has a TODO marking where to plug it in.
- **`ExportTranscriptUseCase`** always returns `Result.failure(NotImplementedError(...))` — the
  export format/UX hasn't been decided yet.
- **App launcher icon** is a placeholder vector (blue background + speech-bubble glyph), not a
  final design asset.
- **No automated tests** are included yet; `test`/`androidTest` dependencies are wired in
  `app/build.gradle.kts` so adding them is a matter of writing the test classes.
- **Single Gradle module** (`:app`) — intentionally not split into multiple modules for this
  scaffold; consider modularizing (`:core`, `:data`, `:domain`, `:feature-*`) once the app grows.

## Project structure

```
app/src/main/java/com/englishfriendai/app/
  di/              Hilt modules (Network, Database, Repository, Dispatcher)
  core/            network, security, audio, util, worker — cross-cutting infrastructure
  data/            local (Room + DataStore), remote (Retrofit DTOs), repository impls, mappers
  domain/          models, repository interfaces, use cases (pure Kotlin, no Android deps)
  presentation/    navigation, theme, and one package per feature (auth, chat, history, ...)
```
