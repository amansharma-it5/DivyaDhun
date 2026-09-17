# Google Play Data Safety draft

This is an engineering draft for the Play Console form. The owner must submit the final answers after reviewing the final release artifact, privacy policy, and any Play Console SDK declarations. Google requires the Data Safety form for published apps, including apps that do not collect user data: [Provide information for Data safety](https://support.google.com/googleplay/android-developer/answer/10787469?hl=en-AE).

## Proposed answers

| Play question | Proposed answer | Evidence/qualification |
| --- | --- | --- |
| Does the app collect or share user data? | **No**, based on the current implementation | No user data is transmitted to DivyaDhun servers; catalog/audio requests do not include app identity or user profile. Owner must confirm dependency disclosures in Play Console. |
| Is data encrypted in transit? | **Yes** for catalog/audio requests | Remote catalog and production media URLs are HTTPS-only; cleartext traffic is disabled. |
| Can users request deletion? | No account deletion path is needed because there is no account | Local data can be cleared through Android settings or app uninstall; history can be cleared in Library. If the app later creates accounts, add in-app and web deletion. |
| Name, email, phone, contacts | Not collected/shared | No corresponding feature or permission. |
| Location, health, financial, authentication, messages | Not collected/shared | No corresponding feature or permission. |
| Photos/videos/files/audio | Not collected/shared | Blessing-card sharing uses a locally generated file and user-directed Sharesheet; the app does not upload user media. |
| App activity | Not collected/shared | Favorites, history, and playback state stay in local DataStore. |
| App info/performance and diagnostics | Not collected/shared | No analytics, crash reporting, or telemetry SDK found. |
| Device or other identifiers | Not collected/shared | No advertising ID, account ID, or device ID code found. |

## Owner submission checklist

- Publish the final privacy-policy URL and add it to the Play Console form. Google’s privacy-policy requirement is documented at [User Data](https://support.google.com/googleplay/android-developer/answer/10144311?hl=en-GB).
- Confirm that all bundled and transitive SDK behavior matches this draft.
- Answer the Play Console questions for the exact signed release artifact, not only the source tree.
- Revisit this form before any analytics, crash reporting, account, subscription, cloud-sync, or upload feature is added.
