# DivyaDhun · दिव्यधुन

Your Daily Bhakti. Bhakti, bas ek tap mein.

DivyaDhun is an Android-first devotional listening experience. **Phase 19 adds the licensed production-audio inventory, deterministic staging, catalog generation, and remote-catalog integration seam on top of the Phase 18 streaming hardening work.** R2 upload authentication and final metadata review remain external release gates. Phase 18A still validates one R2 streaming fixture; Phase 18B adds the reusable rights-gated ingestion pipeline.

## Phase 19 licensed-audio integration

- The explicitly supplied licensed audio directory was scanned read-only: 130 MP3 files, 129 non-zero inputs, one zero-byte file, and two exact duplicate groups.
- Staging produced 127 canonical streaming copies/derivatives (1,333,381,709 bytes): 120 direct MP3 copies and seven audio-only AAC-LC M4A derivatives at 128 kbps for high-bitrate inputs. No original file was renamed, deleted, or overwritten.
- `PRODUCTION_CONTENT_DRAFT.json`, `PRODUCTION_CONTENT_PUBLISHABLE.json`, `catalog/latest.json`, inventories, duplicate reports, quality reports, and an upload plan are generated below the ignored `build/production-audio/` directory. The tracked `content/production-content.json` is a sanitized 54-track publishable metadata fixture and contains no local paths or audio bytes.
- Every supplied recording is marked `LICENSED` with `USER_CONFIRMED` evidence because the owner supplied that assertion. Documentary license contracts were not independently reviewed. Ambiguous deity/category records remain review-blocked; corrupt and exact-duplicate files are never upload-ready.
- The uploader defaults to dry-run, requires an explicitly configured R2 endpoint/CLI, verifies HTTPS/MIME/length/range behavior, and blocks conflicting object replacement unless explicitly overridden. No R2 credentials or production media are committed.
- The app has a single `ContentRepositoryProvider` boundary and an HTTPS `HttpRemoteContentDataSource`; pass `-PdivyadhunCatalogUrl=https://.../catalog/latest.json` to enable remote-first loading. Without that property it remains on the local offline fallback, so an unavailable catalog cannot break the app.

## Phase 17 RC status

- The connected Android suite is deterministic on the checked-in Medium_Phone API 36 AVD: three consecutive full-suite runs passed all 15 instrumentation tests.
- RC fixes remove lazy-list viewport assumptions from Compose flows, wait for asynchronous history state transitions, prevent duplicate Android 13 locale recreation, and use a proper plural resource for mantra progress.
- `lintDebug` reports zero errors and 11 intentionally accepted dependency-freshness warnings; no implementation warnings remain.
- `assembleRelease` produces an unsigned release APK. No production keystore, signing secret, commercial audio, or remote artwork was added.
- Manual emulator evidence covers onboarding, Hindi, settings, navigation, playback controls, dark mode, and large-font layout. Physical-device speaker, lock-screen, TalkBack, and production-content verification remain pending; see [docs/PHYSICAL_DEVICE_QA.md](docs/PHYSICAL_DEVICE_QA.md).
- Release handoff, content ingestion, backend requirements, and blockers are documented in `docs/PRODUCTION_CONTENT_CONTRACT.md`, `docs/PRODUCTION_BACKEND_HANDOFF.md`, and `docs/RELEASE_BLOCKERS.md`.

## Phase 16 completed features

- Manifest security is explicit: the app disallows cleartext traffic, uses non-exported local receivers/providers, declares only the permissions used by playback and opt-in reminders, and supplies Android 12+ data-extraction rules.
- Local preference data and share-card cache files are excluded from backup and device transfer; no account data is silently copied between devices.
- Opted-in daily reminders are restored after `BOOT_COMPLETED` through a short-lived background coroutine and the existing inexact scheduler.
- Catalog and profile tests cover malformed relationships, stable IDs, persistence boundaries, and bounded local input. No release signing keys or publishing configuration were added.

## Phase 15 completed features

- My Bhakti includes a small Family Mandir profile editor for a local mandir name and up to eight household member names.
- Names are trimmed, bounded, deduplicated, and stored through the existing DataStore preferences repository.
- The UI clearly describes this as device-local; accounts, invitations, cloud sync, and cross-device sharing are not implied.

## Phase 14 completed features

- Track Detail offers an explicit Share blessing action with localized English/Hindi copy.
- DivyaDhun generates an original branded PNG locally in the app cache using safe colors, typography, devotional symbols, and catalog metadata.
- Sharing uses a non-exported `FileProvider` and the Android Sharesheet; the app never auto-sends or auto-posts anything.
- No remote artwork, downloaded media, social SDK, or commercial content was introduced.

## Phase 13 completed features

- My Bhakti includes an opt-in Daily Bhakti reminder with English and Hindi copy and an accessible switch.
- Android 13 and newer request `POST_NOTIFICATIONS` only when the user enables the reminder; a denial leaves the setting off.
- The app schedules one inexact daily alarm at a stable hour derived from the saved listening-time preference and cancels it when disabled.
- A non-exported receiver posts a local reminder notification with a dedicated channel and opens DivyaDhun when tapped. No reminder is scheduled by default and no widget or spam behavior was added.

## Phase 12 completed features

- `RemoteContentDataSource` defines the suspendable integration point for a future HTTP/CDN catalog without adding a network SDK or inventing credentials.
- `RemoteCatalogDto` carries a catalog version, localized deity/track metadata, optional lyrics, CDN audio/artwork fields, and fetch metadata.
- `RemoteContentRepository` validates stable IDs, relationships, categories, languages, and HTTPS media URLs before accepting a snapshot.
- One immutable in-memory snapshot is reused across reads. A failed first load uses the existing local catalog; a later refresh keeps the last valid remote snapshot and exposes the error through `ContentRepositoryStatus`.
- The app remains wired to its local repository until a production data source is supplied, so offline behavior and the Phase 1–11 experience remain unchanged.

## Phase 9 completed features

- Home opens Daily Bhakti with a small routine selected from preferred Gods and the saved listening-time preference.
- The planner is deterministic and filters out metadata-only tracks, so a routine never promises playback for an unavailable recording.
- The routine screen supports explicit start/pause, previous, next, reset, current-track highlighting, and progress through the selected playable fixtures.
- Daily Bhakti is localized in English and Hindi, supports light/dark themes, large text, accessible controls, and stable test tags.

## Phase 8 completed features

- Mantra tracks open a reusable Mantra Mode with 11, 21, 51, and 108 repetition targets.
- Counting is explicit and honest: start/pause/resume, count-one, reset, progress, and completion are driven by a small immutable progression model rather than pretending to infer spiritual practice from audio playback.
- Playable demo fixtures retain synchronized previous/play-pause/next controls; metadata-only mantras provide the count experience without a fake audio action.
- Mantra Mode is localized in English and Hindi, supports light/dark themes, large text, accessible controls, and stable test tags.

## Phase 7 completed features

- Traditional, rights-safe lyrics excerpts are available through the local content repository for selected demo tracks, with stable `lyricsId` references and missing-content handling.
- Track Detail opens a reusable Lyrics screen; Aarti tracks use the focused Aarti Mode presentation with large Devanagari text, Romanized text, and an original English meaning.
- Aarti Mode includes a keep-screen-awake control and synchronized playback controls when the selected track has a bundled developer fixture. Metadata-only tracks never show a fake play action.
- Lyrics and Aarti Mode are localized in English and Hindi, support light/dark themes, large text, accessible labels, stable test tags, and normal back navigation.

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

The single `app` module remains small and inspectable. `domain.model` contains immutable `Deity`, `Track`, `Lyrics`, `MantraProgress`, `DevotionalCollection`, `DevotionalCategory`, `Language`, and `ListeningTime` types. `MantraProgression` is a platform-neutral deterministic counter. `AudioSource` is a platform-neutral sealed type with nullable bundled or future remote sources. `data.ContentRepository` is the UI-independent catalog contract implemented by `LocalContentRepository`; `UserPreferencesRepository` owns the single DataStore. Feature ViewModels keep loading, search, onboarding, details, preferences, lyrics, and mantra state out of Composables.

Playback is isolated in `playback`: `TrackMediaItemMapper` converts domain tracks to stable Media3 items, `Media3PlaybackController` is the app-facing controller, and `PlaybackService` owns the player/session lifecycle. Navigation Compose handles onboarding, the main shell, deity/category/track routes, and back-stack behavior. Media3 is not exposed by domain or repository code. `RemoteContentRepository` is an optional data-layer decorator and is not instantiated by the app until a production source is available.

Remote media URL policy is centralized in `RemoteContentConfiguration` and `RemoteContentUrlPolicy`. Catalogs carry metadata and object keys; deployment configuration supplies one HTTPS base URL, so the development R2 hostname can later move to a custom CDN domain without changing playback. The R2 URL used by the Phase 18A smoke test lives only in its instrumentation fixture and is not part of the local production fallback catalog. Remote catalog snapshots now expose a small cache seam, deterministic remote-first merge helpers, and precomputed search indexes so hundreds or thousands of metadata records do not rebuild their haystacks on every query. Track availability is explicit (metadata-only, bundled demo, remote production, or unavailable) and presentation shows the source state honestly. Playback hardening adds a connectivity signal, bounded remote seek, explicit retry actions, buffering indicators, and safe failures when a queue item cannot be mapped. Media3 remains owned by `PlaybackService`; mixed queues continue to skip unavailable metadata-only items while preserving order.

## Local catalog and demo audio

The fixture contains 8 deities, 32 stable track IDs, and six categories: Aarti, Bhajan, Mantra, Chalisa, Stotra, and Meditation. Search normalizes case, whitespace, and punctuation and checks titles, deity names, category names, keywords, aliases, and curated Devanagari aliases such as `महादेव`, `हनुमान`, `राम`, and `कृष्ण`. Lyrics are a separate local map keyed by stable IDs, so presentation can evolve without coupling preferences to localized names.

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

The Phase 17 RC validation passed 37 JVM unit tests and three consecutive complete connected suites (15 instrumentation tests per run). Phase 18A.1 validation passed 39 JVM tests and 17 connected tests, including the isolated remote streaming smoke test. Phase 18C adds a 1,200-track catalog search scale fixture; Phase 18D exercises the same playback architecture with connectivity and retry state. Phase 19 adds the ingestion and catalog tests; the current JVM suite passes 49 tests and the connected suite passes 17 tests, including the live R2 smoke class. `lintDebug` completed with zero errors and 12 dependency-freshness warnings, and debug compilation completed successfully. Emulator flows verify onboarding, local search, playable demo playback, mini/full-player controls, favorites, history, Aarti Mode, Mantra Mode counting, Daily Bhakti routine navigation, Simple Mode, and festival discovery; audible loudspeaker output, system lock-screen presentation, and TalkBack certification still depend on a physical-device pass. Share-sheet delivery depends on the receiving apps installed on the host device and remains a manual-device check.

## Phase 18B ingestion pipeline

Run `python -m tools.audio_ingest scan --source "<authorized audio source directory>" --output build/production-audio --base-url https://pub-867b1331c5b9468db9b3fce509773119.r2.dev --rights-status LICENSED --rights-evidence USER_CONFIRMED --stage` only for the explicitly authorized source directory. The command writes inventory, quality, duplicate, draft, publishable-catalog, ingestion-summary, and R2 upload-plan reports. `python -m tools.audio_ingest validate --manifest <path>` checks IDs, categories, deity relationships, object keys, URLs, durations, and rights. `python -m tools.audio_ingest upload ...` is a dry run by default; `--upload` is rights-gated, requires an authenticated Wrangler/AWS CLI configuration, and verifies existing objects before upload. See [docs/PRODUCTION_AUDIO_PIPELINE.md](docs/PRODUCTION_AUDIO_PIPELINE.md).

## Deferred work and known limitations

Downloads, widgets, backend/API implementation, authentication, cloud sync, subscriptions, analytics, and AI remain deferred. Remote catalog integration is ready behind the external dependency **R2 upload authentication and final production metadata review**; the JSON decoder, repository, cache, validation, and fallback seams are ready for it. Playback currently covers only the five bundled fixtures plus the isolated Phase 18A R2 smoke object; staged production tracks are not presented as playable until their objects are uploaded and catalog availability is promoted. The Phase 19 tool does not approve rights, upload unknown/review-blocked recordings, or modify source masters. Library data and Family Mandir are local to the device and have no account or cross-device sync. Reminder delivery depends on user opt-in, Android notification permission, and system scheduling policy. Sharing is local-only through the platform Sharesheet. Physical-device loudspeaker, lock-screen, TalkBack, and release signing certification are pending the checklist in `docs/PHYSICAL_DEVICE_QA.md`. The lyrics excerpts are short traditional/public-domain-style developer fixtures with no proprietary translation or commercial recording.

Phase 16 was developed on `feat/divyadhun-phase16-hardening` and merged into `main` after validation.
