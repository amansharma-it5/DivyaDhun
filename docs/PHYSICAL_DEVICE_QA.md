# DivyaDhun physical-device QA checklist

Status for Phase 22: **BLOCKED**. `RZCX623LLJK` is connected, but its installed `com.divyadhun.app` 0.1.0 package is debug-signed and is not compatible with the Phase 22 release APK. The release APK was not installed; no app uninstall, data clear, or overwrite was attempted.

Run this checklist on an authorized phone without clearing unrelated applications or personal data.

## Data-preservation finding for `RZCX623LLJK`

- The installed debug build contains a device-local Android DataStore file at `files/datastore/user_preferences.preferences_pb`. Source inspection identifies the stored state as onboarding progress, language, preferred deities, listening time, theme, simple mode, Daily Bhakti reminder setting, optional Family Mandir name/member names, favorites, recent track IDs, and the last-played track ID.
- The app has no user-facing export/import or restore feature. `FileProvider` is used only for user-directed blessing-card sharing from the disposable cache; it is not a backup mechanism.
- The manifest sets `android:allowBackup="false"` and `android:fullBackupContent="false"`. Its data-extraction rules exclude `file`, `database`, and `sharedpref` domains from both cloud backup and device transfer. A debug-only `run-as` listing is not a supported portable backup or restore path, so no private data archive was created.
- A standard uninstall would remove the app sandbox, including the DataStore and disposable cache. The remote catalog and remote audio are not local user data and would remain available after reinstall, but the local preferences, favorites, history, Family Mandir values, reminder choice, and last-played state would not be reliably recoverable.

**Current decision:** do not uninstall, clear data, change certificates, or install the release APK on this phone without explicit owner approval to accept loss of the local DivyaDhun state. No phone data was changed during this assessment.

## Current device gate

- Installed device certificate: Android Debug, SHA-256 `915a1e49e6c952a4c63f882a75ace00ff1016e3dcc4bacc442eaf1722d145b0b`.
- Phase 22 release certificate: `CN=Aman Sharma, L=Delhi, ST=Delhi, C=IN`, SHA-256 `a91a010619faa841768e4f33db69bc3e5956034f61dba6dddcbae722df00630c`.
- Because the signatures differ, do not install the release APK over this existing package. Use an authorized clean test device or a device already carrying the matching release signature; do not uninstall or clear this phone’s app as a workaround.

The owner has no spare Android phone. The signed release has already passed the documented disposable-emulator checks, but that does not replace physical-device QA or human audible confirmation.

## Signed-release disposable-emulator result

- The signed `app-release.apk` was installed on clean `emulator-5554` only. Launch, onboarding, live catalog/deity browsing, playback, pause/resume, seek, mini-player, full player, queue/transport state, background service, foreground media notification, MediaSession, screen-off playback state, and emulator network off/on checks passed.
- Direct release-targeted instrumentation was not run because the existing debug-signed test APK cannot attach to the release-signed target; Android rejected the mismatched signatures. No release test APK was re-signed with owner key material.
- These emulator results do not replace real-phone QA or human audible confirmation.

## Install and first launch

- [ ] Record device model, Android version, ABI, locale, font scale, and battery state.
- [ ] Install the signed Phase 22 release candidate. Do not use the unsigned APK for final acceptance.
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

- [ ] Open Library → Preferences → Privacy policy and verify the HTTPS page and support email.
- [ ] Start each bundled demo fixture and confirm audible output through the phone speaker and headphones.
- [ ] Pause, resume, seek, and move previous/next while the app is foregrounded.
- [ ] Background the app for at least 60 seconds, lock the device, and verify playback continues once.
- [ ] Verify lock-screen and notification transport controls, including a service restart/reconnect.
- [ ] Enable a reminder, grant notification permission, observe one notification, open the app from it, then disable the reminder.
- [ ] Disable network access, observe the documented offline/reconnect behavior, restore network access, and start another remote track.

## Sharing and local Family Mandir

- [ ] Generate a blessing card in English and Hindi; inspect the PNG in a receiving app.
- [ ] Cancel the Sharesheet and repeat the share; confirm no crash and no `file://` URI exposure.
- [ ] Configure a Family Mandir name and members, restart the app, and verify local persistence.
- [ ] Confirm the Family Mandir surface continues to identify data as local device-only data.

Record device model, release build identifier, results, screenshots, and logcat excerpts before marking this checklist complete. Only mark audible playback PASS after a person actually hears the phone output.
