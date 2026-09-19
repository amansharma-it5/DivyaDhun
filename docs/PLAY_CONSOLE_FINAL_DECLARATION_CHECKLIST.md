# DivyaDhun final Play Console declaration checklist

Use this checklist for the signed `com.divyadhun.app` `1.0.0` release. It is an owner submission aid, not a substitute for reviewing the final artifact in Play Console.

## App, privacy, and support

- [ ] App name, package, version code, category, target audience, and content rating match the signed bundle.
- [x] Privacy policy: `https://amansharma-it5.github.io/divyadhun-privacy/`.
- [x] Support contact: `divyadhun.support@gmail.com`.
- [ ] Open the privacy link from Library → Preferences on the signed device build.

## Data Safety answers to review

- [ ] Based on the current implementation, declare no DivyaDhun account/profile collection, analytics, advertising, crash reporting, cloud sync, or sale of personal information.
- [ ] Disclose local-only preferences, Family Mandir values, favorites, recent IDs, last-played ID, and reminder setting as applicable to the form.
- [ ] Disclose HTTPS catalog/audio requests and ordinary hosting-provider network metadata under the provider’s policies.
- [ ] Explain that users can clear local data in Android settings/uninstall the app and clear listening history in Library; there is no account deletion flow because there is no account.
- [ ] Recheck every answer against the exact signed artifact and its dependency declarations before submission.

## Content, testing, and publication gates

- [x] Production audio status recorded as `LICENSED — USER_CONFIRMED`; owner confirmed public streaming/R2 hosting and demo WAV distribution rights. Documentary agreements were not independently reviewed.
- [ ] Complete content rating, target-audience/Families, store listing assets, and account-specific testing/device-verification declarations.
- [x] Owner confirms an encrypted Google Drive backup for `divyadhun-upload-key-IN.p12` / alias `divyadhun-upload`.
- [ ] Create or verify a second independent encrypted backup. Play App Signing enrollment remains owner-controlled.
- [x] Configure the ignored local signing properties and verify the signed AAB. Do not use the similarly named key without `-IN`.
- [ ] Upload the owner-approved 512x512 icon, 1024x500 feature graphic, and genuine signed-build screenshots.
- [ ] Complete [physical-device QA](PHYSICAL_DEVICE_QA.md), including audible playback, background/lock-screen controls, MediaSession, seeking, and reconnect behavior.
- [ ] Do not upload to Play, publish, or merge PR #25 until all mandatory gates are green and the owner makes the release decision.
