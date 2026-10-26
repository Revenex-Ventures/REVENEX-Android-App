> **26 October 2026 deployment checkpoint:** The existing Revenex demo project has migrations 11–19 applied and rollback acceptance passed. Do not rerun its migrations or seed data. See [current backend evidence](../README_BACKEND_PROGRESS.md). New-project steps below apply only to a new empty project. The updated payment recovery handler is deployed; an actual captured Razorpay TEST transaction remains unverified.

> Backend & staff workflow update (26 October 2026): migrations `202610200001` through `202610260018_targeted_notices.sql` define the complete schema, RLS, and RPC boundaries. Migration 11 (`202610260011_result_publication_boundary.sql`) is applied in the hosted demo following migrations 1–10. **Do not blindly replay deployed SQL**: manual SQL Editor application does not populate CLI history. Rollback acceptance scripts (`verify_staff_workflows_rollback.sql`, `verify_assignment_workflows_rollback.sql`, `verify_versioned_marks_rollback.sql`) validate schema and permission boundaries with rollback of all synthetic test rows. See `../docs/BACKEND_COMPLETION_CHECKPOINT.md` and `../docs/ADMIN_UPGRADE_DELIVERY.md`.

> Current hosted status (26 October 2026): migrations 1–11 are active on the hosted Supabase project. Zero public tables lack RLS. The 4 demo profiles exist, teacher Auth is active, and `razorpay-payments` edge function is deployed. Unsigned user/webhook probes reject with 401/400. Preflight checks can be run anytime with `python scripts/check_integration_health.py`. Credential pairing and captured checkout remain in TEST mode; AI remains undeployed. Android reconciles inbox/task rows every 30 seconds; device push remains unconfigured.

> Current action-workflow update: enrollment is supported for principal/admin through migration 7, with optional initial invoice and server-generated school notifications. Migrations 11–18 add result publication boundaries, versioned student submissions, private assignment drafts, subject-scoped marks, optimistic version locking, canonical attendance roll-call sessions, and audience-targeted notices. For sign-in diagnostics, run `diagnose_setup.sql` in SQL Editor and use the exact Auth account/password.

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
   - Teachers only access their assigned sections, classes, and subjects.
   - Students and parents can access only their own records and linked children.
   - Marks are hidden from students and parents until published by authorized school leadership.
   - Fee records are read-only; no client-side balance modifications are allowed.
2. **Private Storage**:
   - Bucket `erp-documents` is strictly private (public = false).
   - Storage upload checks `file_metadata` manifest ownership before persisting.
   - Assignments store durable `supabase://erp-documents/...` references. The app requests a fresh 5-minute signed link only when a permitted user opens the file. Class recipients gain access through database-managed grants; removing an attachment revokes the grant. A signed link already issued remains usable until its expiry.
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
6. For a new project, continue in order with:
   - `202610200006_razorpay_test_payments.sql`: Secure TEST payment storage and settlement schema.
   - `202610200007_action_workflows.sql`: Enrolment, school event triggers, and invoice initialization.
   - `202610210008_assistant_security.sql`: Assistant quota schema and server rate limits.
   - `202610210009_result_notifications.sql`: Published-result notifications.
   - `202610240010_staff_workflows.sql`: Confirmed staff operations, draft marks, and administrator leave decisions.
   - `202610260011_result_publication_boundary.sql`: Staff-only publication boundary and immutable mark locking.
   - `202610260012_assignment_submissions.sql`: Student submissions, recoverable notes, private submission files, and teacher review locks.
   - `202610260013_assignment_publication.sql`: Private assignment drafts, versioned authoring, and class-scoped publication.
   - `202610260014_mark_subject_scope.sql`: Mark drafts restricted to teacher's assigned subjects.
   - `202610260015_versioned_marks.sql`: Optimistic concurrency locking for marks and submission-for-review lifecycle.
   - `202610260016_capability_probe.sql`: Server-validated profile capability discovery.
   - `202610260017_attendance_versions.sql`: Canonical date aliases, atomic whole-class roll calls, and concurrency rollback.
   - `202610260018_targeted_notices.sql`: Audience-scoped notices (school-wide, class-targeted, teacher-only).
   - `202610260019_leave_requests.sql`: Validated, idempotent self-service leave requests and leadership inbox events.
7. Apply each numbered migration once, in order, to a dedicated demo database. Keep `erp_private` out of Data API exposed schemas. Expose `public` in Data API; no anonymous ERP grants are needed.

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

- **Test Suite**: Run `npm ci --ignore-scripts --prefix supabase/tests`, then `npm test --prefix supabase/tests`.
  - **138 PostgreSQL schema/RLS checks passed** (`rls.test.mjs` against PGlite) across all 18 migrations, verifying multi-tenant isolation, role boundaries, private storage, attendance roll calls, versioned marks, and assignment lifecycles.
  - **20 Razorpay Edge Function checks passed** (`razorpay-edge.test.mjs` via Node test runner), verifying order binding, signature checks, atomic payment credit, and failure handling.
  - **Total 158 automated backend checks passed** in `npm test`.
  - **Deno Assistant Entrypoint check**: `deno test --no-check --allow-net --allow-read --allow-env assistant-entrypoint.test.mjs` (1 passed).
- **Integration Health Check**: Run `python scripts/check_integration_health.py` to probe live Supabase endpoints (verifies 200 on health, 401 anonymous rejections across protected endpoints).
- **Credential Security Check**: Run `python scripts/check_secrets.py` (passes with 0 secrets leaked).
- **Rollback Acceptance Scripts**:
  - `verify_staff_workflows_rollback.sql`
  - `verify_assignment_workflows_rollback.sql`
  - `verify_versioned_marks_rollback.sql`
  All rollback scripts execute with clean transactions: zero temporary test rows retained in the database.
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
