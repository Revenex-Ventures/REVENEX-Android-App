# app/

The main Android application module for the REVENEX School ERP.

## Latest changes — 23 October 2026

- Ivory/white canvas, charcoal text, orange actions and semantic pastel surfaces, with a coordinated dark palette.
- Persistent Light / Dark / System appearance; default Light. Compact shared dashboard panels, metrics and navigation; adaptive module layouts and improved large-text dock labels.
- Updated authentication, assignments, dated attendance register/save feedback, fee totals/paid-receipt lookup, grouped student details and notification rows. All five implemented portals share the theme; nested-state/device acceptance is still incomplete.
- Supabase integration is preserved. Firebase dependencies were removed in the earlier migration. Payment errors are sanitized; Android never settles an invoice from its callback alone.
- Build passed with 85 Android tests and lint 0 errors / 69 warnings. Rendered light/dark portal and secondary-screen checks are distinct from real-device and hosted acceptance.

Read the [complete before/after summary](../docs/changes/README.md) and [verification evidence](../docs/CURRENT_DELIVERY.md).

## What's in here

- `../supabase.local.properties` (ignored, in the repository root; template `../supabase.local.properties.example`) — Public cloud-demo configuration (`SUPABASE_URL`, `SUPABASE_PUBLISHABLE_KEY`). Private payment/model/service credentials stay on the backend.
- `proguard-rules.pro` — ProGuard/R8 obfuscation and shrinking rules applied during release builds.
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

Standard build output: `app/build/outputs/apk/debug/app-debug.apk`. The downloaded, tested CI artifact currently available in this workspace is `app/build/final-delivery/apk/app-debug.apk`; see the verification report for its source commit and SHA-256. Signing-key conflicts with existing installations remain possible; do not delete user data merely to bypass an install error.
