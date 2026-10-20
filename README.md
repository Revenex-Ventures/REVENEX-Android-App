> Server AI and delivery update: [security architecture, activation and CI/CD](docs/SERVER_AI_AND_CICD.md). The supplied assistant prompt is server-only; Android sends text through Auth, and the backend derives every permission from school data. Live AI is unavailable until server secrets and migration 8 are configured. GitHub Actions builds the APK and gates optional Supabase function deployment on tests/lint/security checks.

# REVENEX SCHOOL ERP — ENTERPRISE EDITION

> **Unified Digital Operating System for Modern Educational Institutions**

Revenex School ERP is an Android school ERP prototype for principals, teachers, students and parents, built with Jetpack Compose (Material Design 3), Kotlin Coroutines and repository/data-source adapters.

Current readiness, known gaps, test evidence and the implementation roadmap are recorded in [the ERP audit](docs/ERP_AUDIT.md). Feature descriptions below include demo behavior and do not certify live integrations.

## Current demo backend migration

The default local demo remains available. **Supabase Demo** adds email/password login, PostgreSQL data with RLS, private file storage and a database notification inbox with live subscriptions. The Supabase path has no Firebase startup, billing or FCM dependency. Razorpay test checkout is optional and requires the separate server setup below. Legacy SDK code remains for comparison; it has not been deleted.

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
* Legacy Firebase login source is retained but unreachable from demo mode.

### 📊 2. Principal Dashboard & Attendance Hub
* **Real-time snapshot**: Quick overview of student counts, faculty attendance rate, and collected fees.
* **Hierarchical Attendance Registry**:
  * **Level 1**: Overall school attendance average presented in a visual, animated progress ring.
  * **Level 2**: Class-wise attendance breakdown (Class 10-A, 10-B, 9-A, etc.) dynamically calculated from actual database records.
  * **Level 3**: Student-wise percentages inside the selected class, with clickable overlays showing detailed chronological attendance logs.

### 📝 3. Faculty Marks & Evaluations Desk
* **Marks Entry registry**: Teachers select class, exam term, and subject to record marks for every student, with input limits strictly validated (`obtained <= max`).
* **Checked Papers Upload**: Allows teachers to upload scans or photos of a student's evaluated examination paper. Students/parents can open it directly from their marks sheet.
* **Answer Key Attachment**: Class-wide answers and solution booklets can be uploaded by the teacher and downloaded/viewed by all enrolled students.

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

Demo builds use checked-in public defaults from `secrets.defaults.properties` and Android's standard debug signing configuration. Configure the local Android SDK through `ANDROID_HOME` or ignored `local.properties`. Live Firebase services additionally require your project's ignored `app/google-services.json`. Keep all gateway and webhook secrets on the backend.

---

## 📈 Next verification steps

1. Apply the Supabase demo migrations, provision the four synthetic Auth accounts, then run the seed.
2. Run the local hidden-password hosted verification script and the two-device workflow checklist from `supabase/README.md`.
3. Verify live updates, private file access, offline errors and every supported role before replacing the local demo as the default.
4. Design future secure server operations with Edge Functions after the demo is verified. Payments and device push are deferred.

Firebase rule deployment and FCM setup are not part of the demo path.
