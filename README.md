> Current upgrade: [implementation, hosted evidence and remaining limits](docs/ADMIN_UPGRADE_DELIVERY.md), [latest APK/build verification](docs/CURRENT_DELIVERY.md), [screen checklist](docs/CURRENT_SCREEN_CHECKLIST.md), and [admin module matrix](docs/ADMIN_REFERENCE_MATRIX.md). Read these before historical delivery reports. The current Kotlin/Compose app uses ivory/orange light mode, coordinated dark mode and persisted Light/Dark/System appearance.

> Supabase is the active cloud demo backend; Firebase is removed. Migration 10 adds confirmed draft marks/publication and administrator leave decisions. A hosted rollback-only permission test passed; actual two-device and captured Razorpay TEST acceptance remain separate. Payments must be server verified. AI remains undeployed; automatic deployment is gated. Existing school data was preserved.

# REVENEX SCHOOL ERP — ENTERPRISE EDITION

> **Unified Digital Operating System for Modern Educational Institutions**

Revenex School ERP is an Android school ERP prototype for principals, teachers, students and parents, built with Jetpack Compose (Material Design 3), Kotlin Coroutines and repository/data-source adapters.

Current readiness, known gaps, test evidence and the implementation roadmap are recorded in [the ERP audit](docs/ERP_AUDIT.md). Feature descriptions below include demo behavior and do not certify live integrations.

## Current demo backend migration

The default local demo remains available. **Supabase Demo** adds email/password login, PostgreSQL data with RLS, private file storage and a database notification inbox with live subscriptions. The Supabase path has no Firebase startup, billing or FCM dependency. Razorpay test checkout is optional and requires the separate server setup below. Firebase SDKs, legacy Android adapters, FCM service and the old Firebase Functions backend have been removed.

- [What changed, previous architecture, actual checks and remaining limitations](docs/PROJECT_CHANGES_AND_STATUS.md)
- [Supabase setup, SQL migrations, account provisioning and verification](supabase/README.md)
- [Public local configuration template](supabase.local.properties.example)
- [Next-agent handoff prompt](docs/NEXT_AGENT_PROMPT.md)

Use a dedicated synthetic demo project. Configure only the URL and **publishable** key locally. Do not put private keys, service-role credentials or passwords in Android, BuildConfig, Git or chat. Supabase mode does not silently fall back to sample data.

---

## 🎯 Core Goals & Design Philosophy

1. **Academic Trust & Integrity**: Ensure every evaluation metric, marks statement, and attendance registry is accurate, validated, and securely stored without client-side tampering.
2. **Strict Role-Based Access Control (RBAC)**: Ensure that users only access resources corresponding to their roles. Principals oversee the institution; Teachers record attendance and input marks; Students/Parents manage personal details, view academic files, and pay fees.
3. **Seamless Cloud Sync & Offline Fallback**: Local demo remains usable without configuration; Supabase Demo reads and writes the cloud database and displays failures honestly. It has no offline write queue or silent cloud-to-sample fallback.
4. **Unified Mobile Experience**: Supabase student and parent accounts are separate identities. RLS restricts each parent to explicitly linked children.

---

## 🚀 Current Implementation & Features

### 🔐 1. Authentication & Session Lifecycles
* Local demo: choose a sample role.
* Supabase demo: email/password, with role, school and session loaded from protected profiles. Roles cannot be chosen by the cloud client.
* Sessions are memory-only; process restart requires sign-in. Auth can refresh while running.
* Authentication uses Supabase only; local demo uses synthetic personas.

### 📊 2. Principal Dashboard & Attendance Hub
* **Real-time snapshot**: Quick overview of student counts, faculty attendance rate, and collected fees.
* **Hierarchical Attendance Registry**:
  * **Level 1**: Overall school attendance average presented in a visual, animated progress ring.
  * **Level 2**: Class-wise attendance breakdown (Class 10-A, 10-B, 9-A, etc.) dynamically calculated from actual database records.
  * **Level 3**: Student-wise percentages inside the selected class, with clickable overlays showing detailed chronological attendance logs.

### 📝 3. Results and evaluations
* Existing results/report views respect published-data permissions.
* Staff marks-entry/publication backend methods exist, but no reachable Android editor/publication control is implemented. Do not demo this as a completed entry workflow.
* Hosted attachment delivery and publication notifications require setup and acceptance tests.

### 💳 4. Demo Fee Ledger
* Fee records are read from Supabase and mapped into the existing ledger UI.
* Supabase fee balances can only be credited by the payment server after verified test capture. Optional Razorpay test checkout and status reconciliation are documented in [supabase/RAZORPAY_SETUP.md](supabase/RAZORPAY_SETUP.md). No private key belongs in Android.

### 📚 5. Homework Tracker
* **Attachments Support**: Supports attaching reference PDF/DOCX booklets to homework assignments.
* **Private Documents**: The app resolves a managed file reference on open; Storage verifies access before issuing a temporary link. Hosted delivery is part of the remaining verification checklist.

---

## 🛠️ Local Development & CLI Instructions

### Run Unit Tests
Verify core business logic, permissions, and attendance calculations:
```bash
.\gradlew.bat test
```
See [the current verification record](docs/ERP_AUDIT.md#verification-record) for executed checks and remaining blockers.

### Compile & Build Kotlin Source
Verify source code syntax compile correctness:
```bash
.\gradlew.bat compileDebugKotlin
```

### Clean & Assemble Fresh Debug APK
Build a fresh, updated debug APK reflecting current code changes:
```bash
# Clean previous build artifacts
.\gradlew.bat clean

# Assemble the fresh debug package
.\gradlew.bat assembleDebug
```
*Expected output after a successful build*: `app/build/outputs/apk/debug/app-debug.apk`.

Demo builds use checked-in public defaults from `secrets.defaults.properties` and Android's standard debug signing configuration. Configure the local Android SDK through `ANDROID_HOME` or ignored `local.properties`. Cloud demo services require project configuration in ignored `supabase.local.properties` (see `supabase.local.properties.example`). Keep all gateway and webhook secrets on the backend.

---

## Current checks and artifact

The latest tested application source is c29ecd226524ff608a6b944b18ad33660174eef0. [CI run 39982715340](https://github.com/Revenex-Ventures/REVENEX-Android-App/actions/runs/39982715340) passed **85 Android tests**, lint (0 errors / 69 warnings), Firebase removal and APK boundary gates, 105 PostgreSQL checks and 31 server handler tests. Historical 81/82 counts belong to earlier builds.

Downloaded latest APK: `app/build/final-delivery/apk/app-debug.apk`. Exact hash, final light/dark galleries and remaining device/payment blockers are in [CURRENT_DELIVERY.md](docs/CURRENT_DELIVERY.md). A standard new local Gradle build still uses `app/build/outputs/apk/debug/app-debug.apk`.

## 📈 Next verification steps

1. Existing hosted setup: four demo profiles are provisioned; the teacher Auth account is confirmed; missing migrations 6–9 have been applied. Do not rerun the seed or reset this database. SQL Editor application does not populate CLI migration history automatically.
2. Complete a signed-in synthetic student/parent Razorpay test checkout, confirm captured receipt/balance, principal and payer inbox entries, cancellation/failure behavior and webhook replay. See [payment setup](supabase/RAZORPAY_SETUP.md).
3. Run the hosted and two-device workflow checklist from [Supabase setup](supabase/README.md): assignments, draft/published marks, recipient-isolated read state, reconnect and account switching. Verify private files and honest offline errors.
4. Finish nested dialog/form/keyboard, large-font, rotation and accessibility checks on actual devices. Device push remains unconfigured; the database inbox is not background push. Live payments and production accounting remain outside the verified demo scope.

Firebase backend and SDKs have been retired; Supabase handles all cloud persistence, authentication, and security boundaries.
