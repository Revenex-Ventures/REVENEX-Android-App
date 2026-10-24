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


## Navigation and secondary refinements — 25 October

The 008dd79 directory slice compiled but failed two new journey tests (132 total): navigation returned to an old directory, and invoice review sampled a ledger before its test setup ran. The navigation fix anchors primary switches at Dashboard, clears saved navigation on account exit, and retains semantic selection for nested routes. Its actual routed Admin People → Faculty → Students → Academics → Assignments → Back → Activity test passes in run 40120732339.

Source 6170da1 / run 40120732339 compiled 134 tests with one remaining invoice fixture timeout. This is not a successful release check. The fixture is now controlled by its explicit Main dispatcher and asserts the parent and linked child before inspecting invoices (509c6eb); the final run must be read before delivery. No assertion was removed or ignored.

Fresh production-body renders from 6170da1 were inspected for Principal, Teacher, Attendance, Reports, draft marks and Assistant. The first three preserve the requested compact structure. Missing weekdays, a next listed lesson rather than invented today's lessons, unavailable event rows and initials rather than fabricated portraits are deliberate evidence-based differences from the illustrative board. The fee metric now reflects the canonical fee ledger (not the historical separate invoice fixture).

Secondary refinements include responsive marks pairs, short publication/report actions, labeled document/transport/inventory metadata, actual assignment search results, parsed chronological report dates and attendance-only period filters. Financial totals remain explicitly scoped to the loaded academic session. AI availability is checked on request; the banner does not assert unconfigured service status before a response. Artificial reply delay is removed and conversation scrolling respects reduced motion.

See APPROVED_SCREEN_CHECKLIST.md for every routed presentation and nested-state boundaries. Hosted read/write, payment capture and device-push verification are not inferred from these layout tests.


## Runtime gate and final navigation correction

Source 14c5a0d / CI 40122210945 passes all 134 Android tests (zero skipped/failures/errors), both APK builds and lint (0 errors, 78 warnings per variant). Server checks pass, including 111 PostgreSQL schema/RLS assertions. Local assembleDebug/assembleReview and full tests/lint/Firebase-removal checks also passed with ANDROID_HOME pointed to the existing SDK.

The invoice fixture's actual failure was shared singleton test state: its selected child and canonical invoice IDs matched, but an earlier suite action had already settled the invoice. The focused test passed alone. It now uses a fresh in-memory repository and cancels its own scope; no production singleton or hosted data is reset.

Actual installed-app inspection then caught a sign-out crash: cleanup attempted an unregistered principal_attendance route. Cleanup now considers only registered destinations, and the existing Attendance overview module has its missing NavHost destination registered for Principal/Admin. An extended regression journey covers module overview and Settings sign-out. The 14c5a0d APK is therefore superseded and is not the final recommended build, despite its green CI.

Runtime evidence so far: real account/photo entry and explicitly labeled Supabase login, separate local demo, Principal and Teacher portals, stored Dark after process restart, Class 10-A attendance selection/save (4 present/2 absent confirmed in Local Demo). One emulator System UI wait prompt occurred at startup. No hosted Auth, captured Razorpay payment, physical phone or push delivery is claimed.


## Final evidence

Application 88db806 / CI 40123461452 passes 134 Android tests, both builds/lint (0 errors / 78 warnings per variant), 111 local PostgreSQL assertions, 31 mocked server-handler tests and one Deno entrypoint test. The extended registered-overview and sign-out regression passes. The exact CI review APK was installed and cold/warm/reduced-motion entry recorded. Current unauthenticated payment function responds 401 with its own sign-in message; anonymous students access is denied. No authenticated payment or private hosted workflow success is inferred. See ../README_REVENEX_DELIVERY.md for all artifacts and remaining acceptance gaps.
