# Revenex — Combined documentation

Compiled from the 11 current workspace documents listed below. Source delivery documentation commit: ce80da2. This is a reference copy; individual source files remain authoritative.

- README.md
- app/README.md
- docs/README.md
- supabase/README.md
- docs/CURRENT_DELIVERY.md
- docs/ADMIN_UPGRADE_DELIVERY.md
- docs/ADMIN_REFERENCE_MATRIX.md
- docs/CURRENT_SCREEN_CHECKLIST.md
- docs/DEVICE_PAYMENT_ACCEPTANCE.md
- docs/SOURCE_RECONCILIATION.md
- docs/NEXT_AGENT_PROMPT.md

---

# Source: README.md

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


---

# Source: app/README.md

> Current application upgrade: read [ADMIN_UPGRADE_DELIVERY.md](../docs/ADMIN_UPGRADE_DELIVERY.md) and [CURRENT_DELIVERY.md](../docs/CURRENT_DELIVERY.md) for the implemented admin/family layouts, staff marks/publication, reviewed leave decisions, exact tested APK and current evidence. The 23 October counts below are historical.

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


---

# Source: docs/README.md

> Current 24 October upgrade: [implementation and evidence](ADMIN_UPGRADE_DELIVERY.md), [exact APK/build results](CURRENT_DELIVERY.md), [admin module matrix](ADMIN_REFERENCE_MATRIX.md), and [screen checklist](CURRENT_SCREEN_CHECKLIST.md). Staff marks/publication and administrator leave decisions are now implemented through migration 10. Older sections below describe historical deliveries.

# docs/

Project documentation and verification records. All documents here are written for developers, operators, and contributors.

## Latest assigned work and actual outcome — 23 October 2026

Start with [CURRENT_DELIVERY.md](CURRENT_DELIVERY.md), [SOURCE_RECONCILIATION.md](SOURCE_RECONCILIATION.md) and [CURRENT_SCREEN_CHECKLIST.md](CURRENT_SCREEN_CHECKLIST.md). See [changes/README.md](changes/README.md) for the previous/current comparison, implemented screen changes, Supabase/Razorpay repairs, test results and unfinished work. This supersedes older teal/black-orange summaries and historical payment 404 observations for current status.

- [IVORY_DELIVERY_VERIFICATION.md](IVORY_DELIVERY_VERIFICATION.md): tested APK, source commit, CI, actual rendered coverage and device/hosted limitations.
- [IVORY_REDESIGN_CHECKLIST.md](IVORY_REDESIGN_CHECKLIST.md): current visual implementation ledger; full nested-state acceptance is not complete.
- [RAZORPAY_STAGE_DIAGNOSIS.md](RAZORPAY_STAGE_DIAGNOSIS.md): missing migration/function diagnosis, deployed repair and corrected secret-name verification.

## Index

### 1. Current Status, Readiness & Verification (Supabase & Redesign)

| File | Description |
|---|---|
| `changes/README.md` | Current before/after summary and task status; read first |
| `IVORY_DELIVERY_VERIFICATION.md` | Current ivory/orange build, render evidence, hosted repair and limitations |
| `PROJECT_CHANGES_AND_STATUS.md` | Earlier backend migration and UI restoration history |
| `INTEGRATION_READINESS.md` | Hosted endpoints status, preflight verification, and page readiness |
| `REVENEX_REDESIGN_CHECKLIST.md` | Comprehensive screen and workflow redesign audit checklist |
| `REDESIGN_VERIFICATION.md` | Verification record and visual evidence for the redesigned design system |
| `SERVER_AI_AND_CICD.md` | Server-bounded AI architecture, prompt security, and CI/CD workflow |
| `ERP_AUDIT.md` | Deep ERP capability analysis, security boundaries, and technical audit |
| `NEXT_AGENT_PROMPT.md` | Structured handoff prompt and verification instructions |
| `ACTION_WORKFLOW_VERIFICATION.json` | Verification matrix for principal/admin enrollment and payment actions |
| `BLACK_ORANGE_BUILD_VERIFICATION.json` | Build and test verification record for theme restoration |
| `SERVER_AI_VERIFICATION.json` | Test results for Edge AI permissions and request boundaries |
| `ERP_SCREEN_INVENTORY.json` | Machine-readable inventory of all screens and routes |
| `ERP_RULES_REVIEW.json` | Security and database access rules evaluation |
| `ERP_DEPENDENCY_AUDIT.json` | Dependency vulnerabilities and licenses audit |

### 2. Core Architecture & Specifications

| File | Description |
|---|---|
| `ARCHITECTURE.md` | System architecture: MVVM-Repository pattern, data flow, component boundaries |
| `DATA_MODEL.md` | Domain model reference — all entities, fields, types, and relationships |
| `DESIGN_SYSTEM.md` | Design token reference: colors, typography, spacing, elevation, motion |
| `USER_ROLES.md` | Role-based access control matrix (Principal, Admin, Teacher, Student, Parent) |
| `FEATURES.md` | Detailed description of all implemented features by module |
| `AUTHENTICATION.md` | Authentication flows, Supabase Auth profiles, and session lifecycle |
| `FEATURE_COMPLETION_MATRIX.md` | Feature-by-feature completion status matrix |

### 3. Operations, Runbooks & Testing

| File | Description |
|---|---|
| `RUNBOOK.md` | Operational runbook: incident response, common failure modes, remediation steps |
| `ENVIRONMENT.md` | Environment configuration, public Supabase properties, and CI secrets |
| `DEPLOYMENT.md` | Build and deployment guide for Android APK and Supabase functions |
| `TESTING.md` | Testing strategy: unit tests, Robolectric tests, and Roborazzi golden snapshots |
| `TROUBLESHOOTING.md` | Common errors and resolutions for build and runtime issues |
| `PRODUCTION_READINESS.md` | Production readiness checklist and verification gates |
| `FUTURE_INTEGRATIONS.md` | Roadmap for future integrations (biometric attendance, LMS, payment capture) |

### 4. Historical Audits & Legacy Records

| File | Description |
|---|---|
| `CHANGELOG.md` | Version history and release notes |
| `CODEBASE_INVENTORY.md` | Historical inventory of source files |
| `PROJECT_AUDIT.md` | Historical code quality and test coverage audit |
| `PHASE_2_AUDIT.md` | Phase 2 audit findings |
| `PHASE_3_FINAL_REPORT.md` | Phase 3 implementation report |
| `PHASE_3_VERIFICATION_REPORT.md` | Phase 3 verification report |
| `PHASE_4_FINAL_REPORT.md` | Phase 4 implementation report |
| `FIREBASE_SETUP.md` | *(Legacy / Retired)* Archived Firebase project setup reference |
| `FIRESTORE_SCHEMA.md` | *(Legacy / Retired)* Archived Firestore schema reference |
| `SECURITY_RULES.md` | *(Legacy / Retired)* Archived Firestore and Storage rules rationale |


---

# Source: supabase/README.md

> Staff workflow update (24 October): migration `202610240010_staff_workflows.sql` is applied in the hosted demo, following existing migrations 1–9. **Do not blindly replay deployed SQL**: manual SQL Editor application does not populate CLI history. The added RPCs enforce existing marks RLS and verified administrator leave decisions. `verify_staff_workflows_rollback.sql` passed locally and against the hosted synthetic links; temporary rows were rolled back. See `../docs/ADMIN_UPGRADE_DELIVERY.md` for evidence and limits.

> Current hosted status (23 October 2026): missing migrations 6–9 were applied successfully through SQL Editor, including result-publication notifications. Do not reapply them or reseed the existing database. Checks confirmed four demo profiles, the confirmed teacher Auth account and RLS on every public table. `razorpay-payments` is deployed; the three exact payment-secret names are saved by the operator. Unsigned user/webhook probes reject with 401/400. Credential pairing, captured checkout and webhook delivery remain unverified; AI remains undeployed. See [current changes](../docs/changes/README.md) and [delivery evidence](../docs/IVORY_DELIVERY_VERIFICATION.md). Android reconciles inbox/task rows every 30 seconds; device push remains unconfigured.

> Current action-workflow update: enrollment is now supported for principal/admin through migration 7, with optional initial invoice and server-generated school notifications. Optional Razorpay TEST capture/failure notifications require migrations 6–7 and the deployed Edge Function; see [RAZORPAY_SETUP.md](RAZORPAY_SETUP.md). Older read-only fee/enrollment limitations below describe the earlier migration stage. For the reported sign-in failure, run `diagnose_setup.sql` in SQL Editor and use the exact Auth account/password, not a local demo persona. No private values should be shared.

# Revenex Supabase Backend Setup Guide

This guide walks through configuring Supabase for the Revenex Android School ERP.
All setup is performed directly through the **Supabase Dashboard** by the project operator.
Android requires only public-facing credentials (`SUPABASE_URL` and `SUPABASE_PUBLISHABLE_KEY`).
**Never share or place database passwords, service_role keys, or private secrets in Android code, build configs, or chat.**

---

## Architecture & Security Boundary

1. **Row Level Security (RLS)** is the sole authorization authority:
   - Tenant isolation is strictly enforced via composite foreign keys (`school_id`, `session_id`).
   - Android client role switching cannot elevate privileges; database profiles and JWT `sub` determine access.
   - Teachers only access their assigned sections and classes.
   - Students and parents can access only their own records and linked children.
   - Marks are hidden from students and parents until published by assigned faculty.
   - Fee records are read-only; no client-side balance modifications are allowed.
2. **Private Storage**:
   - Bucket `erp-documents` is strictly private (public = false).
   - Storage upload checks `file_metadata` manifest ownership before persisting.
   - Assignments store durable `supabase://erp-documents/...` references. The app requests a fresh 1-hour signed link only when a permitted user opens the file. Class recipients gain access through database-managed grants; removing an attachment revokes the grant. A signed link already issued remains usable until its expiry.
3. **Database-Driven Inbox**:
   - Notifications and audit logs are generated server-side via PostgreSQL triggers.
   - Clients cannot forge inbox rows or modify notification recipients.
   - Clients are granted `UPDATE(is_read)` only on their own inbox rows.

---

## Operator Setup Checklist

### Step 1: Create a Supabase Project
1. Log in to [Supabase](https://supabase.com).
2. Create a new project (e.g., `revenex-demo`).
3. Note your project URL and publishable API key (under Project Settings > API).

### Step 2: Apply Database Schema & Policies (new empty demo project only)
1. In the Supabase Dashboard, navigate to the **SQL Editor**.
2. Open `supabase/migrations/202610200001_schema_rls.sql`, copy its entire content, paste into the SQL Editor, and click **Run**.
   - This creates all ERP tables, foreign keys, private security functions, and RLS policies.
3. Open `supabase/migrations/202610200002_events_storage.sql`, copy its entire content, paste into the SQL Editor, and click **Run**.
   - This configures audit/notification triggers, Realtime publication, and private storage bucket policies.
4. Run `supabase/migrations/202610200003_document_access.sql` next, then `supabase/migrations/202610200004_integrity.sql`. These add scoped document grants, integrity constraints, independent roll-call sessions, and leave-request identity protection.
5. Run `supabase/migrations/202610200005_student_privacy.sql`. This removes duplicated fee fields from student payloads, enforces their removal on future writes, and upgrades no-op notification handling. Financial summaries come only from permitted fee records.
6. For a new project, continue in order with migrations `202610200006_razorpay_test_payments.sql`, `202610200007_action_workflows.sql`, `202610210008_assistant_security.sql`, `202610210009_result_notifications.sql`, and `202610240010_staff_workflows.sql`. These add secure TEST payment storage/settlement, enrolment/events, assistant quota schema, published-result notifications and confirmed staff operations. Schema installation alone does not deploy/activate payment or AI services.
7. Apply each numbered migration once, in order, to a dedicated demo database. Keep `erp_private` out of Data API exposed schemas. Expose `public` in Data API; no anonymous ERP grants are needed. The migration enables the intended Realtime tables in `supabase_realtime`.

### Step 3: Provision Demo Auth Accounts
Go to **Authentication > Users** in the Supabase Dashboard and click **Add User > Create User** for each of the following:

| Email | Role | Recommended Password | Notes |
|---|---|---|---|
| `principal@revenex.demo` | Principal / Administrator | Locally chosen strong password | Executive access |
| `teacher@revenex.demo` | Teacher (Class 10-A Mentor) | Locally chosen strong password | Assigned to Section 10-A |
| `student@revenex.demo` | Student (Class 10-A) | Locally chosen strong password | Enrolled as Demo Student |
| `parent@revenex.demo` | Parent | Locally chosen strong password | Linked to Demo Student |

Choose **Auto Confirm User** when creating these synthetic demo users so no email delivery service is needed. Public signup is not used by this app; profiles and roles are provisioned by the operator, never via user-editable metadata.

*Note: Choose passwords locally and provide them directly to the device user. Passwords must never be embedded in the repository.*

### Step 4: Seed Initial School Data (new demo only; never replay on the configured project)
1. In the Supabase Dashboard, navigate back to the **SQL Editor**.
2. Open `supabase/seed.sql`, paste into the SQL Editor, and click **Run**.
   - The script verifies that all 4 demo users exist in `auth.users`.
   - It links the users to `profiles`, sets up school `revenex_demo`, session `demo_2026`, section `10-A`, student `demo_student`, teacher `demo_teacher`, parent link, timetable, sample notice, and fee record.

### Step 5: Configure Android Client
1. In the root of this project, copy `supabase.local.properties.example` to `supabase.local.properties`:
   ```properties
   SUPABASE_URL=https://<your-project-id>.supabase.co
   SUPABASE_PUBLISHABLE_KEY=sb_publishable_<your-key>
   ```
2. Rebuild and launch the Android application:
   ```powershell
   .\gradlew.bat assembleDebug
   ```
3. Open the app, toggle to **Supabase Demo**, enter the email and chosen password for any of the four demo accounts, and sign in.

---

## Verification & Troubleshooting

- **Test Suite**: Run `npm ci --ignore-scripts --prefix supabase/tests`, then `npm test --prefix supabase/tests` to run the PostgreSQL/RLS checks against PGlite (111 passed in the staff-workflow regression suite). This engine supplies test doubles for Supabase Auth/Storage schemas; it does not verify those hosted services or websocket delivery.
- **Historical Android verification (earlier build, not current ivory)**: Earlier `assembleDebug`, all 82 unit tests, and `lintDebug` passed (zero lint errors, 70 warnings). An isolated API 34 emulator verified installation, local administrator access and inbox read interaction (badge 2 to 1). See `docs/PROJECT_CHANGES_AND_STATUS.md` for exact evidence and hosted limitations.
- **Unconfigured State**: If `supabase.local.properties` is missing or contains dummy keys, the app clearly alerts you that configuration is required, while preserving full functionality in **Local Demo** mode.
- **Session Caches**: Cloud source data is cleared on sign-in/logout and repository collectors clear their cloud caches. This is covered by focused Android tests when they pass; see the status notes for the current result.


## Hosted verification after setup

Run `python supabase/verify_hosted.py` from your terminal. It reads the ignored public configuration and prompts locally for each of the four demo account passwords using hidden input. Passwords/tokens are never written to the report. This verifies real password login, protected profiles and scoped reads; it does not claim to test writes, websocket delivery or Storage delivery.

Then use two devices/emulators:

1. Sign in as teacher on device A and parent on device B. Create an assignment for 10-A; confirm it appears on B without reopening the page and that B receives its own inbox row.
2. Save a roll call, switch its session slot, save again and verify both records remain after sign-out/sign-in. Simulate network loss: no success banner should appear for an unconfirmed write; retry after reconnecting.
3. Teacher/principal → Modules/More → Results → Marks & publication. Select the student, enter the term/subject/marks, and Save private draft. Verify student/parent cannot read it. Review & publish, recheck the linked family and inbox, then explicitly unpublish before correcting marks. Change term and verify separate marks. Hosted rollback-only database acceptance passed; actual Android two-account transport still requires device acceptance.
4. Attach a small PDF to a teacher assignment. Parent/student in that class should open it; a different school/session/account must be denied. Wait/re-sign links; durable references should continue working. Files are limited to 10 MB and PDF/PNG/JPEG/DOCX.
5. Mark a parent notification read. Student's copy must remain unread. Verify inbox persists after signing in again.
6. View fees. Without payment deployment, checkout must fail honestly and no fee balance may change. When enabled, test checkout must credit only after server-verified capture. No Android push permission, FCM token or Firebase configuration is needed.
7. Sign out and switch roles. No prior account's records may remain. Reopen the app and verify the local demo still works without any cloud configuration.

## Scope and limitations

- Local demo remains the default. Supabase is opt-in and is not a production migration or an import of Firebase data.
- SQL contains all nine requested roles. Android currently supports its existing principal/admin/teacher/student/parent dashboards; other roles fail sign-in clearly until dedicated routing is implemented. The seed provisions four separate accounts.
- Enrollment is supported through the principal/admin form and migration 7; Auth account provisioning and parent links remain operator-managed. Library circulation and leave approval UI writes are not enabled; data reads come from PostgreSQL and unsupported actions fail rather than mutating local samples.
- Android cannot directly credit fees. Razorpay TEST schema/function and correctly named secrets are now configured; unsigned user/webhook probes reject with 401/400. Captured checkout and webhook delivery remain unverified. AI remains undeployed. Device push is not configured. See RAZORPAY_SETUP.md and ../docs/SOURCE_RECONCILIATION.md.
- Auth sessions stay in memory and can refresh while the app is running; process restart requires sign-in. No offline write queue is provided.
- Live flows currently load the demo dataset into memory without pagination. Test reconnect/access changes explicitly; this design is for the small demo, not production scale. Re-sign in after changing roles/class membership from the dashboard.
- Some existing UI headings describe the earlier demo cycle. Loaded record collections and financial balances in cloud mode come from Supabase; unsupported GPA/rank values are labelled unavailable.
- Firebase SDKs, adapters, FCM service, deployment configuration, rules and legacy Functions package have been removed. Supabase and local demo remain; optional Razorpay checkout still requires server activation.
- A failed file upload can leave an unused metadata row; an uploaded file whose subsequent assignment save fails can be an orphan. Review/delete these through the project operator; no broad client delete policy is granted.
- Cloud RLS, file service behavior and Realtime must still be verified against your hosted project. A successful Android build or local PostgreSQL suite alone does not establish that.

## Sources used

- [Supabase Kotlin installation and Android compatibility](https://supabase.com/docs/reference/kotlin/installing)
- [Kotlin password login](https://supabase.com/docs/reference/kotlin/auth-signinwithpassword)
- [Kotlin live table flows](https://supabase.com/docs/reference/kotlin/stream)
- [PostgreSQL row-level security](https://supabase.com/docs/guides/database/postgres/row-level-security)
- [Private Storage access control](https://supabase.com/docs/guides/storage/security/access-control)
- [Realtime Postgres Changes](https://supabase.com/docs/guides/realtime/postgres-changes)


---

# Source: docs/CURRENT_DELIVERY.md

# Current implementation delivery — 24 October 2026

Application/test source: **c79f4ae51e12b5e954b09f15f1afd7a2108d3a1c**, branch `codex/revenex-reference-redesign`, draft PR #11. [Successful CI 40017483953](https://github.com/Revenex-Ventures/REVENEX-Android-App/actions/runs/40017483953). The actual PR merge artifact is `b11e20782fdeec7c4c7c1bb947cbca0c9a919015`. Later documentation-only commits do not change this APK's source.

## Tested APK and reports

- Exact APK: `D:/VS Code/Revenex Android/app/build/admin-upgrade-delivery/apk/app-debug.apk`.
- Size: **25,913,832 bytes**.
- SHA-256: **8A342532C1C5796B799D4B846C9DAF51D4EF1BC08ECD7439D6B757F7434139A0**.
- This is the downloaded CI debug artifact, without re-signing or patching. DEX checks confirmed the new workspaces, marks/publication, faculty setup and family attendance markers. Debug signing may conflict with another installed build; preserve user data rather than uninstalling just to bypass a conflict.
- Parsed **26 JUnit suites: 97 tests, 0 failures, 0 errors, 0 skipped**. Lint **0 errors / 69 warnings**.
- Server CI: **111 PostgreSQL/PGlite permission/workflow checks**, **31 AI/payment handler tests**, Deno entrypoint/type/lint, dependency audit and credential/history scans passed. Handler payment dependencies are mocked; no paid model call or captured payment is implied.
- Firebase dependency and APK private-configuration/server-prompt boundary gates passed. Automatic deployment was **skipped** by its branch/environment gate; migration 10 was applied manually as recorded below.
- **90 rendered PNGs** in `app/build/admin-upgrade-delivery/verification/reports/redesign/`. Final light/dark nine-experience galleries, full-size admin/assignment screens, narrow large-font directory and representative staff review/family empty states were inspected. These are actual Compose/Robolectric renders with synthetic local data, not device captures.
- Galleries: `app/build/admin-upgrade-delivery/light-experiences.png`, `dark-experiences.png`; actual previous/current comparisons: `before-after-light.png`, `before-after-dark.png`. Admin phone/tablet, marks forms and narrow states are in the matching verification folder. The previous images in comparisons are from the c29ecd2 delivery, not the user's physical phone.

CI command:
```sh
./gradlew '-Dorg.gradle.jvmargs=-Xmx3g -Dfile.encoding=UTF-8' --no-daemon --no-configuration-cache --no-parallel --max-workers=2 -Proborazzi.test.record=true assembleDebug testDebugUnitTest lintDebug verifyNoFirebaseDependencies --console=plain
```
Local commands also run: focused directory tests and Kotlin compile passed; `npm test --prefix supabase/tests` passed 111; `node --test supabase/tests/assistant-edge.test.mjs supabase/tests/razorpay-edge.test.mjs` passed 31. A later slow local Android render run was interrupted and is not counted as a pass.


Read [implemented changes and hosted evidence](ADMIN_UPGRADE_DELIVERY.md), [every reachable route/state checklist](CURRENT_SCREEN_CHECKLIST.md), [admin module matrix](ADMIN_REFERENCE_MATRIX.md), and [device payment steps](DEVICE_PAYMENT_ACCEPTANCE.md).

The app now includes the reference-led admin work queue, categorized searchable directory, shared role-aware command palette, compact family dashboard, searchable attendance with no fabricated family drafts, filtered assignments and inbox, fee balance/history, a reachable staff marks editor/publication flow and server-confirmed administrator leave decisions. Existing reports, attachments, admission, navigation, appearance and Supabase connections remain. Cloud faculty setup and library circulation explain their actual limits.

Migration 10 is deployed; all public tables retain RLS. Hosted rollback-only database acceptance passed private drafts, linked-family publication, replay safety and independent inbox reads. Temporary marks and notification counts were both zero afterwards. This does not replace Android/Auth/Realtime acceptance.

Payment audit: **one order, zero paid orders, zero receipts**. The payment service rejects unsigned callers; the historical 404 is repaired. No captured TEST payment, real webhook replay or device receipt/balance/principal-inbox acceptance has been established. No callback alone credits fees.

No Android device was connected during this pass. Physical keyboard/gesture/rotation/TalkBack and appearance process-restart/System changes remain device acceptance. In-app notifications are distinct from unconfigured device push. Hosted AI remains undeployed. See the admin matrix for unsupported product modules rather than treating directory breadth as completed workflows.

## Hosted proof and next device step

Hosted evidence files: `app/build/reports/admin-upgrade/migration10-success.png`, `staff-rpcs-ready.png`, `hosted-staff-acceptance.png`, `rollback-clean.png`, `payment-counts.png`. Migration 10 installed the three staff RPCs; the rollback script passed and both temporary-row counts were zero afterwards. No previous migration, seed or school record was reset.

On the device: Supabase Demo → sign in with the existing synthetic student/parent → Home **Fee accounts** → Pending → **Pay** → review TEST mode → complete the provider's official mock Success flow → return in the same account → **Check payment status**. Verify receipt/balance/Paid history, then principal inbox. Provider webhook capture/replay and cancellation/failure acceptance still require device/operator interaction. No device was attached for this run.

Optional hosted administrator login needs an operator-created Auth identity and a protected ADMIN profile for the existing school/session; the Android client does not provision or promote accounts. Local Demo has a distinct School Administrator entry.

## Repository handoff

The final implementation is pushed to the existing draft PR; no main merge was performed. Unrelated local README edits were preserved. Windows retained a lock from a hung staging process; its process was interrupted and an isolated Git index (`.git/revenex-delivery-index`) was used. The normal index is synchronized to committed work after backing it up; no working files were reset. If the OS still holds `.git/index.lock`, close/restart the host before normal staging rather than deleting an active process's lock. The temporary index remains a safe task-specific staging fallback.


---

# Source: docs/ADMIN_UPGRADE_DELIVERY.md

# Revenex admin and connected-workflow upgrade — 24 October 2026

This continues the existing Kotlin/Compose/Supabase app. It replaces neither the framework nor the school database. Read the admin reference matrix for the distinction between implemented workspaces and requested future modules.

## Implemented changes

- Replaced the generic admin control-room panel and tile wall with a dated operational workspace: scoped directory counts, enrolment/faculty/notice actions, unpaid invoices, pending leave, today's recorded attendance exceptions, module links and audit activity.
- Added a searchable directory grouped into People, Academics, Attendance, Finance, Operations and Communication/account. Phone rows adapt into tablet columns. The command palette now uses the same role-aware destination registry instead of offering every role staff shortcuts.
- Replaced the family dashboard's large identity/rank/GPA panel with a concise student identity and attendance/task metrics. Linked-child selection appears for parents with multiple available children. Fee access and existing report/profile navigation remain available; assignment status now reflects the assignment rather than always saying pending.
- Added attendance name/roll/admission search without silently limiting the saved register to filtered rows. ISO database dates now compare correctly with localized register dates; historical records remain view-only; family accounts see only saved registers, never staff default-present drafts. Absent history is not labelled today's register. Session controls retain accessible targets.
- Added assignment subject/pending filters and expandable instructions, preserving documents and student submission actions.
- Rebuilt fee hierarchy around the outstanding balance, exact INR values, invoices and recorded payment history. Checkout/status/receipt use the existing secure path. No invoice is paid from a client callback alone.
- Replaced repeated notification cards with compact divided inbox rows, category/unread filters and confirmed Mark all read. The UI explains that links currently open the related workspace, not a specific record.
- Added a reachable staff marks workspace through Results in the module directory. Staff can save private marks, review/publish a term, explicitly unpublish before correcting it, and still open existing report-card/student detail screens.
- Added confirmed administrator leave decisions with review/remark, pending/error feedback and server recipient notifications.
- Added shared orange filter selection and optional heading insets for alignment. Cloud library clearly presents its catalogue boundary rather than invoking unsupported circulation writes. Local catalogue controls remain available.

Existing persisted Light/Dark/System appearance, default Light, coordinated dark surfaces, system/keyboard inset handling, reduced-motion helpers, private attachments and Supabase authorization are retained. This increment adds no library, Firebase dependency, paid service or background push integration.

## Backend and hosted evidence

Migration `202610240010_staff_workflows.sql` was applied once after checking hosted prerequisites. Earlier migrations were not replayed. It adds RLS-respecting draft-save/publication operations and a server-authorized, school/session-scoped administrator leave decision. Existing table permissions and RLS remain intact.

Hosted checks: all three new RPCs present; zero public tables without RLS. The rollback-only acceptance script passed draft privacy, linked-family publication, repeated-publication safety and separate parent/student read state. Follow-up counts found zero temporary marks and zero temporary notifications. This is actual hosted PostgreSQL role emulation, **not** a two-device Auth/Realtime acceptance test.

`supabase/verify_staff_workflows_rollback.sql` contains the reproducible operator test. It uses only existing synthetic links, rolls temporary writes back and must never be changed to COMMIT. Do not replay migration 10 after this deployment; the SQL Editor does not automatically update CLI migration history.

Payment audit remains: one synthetic order, zero paid orders, zero receipts. Public reachability: Auth 200; protected student/fee/payment tables 401; unsigned payment function 401; AI function 404. These rejection/availability checks do not prove captured payment or working model integration.

## Verification ledger

- Local directory tests passed; Kotlin compilation passed. A later local render run was interrupted after prolonged host resource pressure and is not counted as passed.
- Local PostgreSQL/PGlite regression suite: 111 checks passed.
- Local AI/payment handlers: 31 tests passed with mocked provider dependencies.
- Full CI at a115e16: successful build/test/lint and security gates. The earlier f657f7a run built the APK but failed one new UI test because repository collectors needed Robolectric main-loop advancement; synchronization was corrected without removing assertions. The rendered results also exposed an admin text-encoding defect, which was repaired.
- Final CI 40017483953 passed c79f4ae: 97 Android tests, zero failures/errors/skips; lint 0 errors/69 warnings; 90 rendered PNGs. The exact APK/hash and inspected galleries are recorded in CURRENT_DELIVERY.md. The later ambiguous palette test selector was fixed without removing assertions. Source implementation is unchanged after this delivery apart from documentation.

## Remaining boundaries and device acceptance

- No connected Android device was available (`adb devices -l` returned no devices). Physical keyboard, gestures, rotation, TalkBack, true process restart/System-theme switching and two-account reconnect behavior remain device acceptance work.
- A real synthetic Razorpay TEST success must be completed by the device user. Use the same payer account for status lookup. Capture, receipt/balance persistence, principal inbox and actual provider webhook replay remain unverified. Follow `DEVICE_PAYMENT_ACCEPTANCE.md`; do not send private payment details or keys.
- Cloud faculty/account provisioning, guardian relinking, timetable allocation editing, fee structures/concessions/offline collection, official certificates and library circulation remain unsupported. Hardware/provider integrations and ambiguous vendor modules are inventoried in `ADMIN_REFERENCE_MATRIX.md`; they are not operational features.
- Admin-role Android rendering is covered with synthetic local data. A hosted administrator identity still requires trusted operator provisioning; UI persona switching cannot grant cloud access.
- Some reports retain explicitly illustrative charts. Notification navigation is category-level. AI remains undeployed. None of these is described as verified production functionality.

## Source guidance

The supplied nine-screen Revenex concept governs the visual hierarchy; the legacy ERP screenshot supplies the functional inventory. Earlier education/dashboard/task/Shifty references inform compact surfaces, metadata and category navigation without copying their branding. Existing Compose Material 3 components and semantic tokens implement the design; no framework/library was replaced.

Current payment guidance was checked against [Razorpay's official Android integration and test flow](https://razorpay.com/docs/payments/payment-gateway/android-integration/standard/integration-steps/): preload early, create orders on the server, verify signatures/provider status, and require capture before fulfillment. Its listed Netbanking test flow uses a mock Success/Failure page. The app retains its pinned SDK for a reproducible build; no speculative SDK upgrade was used to claim a latency fix.

## Changed implementation files

- MainActivity: cloud faculty setup boundary; existing payment and role routing preserved.
- ui/components: CommonComponents (heading insets/orange filters), CommandPalette (shared role-aware destinations).
- ui/screens/dashboard: AdminDashboardScreen, StudentDashboardScreen, PrincipalDashboardScreen (alignment).
- ui/screens/academics: AcademicsScreens, ReportCardScreen, new StaffMarksWorkspace.
- ui/screens/attendance: AttendanceScreen, new AttendanceDates, PrincipalAttendanceScreen (alignment).
- ui/screens: FeeManagementScreen, CommunicationScreens, OperationsScreens/new WorkspaceDirectory, TeacherDetailScreen (alignment).
- data: ErpDataRepository confirmed operations; SupabaseDataSource staff RPC/read-all calls; LocalDemoDataSource draft lifecycle and attachment preservation.
- Tests: new AdminWorkflowUiTest, WorkspaceDirectoryTest, AttendanceDateTest; strengthened existing ErpProductionUnitTest.
- Supabase: additive migration 10, expanded rls.test.mjs and rollback-only hosted acceptance script. Existing migrations/data were not rewritten.

The delivery folder contains full-size images, 97-test XML evidence and the lint report. Images are generated verification artifacts and are not committed as application assets.


---

# Source: docs/ADMIN_REFERENCE_MATRIX.md

# Admin reference implementation matrix — 24 October 2026

Starting source: 419f352. This is a live checklist, not a claim of complete ERP coverage.

## Reference inventory and translation

The approved nine-screen board sets typography, compact white/ivory surfaces, orange actions, pastel category wells, register rows, invoice lists and inbox hierarchy. Its displayed names, dates, balances and counts are synthetic examples; they must not become production facts. The web screenshot supplies functional breadth, not its crowded navigation.

Visible web menu labels: Home, Admissions, Notification Center, Attendance, Reports, HRM, Expense, Academics, Timetable, Inventory, Counselling, Fees, Scholarship, Transport, Gallery, Settings, DOC, Counter Billing, Support, Website, Class Test, New CCE, Nirgam Document, Event Management, Library, Tally, Calendar, Voucher, Assignments, Hostel, Changelog, Question Bank Easy DB, Campaign Fees, Help, PTA, Our Products, Principal Dashboard, Admin Dashboard, Doc Repository, Update Password, Staff Shifts, Dynamic Form, LMS Admin, Quiz, New Nirgam Document, Question Bank Easy, School Info, Logout. Top controls: module/student/staff/name/ID search, Profile, Admission, Fees, Next Year Fees. Side controls: callback, support, sync and chat.

Attendance reference controls: Attendance, Machine Attendance, Holiday Record, Weekly Holiday, Leave Management, Machine Status, RFID Punch Report tabs; App/Web/Email/SMS destinations; academic year, date, class, division and sort selectors; student search, select-all-absent, roll/name/gender register; calendar with taken/not-taken/holiday/weekend legend; attendance report/settings/help links. A label alone does not establish a business rule or available integration.

## Implementation / operational boundaries

| Area | Current connected scope | Remaining boundary |
|---|---|---|
| Navigation | Searchable role-filtered directory in six groups; inbox, scoped search, profile, ordinary destinations and context actions | UI visibility is not authorization; backend policies remain authoritative |
| Admin home | Scoped students/faculty, outstanding invoices, pending leave, today's recorded registers, activity, working shortcuts | No invented admission queue or predicted attendance percentage |
| Admissions / people | Existing enrolment RPC, duplicate validation, student directory/detail, class allocation on admission; cloud faculty setup now explains operator provisioning before opening an unsupported local form | Auth provisioning and guardian relinking require trusted operator workflow; cloud faculty writes unsupported |
| Academics | Existing timetable, filtered assignments/submissions, resources, examinations, staff marks workspace and report views | No timetable allocation editor, scholarship, class-test bank or question-bank workflow |
| Marks | Existing RLS and source read/write/publication methods | Results now exposes private save/review/publication/unpublication; migration 10 and hosted rollback-only permission/replay/read-isolation acceptance passed. Android end-to-end/Auth/Realtime transport still needs device acceptance |
| Attendance | Class/date/session register, bulk actions, confirmed save, loaded historical review and overview | Historical correction, staff register, configurable holidays and device ingestion not implemented |
| Fees | Invoice balance/list, TEST checkout, server capture/signature validation, status lookup and receipt UI | Real test capture/receipt acceptance pending; no structures, next-year fee configuration, concessions, expenses/vouchers or offline collection workflow |
| Communication | Notices, persistent recipient inbox, read state; Realtime plus 30-second reconciliation | Real two-device cross-account delivery/reconnect still needs acceptance; no background push/SMS/email configured |
| Operations | Library catalogue, transport routes, inventory, operational summaries, profile/appearance | Some mutations remain unsupported in cloud mode; do not treat catalogue buttons as proof of circulation |
| Documents / certificates | Existing authorized attachments and profile/report document views | Official certificate issuance, general repository and dynamic forms not implemented |
| Integration boundary | Expandable admin directory explanation for hardware/vendor services | RFID/machine attendance, Tally, payroll/staff shifts, website/gallery management require separate design/provider setup |
| Unspecified vendor modules | Names inventoried above | DOC, New CCE, Nirgam variants, Campaign Fees, Question Bank Easy/DB, Our Products require product clarification; no fabricated destinations |
| Additional school modules | Not represented as operational modules | Hostel, counselling, PTA, LMS/quizzes, scholarships and event/calendar editing require defined workflows and permissions |

## Verification ledger for this increment

- [x] Focused directory role/search tests; full final Android suite: 97 passed.
- [x] Admin and directory rendered in both palettes, 800dp tablet and 320dp/1.5-font interaction; final images inspected.
- [x] CI 40017483953: build/tests/lint/security passed; 111 database checks and 31 handler tests passed.
- [x] Exact c79f4ae APK path, SHA-256 and merge artifact recorded in CURRENT_DELIVERY.md.
- [ ] Hosted captured TEST payment, persisted receipt/balance/inbox and duplicate replay.

Older delivery artifacts and their test counts remain historical evidence. They do not verify this increment.


---

# Source: docs/CURRENT_SCREEN_CHECKLIST.md

# Current route and state checklist

Current admin/workflow upgrade continues from 419f352. See ADMIN_UPGRADE_DELIVERY.md and ADMIN_REFERENCE_MATRIX.md for current implementation and operational boundaries. The earlier delivery at c29ecd2 is historical evidence. Source truth: NavDestination and MainActivity. Shared theme is ivory/orange with coordinated dark mode. No backend reset or framework replacement.

Reference mapping: education triptych → surfaces/settings/selection; Android dashboard → compact hierarchy/metrics; task app → assignment/document metadata; Shifty → shortcuts/calendar/task progression. No promotional sections or reference branding copied.

| Reachable screen/group | Current implementation and coverage target |
|---|---|
| Authentication / loading | Existing compact login, explicit local/cloud mode, honest sign-in errors; light/dark entry captures. Loading is a transient shell state, not backend success. |
| Principal / admin dashboards | Principal overview retains shared compact components. Admin Home now uses a dated work queue, scoped counts, outstanding invoices, pending leave, today's register exceptions and activity. Light/dark + 800dp admin capture targets. |
| Teacher dashboard | Existing task-first layout with shared semantic components; light/dark captures. |
| Student **and parent** Home | Production shares StudentDashboardScreen. Compact identity, attendance/task metrics, accurate assignment status and direct Fee accounts action. Multiple linked children can be selected. Production still shares the family screen; no separate parent route is claimed. |
| Student list / detail / admission | Grouped profile data; admission fields now stack at narrow widths/large fonts; shared accessible dialog heading, safe-drawing and IME insets. |
| Teacher list / detail / profile / faculty form | Existing semantic views; faculty form now shares responsive groups and dialog treatment. Cloud faculty mutation remains unsupported. |
| Attendance / principal overview | Date/class/session register, current-day editing, view-only historical records, persistent save feedback; entry captures in both palettes. Date picker/individual status sheets still require physical-device coverage. |
| Timetable / Scores | Bulky header replaced with compact heading, shorter 48dp tabs; explicit empty scores. Timetable Scores remains read-only. Results in the directory now reaches the staff marks editor, reviewed publication/unpublication and existing report views. RPC/RLS enforces permissions. |
| Assignments / create form | Subject/status/deadline cards retained. Responsive subject/date/class groups, readable heading and current-date default; create-form captures in both palettes. |
| Study materials / examinations | Focused headings and explicit empty states added; dates/rooms retain existing data. |
| Results / personal details / certificates | Existing shared surfaces and cloud read-only details. Published-data restrictions preserved; unsupported cloud certificates remain honest. Entry captured; all nested report/certificate states are not independently verified. |
| Fees / invoice / lookup / pending / error / receipt | Invoice actions renamed **Pay**; stacked action layout, semantic test notice, no pre-payment verified-receipt claim, wrapping receipt rows. Status reconciliation now opens the dialog even after checkout closes. UI tests cover no-charge lookup and fixture-confirmed receipt transition. |
| Notices / audit / compose | Compact heading/action, accessible tabs/category chips, explicit empty notices. Publication failure/retry retains stable ID. Narrow 1.5-font form interaction and palette captures. |
| Inbox | Divided rows, category/unread filters, time/opening state, confirmed individual/all-read operations. Related-workspace routing is explicitly disclosed; record-specific navigation is not claimed. Entry captures and prior read-interaction tests; actual recipient/device delivery remains separate. |
| Modules / leave / transport / library / inventory | Grouped searchable/adaptive directory and shared role-aware command palette. Leave decisions now use reviewed server-confirmed RPCs with recipient updates. Cloud library is an explicit catalogue-only boundary; local circulation remains a preview. Operations have explicit empty states. |
| Search / AI | Existing semantic layouts, keyboard-aware input and honest unavailable server AI. Entry captures. No claim of live model deployment. |
| Reports | Current data totals and explicitly illustrative charts; no invented export. Entry captures; physical-device chart accessibility remains unverified. |
| Profile / settings | Persisted mutually exclusive Light/Dark/System, default Light. Existing appearance logic tests; light/dark captures. Device process restart/System configuration changes still require acceptance. |

## Verification rules

- Every batched capture must wait for Compose idle; nested capture tests assert the intended state before taking the image. Earlier local captures caught showing the preceding dialog are superseded.
- Screenshot fixtures use synthetic local data and simulated UI states. A receipt fixture is not a real Razorpay payment.
- Renders do not establish keyboard-open, TalkBack, system gestures, orientation or all physical devices. List those as incomplete until exercised.
- Full latest APK/test/lint/render results are recorded after CI completion; earlier 81/82 counts are not silently reused.

## Previous c29ecd2 evidence (historical, superseded by the new delivery report)

- [x] Final c29ecd2 Android build, 85 tests and lint 0 errors/69 warnings passed.
- [x] 82 synchronized Compose images generated; required role/workspace light/dark galleries and representative nested forms/receipt inspected.
- [x] Dialog root bounds checked against viewport without suppressing the assertion.
- [x] Hosted read-only audit confirms one created/provider-bound order, zero paid orders and zero receipts.
- [ ] Complete signed-in captured test payment, persisted receipt/balance/inbox and actual callback/webhook replay.
- [ ] Complete physical-device keyboard/gesture/rotation/TalkBack and all nested report/certificate checks.
- [x] Subsequent upgrade: implemented staff Results editor and publication flow through migration 10; see ADMIN_UPGRADE_DELIVERY.md for current tests and hosted database acceptance.

See CURRENT_DELIVERY.md for exact artifact/hash and DEVICE_PAYMENT_ACCEPTANCE.md for user/operator steps.

## Current upgrade verification — c79f4ae

- [x] Final Android build; 97 tests, no failures/errors/skips; lint 0 errors/69 warnings.
- [x] 90 Compose renders, including role light/dark entry screens, 23 secondary pairs, nested forms/payment fixtures, marks save/review, family no-record attendance, tablet and narrow 1.5-font directory.
- [x] Actual final galleries/admin/assignment/narrow states visually inspected; previous/current comparison images generated from actual renders.
- [x] New directory/command role scope, marks draft→review→publication, preserved report navigation, compact assignment expansion, read-only family register and ISO/display-date regression tests.
- [x] Migration 10 applied once; hosted rollback-only draft/publication/replay/recipient-read checks passed; no temporary marks or notifications retained.
- [ ] Physical device gestures/keyboard/rotation/TalkBack and process-restart/System appearance acceptance.
- [ ] Real two-account Android/Auth/Realtime reconnect and logout/cache acceptance.
- [ ] Captured synthetic Razorpay payment, persisted receipt/balance/principal inbox and provider webhook replay.
- [ ] Unsupported cloud faculty/guardian provisioning, timetable editing, fee configuration/offline collection, official certificates and library circulation remain product/operator boundaries as listed in ADMIN_REFERENCE_MATRIX.md. No empty destinations or success claims were added for them.


---

# Source: docs/DEVICE_PAYMENT_ACCEPTANCE.md

> 24 October audit: one synthetic order, zero paid orders, zero receipts. The app now has a balance summary and recorded payment history; a client success screen is still insufficient evidence of capture. No Android device is currently connected. The steps below require the payer's device interaction.

# Device Razorpay TEST checkout and receipt verification

No real payment details, private keys, OTPs, tokens or passwords should be sent in chat. Use an existing synthetic Supabase parent/student account and an existing outstanding demo invoice. Do not reset/reseed or alter paid fees to repeat a test.

## What is established so far

- Operator saved all three exact server-secret names. Unsigned user/webhook probes reject with 401/400.
- User reports reaching Razorpay, with slow payment-method loading and no receipt.
- Read-only hosted audit on 23 October 2026: **1 order, 0 creating, 1 created, 0 paid, 0 receipts, 0 paid-without-receipt, 0 duplicate payments**. This establishes order persistence, not captured payment. Duplicate counts with no receipts do not prove hosted replay handling.
- The app had a separate display bug: Fees → Check payment status called reconciliation without reopening the dialog. Fixed in 6830b38. A confirmed result can now display its receipt even after checkout closes.
- Preload previously ran immediately before Checkout.open. It now starts for a signed-in cloud payment-capable account before the user pays. The pinned SDK version remains unchanged. This may reduce resource-loading delay; performance is not yet measured on the user's device.

## Steps requiring the device user

1. Install the latest verified APK listed in the delivery report; retain data if signatures match. Sign in via **Supabase Demo** with the synthetic student or parent account.
2. Home → **Fee accounts · invoices & receipts**. Older APK: Resources/Notes → Fee Accounts. Choose **Pending** and note the invoice balance locally.
3. Tap **Pay**. Confirm the review identifies TEST mode and the expected invoice amount. Payment-method details are handled by Razorpay, not Revenex.
4. Use the provider's current [Android test-integration instructions](https://razorpay.com/docs/payments/payment-gateway/android-integration/standard/integration-steps/). If Netbanking is listed, choose a listed test bank and complete the mock **Success** page yourself; test mode does not send you to the real bank login. Use only official test details for other supported methods. Do not use real card/UPI credentials. Method availability remains provider-controlled.
5. Return to Revenex in the **same account that started checkout**. Wait for server verification. Only a captured/verified server response may display the receipt. If pending/interrupted, use **Check payment status**; do not immediately start another charge. The current status lookup filters orders by the signed-in payer; switching to another linked family account does not transfer that order.
6. Confirm a receipt number, payment ID and amount are displayed. Confirm the balance changes and the invoice moves from Pending to Paid. Reopen Paid → View receipt/status, then sign out/in and confirm persistence.
7. Sign in as principal: check the matching student's payment inbox entry. Parent/student copies should have independent read states. Report only sanitized status/error text if something fails.

## Server/operator checks

Run `supabase/payment_acceptance_readonly.sql` in SQL Editor to inspect aggregate persisted counts. After a successful full payment, paid and receipt counts should increase together; paid-without-receipt and duplicate-payment counts should remain zero. This query does not verify the provider's signature/capture independently and does not settle fees.

In the **Razorpay test dashboard**, verify the payment is captured and the configured payment.captured/payment.failed webhook points to:

`https://nhaxnuaxyqkgnlkwpqkw.supabase.co/functions/v1/razorpay-payments/webhook`

The webhook secret must match the backend secret; enter it only through provider/Supabase secret interfaces. Actual webhook delivery has not been inspected. Once a captured test event exists, resend that same event through the provider dashboard, then rerun the read-only audit: receipt, fee credit and capture notification must not duplicate. Callback plus webhook replay is covered locally, not yet established in the hosted project.

Cancel/failure checks require another existing unpaid synthetic invoice. Cancellation must not fabricate a permanent failed-payment event; provider-confirmed failure may create a scoped inbox entry without crediting fees. Never delete an order to bypass uncertain capture or reconciliation.

## First failing stage guide

| Observed state | Stage to inspect |
|---|---|
| No Pay/review screen | UI role/navigation/guard |
| Sign-in/access/configuration error | Authenticated order request, ownership or deployment/secrets |
| Order stuck creating | Provider request/bind uncertainty; reconcile server-side before retrying |
| Created order, slow checkout methods | Checkout resource/provider/network stage; does not imply paid |
| Checkout success but no receipt | Callback matching, signature, captured provider status, settlement, then dialog display |
| Receipt exists but stale invoice | Authorized ledger refresh/reconnect |
| Receipt exists but no school inbox | Recipient-scoped notification transaction/delivery |

Never mark the fee paid solely from the SDK callback. Report exact sanitized errors, whether checkout returned, and whether Check payment status produces a receipt; retain all private values locally.

Provider preload guidance: [official Android sample](https://github.com/razorpay/razorpay-android-sample-app/blob/master/app/src/main/java/com/razorpay/sampleapp/java/PaymentActivity.java).


---

# Source: docs/SOURCE_RECONCILIATION.md

# Source and evidence reconciliation — 24 October 2026

This upgrade started at 419f352 and preserves earlier work and unrelated README edits. Read CURRENT_DELIVERY.md for the exact delivered source, artifact and current CI totals.

| Conflicting claim | Current source and actual evidence |
|---|---|
| 81 versus 82 versus 85 tests | These were separate historical Android builds. c29ecd2 had 85 passing tests and 82 images; image count is not test count. The staff-workflow upgrade adds tests; use the parsed final XML totals in CURRENT_DELIVERY.md rather than reusing an old number. |
| Payment 404 versus deployed | The initial 404 was repaired by deployed schema/functions. Current protected tables/payment endpoint reject unsigned access with 401; synthetic audit still has one order, zero paid orders and zero receipts. Deployment does not establish capture. |
| Live subscriptions versus 30-second updates | Both exist: selectAsFlow collectors with retry and periodic reconciliation for inbox/assignments/attendance/marks/notices/fees. Device websocket timing and reconnect behavior still need acceptance. |
| Missing marks editor | Superseded. Results now exposes StaffMarksWorkspace to principal/admin/teacher, with private save, review/publish/unpublish and preserved report views. Migration 10 is deployed. Hosted rollback-only draft/publication/replay/recipient-isolation checks passed. Timetable Scores itself remains read-only. |
| Leave approval available in cloud | Previously threw unsupported. Reviewed administrator decisions now use decide_leave, which verifies the protected profile's school/session/role, serializes the decision and sends one recipient update. Local permission/replay tests pass; actual device approval still needs acceptance. |
| Five distinct dashboard layouts | Five Android identities, four distinct production layouts: principal, admin, teacher and shared student/parent Home. The separate ParentDashboardScreen is not production-routed. Child selection is available for multiple authorized children; RLS defines their scope. |
| Attendance present before saving | Staff default-present rows are an explicitly unsaved draft. Family read-only views now render only saved registers; absent records show a no-register state and no staff save/bulk controls. |
| Build success proves live workflows | It does not. Compose fixtures and local SQL/provider tests are distinguished from actual hosted database role tests, Auth/Realtime transport and physical device/provider capture. No captured payment or background push is claimed. |
| All reference modules are active | The directory contains real destinations. Hardware/vendor and unimplemented ordinary modules are explicitly recorded in ADMIN_REFERENCE_MATRIX.md, not fabricated as operational screens. |

Migration 10 was applied once after checking existing schema. No reset, seed replay, older-migration replay or RLS weakening occurred. Private credentials remain server-side. Supabase SQL Editor application does not populate CLI migration history.


---

# Source: docs/NEXT_AGENT_PROMPT.md

# Continuation prompt — current Revenex source

Continue the existing Kotlin/Jetpack Compose Revenex ERP at `D:/VS Code/Revenex Android`. Read AGENTS.md, CURRENT_DELIVERY.md, ADMIN_UPGRADE_DELIVERY.md, CURRENT_SCREEN_CHECKLIST.md and ADMIN_REFERENCE_MATRIX.md. Preserve unrelated local README edits and existing Supabase data. Never reset/reseed, blindly replay migrations, weaken RLS, or substitute UI role selection for authorization.

The approved design is ivory/white, charcoal and orange with restrained semantic pastels, compact readable layouts, adaptive navigation and persisted Light/Dark/System (default Light). Keep this coherent identity. The parent and student production Home share StudentDashboardScreen; parents with multiple accessible children have a selector. Staff Results now reaches StaffMarksWorkspace, with report views still accessible. Do not repeat the obsolete claim that the marks editor is missing.

Implemented in the current upgrade: grouped/searchable role-aware directory, shared command-palette destinations, operational admin work queues, compact family Home, assignment filters/details, attendance search and explicit read-only no-record states, fee balance/history, compact filtered inbox and confirmed all-read, staff draft/review/publication/unpublication, reviewed administrator leave decisions and honest cloud catalogue/faculty boundaries.

Supabase migrations 1–10 are present in the hosted demo; 6–10 were applied manually through SQL Editor. CLI migration history is not automatically synchronized. Migration 10 adds save_draft_mark, set_result_publication and decide_leave. Marks operations respect existing RLS; leave decisions derive the administrator role and school/session from profiles. No Auth provisioning is performed by the Android client. The rollback-only hosted staff script passed draft/privacy/publication/replay/recipient read-isolation checks and left no temporary records. This is hosted database role emulation, not Android Auth or Realtime transport acceptance.

Realtime subscriptions and 30-second reconciliation coexist. Preserve cancellation, account-scoped cache clearing and recipient-specific reads. Device push is unconfigured. AI backend code/prompt exists but the hosted AI function remains undeployed. Do not claim either feature is active.

Razorpay payment schema/function are deployed. Android uses only public configuration; all private credentials stay on the backend. The last aggregate audit found one order, zero paid orders and zero receipts. User reports slow method loading and no receipt. Prior UI fixes reopen status lookup and preload checkout earlier; no performance or capture success is proven. Follow DEVICE_PAYMENT_ACCEPTANCE.md with an actual synthetic signed-in payer on a device, then verify capture, receipt, balance, principal/family inbox and real webhook replay. Never settle from the Android callback alone or delete an uncertain order to retry. Do not request secrets in chat.

Current delivered source is c79f4ae; 97 Android tests, lint 0 errors/69 warnings, 111 SQL checks, 31 handler tests and 90 renders. Read CURRENT_DELIVERY.md for the exact tested APK/hash and screenshots; do not reuse older 81/82/85 counts. Local PostgreSQL checks and mocked provider tests are distinct from hosted/device verification. No connected Android device was available in the last inspection. Finish physical keyboard/gesture/rotation/large-font/TalkBack, process-restart/System appearance and two-account reconnect/account-switch acceptance when a device is supplied.

Remaining product boundaries are explicit in the admin matrix: cloud faculty/guardian/Auth provisioning, timetable allocation editing, fee configuration/concessions/offline collections, official certificates, library circulation, hardware/vendor integrations and ambiguous reference modules. Do not invent successful operations or fake destinations. Define their domain behavior and implement focused tested slices when authorized.

Git note: a Windows staging operation hung holding the original index lock. The task used the temporary `.git/revenex-delivery-index` after preserving the original index and unrelated changes. Check current process/lock state before writing Git metadata; never remove an active writer's lock or reset user edits. Use the current delivery note for any final cleanup status.

