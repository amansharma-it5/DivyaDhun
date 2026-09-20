# DivyaDhun release signing setup

Status: `SIGNED_LOCAL_BUILD_COMPLETE; OWNER_ACTION_REQUIRED_FOR_BACKUP_AND_PLAY`

The Phase 22 release candidate is `com.divyadhun.app` version `1.0.0` (`versionCode 1`). The repository contains an optional local signing configuration that activates only when a complete ignored `keystore.properties` exists. The owner supplied `divyadhun-upload-key-IN.p12` with alias `divyadhun-upload`; the local signed build is verified. The owner confirms an encrypted Google Drive backup; a second independent backup, Play App Signing enrollment, and human audible confirmation remain pending. Physical privacy-link, layout, playback-state, and background MediaSession checks passed on `RZCX623LLJK`.

This is intentional. The debug keystore must not be used for Play distribution, and a private upload key must not be generated or committed by an automated run.

## Beginner-safe owner procedure

Do these steps manually on the owner’s protected workstation. Codex must not generate, receive, or adopt the owner’s permanent key.

1. Install a trusted JDK and Android SDK, then open a terminal in this repository. Do not put the keystore, passwords, or recovery notes in Git or chat.
2. If using the already-generated owner key, select only `divyadhun-upload-key-IN.p12` with alias `divyadhun-upload`; never use the similarly named key without `-IN`. Do not generate another key.
3. Make at least two encrypted backups of the keystore in separate access-controlled locations. The existing 7z archive is unencrypted and must not be uploaded or treated as a verified backup. Record the alias and recovery process privately.
4. After encrypted backup verification, copy `keystore.properties.example` to `keystore.properties` and fill in local paths/secrets only. Verify Git ignores it with `git check-ignore -v keystore.properties`.
5. Build the production bundle with `:app:bundleRelease`. The checked-in Gradle configuration fails closed unless all four fields are present: `storeFile`, `storePassword`, `keyAlias`, and `keyPassword`.
6. Verify the signed artifact locally with `:app:signingReport`, `jarsigner -verify -verbose -certs app/build/outputs/bundle/release/app-release.aab`, and the Play Console upload check. Do not use the debug keystore.
7. Enroll in Play App Signing during the first upload and retain the upload-key recovery process: [Play App Signing](https://support.google.com/googleplay/android-developer/answer/9842756?hl=en).

Example key-generation command (run manually by the owner; do not paste the resulting secrets here):

```text
keytool -genkeypair -v -keystore divyadhun-upload-key.jks -alias divyadhun-upload -keyalg RSA -keysize 2048 -validity 10000
```

The first Play submission must be an Android App Bundle, and every future update needs a higher `versionCode`: [Prepare and roll out a release](https://support.google.com/googleplay/android-developer/answer/9859152?rd=1).

## Current evidence

- Release APK: `app/build/outputs/apk/release/app-release.apk` (signed; APK Signature Scheme v2 verified)
- Release AAB: `app/build/outputs/bundle/release/app-release.aab` (rebuilt with the production catalog URL; JAR signature verified; SHA-256 `4CC53352F99A4A4462DF94C18C95B9EAF3CD95D36268288E700AD5F2031ADAF5`)
- Owner key filename reported outside Git: `divyadhun-upload-key-IN.p12`; alias: `divyadhun-upload`; private location is owner-controlled and was not inspected by this audit.
- Package: `com.divyadhun.app`
- Version: `1.0.0` / `versionCode 1`
- Certificate identity: `CN=Aman Sharma, L=Delhi, ST=Delhi, C=IN`; Gradle release signing report matches alias `divyadhun-upload`.
- Target SDK: 37
- Production catalog URL is injected with `-PdivyadhunCatalogUrl=...`; no catalog or audio files are embedded by this setting.
- No private key, password, or signing file was added to the repository. The ignored local `keystore.properties` is not committed.

## Backup verification record

The following filesystem checks were performed without opening the P12 or revealing any password:

- `divyadhun-upload-key-IN.p12` exists at the owner-supplied protected location and is the only permitted key for this release. The older filename without `-IN` is excluded.
- `divyadhun-upload-key-FINAL.7z` exists (3,665 bytes) and `divyadhun-upload-key-IN.7z` exists (3,665 bytes). A no-password 7-Zip listing test reports `Cannot open encrypted archive`, `Wrong password?`, and `Headers Error` for both. This is not an integrity pass and no password was guessed.
- The P12 contents were never read by this audit. The owner confirms an encrypted Google Drive backup; no USB backup is expected.
- Local signing proceeded only from the owner-configured ignored properties; no archive was uploaded or altered.
