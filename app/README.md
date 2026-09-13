# app/

The main Android application module for the REVENEX School ERP (ScholaOS).

## What's in here

- `google-services.json` — Firebase client configuration for the Android app. Required for all Firebase SDK features (Auth, Firestore, Storage, AI).
- `proguard-rules.pro` — ProGuard/R8 obfuscation and shrinking rules applied during release builds.
- `app-debug.apk` — Latest built debug APK artifact. Install directly on a device for testing without building from source.
- `src/` — All Kotlin source code, tests, and Android resources. See `src/README.md` for details.
- `build/` — Gradle-generated build output (DEX files, APK, test results). Do not commit or edit manually.

## Build commands

```bash
# Compile Kotlin source only
.\gradlew.bat compileDebugKotlin

# Build debug APK
.\gradlew.bat assembleDebug

# Run all unit tests
.\gradlew.bat test

# Full clean + fresh debug build
.\gradlew.bat clean assembleDebug
```

Output APK is written to `app/build/outputs/apk/debug/app-debug.apk` and also copied to the project root as `app-debug.apk`.
