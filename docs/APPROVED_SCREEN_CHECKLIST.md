# Revenex routed-screen checklist

Current implementation scope: approved ivory/orange presentation; official RV/company-card wordmark retained. This file distinguishes implementation from visual and hosted verification. MainActivity's actual NavHost was traced; the old ParentDashboardScreen and preview-only ScholaOS screenshots are not delivery evidence.

## Routed layouts

| Route / experience | Actual production presentation | Rebuild and retained behavior | Evidence / remaining check |
|---|---|---|---|
| Auth / portal / sign-in / demo | AuthScreen + RevenexLaunchHost | Local bundled photo, separate portal sign-in and secondary demo selection; backend identity controls access | LaunchEntryTest; fresh emulator recording pending |
| Principal | PrincipalDashboardScreen | Three metrics, labeled weekly chart, grouped actions, events, management/activity | First-batch comparisons inspected; final recapture pending |
| Admin | AdminDashboardScreen | Context, operational metrics/exceptions, grouped modules, audit and financial activity | Actual NavHost People/Academics/Activity journey; final runtime pending |
| Teacher | TeacherDashboardScreen | Identity, real lesson timeline, two actions, class summary, tasks | Gate render and routed attendance/class assertions |
| Student | StudentDashboardScreen | Slim assistant entry, next class, three shortcuts, due tasks, notice | Both-palette body renders; runtime pending |
| Parent | StudentDashboardScreen parent branch | Permitted child selection, summary, chronological updates, balance, upcoming | Body render inspected; real multi-child hosted login pending |
| Students / detail | StudentListScreen / StudentDetailScreen | Compact directory, routed Students/Faculty tabs, grouped identity/contact/academic/documents | Directory renders inspected; full-detail render pending |
| Faculty / detail / own profile | TeacherListScreen / TeacherDetailScreen / TeacherProfileScreen | Compact rows, exact schedule matching, labeled details, local edit form preserved | Body render inspected; nested edit tests |
| Attendance register | AttendanceScreen | Selectors, summaries, searchable P/A/L/E rows, anchored save/confirmed feedback | Target/current gate and selection tests; hosted save pending |
| Attendance overview / class / history | PrincipalAttendanceScreen | Compact metrics, class/student drilldown, saved history dialog | Nested navigation and history capture tests |
| Timetable / score tab | TimetableScreen | Day/class controls and readable lesson timeline; actual score records | Both-palette body captures |
| Assignments / resources / expansion | HomeworkScreen + StudyMaterialScreen | Tabs, filters, pastel rows, full detail/attachment/confirmed submission | Body/nested tests; hosted teacher-to-child flow pending |
| Exams / expansion | ExamsScreen | Concise schedule rows and labeled term/subject details | Both-palette body captures; final expansion inspection pending |
| Results / marks | ReportCardScreen + StaffMarksWorkspace | Private drafts, saved subjects, responsive numeric fields, review/publication confirmation | AdminWorkflowUiTest; hosted draft visibility pending |
| Student profile hub / details / academic reports | ReportCardScreen nested sections | Grouped data and actual reports, existing PDF export retained | Existing nested tests; full-scroll inspection pending |
| Certificates | ReportCardScreen nested certificate sections | Local sample document composition; explicit cloud unavailability | No invented cloud issuance |
| Fees / invoice review / history | FeeManagementScreen + FeePaymentDialog | Canonical fee-record projection, selected-child invoices, Pending/Paid, expandable fee heads, secure Pay/status/receipt path | Invoice callback identity/amount test; real captured checkout pending |
| Notices / audit | NoticesScreen | Compact published rows; distinct activity route opens Audit | Actual Admin activity journey; hosted recipient check pending |
| Inbox / categories / detail destination | NotificationsScreen | Compact chronology, read states/timestamps, categories and destination routing | Recipient isolation/local transport tests; hosted two-account pending |
| More / academic hub | OperationsHubScreen | Eight semantic module groups, scoped search and real destinations | Actual academic route journey |
| Leave / decision | LeaveManagementScreen | Pending/history, expandable reason, confirmed approval/rejection and remark | Existing workflow tests; hosted approval pending |
| Transport | TransportScreen | Route rows, expanded driver/contact and ordered stops | Both-palette captures; no tracking claim |
| Library | LibraryScreen | Searchable catalogue, local circulation action; explicit cloud limitation | Both-palette captures |
| Inventory | InventoryScreen | Compact asset rows, condition, labeled quantity/value | Both-palette captures |
| Reports | ReportsAnalyticsScreen | Loaded-session financial totals, class/department counts, scoped attendance periods and chronology | Final render/check pending; export explicitly unavailable |
| Search | GlobalSearchScreen | Grouped students/faculty/invoices/assignments/notices with original destinations | Final results-state inspection pending |
| Assistant | AiAssistantScreen | Compact conversation/composer, source labels, honest on-request availability, no artificial reply delay | Local guidance tests; live provider availability unverified |
| Settings / account / help / security | ProfileScreen | Account row, settings list, one persistent Light/Dark/System selector and detail dialogs | Appearance tests; process restart runtime pending |
| Forms / payment states / dialogs | Dialogs + shared components | Compact headers, responsive pairs, anchored actions, validation, safe inset/keyboard layout | Nested screenshots and narrow/large-font form tests |
| Loading / empty / error | Shared controls and each production screen | Pending/failed actions kept honest; no success until confirmation | Local fixtures do not prove hosted success |

## Account and transport boundaries

Primary navigation returns through Dashboard rather than the removed Auth destination; account changes clear saved stacks. Nested routes keep their primary selection. UI entry/role choice is never authorization. Supabase RLS and secure payment server checks are unchanged.

Inbox uses Realtime plus periodic reconciliation; this is in-app delivery, not device push. Payment callbacks alone cannot settle an invoice. No Supabase reset, reseed or migration replay was performed.

## Evidence rules

Record final source SHA, CI run, test totals and artifact hashes in the delivery report. Roborazzi production-body renders are layout evidence; actual NavHost interaction tests are navigation evidence. Neither equals a hosted two-account flow or a physical-device payment. Mark any remaining gap explicitly rather than promoting a compile result to visual verification.
