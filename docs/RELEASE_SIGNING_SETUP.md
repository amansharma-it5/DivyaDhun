# DivyaDhun release signing setup

Status: `OWNER_ACTION_REQUIRED`

The Phase 22 release candidate is `com.divyadhun.app` version `1.0.0` (`versionCode 1`). The repository contains an optional local signing configuration that activates only when a complete ignored `keystore.properties` exists. No production key is present in the current environment, so `:app:assembleRelease` produces `app-release-unsigned.apk`, `:app:bundleRelease` produces an unsigned `app-release.aab`, and `:app:signingReport` reports `release: Config: none`.

This is intentional. The debug keystore must not be used for Play distribution, and a private upload key must not be generated or committed by an automated run.

## Owner procedure

1. Create or obtain a dedicated RSA 2048+ Play upload key on a protected owner workstation. Keep the keystore and passwords outside Git, backups that are not access-controlled, and chat transcripts.
2. Copy `keystore.properties.example` to `keystore.properties` and fill in local paths/secrets. The existing `.gitignore` excludes that file and `*.jks`/`*.keystore`.
3. The checked-in Gradle configuration reads the local properties only when all four fields are present. Do not hard-code passwords or key paths. An incomplete local file fails closed with a clear configuration error.
4. Run `:app:bundleRelease` and verify the resulting AAB with `jarsigner`/Play Console. Keep the upload key available for future updates; Play App Signing protects the app signing key after enrollment.
5. Enroll the app in Google Play App Signing during the first Play upload. Google’s current guidance uses a developer-held upload key and a Google-held app-signing key: [Play App Signing](https://support.google.com/googleplay/android-developer/answer/9842756?hl=en).

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
