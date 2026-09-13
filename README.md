# DivyaDhun · दिव्यधुन

Your Daily Bhakti. Bhakti, bas ek tap mein.

DivyaDhun is an Android-first devotional discovery experience. **Phase 3 adds a polished personalized Home, data-backed Explore discovery, reusable category/deity/track detail surfaces, and richer English/Hindi navigation. Audio playback is intentionally not implemented yet.**

## Phase 3 completed features

- Four-step onboarding remains available for new users: welcome, language, favorite deities, and listening time.
- Home uses a pure `HomeContentAssembler` for deterministic Your Gods, Recommended, Aaj Ki Bhakti, Popular Aartis, and Mantras sections.
- Explore presents all six devotional categories with localized descriptions and counts, plus all eight deities.
- One reusable Category Detail destination supports Aarti, Bhajan, Mantra, Chalisa, Stotra, and Meditation.
- One reusable Deity Detail destination shows invocation, original artwork, available category chips, featured content, and only non-empty groups.
- Track Detail shows honest metadata and links back to its deity or category without fake playback controls.
- Search results open Track Detail and matching deity suggestions open Deity Detail.
- English and Hindi resources cover Home, Explore, detail surfaces, metadata, empty states, and navigation.
- My Bhakti preferences remain persisted for language, deities, listening time, and System/Light/Dark appearance.
- Original Compose-drawn artwork uses restrained deity-specific accents with accessible touch targets and selection semantics.

## Architecture

The single `app` module remains small and inspectable. `domain.model` contains immutable `Deity`, `Track`, `Lyrics`, `DevotionalCollection`, `DevotionalCategory`, `Language`, and `ListeningTime` types. `data.ContentRepository` is the UI-independent catalog contract, implemented by `LocalContentRepository`. `UserPreferencesRepository` owns the single DataStore and exposes immutable preference state through `Flow`. `HomeContentAssembler` keeps deterministic personalization and section assembly out of Composables. Feature ViewModels keep loading, search, onboarding, detail assembly, and preference changes out of Composables. Navigation Compose handles onboarding, the main shell, deity/category/track routes, and back-stack behavior.

Locale changes use Android application locale APIs on API 33+ and a compatibility configuration update below that level. The selected onboarding step is persisted so a locale-triggered Activity recreation resumes safely.

## Local catalog

The fixture contains 8 deities, 32 metadata-only tracks, and six stable categories: Aarti, Bhajan, Mantra, Chalisa, Stotra, and Meditation. Deity IDs (`hanuman`, `shiv`, `krishna`, `ram`, `ganesh`, `durga`, `lakshmi`, `sai`) and track IDs are stable and independent of localized display names. Search normalizes case, whitespace, and punctuation and checks titles, deity names, category names, keywords, aliases, and curated Devanagari aliases such as `महादेव`, `हनुमान`, `राम`, and `कृष्ण`.

The repository is ready for a future JSON, database, or network implementation without exposing that choice to the UI. `audioSource` is nullable and all current records are metadata/demo entries; no remote URLs or recordings are bundled.

## Build and tests

Pinned to the available environment: AGP 9.4.0, Gradle 9.7.1, Kotlin/Compose compiler 2.2.10, Compose BOM 2026.02.01, Material 3, Navigation 2.9.0, minimum runtime API 26. Set `JAVA_HOME` to a compatible JDK and `ANDROID_HOME` to the Android SDK.

Windows:

```powershell
.\gradlew.bat testDebugUnitTest --console=plain
.\gradlew.bat lintDebug --console=plain
.\gradlew.bat assembleDebug --console=plain
.\gradlew.bat connectedDebugAndroidTest --rerun-tasks --console=plain
```

The debug APK is `app/build/outputs/apk/debug/app-debug.apk`. Unit test reports are under `app/build/test-results`; instrumentation reports are under `app/build/outputs/androidTest-results`; lint writes `app/build/reports/lint-results-debug.html`.

## Content and asset safety

Phase 3 includes no MP3s, commercial recordings, embedded streaming URLs, scraped services, commercial album artwork, or downloaded photographs. Deity visuals are original Compose-drawn symbols with local accents. Traditional titles are metadata only and use neutral sources such as Traditional or DivyaDhun Demo; no singer or label is falsely attributed. Future recordings and artwork require documented rights.

## Deferred work and known limitations

Media3/ExoPlayer, playback services, MediaSession, mini/full player, downloads, notifications, widgets, backend/API, authentication, cloud sync, favorites/history, lyrics UI, daily sequencing, Mantra/Aarti modes, sharing, subscriptions, analytics, AI, and production devotional recordings remain deferred. Search is intentionally deterministic local matching, not fuzzy or transliteration search. Physical-device, TalkBack, and production-release certification are outside this phase.

Phase 3 is developed on `feat/divyadhun-phase3-discovery` and is not merged to `main`.
