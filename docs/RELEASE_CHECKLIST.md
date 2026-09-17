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

- [ ] Owner supplies a monitored support contact and publishes the reviewed privacy policy at a stable HTTPS URL.
- [ ] Owner adds an in-app privacy-policy link or policy text and tests it on the signed release.
- [ ] Owner creates/loads a dedicated Play upload key; configure release signing locally; verify the signed AAB.
- [ ] Owner enrolls in Play App Signing and records the upload-key recovery process.
- [ ] Owner completes Data Safety, content rating, target audience, and any account-specific declarations.
- [ ] Owner supplies Play-ready icon/adaptive icon, feature graphic, and screenshots.
- [ ] Owner confirms rights for the five bundled offline demo WAV fixtures, or removes/replaces them before publication.
- [ ] If applicable, complete the 12-tester/14-day closed test for a new personal developer account.
- [ ] If applicable, complete Play Console real-device verification.
- [ ] Install the signed release on `RZCX623LLJK` and complete audible playback, background, lock-screen, notification, MediaSession, seeking, and network-recovery QA.
- [ ] Run final signed-artifact regression, remote smoke, catalog verification, secrets audit, and media audit.
- [ ] Owner separately confirms the final release decision; do not publish or merge while any mandatory gate is open.

## Explicitly not part of this run

- Google Play upload, rollout, or production publication.
- Additional R2 audio uploads or catalog expansion.
- Billing-plan changes or service purchases.
