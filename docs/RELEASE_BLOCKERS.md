# Release blockers

## Technical blockers

- Physical-device QA is blocked on `RZCX623LLJK`: the existing app is signed with the Android debug certificate while the Phase 22 APK uses the owner release certificate. The signed APK was not installed because doing so would require a destructive uninstall/overwrite path, and source inspection found no supported export/restore path for the phone's local DataStore state. Emulator release QA remains separate from physical QA.
- A second independent encrypted backup of the owner-controlled upload key remains pending. The encrypted Google Drive backup is owner-confirmed; no USB backup exists. Play App Signing enrollment and final Play Console declarations remain owner-controlled.

## External blockers

- Production artwork with distribution licenses and attribution records.
- Google Play Console setup, store listing, Data Safety declarations, and owner privacy review. The public privacy/support URL is supplied and linked from the app source; signed-release verification remains pending.
- Authorized physical-device acceptance QA for the signed release artifact.

The production audio status is `LICENSED — USER_CONFIRMED`; public streaming and Cloudflare R2 hosting were confirmed by the owner, not independently reviewed. The five bundled WAV files have `OWNER_CONFIRMED` distribution rights; documentary agreements were not independently reviewed. Neither status replaces the owner’s final Play Console release decision.
