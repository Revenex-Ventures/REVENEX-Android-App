# Presentation rebuild acceptance checklist

Baseline source: ce80da2 (application c79f4ae). The earlier delivery is rejected for layout acceptance. No earlier screenshot is evidence of this rebuild.

## Current acceptance snapshot — 24 October 2026

This section supersedes the historical pending table and older build claims below. Tested application source: `e68b414a99bc3b6aa7fd6a3830d1d18e85c888da`; CI merge: `dc5b45b4bf7d23a611a39afe4a842d99ba6ed46c`; successful run **40095052074**. See [BRAND_REBUILD.md](BRAND_REBUILD.md) for exact artifacts and verification limits.

| Reachable presentation | Implementation / evidence | Remaining acceptance |
|---|---|---|
| Launch, photo entry, four portal hints, Supabase login, separate five-persona demo | RV/card artwork, opaque transition, focused form and adaptive picker; fresh Light/Dark, tablet and narrow/large-text renders; routed Teacher demo test | Successful private school-account login on user's device |
| Principal, Admin, Teacher, Student, Parent | Compact role-specific dashboard composition; fresh Light/Dark renders; no invented schedules/counts | Hosted role-specific data acceptance |
| Staff register, principal overview/history, family attendance | Aligned register controls, scoped date/class/slot, anchored save; renders and selection/save-state tests | Hosted multi-account persistence |
| Students/teachers directories and details, faculty profile | Grouped identity/academic/contact/document presentation; narrow/tablet renders | Real uploaded documents/device downloads |
| Timetable, assignments/resources, exams/results | Selectors, concise task/detail rows, existing attachment/submission actions; Light/Dark renders | Hosted submission/attachment acceptance |
| Staff marks editor / family academic reports | Searchable student selection, draft/publish/review, full report/export controls; fresh nested renders and publish workflow tests | Hosted two-account draft visibility |
| Fees, payment review/pending/error/verified receipt | Compact balances/invoices/history and scroll-safe dialog; Light/Dark renders and receipt/payment contract tests | Real signed-in Razorpay TEST capture and receipt reconciliation |
| Notices/inbox/notification navigation | Compact categorized rows, unread state, existing destinations; render and interaction tests | Hosted recipient-specific/reconnect acceptance; device push is not configured |
| Operations hub, leave, transport, library, inventory, reports | Grouped workspaces, scoped records/charts/tables and honest boundaries; fresh secondary renders | Existing cloud-module limitations retained |
| Search / AI / settings | Compact query/dialog/chat feedback, actual account/context, persistent Light/Dark/System; renders and preference tests | Live AI depends on existing server configuration |
| Admission/faculty/notice/assignment/leave forms, sheets/dialogs | Shared scroll/IME-safe form structure, busy/error/confirm actions; nested Light/Dark and large-text checks | Physical-device keyboard/accessibility acceptance |
| Family profile / personal details / academic report / sample certificates | Old nested hierarchy replaced; actual data groups and explicit sample-document status; fresh Light/Dark renders and Back tests | Real-school certificate authority is not fabricated |

**Checks:** 119 Android tests passed, 0 failures/errors/skips; debug and review builds/lint succeeded (0 lint errors, 78 warnings per variant). Server policy/security CI passed; deployment job skipped on this PR. APK private-configuration/Firebase boundary checks passed. 142 PNG artifacts were downloaded, of which 131 are in current presentation/launch/redesign/brand-revision suites. Curated galleries exclude legacy preview screenshots. Compose/Robolectric captures are distinguished from emulator runtime captures; they are not hosted/device proof.

No backend reset, reseed, migration replay, permission relaxation or payment-settlement change was made for this rebuild. Original callbacks remain. Missing data is displayed honestly rather than manufactured to match the target picture.

## Historical implementation log (superseded snapshots)

The following initial inventory and incremental logs are retained for provenance. Their pending states, old logos, test totals and APKs are not the current delivery.

## Gate and specification
Principal → Teacher → Attendance must be rendered and compared before remaining layouts are extended. Target is the supplied nine-screen image (`codex-clipboard-16848336-cca3-4ce3-95f8-8b9a931f66ae.png`). 393 × 852dp, font scale 1.0 is the primary render viewport; target cells are scaled proportionally for comparison, never stretched. Physical-device/keyboard tests remain distinct.

Reusable presentation: SchoolSection (compact heading/action), SchoolGroup (one fine-bordered section), SchoolRow (aligned identity/detail/action), SchoolMetric (three-column summary). Existing spacing: 16dp page / 24dp sections / 8–12dp related content. Existing semantic Light/Dark/System palettes and persistence remain. No new backend or library.

Actual render path: MainActivity NavHost → the Composables below → ScholaResponsiveScaffold. Principal's FeatureHeroCard and Teacher's AnimatedAttendanceHero were active, explaining the previous result. Their routed calls are now removed. Attendance's two-line student cards have been replaced with aligned inline controls (accessible fallback stacks on narrow/large text). Four existing statuses remain; L means Late, E means Excused, not invented leave authorization.

| Route/state | Actual renderer | Layout status | Fresh render | Interaction checks | Remaining issues |
|---|---|---|---|---|---|
| Auth | AuthScreen | Pending: sign-in-first composition; Try demo sheet | Pending | Pending | Not accepted until individually verified |
| Loading | LoadingScreen | Pending: inspect startup/loading composition | Pending | Pending | Not accepted until individually verified |
| Dashboard / PRINCIPAL | PrincipalDashboardScreen | Replaced; gate render pending | Pending | Pending | Not accepted until individually verified |
| Dashboard / TEACHER | TeacherDashboardScreen | Replaced; gate render pending | Pending | Pending | Not accepted until individually verified |
| Attendance / staff | AttendanceScreen | Replaced; gate render pending | Pending | Pending | Not accepted until individually verified |
| Attendance / principal overview | PrincipalAttendanceScreen | Pending; old hierarchy | Pending | Pending | Not accepted until individually verified |
| Dashboard / ADMIN | AdminDashboardScreen | Pending; old hierarchy | Pending | Pending | Not accepted until individually verified |
| Dashboard / STUDENT | StudentDashboardScreen | Pending; old hierarchy | Pending | Pending | Not accepted until individually verified |
| Dashboard / PARENT | StudentDashboardScreen | Pending; shared routed family layout; ParentDashboardScreen is not routed | Pending | Pending | Not accepted until individually verified |
| Students | StudentListScreen | Pending | Pending | Pending | Not accepted until individually verified |
| StudentDetail/{id} | StudentDetailScreen | Pending; academic/contact/fees/documents nested states | Pending | Pending | Not accepted until individually verified |
| Teachers | TeacherListScreen | Pending | Pending | Pending | Not accepted until individually verified |
| TeacherDetail/{id} | TeacherDetailScreen | Pending | Pending | Pending | Not accepted until individually verified |
| TeacherProfile | TeacherProfileScreen | Pending | Pending | Pending | Not accepted until individually verified |
| Timetable | TimetableScreen | Pending; day/class selectors | Pending | Pending | Not accepted until individually verified |
| Homework | HomeworkScreen | Pending; details, attachment, submission states | Pending | Pending | Not accepted until individually verified |
| StudyMaterial | StudyMaterialScreen | Pending; attachment/details | Pending | Pending | Not accepted until individually verified |
| Exams | ExamsScreen | Pending; exam detail state | Pending | Pending | Not accepted until individually verified |
| ReportCard / staff | ReportCardScreen → StaffMarksWorkspace | Pending; draft/edit/review/publish/report switch | Pending | Pending | Not accepted until individually verified |
| ReportCard / family | ReportCardScreen | Pending; published-only reports/details/export | Pending | Pending | Not accepted until individually verified |
| Fees | FeeManagementScreen | Pending; filters/payment history | Pending | Pending | Not accepted until individually verified |
| Payment review/status/receipt | FeePaymentDialog | Pending; security/data behavior unchanged | Pending | Pending | Not accepted until individually verified |
| Notices | NoticesScreen | Pending; notice/audit/detail | Pending | Pending | Not accepted until individually verified |
| Notifications | NotificationsScreen | Pending; grouped inbox/filter/read/navigation | Pending | Pending | Not accepted until individually verified |
| Leaves | LeaveManagementScreen | Pending; application/review/decision | Pending | Pending | Not accepted until individually verified |
| Transport | TransportScreen | Pending | Pending | Pending | Not accepted until individually verified |
| Library | LibraryScreen | Pending; catalogue-only cloud boundary retained | Pending | Pending | Not accepted until individually verified |
| Inventory | InventoryScreen | Pending | Pending | Pending | Not accepted until individually verified |
| OperationsHub | OperationsHubScreen | Pending; groups/search/limitations | Pending | Pending | Not accepted until individually verified |
| Reports | ReportsAnalyticsScreen | Pending; periods/chart/table states | Pending | Pending | Not accepted until individually verified |
| AI | AiAssistantScreen | Pending; unavailable/error/chat | Pending | Pending | Not accepted until individually verified |
| Search | GlobalSearchScreen / CommandPalette | Pending; query/empty/results | Pending | Pending | Not accepted until individually verified |
| Profile | ProfileScreen | Pending; appearance/signout/security | Pending | Pending | Not accepted until individually verified |
| Admission | AddStudentDialog | Pending; form/keyboard/error/saving | Pending | Pending | Not accepted until individually verified |
| Faculty | AddTeacherDialog / cloud provisioning information | Pending | Pending | Pending | Not accepted until individually verified |
| Notice composer | CreateNoticeDialog | Pending | Pending | Pending | Not accepted until individually verified |
| Assignment composer | CreateAssignmentDialog | Pending | Pending | Pending | Not accepted until individually verified |
| Leave composer | ApplyLeaveDialog | Pending | Pending | Pending | Not accepted until individually verified |

## Data honesty
- Principal chart uses only saved attendance records for the displayed week, with missing days explicitly absent; historical hard-coded percentage bars removed.
- No fabricated pending-admission count, teacher class fallback, period count, student photos or per-student submission history.
- Scheduled examinations are labeled as such rather than invented upcoming calendar events. There is no authenticated general events feed in the existing interface.
- Existing Supabase, authenticated payment, RLS, state ownership and receipt confirmation remain unchanged.

## Verification log
- First compile attempt blocked by existing Gradle journal lock owned by PID 28824.
- Second isolated-user-cache attempt blocked by the same owner's project file-hash lock.
- Retrying with isolated user AND project cache; no lock deleted and no unrelated process killed.
- Render gate: pending. Build/APK: pending. All pages outside the gate remain explicitly incomplete.

## First rendered comparison (56e79dc)
CI 40057113404 compiled and ran 102 Android tests: 99 passed, three new gate checks failed on lazy/off-screen test queries. This is not a green delivery. Fresh source renders and side-by-side target comparisons are in `app/build/presentation-gate-56e79dc/verification/reports/presentation-rebuild/`.

Inspected Principal, Teacher, Attendance and dark Teacher. The hero hierarchy is removed. Remaining differences identified and being fixed before gate acceptance:
- Principal had an extra action row pushing the event section down; attendance action moved into the retained management section.
- Metric icons lacked the reference's semantic pastel wells; added consistent blue/green/red/butter wells.
- Attendance selectors were overly rounded/dark-bordered and session labels clipped; compact selectors, fine borders and equal-width abbreviated labels now retain the same underlying slot values.
- The real Attendance route's outer shell would duplicate the toolbar and crowd the anchored save; phone attendance is now a focused task surface with its existing Back callback. Tablet/desktop rail/drawer remains available.
- Dark headings inherited black LocalContentColor outside Surface; the semantic theme now provides its matching onBackground content color.
- Missing days, weekend lessons, photos and individual submissions are not invented. No random trend percentages. General upcoming events/submission-history data remains an interface limitation.
- Tests now scroll the lazy container to a matching node and assert the actual attendance selection, instead of searching off-screen content. The final gate captures Attendance inside the production scaffold too.

All remaining layouts are still pending. No previous gallery or compilation-only result is accepted as visual completion.

## Verified second screen batch (4f1c353)
CI 40071495556: 28 suites / 104 Android tests passed, zero failed/error/skipped; lint 0 errors / 69 warnings. 115 PNGs downloaded and the new eleven-experience Light/Dark galleries were inspected. The galleries are actual Compose/Robolectric renders of routed implementations; secondary-screen renders capture the routed screen body. They are not device captures.

Galleries: `app/build/presentation-batch-4f1c353/screen-batch-light.png`, `screen-batch-dark.png`. The first gate is structurally replaced and its dark contrast correction is visible. The weekend teacher schedule and missing upcoming events remain honest empty states. Four attendance statuses are retained; no fake admission counts, photos, trend deltas or live AI are added.

New source after that verified snapshot, awaiting the next build/render: grouped student and faculty profiles, faculty editing groups, adaptive directories, record-only principal attendance overview/history, loaded-data reports, compact timetable/scores, expandable resource/exam details, categorized workspace rows and transport/library/inventory/leave presentations.

Correction to the initial interface note: the repository does expose an events state. The Principal/Parent now use it for parseable upcoming records and show empty states otherwise; cloud population is not assumed. All existing examination information remains reachable.


## Current launch delivery: bfc5dc1 (24 October 2026)

The new native launch/account-entry slice is now implemented: Android splash handoff, one-time original Revenex reveal, bundled licensed classroom photograph, four generic portal-entry routes, separate five-persona offline demo, and existing backend-verified sign-in. Light/Dark/System was verified on Android 14, including correction of a real System-mode override bug. All five Local Demo role dashboards were captured from the actual final APK.

Current evidence: **116 Android tests passed; 0 failures/errors/skips; lint 0 errors / 71 warnings; 129 fresh Robolectric PNGs**, plus actual emulator captures/recordings. APK source `bfc5dc16bff87216405849ff5820e1853768316d`; CI 40080849234. Earlier counts and pending logs above are historical snapshots, not current delivery claims. See [LAUNCH_EXPERIENCE.md](LAUNCH_EXPERIENCE.md) for exact artifact/hash, verification matrix and signing/hosted-device blockers.

This completes the launch slice, not every earlier nested ERP presentation: the report/certificate/marks-editor and some payment-dialog layouts are still outside this completed slice. No database reset, reseed, deployed migration replay or payment logic change occurred.
