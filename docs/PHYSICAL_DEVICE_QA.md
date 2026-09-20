# DivyaDhun physical-device QA checklist

Status for Phase 22: **PARTIAL — owner confirmation pending**. The prior debug-signed `com.divyadhun.app` package was removed only after explicit owner approval, and the verified Phase 22 release APK is now installed on `RZCX623LLJK`. The app remains installed. The owner subsequently reported two signed-release defects: the Privacy policy action did not open, and Aaj Ki Bhakti titles wrapped vertically. Fixes are now covered by release-targeted tests, but physical re-verification is still pending. Human audible confirmation also remains pending.

## Owner-reported signed-release defects

- **Previously reported FAIL — Privacy policy:** the original handler gated `startActivity()` behind `resolveActivity()`. The fix now starts a browsable HTTPS intent directly and only handles `ActivityNotFoundException`; the published URL is asserted by `PrivacyPolicyIntentTest`.
- **Previously reported FAIL — Aaj Ki Bhakti title layout:** the shared `TrackRow` and Daily Bhakti current/queue rows had insufficient width constraints and oversized trailing content. The fix gives the text column a zero minimum width, keeps artwork fixed, compacts trailing actions, and applies single-line ellipsis to constrained title/subtitle fields. `ReleaseRegressionLayoutTest` covers a 320dp viewport and long titles.
- These are not marked physically PASS until `RZCX623LLJK` is reconnected and the owner confirms the actual signed APK behavior.

Run this checklist on an authorized phone without clearing unrelated applications or personal data.

## Data-preservation finding for `RZCX623LLJK`

- The installed debug build contains a device-local Android DataStore file at `files/datastore/user_preferences.preferences_pb`. Source inspection identifies the stored state as onboarding progress, language, preferred deities, listening time, theme, simple mode, Daily Bhakti reminder setting, optional Family Mandir name/member names, favorites, recent track IDs, and the last-played track ID.
- The app has no user-facing export/import or restore feature. `FileProvider` is used only for user-directed blessing-card sharing from the disposable cache; it is not a backup mechanism.
- The manifest sets `android:allowBackup="false"` and `android:fullBackupContent="false"`. Its data-extraction rules exclude `file`, `database`, and `sharedpref` domains from both cloud backup and device transfer. A debug-only `run-as` listing is not a supported portable backup or restore path, so no private data archive was created.
- A standard uninstall would remove the app sandbox, including the DataStore and disposable cache. The remote catalog and remote audio are not local user data and would remain available after reinstall, but the local preferences, favorites, history, Family Mandir values, reminder choice, and last-played state would not be reliably recoverable.

**Previous decision:** preserve the phone until the owner explicitly approved the local-state loss. That approval was later provided; only `com.divyadhun.app` was uninstalled, and the signed release was installed. No unrelated phone data was changed.

## Current device gate

- Pre-install package record: version `0.1.0`, versionCode `1`, `DEBUGGABLE`, Android Debug certificate SHA-256 `915a1e49e6c952a4c63f882a75ace00ff1016e3dcc4bacc442eaf1722d145b0b`.
- Phase 22 release certificate: `CN=Aman Sharma, L=Delhi, ST=Delhi, C=IN`, SHA-256 `a91a010619faa841768e4f33db69bc3e5956034f61dba6dddcbae722df00630c`.
- Because the signatures differed, the owner explicitly approved the package-only uninstall. No unrelated package or phone data was changed. The release package is now installed with version `1.0.0`, versionCode `1`, APK signing version 2, and non-debuggable package flags.

The owner has no spare Android phone. The signed release has already passed the documented disposable-emulator checks, but that does not replace physical-device QA or human audible confirmation.

## Signed-release disposable-emulator result

- The signed `app-release.apk` was installed on clean `emulator-5554` only. Launch, onboarding, live catalog/deity browsing, playback, pause/resume, seek, mini-player, full player, queue/transport state, background service, foreground media notification, MediaSession, screen-off playback state, and emulator network off/on checks passed.
- A signed `releaseQa` instrumentation variant is enabled with the existing release signing configuration; its test-only Compose host is excluded from the production `release` APK/AAB. The targeted privacy and layout regressions pass on the disposable AVD. This does not replace real-phone QA or human audible confirmation.
- The complete signed `releaseQa` connected suite passes on the disposable AVD: 21/21 tests, including production-catalog remote streaming smoke with the live catalog URL supplied. This does not replace real-phone QA or human audible confirmation.
- These emulator results do not replace real-phone QA or human audible confirmation.

## Physical signed-release results

- **PASS:** launch, fresh onboarding, production HTTPS catalog load, and host-side catalog verification of 77 unique records.
- **PASS:** search returned 15 Hanuman devotional tracks; a production Hanuman track loaded into the player.
- **PASS:** MediaSession playback, pause, resume, fast-forward seek, background playback with the launcher foreground, and the foreground media service notification.
- **PASS:** screen-off playback state remained active; the Samsung lock screen exposed the DivyaDhun media card with Previous, Pause, and Next controls.
- **PASS:** Wi-Fi and mobile data were temporarily disabled and then restored; the active MediaSession remained PLAYING and Wi-Fi reconnected. Connectivity was restored to its original state.
- **NOT CONFIRMED:** human-audible output. A person must listen through the phone speaker or headphones.
- **PENDING:** tap the in-app Privacy policy link while the phone is unlocked. The phone was left keyguard-locked after the lock-screen check; no lock bypass was attempted.

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
