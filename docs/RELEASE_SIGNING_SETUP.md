# DivyaDhun release signing setup

Status: `OWNER_ACTION_REQUIRED`

The Phase 22 release candidate is `com.divyadhun.app` version `1.0.0` (`versionCode 1`). The repository contains an optional local signing configuration that activates only when a complete ignored `keystore.properties` exists. No production key is present in the current environment, so `:app:assembleRelease` produces `app-release-unsigned.apk`, `:app:bundleRelease` produces an unsigned `app-release.aab`, and `:app:signingReport` reports `release: Config: none`.

This is intentional. The debug keystore must not be used for Play distribution, and a private upload key must not be generated or committed by an automated run.

## Beginner-safe owner procedure

Do these steps manually on the owner’s protected workstation. Codex must not generate, receive, or adopt the owner’s permanent key.

1. Install a trusted JDK and Android SDK, then open a terminal in this repository. Do not put the keystore, passwords, or recovery notes in Git or chat.
2. Generate or obtain a dedicated RSA 2048+ upload key. If generating it yourself, run the command below and answer the prompts locally; never paste the output or passwords into chat.
3. Make at least two encrypted backups of the keystore in separate access-controlled locations. Record the alias and recovery process privately.
4. Copy `keystore.properties.example` to `keystore.properties` and fill in local paths/secrets only. Verify Git ignores it with `git check-ignore -v keystore.properties`.
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
- Package: `com.divyadhun.app`
- Target SDK: 37
- Production catalog URL is injected with `-PdivyadhunCatalogUrl=...`; no catalog or audio files are embedded by this setting.
- No private key, password, or signing file was added to the repository.
