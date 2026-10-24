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
