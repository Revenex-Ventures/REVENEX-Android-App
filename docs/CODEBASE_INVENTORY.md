# REVENEX SCHOOL ERP — CODEBASE INVENTORY & GAP ANALYSIS

This document provides a written inventory of the existing codebase, identifying working modules, broken segments, and missing functional layers against the production-grade Indian School ERP specifications.

---

## 1. File Inventory & Structural Analysis

The workspace is organized into a single **Android application codebase**. 

### A. Core Directories and Entry Points
*   **`/app/src/main/java/com/example/MainActivity.kt`**: Client application lifecycle and payment callback.
*   **`/app/src/main/java/com/example/data/model/Models.kt`**: Client-side data models.
*   **`/app/src/main/java/com/example/data/datasource/`**:
    *   `FirebaseDataSource.kt`: Client-side Firestore SDK collection listener.
    *   `LocalDemoDataSource.kt`: In-memory static mock client.
    *   `AppEnvironment.kt`: Status configuration module.
    *   `FirebaseStorageManager.kt`: Direct attachment upload controller.
*   **`/app/src/main/java/com/example/data/repository/`**:
    *   `ErpDataRepository.kt`: Application state manager, maps flows.
    *   `SampleData.kt`: Static dataset.
*   **`/app/src/main/java/com/example/ui/components/`**: Common UI components and modals.
*   **`/app/src/main/java/com/example/ui/screens/`**: Role dashboards and operations screens.
*   **`/app/src/main/java/com/example/ui/theme/`**: Stylings, tokens, colors, and typography.
*   **`/firestore.rules` & `/storage.rules`**: Database access control scripts.

---

## 2. Broken Architecture & Structural Gaps

1.  **Backend Services & REST API**: 
    *   **Broken/Missing**: There is **no backend server codebase** (NodeJS/Go/Python/Java server) in this repository. 
    *   **Current State**: The application interacts directly with Firestore database collections. Consequently, features like **JWT Auth with refresh tokens, backend rate limiting, central error monitoring, and backend signature check webhooks** are entirely missing.
2.  **Normalized Database & Migrations**:
    *   **Broken/Missing**: There is **no SQL database or migration script** configured. The application uses Cloud Firestore (NoSQL document database) client-side.
3.  **Role-Based Access Control (RBAC)**:
    *   **Broken/Missing**: Permission checks are not validated by a backend API. While Compose UI hides buttons, Firestore rules represent the only backend validator.
    *   **Auth Vulnerability**: Only the Principal logs in via Firebase Auth. Teachers and students match ID/DOB details against database fields. Without Firebase Auth tokens, their client requests to Firestore are blocked under standard rules.
4.  **Money Math Accuracy**:
    *   **Broken/Missing**: Monetary amounts inside `FeeRecord` are declared as double floats (`Double`). Currency values are multiplied client-side to calculate paise: `(amount * 100).toInt()`. Floating-point operations can introduce rounding inaccuracies in financial ledgers.

---

## 3. Module Inventory & Gap Analysis (Phases 1–10)

### Phase 1 — Foundation
*   **What Exists**: UI login screens, local role switching options, and model entries.
*   **What is Broken**: Teachers/students log in without Firebase Authentication tokens. Forgot password flow relies on mock layouts.
*   **What is Missing**: Custom JWT auth endpoints, OTP verification gateways, custom database tables, and academic year/session scoping on records.

### Phase 2 — Students & Admissions
*   **What Exists**: Student admissions addition and deactivation popup windows.
*   **What is Missing**: Enquiry pipelines, Aadhaar/document uploads to secure folders, printable student ID card exporters, and row-level bulk CSV upload error reporting.

### Phase 3 — Fees Lifecycle
*   **What Exists**: Basic listing of fee heads and in-memory discount calculations.
*   **What is Broken**: Razorpay payments are confirmed immediately based on the client SDK callback; no server-side signature checks, webhooks, or idempotency checks exist.
*   **What is Missing**: Installment scheduling, dynamic late fee configurations, ledger DAYBOOK reports, automated payment reminders, and serial-numbered PDF receipt downloads.

### Phase 4 — Attendance & Timetable
*   **What Exists**: Full class attendance marking screen and dashboard progress graphs.
*   **What is Missing**: Clash detection when assigning rooms/teachers, substitution management, and SMS gateway triggers for absent students.

### Phase 5 — Exams & Grading
*   **What Exists**: Marks entry dialog with obtained <= max score validation.
*   **What is Missing**: CBSE grading scale configurator, promotion/detention rollover scheduler, and bulk report card PDF exporters.

### Phase 6 — Homework & Learning
*   **What Exists**: Homework attachment downloads and document preview windows.
*   **What is Missing**: Student submission folders, review remarks logs, and syllabus trackers.

### Phase 7 — Communication
*   **What Exists**: Targeted notice publishing system and internal notification tray.
*   **What is Missing**: FCM push notifications, pluggable SMS gateway abstractions, and real-time chat rooms.

### Phase 8 — Operations
*   **What Exists**: Transport routes list, library book list, and inventory catalog directories.
*   **What is Missing**: Issue/return logging, library late fee links, transport route Stop-to-Fee auto-linking, and procurement management.

### Phase 9 — Certificates & Compliance
*   **What Exists**: None.
*   **What is Missing**: Templated PDF generators for Transfer Certificates/Bonafide letters, admin audit log trails, and CSV database exporter.

### Phase 10 — Hardening & Deployment
*   **What Exists**: Basic local Robolectric and JUnit unit tests.
*   **What is Missing**: Signed production Android builds (AAB), HTTPS staging/production server deployments, Sentry logging, and Runbooks.

---

## 4. Next Steps & Phase 1 Execution Plan

As requested by the ground rules, I have documented this inventory before introducing any new code. To proceed to **Phase 1 — Foundation**, we need to align on whether we are implementing:
1.  **Direct Firebase Backend**: Enhancing the Firestore rules, configuring Firebase Custom Auth (via serverless Workers), and scoping all records to academic sessions.
2.  **Dedicated REST Backend**: Initializing a separate backend REST API (e.g., Node/Express or Python/FastAPI) and SQL Database, and routing all client requests through it.

Please review this inventory and let me know your instructions.
