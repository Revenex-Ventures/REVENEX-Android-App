# Development

## Tools

Use JDK 21, Android Studio/SDK, and the checked-in Gradle wrapper. The app targets API 36, compiles against SDK 36.1 and supports API 24+. Dependencies are pinned in the Gradle catalog/build script. Node 24 runs the isolated backend tests; Deno 2.9.7 checks Edge Functions and their HTTP entrypoint.

The project is Kotlin/Jetpack Compose. Do not introduce Flutter, a second frontend, new libraries or a backend rewrite to perform routine maintenance.

## Local configuration

Copy [the public configuration template](../supabase.local.properties.example) into ignored `supabase.local.properties`. It accepts only the Supabase HTTPS URL and publishable key. Private keys must never be added to this file. Cloud mode reports configuration/network/authentication failures honestly; it does not silently switch to sample records.

Android Studio creates ignored `local.properties` with the SDK path. Command-line users can instead set `ANDROID_HOME`. Generated files belong under `build/` or `app/build/`, not beside source code.

## Build variants

```powershell
.\gradlew.bat assembleDebug
.\gradlew.bat assembleReview
```

Debug uses the existing `com.example` application ID. Review installs as `com.example.review` alongside another build. Keep package identity stable to avoid breaking upgrades. Production signing requires an operator-owned keystore/environment configuration; this project does not publish a production release automatically.

## Source organization

- `data/model`: shared domain records and school dates.
- `data/datasource`: explicit Local Demo versus Supabase adapters, session checks, files and reconciliation.
- `data/repository`: screen-facing state and confirmed operations.
- `ui/theme`, `ui/components`, `ui/navigation`: shared tokens, controls and role navigation.
- `ui/screens`: routed portal layouts and workflows.
- `supabase/migrations`: immutable ordered database changes. Add a new version for changes; never casually rewrite deployed history.
- `supabase/functions`: backend-only payment/AI code and prompt.

Follow [AGENTS.md](../AGENTS.md). Preserve public component contracts and user changes. UI changes should stay in the permitted UI files. A portal selection is never authorization.

Read [testing](TESTING.md) before reporting a build ready. Build success alone is not visual or hosted verification.
