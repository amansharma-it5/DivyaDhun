# DivyaDhun physical-device QA checklist

Status for Phase 17: **PENDING**. No authorized physical Android device was connected during the RC pass; the Medium_Phone API 36 emulator was used for reproducible instrumentation and UI checks.

Run this checklist on an authorized phone without clearing unrelated applications or personal data.

## Install and first launch

- [ ] Record device model, Android version, ABI, locale, font scale, and battery state.
- [ ] Install the unsigned debug APK supplied by the engineering build process.
- [ ] Launch DivyaDhun and complete onboarding in English.
- [ ] Repeat on a clean app-data state in Hindi.
- [ ] Confirm that closing and reopening the app bypasses onboarding.

## Preferences and accessibility

- [ ] Select multiple Gods, change listening time, and verify both survive an app restart.
- [ ] Change language from My Bhakti and verify all visible labels update without a crash.
- [ ] Check System, Light, and Dark appearance modes.
- [ ] Test 1.3x and 1.5x font scales on Home, Explore, Search, My Bhakti, and the player.
- [ ] Use TalkBack to reach navigation, selection controls, playback controls, and dialogs.

## Playback and notifications

- [ ] Start each bundled demo fixture and confirm audible output through the phone speaker and headphones.
- [ ] Pause, resume, seek, and move previous/next while the app is foregrounded.
- [ ] Background the app for at least 60 seconds, lock the device, and verify playback continues once.
- [ ] Verify lock-screen and notification transport controls, including a service restart/reconnect.
- [ ] Enable a reminder, grant notification permission, observe one notification, open the app from it, then disable the reminder.

## Sharing and local Family Mandir

- [ ] Generate a blessing card in English and Hindi; inspect the PNG in a receiving app.
- [ ] Cancel the Sharesheet and repeat the share; confirm no crash and no `file://` URI exposure.
- [ ] Configure a Family Mandir name and members, restart the app, and verify local persistence.
- [ ] Confirm the Family Mandir surface continues to identify data as local device-only data.

Record results, screenshots, and logcat excerpts with the release candidate build identifier before marking this checklist complete.
