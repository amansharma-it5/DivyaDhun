# DivyaDhun release signing setup

Status: `OWNER_ACTION_REQUIRED`

The Phase 22 release candidate is `com.divyadhun.app` version `1.0.0` (`versionCode 1`). The repository contains an optional local signing configuration that activates only when a complete ignored `keystore.properties` exists. The owner reports that `divyadhun-upload-key-IN.p12` with alias `divyadhun-upload` was generated and verified locally outside this repository. Its encrypted backup has not yet been verified, so production signing remains intentionally deferred; the checked-in build outputs remain unsigned.

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

- Release APK: `app/build/outputs/apk/release/app-release-unsigned.apk`
- Release AAB: `app/build/outputs/bundle/release/app-release.aab`
- Owner key filename reported outside Git: `divyadhun-upload-key-IN.p12`; alias: `divyadhun-upload`; private location is owner-controlled and was not inspected by this audit.
- Package: `com.divyadhun.app`
- Target SDK: 37
- Production catalog URL is injected with `-PdivyadhunCatalogUrl=...`; no catalog or audio files are embedded by this setting.
- No private key, password, or signing file was added to the repository. Do not configure signing until the encrypted backup is verified.

## Backup verification record

The following filesystem checks were performed without opening the P12 or revealing any password:

- `divyadhun-upload-key-IN.p12` exists at the owner-supplied protected location and is the only permitted key for this release. The older filename without `-IN` is excluded.
- `divyadhun-upload-key-FINAL.7z` exists (3,665 bytes) and `divyadhun-upload-key-IN.7z` exists (3,665 bytes). A no-password 7-Zip listing test reports `Cannot open encrypted archive`, `Wrong password?`, and `Headers Error` for both. This is not an integrity pass and no password was guessed.
- The P12 and archive contents have therefore not been accepted, and no Google Drive upload or second backup was attempted.
- After the owner enters the intended password in the trusted local 7-Zip window, run the archive's built-in integrity test and confirm exactly one expected `divyadhun-upload-key-IN.p12` entry before any signing or cloud backup.
