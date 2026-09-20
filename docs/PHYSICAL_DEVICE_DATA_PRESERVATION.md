# Physical-device data preservation assessment

Date: 2026-09-19  
Device: `RZCX623LLJK`  
Package: `com.divyadhun.app` 0.1.0 debug build

## Finding

The phone’s installed app stores user state locally in Android DataStore, backed by the app-private file `files/datastore/user_preferences.preferences_pb`. The source-defined fields are:

- onboarding completion/step, language, preferred deities, listening time, theme, and simple mode;
- Daily Bhakti reminder setting;
- optional Family Mandir name and member names;
- favorite track IDs, recent track IDs, and the last-played track ID.

The app has no export/import or restore feature. The FileProvider shares a generated blessing-card image from `cache/share-cards/`; it does not export app state. The installed debug package was inspected only for package metadata and private file names; no private preference contents were read or copied.

## Backup assessment

No reliable supported backup is available for this release. The manifest sets `android:allowBackup="false"` and `android:fullBackupContent="false"`; `data_extraction_rules.xml` excludes `file`, `database`, and `sharedpref` domains from both cloud backup and device transfer. A `run-as` shell listing confirms the DataStore exists in the debug sandbox, but raw file copying would not provide an app-supported restore workflow and could expose private profile values. Therefore no archive was created and no backup is claimed as verified.

## Risk and safe plan

Uninstalling the existing debug package would normally delete its app sandbox, including the DataStore and cache. The likely loss is local preferences, favorites, history, Family Mandir values, reminder choice, and last-played state. Remote catalog/audio are not stored user state and can be loaded again after installation. Because the release certificate differs from the installed debug certificate, Android will reject an in-place update; resolving that mismatch requires an uninstall or a separate matching device.

The safe plan is to preserve the current app and use the signed-emulator evidence as release-candidate validation. Physical QA may proceed on this phone only after the owner explicitly accepts that the listed local state may be lost; no uninstall, clear-data, certificate change, or release installation is authorized by this assessment.
