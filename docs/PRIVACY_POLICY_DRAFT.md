# DivyaDhun privacy policy draft

**Effective date:** `[OWNER TO SET]`
**App:** DivyaDhun
**Contact:** `[OWNER TO REPLACE WITH A MONITORED SUPPORT EMAIL OR WEB FORM]`

This draft describes the current Android implementation. Replace the bracketed owner fields and have the owner review it before publishing. It is not legal advice.

## What DivyaDhun does

DivyaDhun provides devotional audio, catalog browsing, search, playback, favorites, listening history, daily reminders, a Family Mandir profile, and user-directed sharing of blessing cards.

## Information stored on your device

DivyaDhun stores app preferences and library state on your device using Android DataStore. This can include your selected language, deity preferences, theme, simple-mode choice, listening-time preference, daily-reminder setting, Family Mandir name and member names that you choose to enter, favorite track IDs, recent track IDs, and the last-played track ID. The app does not send this information to a DivyaDhun account or synchronize it between devices.

You can clear local app data in Android settings or uninstall the app. You can also clear listening history from the Library screen.

## Catalog and audio requests

When you browse or play production content, the app requests the catalog and selected audio over HTTPS from the configured Cloudflare R2 hosting endpoint. The app does not attach an account, advertising ID, analytics identifier, cookie, or user profile to those requests. The hosting provider may process ordinary network metadata such as an IP address under its own policies.

## Notifications and background playback

If you enable Daily Bhakti reminders, Android may request notification permission on Android 13 and later. The reminder is scheduled locally and does not transmit your reminder setting. Background playback uses Android’s MediaSession and foreground media-playback service.

## Sharing

If you choose to share a blessing card, Android’s Sharesheet lets you select another app. DivyaDhun does not automatically send, post, or publish the card. The selected app’s handling is governed by that app’s privacy policy.

## Information DivyaDhun does not collect

The current app has no account registration, analytics, advertising, crash-reporting, cloud-sync, contacts, location, camera, microphone, phone, SMS, or payment feature. It does not intentionally collect or sell personal information.

## Children

DivyaDhun is not designed as a child-directed service. The final Play Console target-audience selection and any applicable Families declarations must be completed by the owner.

## Changes and contact

We may update this policy when the app’s data practices change. Questions or privacy requests can be sent to `[OWNER CONTACT]`.
