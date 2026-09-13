# Project Inventory — Revenex School ERP

Brief automated inventory and key info extracted from the workspace.

- Total files scanned: 120
- Top-level folders: `app/`, `functions/`, `docs/`, `gradle/`, `scripts/`, `assets/`
- Primary languages: Kotlin (Android app), JavaScript (Cloud Functions), JSON, TOML, Firebase rules, XML

Key files and highlights
- Android app: `app/` — Jetpack Compose Kotlin app; AGP `9.1.1`; Kotlin `2.2.10`; `minSdk=24`, `targetSdk=36`.
- Gradle version catalog: `gradle/libs.versions.toml` — firebase-bom `34.17.0`, okhttp `4.10.0`, retrofit `2.12.0`, roborazzi `1.59.0`.
- Firebase config: `firebase.json` — Firestore and Storage rules present; Functions configured from `functions/` and emulator settings included.
- Cloud Functions: `functions/` — Node 20 engine; dependencies include `firebase-admin`, `firebase-functions`, `express`, `razorpay`; key files: `index.js`, `src/config.js`, `src/claims.js`, `src/provision.js`.
- Security rules: `firestore.rules` and `storage.rules` implement RBAC / tenant-scoped access; rules reference `request.auth.token.role` and rely on custom claims provisioned by Functions.
- Sensitive/config files present: `app/google-services.json` (mobile Firebase config). Secrets are expected via `.env` and environment variables (Gradle `secrets` plugin + Functions `process.env`).

Tests and tooling
- Unit tests under `app/src/test/` and `functions/test/` with rules unit tests.
- CI/build: Gradle wrapper present; common tasks documented in `README.md` (tests, assembleDebug).

Notes & next steps
- A deeper dependency scan (npm audit / Gradle dependency report) can be run on request.
- I can also redact or extract any specific file contents if you want a per-file report.

Generated automatically by analysis on 2026-09-02.
