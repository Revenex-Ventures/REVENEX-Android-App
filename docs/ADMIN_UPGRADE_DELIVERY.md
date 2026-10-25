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
