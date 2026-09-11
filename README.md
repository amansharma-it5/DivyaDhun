# DivyaDhun · दिव्यधुन

Your Daily Bhakti. Bhakti, bas ek tap mein.

An Android-first devotional listening product. **Current scope: Phase 0 and Phase 1 foundation only.** This build contains no playable catalog.

## Implemented

- Material 3 Compose theme with warm light/dark palettes, scalable English/Devanagari typography, semantic colors, spacing and rounded shapes.
- Home, Explore, Search and My Bhakti navigation scaffolds; selected tab and navigation restoration.
- Home discovery link, honest future-content states, persistent System/Light/Dark appearance settings via DataStore.
- Original vector launcher mark and decorative line artwork; resource-based visible strings.
- Edge-to-edge scaffold with system insets, scrollable screens, minimum 48dp controls and labeled navigation.

## Architecture

One app module keeps this initial project easy to inspect. Packages separate UI, reusable components and theme. ShellViewModel exposes immutable ShellUiState through StateFlow; DataStore owns appearance persistence. Navigation Compose owns the back stack. UI does not access storage.

Future phases should introduce domain repository contracts and data implementations with feature ViewModels when real content arrives. Media3 playback will live in a service behind PlaybackController, separate from Compose. Room, Hilt, Coil, backend and media libraries are intentionally absent until needed.

## Toolchain and build

Pinned to the available environment: AGP 9.4.0, Gradle 9.7.1, Kotlin/Compose compiler 2.2.10, Compose BOM 2026.02.01, Material 3, Navigation 2.9.0. AGP provides built-in Kotlin; no duplicate Kotlin Android plugin. Requires Android SDK 37, minimum runtime API 26, Java 17 or compatible newer JDK (tested with Android Studio bundled JBR).

Open this directory in Android Studio. Set ANDROID_HOME or create an ignored local.properties containing sdk.dir pointing to your SDK. Set JAVA_HOME to your JDK.

Windows:

```powershell
.\gradlew.bat testDebugUnitTest
.\gradlew.bat lintDebug
.\gradlew.bat assembleDebug
.\gradlew.bat connectedDebugAndroidTest
```

macOS/Linux: use ./gradlew with the same tasks. Connected tests need a running emulator or connected device.

APK: app/build/outputs/apk/debug/app-debug.apk.
Reports: app/build/reports/tests, app/build/reports/lint-results-debug.html and app/build/reports/androidTests.

## Audit and Git

The supplied workspace was empty and not a Git repository. No starting SHA, existing branch, Gradle configuration, tests, README, CI or technical debt existed. A new repository was created on feat/divyadhun-foundation. No existing code was removed. No remote or merge is configured.

## Content, copyright and privacy

No audio, photographs, downloaded deity images, font files, analytics or network service are included. The launcher vector and decorative petal artwork were authored for this project. Navigation uses AndroidX Material icons. Typography uses Android system fonts and Devanagari fallback. Future demo recordings require documented rights; a traditional prayer does not make a commercial recording public domain.

No permissions are requested. Appearance stays on the device, with backup disabled. No secrets or signing keys are committed. Release signing and Play Store readiness belong to Phase 16.

## Deferred and roadmap

Phase 2: onboarding, Hindi/English preference, deity and listening-time selection, local domain models and demo catalog.
Phase 3: real Home/Explore/deity content.
Phases 4–6: media service, mini/full player, search, favorites and history.
Phases 7–11: lyrics/Aarti, mantra counts, daily routines, Simple Mode and festival configuration.
Phases 12–16: remote content, separately scoped offline/reminders/widget, sharing, explicitly approved Family Mandir and production hardening.

No fake search field, play control, favorites action or mini-player appears before its behavior exists.

## Limitations

English interface with Hindi branding; full Hindi localization is Phase 2. No physical-device, TalkBack or production-release certification. Scaffold copy intentionally describes future content. No CI service is configured. The current package ID com.divyadhun.app must be confirmed before publication.
