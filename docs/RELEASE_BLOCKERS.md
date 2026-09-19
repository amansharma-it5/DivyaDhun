# Release blockers

## Technical blockers

- Physical-device QA has not been run. The last valid complete emulator suite was 17/18 and the current emulator environment is blocked; speaker output, lock-screen controls, vendor lifecycle behavior, and TalkBack still need a real-device pass.
- A production release signing configuration is not present. A production keystore, Play App Signing enrollment, and release signing ownership must be supplied before publishing.

## External blockers

- Rights-cleared production devotional recordings and any required translations.
- Production artwork with distribution licenses and attribution records.
- Backend/CDN deployment, content operations, and credentials for the Phase 12 remote catalog seam.
- Google Play Console setup, store listing, Data Safety declarations, and owner privacy review. The public privacy/support URL is now supplied and linked from the app source.

The five bundled WAV files are developer-created fixtures and are not a production catalog. Their absence does not represent an application crash blocker; production content integration is a separate rights and operations gate.
