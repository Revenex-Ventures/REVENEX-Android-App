# Revenex redesign: screen and journey checklist

## Visual direction and component specification

Warm-white canvas (#F7F8F4), deep ink (#172B2B), teal primary (#006B61).
Mint supports attendance/completion, lavender academics, peach operational accents.
Dark mode uses forest/ink surfaces and mint teal actions; it is a separately defined
palette. Status includes words/icons, never color alone. No new libraries.

14–15sp body, 19–22sp headings, 12sp supporting labels. Spacing 4/8/12/16/24dp.
Cards use 16dp padding, 14–16dp corners, borders and no decorative elevation.
Material controls retain accessible touch targets. Shared surfaces and text resolve
through MaterialTheme; normal, selected, disabled, dialog and sheet states inherit it.

Phones have up to five task destinations. Account/settings are in the top menu
and module hub. Rail starts at 600dp; drawer above 1024dp. Existing split panes
remain. Navigation is immediate, with standard ripple feedback and no sound.
Appearance has one Light/Dark/System selector. Light is the fresh-install default;
the public preference is read synchronously before first Compose content.

One reference was subsequently attached and visually reviewed: the Chegg study/library/profile triptych.
Useful patterns applied: compact account header, light surfaces, grouped pastel rows,
short tabs and regular floating navigation. Its orange accent, premium upsell, illustrations
and ornamental central action were not copied. The other three references are unavailable.

## Screen inventory

All listed screen groups have adopted the shared design system. Top-level captures
are real Compose renders of the app, not mockups. They do not prove every nested
interaction, every backend state, or all accessibility services.

| Group | Every route / important state | Implementation | Render review |
|---|---|---|---|
| Entry | Auth: local demo, Supabase login, credentials, pending, failure; loading route | Shared theme | Top-level light/dark capture; nested states need manual review |
| Portals | Principal, Admin, Teacher, Student, Parent dashboards; missing student | Shared theme + compact shared heroes | Top-level light/dark capture; nested states need manual review |
| Students | Directory/search/filter, tablet split, detail, admission dialog/validation/pending/error | Shared theme | Top-level light/dark capture; nested states need manual review |
| Teachers | Directory, detail, faculty profile/edit forms | Shared theme | Top-level light/dark capture; nested states need manual review |
| Attendance | Principal summary, class selector, register, save/error/empty | Shared theme | Top-level light/dark capture; nested states need manual review |
| Academics | Timetable, exams, homework/create/submission, study material/documents | Shared theme | Top-level light/dark capture; nested states need manual review |
| Results | Report/profile hub, details, annual reports, certificates, PDF/export states | Shared theme | Top-level light/dark capture; nested states need manual review |
| Finance | Fee list/detail, payment dialog, pending/verified/failure/status, receipts | Shared theme | Top-level light/dark capture; nested states need manual review |
| Communication | Notices/audit filters, compose notice, recipient inbox, read/unread/empty | Shared theme | Top-level light/dark capture; nested states need manual review |
| Operations | Module hub, leave form/list, transport, library, inventory | Shared theme | Top-level light/dark capture; nested states need manual review |
| Reports | Charts, executive reports, empty/export states | Shared theme | Top-level light/dark capture; nested states need manual review |
| Utilities | Global search/results, command palette, AI chat/history/error/unavailable | Shared theme | Top-level light/dark capture; nested states need manual review |
| Account | Settings/profile, appearance selector, biometrics, logout | Shared theme + selector | Top-level light/dark capture; nested states need manual review |
| Shared | Dialogs, sheets, fields, badges, buttons, skeleton, empty, success/error | Shared theme | Top-level light/dark capture; nested states need manual review |

Supported Android portals: PRINCIPAL, ADMIN, TEACHER, STUDENT, PARENT.
SQL's additional roles do not have Android portals; login rejects unsupported roles
honestly. No authorization depends on switching a UI role.

## Connected journeys and limitations

- Admission: admin/directory → form → enroll_student RPC → roster + optional invoice
  + principal/admin inbox. Requires hosted migration 7. Faculty provisioning is
  operator-managed; student deletion is disabled in the cloud demo.
- Attendance: class/register → atomic upsert → reload → linked student/parent inbox.
- Assignment: teacher/homework → create → save → section recipients → homework →
  student submission. Creation is publication: there is no assignment draft model.
- Results: PostgreSQL RLS hides drafts and restricts linked children/school/session.
  **Missing transition:** repository marks-entry/publication methods exist, but CURRENT
  reachable screens have no marks-entry/publication control. SQL tests verify the
  underlying policy path; a usable end-to-end staff publication journey remains work.
  Cloud reports now use authorized records only; missing years are never synthesized.
  Local demo sample reports remain. Cloud profile edits/certificates that the backend
  does not support show an honest operator-managed/unavailable state.
- Notice: compose → save → recipient inbox → notice. Current notices are school-wide;
  audience display metadata is not a backend authorization boundary.
- Fees: authorized account → checkout/status → existing verified test-payment server
  → ledger/inbox. No live payment or new paid service is introduced.
- Inbox: recipient row → confirmed read → relevant module. Counts derive from that
  inbox. A single account-scoped live job reconciles task data and inbox every 30 seconds,
  including after missed Realtime delivery. Logout cancels it and removes channels.
  Read actions await server confirmation and offer retry; navigation opens the category
  module, not a fabricated exact record deep link (the existing inbox has no target ID).
- Secondary modules remain accessible. Cloud leave approval and library circulation
  remain unsupported; no fabricated successful action is introduced.

## Evidence ledger

Latest completed run: 79 Android tests passed; build passed; lint 0 errors/68 warnings.
The final incremental run adds three appearance/form captures; its exact totals are
recorded in REDESIGN_VERIFICATION.md after completion. 105 PostgreSQL/RLS checks
and 31 Edge Function tests passed locally. Release/Git-history secret scan passed.

Rendered evidence: all five role dashboards in light/dark; phone 393dp, narrow
320dp with 1.5x font, 800dp tablet; 23 secondary screen entry states in both palettes.
Images were inspected and used to fix dark selected-state contrast, badge text,
chart label clipping, card density and nested light surfaces. Captures do not
constitute pixel-regression baseline approval.

Actual Android emulator: local principal login/navigation; Light/Dark/System;
Dark preference persisted through force-stop/relaunch; System reacted to Android
night-mode change; admission keyboard open and form scrolling. Device screenshots
are under app/build/reports/redesign/device. The exact pre-change CI APK at 2f365b2
was downloaded, locally debug re-signed without code changes, and captured on the
same emulator as the before reference. No app uninstall or database reset occurred.

Hosted two-account authentication/actions, live WebSocket interruption/reconnect,
private Storage service behavior and provider checkout were **not verified**.
Only the public publishable configuration is available; no test-account credentials
or deployment access were supplied. No hosted data or migrations were changed.
Migration 9 must be applied by the operator to enable result-publication inbox rows.
Device push is not configured. Android OS launch-frame timing, TalkBack traversal,
all certificate/PDF child states and every physical-device keyboard remain unverified.

Official references guiding implementation:
- https://developer.android.com/develop/ui/compose/designsystems/material3
- https://developer.android.com/develop/ui/compose/accessibility/api-defaults
- https://supabase.com/docs/reference/kotlin/stream

## Behavior corrections included in this UI task

- Notice and assignment dialogs await confirmed saves, retain input after failure,
  disable repeated submissions and reuse a stable record ID on retry.
- Assignment completion has pending/error states and awaits persistence.
- Attendance cohorts derive from accessible students; reopening today's register
  restores saved status for the selected session slot instead of defaulting all present.
- Admission action is explicitly visible in the directory header. Existing module
  routes for assignments, examinations and results are now discoverable in the hub.
- Account changes clear dialog/chat/navigation composition state; the student detail
  pane re-resolves selected data against the current authorized list.
- Cloud result generation cannot invent grades or official certificates.
- Migration 202610210009_result_notifications.sql adds linked-recipient notices on
  publication only. No RLS policy is changed, and no existing data is reset.
