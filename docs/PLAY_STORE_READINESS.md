# DivyaDhun Phase 22 Play Store readiness

As of 2026-09-17, this branch is a release-readiness preparation branch. It does not publish to Google Play, change the live catalog, or upload audio.

## Release candidate

- Application ID: `com.divyadhun.app`
- Version: `1.0.0` / `versionCode 1`
- Target SDK: 37
- Minimum SDK: 26
- Production catalog: `https://pub-867b1331c5b9468db9b3fce509773119.r2.dev/catalog/latest.json`
- Live catalog remains 77 tracks; Phase 22 does not add tracks.
- Rights status: `LICENSED — USER_CONFIRMED`. Public streaming through DivyaDhun and public Cloudflare R2 hosting were confirmed by the owner; documentary license agreements were not independently reviewed.

## Engineering audit

| Gate | Status | Evidence / next action |
| --- | --- | --- |
| Clean starting point from merged Phase 21 | PASS | Branch starts at merged main `26aeacff494188e2e40f2e5767157caf4fc9fec5`. |
| Production catalog wiring | PASS | HTTPS-only `BuildConfig.REMOTE_CATALOG_URL` with deterministic local fallback. |
| Manifest and Android media service | PASS | INTERNET, network-state, foreground media playback, notifications, boot receiver; Media3 service is declared with `mediaPlayback`. |
| Target API | PASS | Target SDK 37 is above the current new-app API 36 requirement described by [Target API requirements](https://support.google.com/googleplay/android-developer/answer/11926878?hl=en). Recheck before submission because Play policy is time-sensitive. |
| Debug validation | PASS from Phase 21 | Python 19, Android unit 49, connected 18/18 twice, lint, debug build, remote smoke, and production checks were previously recorded after the Phase 21 merge. |
| Release APK | PASS, unsigned | `app/build/outputs/apk/release/app-release-unsigned.apk`; package metadata is correct. |
| Release AAB | PASS, unsigned | `app/build/outputs/bundle/release/app-release.aab`. Play accepts an app bundle only after it is correctly signed. |
| Production signing | OWNER_ACTION_REQUIRED | Optional local signing wiring is present, but no production key/configuration exists in this environment. See `docs/RELEASE_SIGNING_SETUP.md`. |
| R8/resource shrinking | REVIEW | No custom R8 rules or release minification configuration is present. This is not a publication blocker by itself; enable and test only if desired for the signed candidate. |
| Native/ABI audit | REVIEW | AAB includes dependency `.so` files for arm64-v8a, armeabi-v7a, x86, and x86_64. Recheck Play’s current native/16 KB page-size guidance against the signed bundle. |
| Bundled fallback media | OWNER_ACTION_REQUIRED | The APK also contains five existing demo WAV fixtures (`1,852,420` bytes total) for the offline/local fallback. Confirm their distribution rights or remove/replace them before Play publication; Phase 22 did not modify them. |
| Privacy policy | OWNER_ACTION_REQUIRED | Draft exists, but owner contact/URL is not supplied and the current app has no dedicated in-app policy destination. See `docs/PRIVACY_POLICY_DRAFT.md`. |
| Data Safety | OWNER_ACTION_REQUIRED | Draft proposes no collection/sharing based on source audit. Owner must submit the final Play Console declaration. |
| Content rating | OWNER_ACTION_REQUIRED | Complete the Play questionnaire; unrated apps cannot be published. |
| Target audience | OWNER_ACTION_REQUIRED | Confirm general-audience selection and assess Families requirements if the audience includes children. |
| Store listing assets | OWNER_ACTION_REQUIRED | Listing copy drafted; Play icon/adaptive icon, feature graphic, screenshots, support contact, and privacy URL remain. |
| Account deletion | NOT_APPLICABLE | The app has no account creation or sign-in. Reassess if that changes. |
| New personal-account testing | OWNER_ACTION_REQUIRED | If the developer account is a new personal account created after 2023-11-13, Google requires a closed test with at least 12 opted-in testers for 14 continuous days before production access: [Closed testing requirements](https://support.google.com/googleplay/android-developer/answer/14151465?hl=en-GB). |
| Developer device verification | OWNER_ACTION_REQUIRED_IF_APPLICABLE | New personal accounts may need real-device verification using the Play Console mobile app: [Device verification](https://support.google.com/googleplay/android-developer/answer/14316361?hl=en). |
| Play App Signing | OWNER_ACTION_REQUIRED | Enroll during first upload and retain the upload key securely: [Play App Signing](https://support.google.com/googleplay/android-developer/answer/9842756?hl=en). |
| Google Play publication | NOT RUN | Explicitly deferred. No Play Console upload or release was performed. |

## Current blockers

1. Production upload key/signing configuration is absent.
2. Owner must replace the privacy-policy contact/URL and make the policy available in-app or through a stable in-app link.
3. Owner must complete Data Safety, content rating, target audience, store assets, and account-specific testing/device-verification steps.
4. A signed release candidate still needs installation and physical-device acceptance on `RZCX623LLJK` before any Play submission.

Official Play preparation guidance also requires accurate metadata, a privacy policy, and Data Safety information before submission: [Prepare your app for review](https://support.google.com/googleplay/android-developer/answer/10788890?hl=en).
