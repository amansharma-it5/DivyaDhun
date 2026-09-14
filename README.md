# DivyaDhun · दिव्यधुन

Your Daily Bhakti. Bhakti, bas ek tap mein.

DivyaDhun is an Android-first devotional listening experience. **Phase 6 adds a local My Bhakti library on top of the Phase 5 player: favorites, ordered recent history, last played, and clear-history controls.** Production devotional recordings and cloud sync remain future work.

## Phase 6 completed features

- Favorites persist in the existing DataStore and can be added or removed from track rows, Track Detail, the full player, and My Bhakti.
- Playback start and automatic queue changes record an ordered recent history (up to 20 stable track IDs) and expose the last played item.
- My Bhakti now renders Favorites and Recently played collections with the same reusable track rows and playback controls, plus a clear-history action.
- Library state is isolated in `LibraryViewModel`; catalog metadata still comes from `ContentRepository`, and playback remains owned by `PlaybackService`.

## Phase 5 completed features

- The mini player opens a full-screen player without creating a second playback engine. The player observes the same controller state as the notification and background service.
- Full-player artwork, localized title/source, elapsed and total duration, seek bar, previous, play/pause, next, repeat (off/all/one), queue rows, and playback error state are available for the five bundled demo fixtures.
- Selecting a queue row starts that track through the existing service-owned Media3 controller. Metadata-only catalog rows remain honest discovery surfaces and never expose a non-functional play control.
- Player controls include localized content descriptions, stable test tags, 48dp-or-larger touch targets, light/dark theme support, and Hindi strings.

## Phase 4 completed features

- The Phase 2 onboarding and Phase 3 discovery shell remain intact, including English/Hindi localization, personalized Home, Explore, category/deity/track detail, and deterministic local search.
- Five developer-generated WAV fixtures are playable from the catalog. The remaining catalog records stay honest metadata-only entries.
- `Media3PlaybackController` exposes immutable `PlaybackState` through `StateFlow` and keeps UI code independent of ExoPlayer APIs.
- `PlaybackService` owns one ExoPlayer and Media3 `MediaSession`, handles audio focus/noisy-audio events, and supports background playback through the system media controls.
- Track rows and Track Detail expose play/pause only for records with a bundled source. Queue playback filters out unavailable records while preserving catalog order.
- The mini player shows current artwork, title, progress, play/pause, next, stop, and an honest track-info route. There is no fake control for metadata-only tracks.

## Architecture

The single `app` module remains small and inspectable. `domain.model` contains immutable `Deity`, `Track`, `Lyrics`, `DevotionalCollection`, `DevotionalCategory`, `Language`, and `ListeningTime` types. `AudioSource` is a platform-neutral sealed type with nullable bundled or future remote sources. `data.ContentRepository` is the UI-independent catalog contract implemented by `LocalContentRepository`; `UserPreferencesRepository` owns the single DataStore. Feature ViewModels keep loading, search, onboarding, details, and preferences out of Composables.

Playback is isolated in `playback`: `TrackMediaItemMapper` converts domain tracks to stable Media3 items, `Media3PlaybackController` is the app-facing controller, and `PlaybackService` owns the player/session lifecycle. Navigation Compose handles onboarding, the main shell, deity/category/track routes, and back-stack behavior. Media3 is not exposed by domain or repository code.

## Local catalog and demo audio

The fixture contains 8 deities, 32 stable track IDs, and six categories: Aarti, Bhajan, Mantra, Chalisa, Stotra, and Meditation. Search normalizes case, whitespace, and punctuation and checks titles, deity names, category names, keywords, aliases, and curated Devanagari aliases such as `महादेव`, `हनुमान`, `राम`, and `कृष्ण`.

Five records have bundled developer-created demo tones under `app/src/main/res/raw/`:

- `hanuman_mantra` → `demo_hanuman_dhun`
- `om_namah_shivaya` → `demo_shiv_dhun`
- `hare_krishna_mantra` → `demo_krishna_dhun`
- `shri_ram_jai_ram` → `demo_ram_dhun`
- `ganesh_mantra` → `demo_ganesh_dhun`

Each WAV is a short sine/chime fixture generated for local development. No remote URL, MP3, commercial recording, embedded stream, scraped service, or downloaded artwork is used. Traditional titles are metadata only and use neutral sources such as Traditional or DivyaDhun Demo; no singer or label is attributed.

## Build and tests

Pinned to the available environment: AGP 9.4.0, Gradle 9.7.1, Kotlin/Compose compiler 2.2.10, Compose BOM 2026.02.01, Media3 1.6.1, Material 3, Navigation 2.9.0, and minimum runtime API 26. Set `JAVA_HOME` to a compatible JDK and `ANDROID_HOME` to the Android SDK.

Windows:

```powershell
.\gradlew.bat --no-daemon testDebugUnitTest --console=plain
.\gradlew.bat --no-daemon lintDebug --console=plain
.\gradlew.bat --no-daemon assembleDebug --console=plain
.\gradlew.bat --no-daemon connectedDebugAndroidTest --rerun-tasks --console=plain
```

The debug APK is `app/build/outputs/apk/debug/app-debug.apk`. Unit reports are under `app/build/test-results`; instrumentation reports are under `app/build/outputs/androidTest-results`; lint writes `app/build/reports/lint-results-debug.html`.

The latest validation run passed 19 JVM unit tests and 10 connected emulator tests (Phase 2 through Phase 6 flows). `lintDebug` completed with zero errors and 13 non-critical warnings, and `assembleDebug` completed successfully. The emulator flow verified onboarding, local search, a playable demo result, the mini player, full-player controls, favorites, and history; audible loudspeaker output and system lock-screen presentation still depend on the host device.

## Deferred work and known limitations

Lyrics UI, downloads, widgets, routine sequencing, Mantra/Aarti modes, backend/API, authentication, cloud sync, sharing, subscriptions, analytics, AI, and production devotional recordings remain deferred. Playback currently covers only the five bundled fixtures; metadata-only rows remain discovery surfaces. Library data is local to the device and has no account or cross-device sync. Notification appearance and lock-screen controls depend on the host Android system and notification policy. Physical-device loudspeaker, TalkBack, and release certification are outside this phase.

Phase 6 is developed on `feat/divyadhun-phase6-library`; it is not merged to `main` until its validation and review are complete.
