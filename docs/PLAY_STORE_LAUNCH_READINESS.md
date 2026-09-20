# DivyaDhun Play Store launch readiness

As of 2026-09-20. This is an engineering preparation record only. No Play Console declaration was submitted, no audio/catalog was changed, and no Play release was published.

## Status matrix

| Area | Status | Evidence / owner boundary |
| --- | --- | --- |
| Application identity | PASS | `com.divyadhun.app`, version `1.0.0`, versionCode `1`, min SDK 26, target SDK 37. |
| Production catalog | PASS | HTTPS remote catalog is configured in the latest signed build; live catalog remains 77 tracks. |
| Privacy/support | PASS | Physical signed-build tap opened Chrome at `https://amansharma-it5.github.io/divyadhun-privacy/`; support email is `divyadhun.support@gmail.com`. Owner must review policy accuracy before submission. |
| Signed artifacts | PASS | Latest signed APK/AAB use the expected upload certificate; package/version checks passed. The AAB must be rebuilt with the remote URL before final upload if its timestamp predates this record. |
| Physical QA | PENDING | Layout, privacy, playback state, and background MediaSession passed on `RZCX623LLJK`; human audible confirmation is still pending. |
| Data Safety | OWNER ACTION | Engineering draft is based on source/dependency audit; owner must complete and submit the exact Play form. |
| App access | OWNER ACTION | No account, login, paid wall, or restricted content was found. Owner must select the applicable Play Console answer and provide no access credentials unless the app changes. |
| Content rating | OWNER ACTION | Complete the IARC questionnaire accurately; do not pre-assign a rating. [Official guidance](https://support.google.com/googleplay/android-developer/answer/9898843?hl=en). |
| Target audience | OWNER ACTION | Owner must choose the actual intended age groups. Do not infer a Families/children declaration from the devotional subject matter. [Official guidance](https://support.google.com/googleplay/android-developer/answer/9867159?hl=en). |
| Store listing assets | OWNER ACTION | Listing copy is drafted. Play-ready icon, 1024x500 feature graphic, and owner-approved genuine screenshots remain. See `docs/PLAY_STORE_ASSET_PREPARATION.md`. |
| Play App Signing | OWNER ACTION | Enroll during first bundle upload, retain the upload-key recovery path, and keep the second independent encrypted backup pending until verified. [Official guidance](https://support.google.com/googleplay/android-developer/answer/9842756?hl=en). |
| New personal-account testing | BLOCKED/IF APPLICABLE | If the developer account was created after 2023-11-13, complete the required closed test with at least 12 opted-in testers for 14 continuous days before production access. [Official guidance](https://support.google.com/googleplay/android-developer/answer/14151465?hl=en-GB). Account age/type is owner-only. |
| Device verification | OWNER ACTION/IF APPLICABLE | Complete Play Console device verification if the account flow requests it. [Official guidance](https://support.google.com/googleplay/android-developer/answer/14316361?hl=en). |
| Foreground service declaration | OWNER ACTION | The app declares media playback foreground service. Play Console may require the media-playback declaration, use-case explanation, interruption impact, and demonstration video for the target Android version. [Official guidance](https://support.google.com/googleplay/android-developer/answer/13392821?hl=en). |
| Publication | BLOCKED | Explicitly not authorized in this run. Do not upload, roll out, or merge while owner gates remain open. |

## Data Safety and permissions preparation

The app has no account/profile, analytics, ads, crash SDK, social posting, subscription, or cloud-sync feature in the audited source/dependency set. Device-local DataStore holds onboarding/language/preferences, Family Mandir values, favorites, recent/last-played IDs, listening state, and reminder choice. Catalog/audio use HTTPS; ordinary hosting-provider request metadata is outside the app’s direct control and must be reviewed in the Console form. The app does not request location, contacts, camera, microphone, SMS, Bluetooth, or broad storage permissions.

Manifest permission purposes:

- `INTERNET` and `ACCESS_NETWORK_STATE`: HTTPS catalog/audio and connectivity handling.
- `FOREGROUND_SERVICE` and `FOREGROUND_SERVICE_MEDIA_PLAYBACK`: continued user-started audio playback while backgrounded.
- `POST_NOTIFICATIONS`: user-facing reminder/media notification behavior on Android 13+.
- `RECEIVE_BOOT_COMPLETED`: restore an opted-in reminder schedule; receiver is not exported.

This is not a submitted declaration. Recheck the final bundle and Google’s current [permissions policy](https://support.google.com/googleplay/android-developer/answer/9214102?hl=en) before submission.

## Listing materials

Draft app name: **DivyaDhun**. Draft short description: **Devotional audio, daily bhakti, and peaceful listening.** The full description is in `docs/PLAY_STORE_LISTING_DRAFT.md`. It contains no unsupported claims, pricing, awards, or copied Spotify branding. The five bundled WAV fixtures are `OWNER_CONFIRMED` for distribution; documentary agreements were not independently reviewed. Production audio is `LICENSED — USER_CONFIRMED`; agreements were not independently reviewed.

## Owner actions in priority order

1. Personally listen to the installed signed release and confirm audible playback; record speaker/headphone and basic transport results.
2. Verify a second independent encrypted signing-key backup; do not use the old key or unencrypted archive.
3. Approve final icon, feature graphic, genuine screenshots, listing copy, and rights records.
4. Complete Play App Signing enrollment, Data Safety, app access, content rating, target audience, permissions/FGS, and any account-specific testing/device-verification flows.
5. Upload the owner-approved AAB to an internal/closed track and run Play-distributed QA before deciding on publication.
