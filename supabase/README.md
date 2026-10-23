> Redesign workflow fix: after existing migrations 1–8, apply `migrations/202610210009_result_notifications.sql` once in the SQL Editor. It adds linked student/parent inbox notifications when marks become published; it does not change RLS or reset data. Local policy tests cover publication, duplicate prevention and recipient isolation. This migration has **not** been applied to the hosted project by this run. Android now reconciles inbox/task rows every 30 seconds and awaits confirmed notice/assignment/read operations. Device push remains unconfigured.

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

### Step 2: Apply Database Schema & Policies
1. In the Supabase Dashboard, navigate to the **SQL Editor**.
2. Open `supabase/migrations/202610200001_schema_rls.sql`, copy its entire content, paste into the SQL Editor, and click **Run**.
   - This creates all ERP tables, foreign keys, private security functions, and RLS policies.
3. Open `supabase/migrations/202610200002_events_storage.sql`, copy its entire content, paste into the SQL Editor, and click **Run**.
   - This configures audit/notification triggers, Realtime publication, and private storage bucket policies.
4. Run `supabase/migrations/202610200003_document_access.sql` next, then `supabase/migrations/202610200004_integrity.sql`. These add scoped document grants, integrity constraints, independent roll-call sessions, and leave-request identity protection.
5. Run `supabase/migrations/202610200005_student_privacy.sql`. This removes duplicated fee fields from student payloads, enforces their removal on future writes, and upgrades no-op notification handling. Financial summaries come only from permitted fee records.
6. Apply each numbered migration once, in order, to a dedicated demo database. Keep `erp_private` out of Data API exposed schemas. Expose `public` in Data API; no anonymous ERP grants are needed. The migration enables the intended Realtime tables in `supabase_realtime`.

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

### Step 4: Seed Initial School Data
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

- **Test Suite**: Run `npm ci --ignore-scripts --prefix supabase/tests`, then `npm test --prefix supabase/tests` to run the PostgreSQL/RLS checks against PGlite (105 passed in the redesign verification). This engine supplies test doubles for Supabase Auth/Storage schemas; it does not verify those hosted services or websocket delivery.
- **Android verification**: Final `assembleDebug`, all 82 unit tests, and `lintDebug` passed (zero lint errors, 70 warnings). An isolated API 34 emulator verified installation, local administrator access and inbox read interaction (badge 2 to 1). See `docs/PROJECT_CHANGES_AND_STATUS.md` for exact evidence and hosted limitations.
- **Unconfigured State**: If `supabase.local.properties` is missing or contains dummy keys, the app clearly alerts you that configuration is required, while preserving full functionality in **Local Demo** mode.
- **Session Caches**: Cloud source data is cleared on sign-in/logout and repository collectors clear their cloud caches. This is covered by focused Android tests when they pass; see the status notes for the current result.


## Hosted verification after setup

Run `python supabase/verify_hosted.py` from your terminal. It reads the ignored public configuration and prompts locally for each of the four demo account passwords using hidden input. Passwords/tokens are never written to the report. This verifies real password login, protected profiles and scoped reads; it does not claim to test writes, websocket delivery or Storage delivery.

Then use two devices/emulators:

1. Sign in as teacher on device A and parent on device B. Create an assignment for 10-A; confirm it appears on B without reopening the page and that B receives its own inbox row.
2. Save a roll call, switch its session slot, save again and verify both records remain after sign-out/sign-in. Simulate network loss: no success banner should appear for an unconfirmed write; retry after reconnecting.
3. Save draft marks. Student/parent must not see drafts. Publish the report; recheck access. Change term and verify distinct marks.
4. Attach a small PDF to a teacher assignment. Parent/student in that class should open it; a different school/session/account must be denied. Wait/re-sign links; durable references should continue working. Files are limited to 10 MB and PDF/PNG/JPEG/DOCX.
5. Mark a parent notification read. Student's copy must remain unread. Verify inbox persists after signing in again.
6. View fees. Without payment deployment, checkout must fail honestly and no fee balance may change. When enabled, test checkout must credit only after server-verified capture. No Android push permission, FCM token or Firebase configuration is needed.
7. Sign out and switch roles. No prior account's records may remain. Reopen the app and verify the local demo still works without any cloud configuration.

## Scope and limitations

- Local demo remains the default. Supabase is opt-in and is not a production migration or an import of Firebase data.
- SQL contains all nine requested roles. Android currently supports its existing principal/admin/teacher/student/parent dashboards; other roles fail sign-in clearly until dedicated routing is implemented. The seed provisions four separate accounts.
- Enrollment is supported through the principal/admin form and migration 7; Auth account provisioning and parent links remain operator-managed. Library circulation and leave approval UI writes are not enabled; data reads come from PostgreSQL and unsupported actions fail rather than mutating local samples.
- Android cannot directly credit fees. Optional Razorpay TEST checkout and server AI code exist, but their hosted endpoints returned 404 in the latest public check. Device push is not configured. See RAZORPAY_SETUP.md and ../docs/INTEGRATION_READINESS.md.
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
