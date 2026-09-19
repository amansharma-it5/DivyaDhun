# Owner actions for DivyaDhun Play release

The engineering branch is ready for owner-controlled release setup but is not ready for Play publication.

1. Review the published policy at `https://amansharma-it5.github.io/divyadhun-privacy/` and confirm it remains accurate.
2. Verify that the final signed build exposes the policy link from Library → Preferences.
3. Create a protected Play upload key and configure `keystore.properties` locally. Never commit the keystore or passwords.
4. Upload the signed AAB to an internal/closed test track only after the signed artifact passes physical QA.
5. Complete Play Console Data Safety, content rating, target audience, store listing, app-access (not applicable to the current no-account app), and content-rights declarations.
6. Confirm whether the developer account is subject to the 12-testers/14-days closed-testing rule and complete it if required.
7. Complete developer device verification if Play Console requests it.
8. Supply the final icon, adaptive icon/monochrome asset if required by the current Play UI, feature graphic, screenshots, and support URL.
9. Confirm distribution rights for the five bundled offline demo WAV fixtures, or remove/replace them before publication.
10. Review the live production catalog and rights record. The status is `LICENSED — USER_CONFIRMED`; agreements were not independently reviewed by this engineering audit.
11. After all gates pass, make the Play release decision and retain rollback/version records.

No Google Play publication, billing change, catalog change, or additional audio upload was performed by this branch.
