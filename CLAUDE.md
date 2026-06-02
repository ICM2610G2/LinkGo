# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

LinkGo is a single-module Android app (`:app`) built with Kotlin, Jetpack Compose, Firebase, and Google Maps. It is a location-based social app for discovering hotspots, sharing locations, and arranging meetups.

- **AGP:** 9.2.0 | **Kotlin:** 2.2.10 | **Gradle:** 9.4.1
- **Min SDK:** 30 | **Target/Compile SDK:** 36
- **UI:** Jetpack Compose (no XML layouts)

## Commands

Use the Gradle wrapper from the repo root on Windows PowerShell:

```powershell
./gradlew.bat :app:assembleDebug                   # Build debug APK
./gradlew.bat :app:lintDebug                       # Lint
./gradlew.bat :app:testDebugUnitTest               # Unit tests
./gradlew.bat :app:testDebugUnitTest --tests "com.friendevs.linkgo.ExampleUnitTest"  # Single test class
./gradlew.bat :app:connectedDebugAndroidTest       # Instrumentation tests (device/emulator required)
```

Recommended verification order for code changes: `lintDebug` → `testDebugUnitTest` → `assembleDebug`.

## Architecture

### Entry Point & Globals

`MainActivity.kt` is the real entry point. It initializes Firebase Auth + Realtime Database as **top-level `lateinit` globals** (`auth`, `database`), initializes the Google Places API, and mounts the `Navigation()` composable with a `SensorViewModel`. Screens import these globals directly — do not access them before `onCreate` runs.

### Navigation

Routes are defined in the `Screens` enum in `ui/navigation/navigation.kt`. Route strings are `enum.name` — mixed case by design:
- `login`, `register` — lowercase, no bottom nav bar
- `Map`, `Feed`, `Chat`, `ChatDetail`, `Hotspots`, `Profile`, `MeetUp`, `AddHotspot` — capitalized, with bottom nav

### Feature Structure

Each feature lives under `ui/feature/<name>/` and follows a screen + ViewModel pattern:

| Feature | Purpose |
|---------|---------|
| `auth/` | Login and register screens |
| `map/` | Map view, hotspot list, add hotspot |
| `chat/` | Chat list and individual chat detail |
| `feed/` | Social feed |
| `profile/` | User profile |
| `meetup/` | MeetUp scheduling |
| `routes/` | Directions service and polyline decoding |

### Data Layer

Firebase Realtime Database is accessed **directly from ViewModels/repositories** — there is no dependency injection layer.

Key Firebase paths:
- `/users/{uid}` — user profile documents
- `/hotspots` — hotspot entries with `creatorId` field
- `/users/{uid}/hotspots/{hotspotId}=true` — per-user hotspot index
- Firebase Storage: `Post/{uid}/...` for moment photos, `ptps/{uid}/...` for profile photos

### Sensor Safety Feature

`SensorViewModel` (in `domain/model/`) detects shake events and triggers an emergency modal bottom sheet prompting the user to call for help. It is passed into the `Navigation()` composable from `MainActivity`.

## Environment Gotchas

- `app/google-services.json` must exist locally — it is `.gitignore`d and is required for builds. Builds fail at `:app:processDebugGoogleServices` without it.
- The Google Maps API key is duplicated in two places: `AndroidManifest.xml` meta-data and the `Places.initialize(...)` call in `MainActivity.kt`. Keep both in sync when changing the key.
- `local.properties` must contain `sdk.dir=<path-to-android-sdk>`.

## Tests

The current test suite is minimal template coverage (`ExampleUnitTest`, `ExampleInstrumentedTest`). Functional regressions are primarily caught by lint, build, and manual device testing.

See `AGENTS.md` for additional agent-oriented notes on the same topics.
