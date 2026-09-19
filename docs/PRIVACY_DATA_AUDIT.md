# DivyaDhun privacy and data audit

Audit basis: source inspection of the Android app at the Phase 22 branch, including the manifest, DataStore repository, HTTP data source, playback service, reminder receivers, sharing flow, and release artifact. This is an engineering inventory, not legal advice.

## Collection and sharing summary

| Area | Observed behavior | Off-device transmission by DivyaDhun | Release status |
| --- | --- | --- | --- |
| Account identity | No account, sign-in, email, phone, contacts, or user ID flow | None | PASS |
| Family Mandir profile | Optional mandir name and member names are trimmed/bounded and stored in Android DataStore | None | PASS; disclose local storage |
| Preferences | Language, deity preferences, theme, simple mode, listening time, reminder opt-in | None | PASS |
| Library | Favorites, recent IDs, and last-played ID are stored locally | None | PASS |
| Catalog and audio | App requests the configured HTTPS catalog and requested audio from the production R2 hostname | The requested URL and normal network metadata reach the hosting service; the app does not attach an account ID, analytics payload, cookie, or custom user profile | PASS; disclose service provider/hosting |
| Playback | Media3 ExoPlayer and MediaSession run locally; foreground media playback is declared | None beyond requested media/network traffic | PASS |
| Reminders | Optional local inexact alarm and local notification; boot receiver restores an opted-in reminder | None | PASS |
| Sharing | A local blessing-card file is exposed through a non-exported FileProvider and Android Sharesheet only after the user chooses a target | The selected target app may receive the shared card; DivyaDhun does not auto-send or auto-post it | PASS; disclose user-directed sharing |
| Diagnostics/analytics/ads | No analytics SDK, crash reporter, advertising ID, ads, telemetry, or cloud sync found | None | PASS |
| Sensitive permissions | `INTERNET`, `ACCESS_NETWORK_STATE`, foreground media playback, `POST_NOTIFICATIONS`, and `RECEIVE_BOOT_COMPLETED` only | No location, camera, microphone, contacts, phone, SMS, or broad storage permission | PASS |

## Evidence locations

- `app/src/main/AndroidManifest.xml`: declared permissions and non-exported sharing provider.
- `app/src/main/java/com/divyadhun/app/data/UserPreferencesRepository.kt`: local DataStore keys and writes.
- `app/src/main/java/com/divyadhun/app/data/HttpRemoteContentDataSource.kt`: bounded HTTPS GET, no redirect, no cookies or application identifiers.
- `app/src/main/java/com/divyadhun/app/playback/PlaybackService.kt`: local Media3 player/session lifecycle.
- `app/src/main/java/com/divyadhun/app/engagement/ReminderScheduler.kt`: opt-in local reminder behavior.
- `app/src/main/java/com/divyadhun/app/sharing/ShareBlessingCard.kt`: user-directed local Sharesheet flow.

## Deletion and retention

There is no DivyaDhun account and no server-side user profile to delete. Users can remove local app data through Android settings or uninstall the app; the Library screen also exposes history clearing. A future release that introduces an account or server sync must add a real account deletion path and update this audit before publication.

The app does not intentionally retain a user identifier or usage history on the production host. Standard infrastructure request logs may exist at the hosting provider under that provider’s policies; the app does not control or enrich those logs.

## Rights note

The production audio rights status is `LICENSED — USER_CONFIRMED`. The owner confirmed public streaming through DivyaDhun and public Cloudflare R2 hosting. Documentary license agreements were not independently reviewed in this audit.

## Current release follow-up

- The verified public policy is published at `https://amansharma-it5.github.io/divyadhun-privacy/` with contact `divyadhun.support@gmail.com`, effective September 19, 2026.
- The Android app now exposes that HTTPS policy from Library → Preferences. The signed release still needs device verification.
- Confirm the Play Console Data Safety answers after reviewing the final dependency and Play Console declarations.
