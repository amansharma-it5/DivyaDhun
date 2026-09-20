# Release blockers

## Technical blockers

- Physical-device QA is partial on `RZCX623LLJK`: after explicit owner approval, the debug package was removed and the signed Phase 22 release was installed without touching unrelated phone data. The owner subsequently reported a privacy-link failure and vertical Aaj Ki Bhakti title wrapping; both fixes now pass the complete signed `releaseQa` connected suite (21/21) on the disposable AVD, with a phone-sized visual screenshot captured. Physical re-verification, human audible confirmation, and an unlocked in-app tap-through remain pending.
- A second independent encrypted backup of the owner-controlled upload key remains pending. The encrypted Google Drive backup is owner-confirmed; no USB backup exists. Play App Signing enrollment and final Play Console declarations remain owner-controlled.

## External blockers

- Production artwork with distribution licenses and attribution records.
- Google Play Console setup, store listing, Data Safety declarations, and owner privacy review. The public privacy/support URL is supplied and linked from the app source; signed-release verification remains pending.
- Authorized physical-device acceptance QA for the signed release artifact.

The production audio status is `LICENSED — USER_CONFIRMED`; public streaming and Cloudflare R2 hosting were confirmed by the owner, not independently reviewed. The five bundled WAV files have `OWNER_CONFIRMED` distribution rights; documentary agreements were not independently reviewed. Neither status replaces the owner’s final Play Console release decision.
