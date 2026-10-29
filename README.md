# Revenex School ERP

Native Android school ERP built with Kotlin and Jetpack Compose. It supports school leadership, school administrators, teachers, students and guardians through role-aware portals.

The active cloud backend is Supabase: Auth, PostgreSQL with RLS, private Storage, database inboxes and Realtime. Local Demo is a separate offline experience with synthetic personas. Razorpay is TEST-only and server-verified. Firebase is not part of the application or deployment.

## Project layout

```text
app/
  src/main/java/com/example/
    data/                 # Models, data sources and repository
    ui/                   # Theme, components, navigation and role screens
  src/main/res/           # Bundled brand/photo assets, icons and Android resources
  src/test/               # JVM, Compose interaction and native render tests
  src/review/             # Optional side-by-side review build resources
supabase/
  migrations/             # Ordered schema, RLS and RPC migrations
  functions/              # Server-only payment and AI handlers
  tests/                  # Isolated PostgreSQL and handler tests
scripts/                  # Public configuration, security and verification utilities
docs/                     # Current setup, architecture, workflows and limitations
.github/                  # CI/CD and dependency update configuration
gradle/                   # Wrapper and version catalog
AGENTS.md                 # Repository working rules
```

Build output, APKs, logs, caches, local configuration and historical cleanup backups are ignored by Git. Keep the existing package/application ID when updating installed demo devices.

## Build and run

Use JDK 21 and the Android SDK/build tools declared in the Gradle project (compile SDK 36.1, minimum Android API 24). Open the project root in Android Studio or use the wrapper.

1. Copy [supabase.local.properties.example](supabase.local.properties.example) to ignored `supabase.local.properties`.
2. For cloud sign-in, set only `SUPABASE_URL` and `SUPABASE_PUBLISHABLE_KEY`. Without cloud configuration, use Try demo.
3. Build and install:

```powershell
.\gradlew.bat assembleDebug
adb install -r app\build\outputs\apk\debug\app-debug.apk
```

Install as an update to preserve app data. Stop on a signature/package conflict; do not uninstall or reset school data to work around it.

## Documentation

- [Documentation index](docs/README.md)
- [Development and project conventions](docs/DEVELOPMENT.md)
- [Architecture and security boundaries](docs/ARCHITECTURE.md)
- [Supabase setup](supabase/README.md)
- [Razorpay TEST setup](supabase/RAZORPAY_SETUP.md)
- [Testing commands](docs/TESTING.md) and [device acceptance checklist](docs/DEVICE_TESTING.md)
- [Screens and connected workflows](docs/SCREENS_AND_WORKFLOWS.md)
- [Known limitations](docs/KNOWN_LIMITATIONS.md) and [verification evidence](docs/VERIFICATION.md)
- [Visual system and asset attribution](docs/DESIGN_SYSTEM.md)

## Readiness

This is a demo acceptance-test candidate, not a production certification. Hosted SQL permission/workflow checks have passed, but a captured device Razorpay TEST payment, real webhook delivery and full two-account Android transport acceptance remain unverified. Hosted AI is unavailable until an operator configures/deploys it. See the limitations before demonstrating cloud behavior.

Secrets belong only in server secret stores or CI environments. Never commit private credentials or place model, payment, service-role or database secrets in Android.
