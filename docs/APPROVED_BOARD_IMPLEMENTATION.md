# Approved board implementation — 24 October 2026

The new request rejects source e68b414a's visual acceptance. It remains a functional baseline, not proof that this board is matched. Local Git HEAD is ce80da2; current remote source before this batch is 4814162 (docs after e68b414a). Existing unrelated README edits are preserved. Current screenshots/APK must be regenerated for each code batch.

## Sources translated to decisions

| Official source | Decision in Revenex |
|---|---|
| [Fedena process flow](https://fedena.freshdesk.com/support/solutions/articles/266503-process-flow-start-using-fedena) | Setup and enrollment precede academic transactions; operator-only dependencies stay explicit, never fake completion. |
| [Fedena mobile roles](https://fedena.com/mobile_app) | Administrator emphasizes exceptions/approvals/finance; Teacher emphasizes class tasks; families emphasize child updates. Existing backend role checks stay authoritative. |
| [Frappe admission](https://docs.frappe.io/education/student_admission), [enrollment](https://docs.frappe.io/education/program-enrollment) | Preserve academic session and class through enrollment/task navigation; do not conflate application, enrolled student and Auth account creation. |
| [Frappe attendance](https://docs.frappe.io/education/student-attendance) | Keep class/date/session context attached to register actions; retain supported attendance statuses rather than relabel Late as Leave. |
| [Frappe student portal](https://docs.frappe.io/education/student-portal), [fee schedules](https://docs.frappe.io/education/fee-schedule) | Server-confirmed payment/ledger status controls invoice/receipt feedback; callback alone is insufficient. |
| [Android accessibility defaults](https://developer.android.com/develop/ui/compose/accessibility/api-defaults), [adaptive layouts](https://developer.android.com/develop/ui/compose/build-adaptive-apps) | Compact visual controls retain accessible hit areas, readable text, large-text stacking, phone navigation and larger-window rail/drawer behavior. No framework migration is required. |

The attached approved nine-screen board governs proportions, task hierarchy, semantic pastels and concise writing. Existing official company RV artwork is retained from the prior explicit branding request. No illustrative data, photos, AI availability, schedules or counts are manufactured.

## First batch

Actual route chain is MainActivity.ScholaErpApp NavHost → PrincipalDashboardScreen / AdminDashboardScreen / TeacherDashboardScreen / AttendanceScreen / AuthScreen → production responsive scaffold. These are not unused replacement previews. SchoolSection's 48dp TextButton header plus 24dp inter-section gap produced excess space in the actual routed compositions; this batch compacts visual section actions and row padding without shrinking typography. Native launcher/brand reveal and auth backend remain intact.

- Login: short portal and persona names; separate school sign-in/demo, concise Continue, same fields/validation/permissions.
- Principal: three metrics, labeled attendance axes/grid with absent data left absent, grouped actions/events, retained management workflows below.
- Teacher: compact identity, timeline, fitting actions, class statistics, assignment activity. Mark attendance now carries the displayed teacher cohort through the existing route; it previously opened the school's first class instead.
- Admin: current school/session context, operational metrics, exceptions/approvals, concise frequent actions/modules, real loaded collection summaries and audit activity. No fake pending admissions count.
- Attendance: aligned four-state register, short save action, separate counts and confirmed save feedback; class/date/session remain explicit. Backend callback semantics preserved.

Gate acceptance remains pending until fresh render comparison and CI results are recorded. The supplied board has no Admin/Login panel; those are assessed against its component language and the prior auth composition, not falsely labeled direct target matches.

## Evidence rules

Comparisons crop the actual attached Principal/Teacher/Attendance panels. Target is scaled proportionally to 393dp width and letterboxed rather than distorted; current captures use 393×852dp, 1× font. A four-state register and honest missing-weekday/holiday states are deliberate functional differences. Later narrow/large-font/tablet renders are separate from this primary visual gate. Compose fixtures, actual NavHost tests, emulator captures and hosted-device acceptance are reported separately.

## First gate review evidence

CI 40100074198 / source 3fda633 passed Android and server checks. Fresh 393×852dp target/current comparisons were inspected for Principal, Teacher and Attendance. Four-state register, missing weekday bars, initials instead of unavailable portraits, and a real next listed weekday instead of invented lessons are intentional data/functional differences. Generic oversized hero hierarchy is absent.

The render caught a new typography regression: the collected total wrapped its final zero. A focused correction uses compact Indian currency units for this dashboard metric, with the exact amount retained in its accessibility description and full Finance view. This correction awaits the next render; the gate is not described as pixel-identical. Board Admin/Login panels do not exist, so their captures are evaluated against its common components and prior auth reference.

Hosted read-only setup check (24 October): public tables without RLS = 0; profiles = 4; the four named workflow RPCs (enroll_student, save_draft_mark, set_result_publication, decide_leave) exist. This checks setup presence, not private Auth success or correctness of every hosted policy. No SQL mutation or migration was run.

## Focused connected-workflow corrections

- Teacher attendance navigation carries its displayed cohort through the existing authorized route; the actual NavHost test now asserts the selected class.
- Writes/RPC confirmations are bound to an initiating session revision. Enrollment, attendance, assignment/notice saves, marks publication and leave decisions cannot report success or reload into another account after logout/re-authentication. A server request already sent may still commit; the error tells the user to check the original account before retrying. No rollback or automatic duplicate retry is invented.
- Guardian assignment status is projected for the selected permitted child, not always the first child loaded at login. The source rejects unavailable child selections and still relies on PostgreSQL RLS for authorization. The UI limits a guardian assignment list to the selected child's class. No role, school or permission is accepted from portal selection.
- New focused tests cover successful confirmation, account switch during persistence, logout during refresh, server rejection, signed-out submission, and separate child completion. These are local lifecycle/projection tests, not a hosted two-device login simulation.

No schema migration, database reset, reseed, permission relaxation or payment-settlement change is introduced by these fixes. Existing server-derived amounts, signature checks and duplicate-safe settlement remain in place.

## Connected portal presentation batch

The final first-batch amount correction (74dd493, CI 40100896129) passed and its actual Principal render was inspected: ₹12.9k stays on one line, while exact totals remain in Finance/accessibility. The separate account/child correction run (32e7771, CI 40101822743) passed Android and server checks.

Next presentation slice recomposes student shortcuts/tasks, guardian task rows, assignment tab/status structure, timetable timeline, invoice review rows, compact notification chronology and Settings. The Admin Academics item now opens the academic module group instead of disguising a timetable-only link. Recent activity opens the Audit tab, not the unrelated Notices tab. Eight module groups are explicit. A new actual NavHost test follows Administrator → Academics → Assignments → Back → Activity, with an accessible section-action touch-target check.

Persistent successful sync/reconciliation banners no longer consume space on every cloud screen. Pending/errors remain visible; confirmed save feedback is brief. No background push is claimed. Inbox transport limitations are available through contextual information rather than repeated footer prose. Forms retain validation/details with shorter action labels.

Secondary directory/detail/report/operations/chat refinements and final verification remain in progress; this paragraph does not declare every screen finished.

## Directory and financial consistency slice

Connected portal source 844f040 / CI 40103535176 is green. Its fresh portal renders were reviewed. Parent updates now preview title/category/time, with full messages retained in Inbox; duplicated parent task previews are removed while the task statistic keeps its working assignment destination. Student shortcuts retain their direct destinations; family Schedule is explicitly a timetable rather than the Admin academic hub.

Directories use compact identity rows and a continuous student collection, retaining adaptive detail panes. Administrator People connects Students and Faculty through real routed tabs. Profile data is grouped into labeled academic/contact/teaching fields; full identifiers and details remain accessible. Large-font navigation keeps visible destination labels in two rows rather than hiding text.

A separate demo-data inconsistency was found: Home/fee records and legacy invoice fixtures had different balances. Invoices now project from the existing fee records in both modes, so Home, Finance, reports and payment history share the same authoritative amounts. This is an in-memory projection correction, not a Supabase reseed. Family Finance is scoped to the selected child. Full invoice identity and fee heads are available in its expanded review, whose Pay action carries the exact record ID; the server still derives and validates payable amounts independently.

Focused tests cover ledger identity/amount consistency, selected-child pending counts, invoice-to-checkout identifiers, People navigation and retained family timetable destinations. Final results remain pending until this slice completes CI and render review.
