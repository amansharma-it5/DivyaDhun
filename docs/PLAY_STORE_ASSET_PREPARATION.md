# Play Store asset preparation

Status: `OWNER_ACTION_REQUIRED`. No fabricated graphics or screenshots were added to the repository. Physical QA evidence is retained separately; Play listing assets still require owner review and upload.

## Current app icon review

- Android launcher source: `app/src/main/res/drawable/ic_brand.xml`.
- It is a 108dp vector used by the app and is not itself the Play Console upload asset.
- Export an owner-approved 512x512 32-bit PNG with alpha, at most 1024 KB, and review it against Google Play icon policy before upload.

## Required owner assets

- Feature graphic: 1024x500 JPEG or 24-bit PNG without alpha. Keep important branding centered and avoid claims about ranking, price, awards, or unsupported features.
- Phone screenshots: at least two screenshots across the required device types to publish the listing; use genuine signed-release screens, JPEG or 24-bit PNG without alpha, minimum dimension 320 px, maximum dimension 3840 px, and no dimension more than twice the other.
- Capture the signed release after production signing and physical QA. Do not use emulator/debug-only states as final evidence, and do not add device frames or promotional text that makes the app look different from the tested build.
- Suggested truthful sequence: Home, Explore/deity browsing, Search, full player/queue, and Library/preferences with the privacy link. Capture only screens that exist in the signed build.
- Current physical evidence includes `build/physical-qa-remote-daily.png` (1080x2340) and `build/physical-qa-remote-privacy-page.png` (1080x2340). These are QA evidence, not yet approved Play assets; inspect/convert to Play-accepted JPEG or 24-bit PNG without alpha before upload.

## Current official size checks

- App icon: 512x512 PNG, 32-bit with alpha, no more than 1024 KB.
- Feature graphic: exactly 1024x500 JPEG or 24-bit PNG, without alpha.
- Phone screenshots: JPEG or 24-bit PNG, minimum 320 px and maximum 3840 px; the maximum dimension must not exceed twice the minimum. A listing needs at least two screenshots for a supported device type and can include up to eight phone screenshots.
- Source: [Add preview assets to showcase your app](https://support.google.com/googleplay/android-developer/answer/9866151?hl=en-GB).

## Owner upload checklist

- [ ] Export the final icon/adaptive icon and inspect at small sizes.
- [ ] Create the feature graphic at exactly 1024x500.
- [ ] Capture and review genuine signed-build screenshots.
- [ ] Confirm all text and images are owner-controlled or distribution-cleared.
- [ ] Upload assets in Play Console → Grow users → Store presence → Main store listing.

Official requirements: [Add preview assets to showcase your app](https://support.google.com/googleplay/android-developer/answer/9866151?hl=en-GB).
