# DivyaDhun Phase 22 release checklist

## Completed in this branch

- [x] Branch created from merged Phase 21 main.
- [x] Production catalog URL is HTTPS-only and remains read-only.
- [x] Version set to `1.0.0` / `versionCode 1`.
- [x] Unsigned release APK and AAB built with the production catalog URL.
- [x] Package, min SDK, target SDK, manifest permissions, media service, and privacy-related code audited.
- [x] Listing, privacy policy, Data Safety, signing, and owner-action drafts written.
- [x] No audio uploaded, no catalog object changed, no Google Play release created.

## Required before Play submission

- [x] Support contact supplied and privacy policy published at `https://amansharma-it5.github.io/divyadhun-privacy/`.
- [x] Android source exposes the HTTPS privacy-policy link from Library → Preferences.
- [ ] Owner verifies the privacy link on the signed release and confirms the published policy remains accurate.
- [ ] Owner verifies an encrypted backup of `divyadhun-upload-key-IN.p12` (alias `divyadhun-upload`), then populates the ignored `keystore.properties` and verifies the signed AAB. Never use the similarly named key without `-IN`.
- [ ] Owner enrolls in Play App Signing and records the upload-key recovery process.
- [ ] Owner completes Data Safety, content rating, target audience, and any account-specific declarations.
- [ ] Owner supplies a Play-ready 512x512 PNG icon/adaptive icon, 1024x500 feature graphic, and genuine screenshots from the signed release; see `docs/PLAY_STORE_ASSET_PREPARATION.md`.
- [x] Owner confirmed distribution rights for the five bundled offline demo WAV fixtures; documentary agreements were not independently reviewed.
- [ ] If applicable, complete the 12-tester/14-day closed test for a new personal developer account.
- [ ] If applicable, complete Play Console real-device verification.
- [ ] Install the signed release on `RZCX623LLJK` and complete audible playback, background, lock-screen, notification, MediaSession, seeking, and network-recovery QA.
- [ ] Run final signed-artifact regression, remote smoke, catalog verification, secrets audit, and media audit.
- [ ] Owner separately confirms the final release decision; do not publish or merge while any mandatory gate is open.

## Explicitly not part of this run

- Google Play upload, rollout, or production publication.
- Additional R2 audio uploads or catalog expansion.
- Billing-plan changes or service purchases.
