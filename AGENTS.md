# LinkGo agent notes

## Fast start
- This is a single-module Android app (`:app`) built with AGP 9.2.0, Kotlin 2.2.10, Gradle 9.4.1, and Jetpack Compose.
- Use the wrapper from repo root on Windows PowerShell: `./gradlew.bat`.
- Required local prerequisite: Android SDK path in `local.properties` (`sdk.dir=...`).

## Verified commands
- Build debug APK: `./gradlew.bat :app:assembleDebug`
- Run unit tests: `./gradlew.bat :app:testDebugUnitTest`
- Run one test class: `./gradlew.bat :app:testDebugUnitTest --tests "com.friendevs.linkgo.ExampleUnitTest"`
- Run lint: `./gradlew.bat :app:lintDebug`
- Run instrumentation tests (device/emulator required): `./gradlew.bat :app:connectedDebugAndroidTest`
- Practical verification order for code changes: `:app:lintDebug` -> `:app:testDebugUnitTest` -> `:app:assembleDebug`.

## Build and env gotchas
- `com.google.gms.google-services` is applied in `app/build.gradle.kts`; `app/google-services.json` must exist locally or builds fail at `:app:processDebugGoogleServices`.
- `.gitignore` ignores `google-services.json`; do not rely on it being present in fresh clones.

## App wiring that is easy to break
- Real entrypoint is `app/src/main/java/com/friendevs/linkgo/MainActivity.kt`: initializes Firebase (`auth`, `database`), initializes Places, then mounts `Navigation()`.
- `auth` and `database` are top-level `lateinit` globals in `MainActivity.kt` and are imported directly by screens; avoid using them before `MainActivity.onCreate` runs.
- Navigation routes come from `Screens` enum in `app/src/main/java/com/friendevs/linkgo/ui/navigation/navigation.kt`; route names are `enum.name` strings.
- Route naming is mixed-case by design: `login` and `register` are lowercase enum entries, while other routes are capitalized (`Map`, `Feed`, etc.).

## Data/back-end conventions from code
- Firebase Realtime Database is accessed directly from ViewModels/repositories (no DI layer).
- Hotspots are stored under `/hotspots` with `creatorId`; user hotspot index is `/users/{uid}/hotspots/{hotspotId}=true`.
- User profile documents are stored at `/users/{uid}`.
- Firebase Storage paths used by profile flow: moment photos in `Post/{uid}/...`, profile photos in `ptps/{uid}/...`.

## Maps/Places specifics
- Google Maps key is currently duplicated in two places: manifest meta-data (`AndroidManifest.xml`) and `Places.initialize(...)` in `MainActivity.kt`; keep both in sync if changed.
- Map screen location UX depends on runtime `ACCESS_FINE_LOCATION` permission handling in `MapScreen.kt`.

## Tests scope
- Current test suite is minimal template coverage (`ExampleUnitTest`, `ExampleInstrumentedTest`), so functional regressions are mostly caught by lint/build/manual checks.
