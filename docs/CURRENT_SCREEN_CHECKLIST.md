# Current route and state checklist

Current admin/workflow upgrade continues from 419f352. See ADMIN_UPGRADE_DELIVERY.md and ADMIN_REFERENCE_MATRIX.md for current implementation and operational boundaries. The earlier delivery at c29ecd2 is historical evidence. Source truth: NavDestination and MainActivity. Shared theme is ivory/orange with coordinated dark mode. No backend reset or framework replacement.

Reference mapping: education triptych → surfaces/settings/selection; Android dashboard → compact hierarchy/metrics; task app → assignment/document metadata; Shifty → shortcuts/calendar/task progression. No promotional sections or reference branding copied.

| Reachable screen/group | Current implementation and coverage target |
|---|---|
| Authentication / loading | Existing compact login, explicit local/cloud mode, honest sign-in errors; light/dark entry captures. Loading is a transient shell state, not backend success. |
| Principal / admin dashboards | Principal overview retains shared compact components. Admin Home now uses a dated work queue, scoped counts, outstanding invoices, pending leave, today's register exceptions and activity. Light/dark + 800dp admin capture targets. |
| Teacher dashboard | Existing task-first layout with shared semantic components; light/dark captures. |
| Student **and parent** Home | Production shares StudentDashboardScreen. Compact identity, attendance/task metrics, accurate assignment status and direct Fee accounts action. Multiple linked children can be selected. Production still shares the family screen; no separate parent route is claimed. |
| Student list / detail / admission | Grouped profile data; admission fields now stack at narrow widths/large fonts; shared accessible dialog heading, safe-drawing and IME insets. |
| Teacher list / detail / profile / faculty form | Existing semantic views; faculty form now shares responsive groups and dialog treatment. Cloud faculty mutation remains unsupported. |
| Attendance / principal overview | Date/class/session register, current-day editing, view-only historical records, persistent save feedback; entry captures in both palettes. Date picker/individual status sheets still require physical-device coverage. |
| Timetable / Scores | Bulky header replaced with compact heading, shorter 48dp tabs; explicit empty scores. Timetable Scores remains read-only. Results in the directory now reaches the staff marks editor, reviewed publication/unpublication and existing report views. RPC/RLS enforces permissions. |
| Assignments / create form | Subject/status/deadline cards retained. Responsive subject/date/class groups, readable heading and current-date default; create-form captures in both palettes. |
| Study materials / examinations | Focused headings and explicit empty states added; dates/rooms retain existing data. |
| Results / personal details / certificates | Existing shared surfaces and cloud read-only details. Published-data restrictions preserved; unsupported cloud certificates remain honest. Entry captured; all nested report/certificate states are not independently verified. |
| Fees / invoice / lookup / pending / error / receipt | Invoice actions renamed **Pay**; stacked action layout, semantic test notice, no pre-payment verified-receipt claim, wrapping receipt rows. Status reconciliation now opens the dialog even after checkout closes. UI tests cover no-charge lookup and fixture-confirmed receipt transition. |
| Notices / audit / compose | Compact heading/action, accessible tabs/category chips, explicit empty notices. Publication failure/retry retains stable ID. Narrow 1.5-font form interaction and palette captures. |
| Inbox | Divided rows, category/unread filters, time/opening state, confirmed individual/all-read operations. Related-workspace routing is explicitly disclosed; record-specific navigation is not claimed. Entry captures and prior read-interaction tests; actual recipient/device delivery remains separate. |
| Modules / leave / transport / library / inventory | Grouped searchable/adaptive directory and shared role-aware command palette. Leave decisions now use reviewed server-confirmed RPCs with recipient updates. Cloud library is an explicit catalogue-only boundary; local circulation remains a preview. Operations have explicit empty states. |
| Search / AI | Existing semantic layouts, keyboard-aware input and honest unavailable server AI. Entry captures. No claim of live model deployment. |
| Reports | Current data totals and explicitly illustrative charts; no invented export. Entry captures; physical-device chart accessibility remains unverified. |
| Profile / settings | Persisted mutually exclusive Light/Dark/System, default Light. Existing appearance logic tests; light/dark captures. Device process restart/System configuration changes still require acceptance. |

## Verification rules

- Every batched capture must wait for Compose idle; nested capture tests assert the intended state before taking the image. Earlier local captures caught showing the preceding dialog are superseded.
- Screenshot fixtures use synthetic local data and simulated UI states. A receipt fixture is not a real Razorpay payment.
- Renders do not establish keyboard-open, TalkBack, system gestures, orientation or all physical devices. List those as incomplete until exercised.
- Full latest APK/test/lint/render results are recorded after CI completion; earlier 81/82 counts are not silently reused.

## Previous c29ecd2 evidence (historical, superseded by the new delivery report)

- [x] Final c29ecd2 Android build, 85 tests and lint 0 errors/69 warnings passed.
- [x] 82 synchronized Compose images generated; required role/workspace light/dark galleries and representative nested forms/receipt inspected.
- [x] Dialog root bounds checked against viewport without suppressing the assertion.
- [x] Hosted read-only audit confirms one created/provider-bound order, zero paid orders and zero receipts.
- [ ] Complete signed-in captured test payment, persisted receipt/balance/inbox and actual callback/webhook replay.
- [ ] Complete physical-device keyboard/gesture/rotation/TalkBack and all nested report/certificate checks.
- [x] Subsequent upgrade: implemented staff Results editor and publication flow through migration 10; see ADMIN_UPGRADE_DELIVERY.md for current tests and hosted database acceptance.

See CURRENT_DELIVERY.md for exact artifact/hash and DEVICE_PAYMENT_ACCEPTANCE.md for user/operator steps.

## Current upgrade verification — c79f4ae

- [x] Final Android build; 97 tests, no failures/errors/skips; lint 0 errors/69 warnings.
- [x] 90 Compose renders, including role light/dark entry screens, 23 secondary pairs, nested forms/payment fixtures, marks save/review, family no-record attendance, tablet and narrow 1.5-font directory.
- [x] Actual final galleries/admin/assignment/narrow states visually inspected; previous/current comparison images generated from actual renders.
- [x] New directory/command role scope, marks draft→review→publication, preserved report navigation, compact assignment expansion, read-only family register and ISO/display-date regression tests.
- [x] Migration 10 applied once; hosted rollback-only draft/publication/replay/recipient-read checks passed; no temporary marks or notifications retained.
- [ ] Physical device gestures/keyboard/rotation/TalkBack and process-restart/System appearance acceptance.
- [ ] Real two-account Android/Auth/Realtime reconnect and logout/cache acceptance.
- [ ] Captured synthetic Razorpay payment, persisted receipt/balance/principal inbox and provider webhook replay.
- [ ] Unsupported cloud faculty/guardian provisioning, timetable editing, fee configuration/offline collection, official certificates and library circulation remain product/operator boundaries as listed in ADMIN_REFERENCE_MATRIX.md. No empty destinations or success claims were added for them.
