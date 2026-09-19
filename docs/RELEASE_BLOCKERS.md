# Release blockers

## Technical blockers

- Physical-device QA is blocked on `RZCX623LLJK`: the existing app is signed with the Android debug certificate while the Phase 22 APK uses the owner release certificate. The signed APK was not installed because doing so would require a destructive uninstall/overwrite path. Use an authorized clean or release-signed test device; emulator instrumentation remains 18/18.
- A production release signing configuration is not present. A production keystore, Play App Signing enrollment, and release signing ownership must be supplied before publishing.

## External blockers

- Production artwork with distribution licenses and attribution records.
- Google Play Console setup, store listing, Data Safety declarations, and owner privacy review. The public privacy/support URL is supplied and linked from the app source; signed-release verification remains pending.
- Authorized physical-device acceptance QA for the signed release artifact.

The production audio status is `LICENSED — USER_CONFIRMED`; public streaming and Cloudflare R2 hosting were confirmed by the owner, not independently reviewed. The five bundled WAV files have `OWNER_CONFIRMED` distribution rights; documentary agreements were not independently reviewed. Neither status replaces the owner’s final Play Console release decision.
