# Owner actions for DivyaDhun Play release

The engineering branch is ready for owner-controlled release setup but is not ready for Play publication.

1. Review the published policy at `https://amansharma-it5.github.io/divyadhun-privacy/` and confirm it remains accurate.
2. Verify that the final signed build exposes the policy link from Library → Preferences.
3. Verify an encrypted backup of the already-generated `divyadhun-upload-key-IN.p12` (alias `divyadhun-upload`), then configure `keystore.properties` locally. Never use the similarly named key without `-IN`, and never commit the keystore or passwords. The existing unencrypted 7z archive is not an acceptable backup.
4. Upload the signed AAB to an internal/closed test track only after the signed artifact passes physical QA.
5. Complete Play Console Data Safety, content rating, target audience, store listing, app-access (not applicable to the current no-account app), and content-rights declarations.
6. Confirm whether the developer account is subject to the 12-testers/14-days closed-testing rule and complete it if required.
7. Complete developer device verification if Play Console requests it.
8. Supply the final 512x512 Play icon/adaptive icon, 1024x500 feature graphic, genuine signed-build screenshots, and support URL; see `docs/PLAY_STORE_ASSET_PREPARATION.md`.
9. Distribution rights for the five bundled offline demo WAV fixtures are `OWNER_CONFIRMED`; retain the owner’s confirmation and note that documentary agreements were not independently reviewed.
10. Review the live production catalog and rights record. The status is `LICENSED — USER_CONFIRMED`; agreements were not independently reviewed by this engineering audit.
11. After all gates pass, make the Play release decision and retain rollback/version records.

No Google Play publication, billing change, catalog change, or additional audio upload was performed by this branch.
