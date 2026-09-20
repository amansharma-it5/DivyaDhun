# Play Store asset preparation

Status: `OWNER_ACTION_REQUIRED`. Original branded assets and compliant screenshot copies from the fixed signed release-QA build are prepared for owner approval; no Play upload was performed.

## Current app icon review

- Android launcher source: `app/src/main/res/drawable/ic_brand.xml`.
- It is a 108dp vector used by the app and is not itself the Play Console upload asset.
- Export an owner-approved 512x512 32-bit PNG with alpha, at most 1024 KB, and review it against Google Play icon policy before upload.

## Fixed Daily Bhakti screenshot finding

- The raw `For 2131362129` text was a genuine app bug, not a screenshot artifact. `listeningTimeNameRes(...)` returns an Android `@StringRes` ID, and the Daily Bhakti composable had passed that integer directly to the formatted string as `%1$s`.
- The fix resolves the nested string resource before formatting. The new regression test `dailyBhaktiResolvesListeningTimeLabelInsteadOfShowingResourceId` requires `For Anytime`; the fixed signed release-QA capture is `divyadhun-screenshot-daily-bhakti-v2.png`.

## Required owner assets

- Feature graphic: 1024x500 JPEG or 24-bit PNG without alpha. Keep important branding centered and avoid claims about ranking, price, awards, or unsupported features.
- Phone screenshots: at least two screenshots across the required device types to publish the listing; use genuine signed-release screens, JPEG or 24-bit PNG without alpha, minimum dimension 320 px, maximum dimension 3840 px, and no dimension more than twice the other.
- Capture the signed release after production signing and physical QA. Do not use emulator/debug-only states as final evidence, and do not add device frames or promotional text that makes the app look different from the tested build.
- Suggested truthful sequence for this review: Home, Explore/discovery, and Daily Bhakti/playback. Preferences is intentionally omitted because it adds less listing value than the discovery and playback surfaces.
- Current physical evidence includes `build/physical-qa-remote-daily.png` and `build/physical-qa-remote-privacy-page.png`. The original 1080x2340 captures remain unchanged as QA evidence; compliant copies are listed below.

## Validation result for current files

- The v2 captures are genuine signed release-QA emulator evidence from the fixed application. The earlier Daily Bhakti candidate showed the real UI defect `For 2131362129` and is superseded; it must not be uploaded. The v2 Daily Bhakti capture shows the resolved `For Anytime` label. The original physical QA captures remain evidence but are not used as the final v2 candidate sequence.
- `app/src/main/res/drawable/ic_brand.xml` is an original DivyaDhun vector source with the existing maroon/gold branding. It is not a Play upload file; export an owner-approved 512x512 32-bit PNG with alpha and keep it at or below 1024 KB.
- The feature graphic below is original DivyaDhun branding; no copyrighted artwork or unverified devotional imagery was added. Version 2 improves safe margins, hierarchy, contrast, and small-preview readability.

## Prepared approval package

- Icon PNG: `docs/assets/play-store/divyadhun-icon.png` — 512x512, RGBA PNG, 7,794 bytes. Deterministic raster export of the existing `ic_brand.xml` mark.
- Feature graphic: `docs/assets/play-store/divyadhun-feature-graphic.png` — 1024x500, RGB PNG. Original maroon/gold branding with no rankings, pricing, store badges, or unsupported claims.
- Genuine fixed-build screenshots: `divyadhun-screenshot-home-v2.png`, `divyadhun-screenshot-explore-v2.png`, and `divyadhun-screenshot-daily-bhakti-v2.png` — each 1080x2160 RGB PNG. Cropped directly from the signed release-QA app on `emulator-5554`; no text or artwork was painted over.
- Visual review sheet: `docs/assets/play-store/divyadhun-asset-review-sheet.png` — owner-facing v2 composite of the icon, polished feature graphic, and revised screenshot sequence.
- SVG provenance sources: `docs/assets/play-store/divyadhun-icon.svg` and `divyadhun-feature-graphic.svg`.

The package is dimension- and pixel-format-validated with FFprobe and visually inspected. Owner approval is still required for branding, listing copy, rights, and whether emulator captures are acceptable for the final listing before upload.

## Current official size checks

- App icon: 512x512 PNG, 32-bit with alpha, no more than 1024 KB.
- Feature graphic: exactly 1024x500 JPEG or 24-bit PNG, without alpha.
- Phone screenshots: JPEG or 24-bit PNG, minimum 320 px and maximum 3840 px; the maximum dimension must not exceed twice the minimum. A listing needs at least two screenshots for a supported device type and can include up to eight phone screenshots.
- Source: [Add preview assets to showcase your app](https://support.google.com/googleplay/android-developer/answer/9866151?hl=en-GB).

## Owner upload checklist

- [x] Export the icon and inspect it at small sizes; owner approval remains required.
- [x] Create and validate the feature graphic at exactly 1024x500; owner approval remains required.
- [x] Prepare and visually review genuine signed-build screenshots in Play-compliant dimensions.
- [ ] Confirm all text and images are owner-controlled or distribution-cleared.
- [ ] Upload assets in Play Console → Grow users → Store presence → Main store listing.

Official requirements: [Add preview assets to showcase your app](https://support.google.com/googleplay/android-developer/answer/9866151?hl=en-GB).
