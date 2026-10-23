# Current route and state checklist

Implementation pass starts at b55a57c; UI/receipt code is committed at 6830b38, render synchronization at 0824287; viewport bounding and root assertions at c29ecd2. Source truth: NavDestination and MainActivity. Shared theme is ivory/orange with coordinated dark mode. No backend reset or framework replacement.

Reference mapping: education triptych → surfaces/settings/selection; Android dashboard → compact hierarchy/metrics; task app → assignment/document metadata; Shifty → shortcuts/calendar/task progression. No promotional sections or reference branding copied.

| Reachable screen/group | Current implementation and coverage target |
|---|---|
| Authentication / loading | Existing compact login, explicit local/cloud mode, honest sign-in errors; light/dark entry captures. Loading is a transient shell state, not backend success. |
| Principal / admin dashboards | Shared compact panels, metrics, tasks and semantic dark controls; light/dark + 800dp admin captures. |
| Teacher dashboard | Existing task-first layout with shared semantic components; light/dark captures. |
| Student **and parent** Home | Production shares StudentDashboardScreen. New direct Fee accounts button; actual parent route now represented by screenshots; tested button navigation. |
| Student list / detail / admission | Grouped profile data; admission fields now stack at narrow widths/large fonts; shared accessible dialog heading, safe-drawing and IME insets. |
| Teacher list / detail / profile / faculty form | Existing semantic views; faculty form now shares responsive groups and dialog treatment. Cloud faculty mutation remains unsupported. |
| Attendance / principal overview | Date/class/session register, current-day editing, view-only historical records, persistent save feedback; entry captures in both palettes. Date picker/individual status sheets still require physical-device coverage. |
| Timetable / Scores | Bulky header replaced with compact heading, shorter 48dp tabs; explicit empty scores. Scores are read-only; no reachable staff editor/publish control. |
| Assignments / create form | Subject/status/deadline cards retained. Responsive subject/date/class groups, readable heading and current-date default; create-form captures in both palettes. |
| Study materials / examinations | Focused headings and explicit empty states added; dates/rooms retain existing data. |
| Results / personal details / certificates | Existing shared surfaces and cloud read-only details. Published-data restrictions preserved; unsupported cloud certificates remain honest. Entry captured; all nested report/certificate states are not independently verified. |
| Fees / invoice / lookup / pending / error / receipt | Invoice actions renamed **Pay**; stacked action layout, semantic test notice, no pre-payment verified-receipt claim, wrapping receipt rows. Status reconciliation now opens the dialog even after checkout closes. UI tests cover no-charge lookup and fixture-confirmed receipt transition. |
| Notices / audit / compose | Compact heading/action, accessible tabs/category chips, explicit empty notices. Publication failure/retry retains stable ID. Narrow 1.5-font form interaction and palette captures. |
| Inbox | Category, time, unread/opening state, confirmed read and existing category destination preserved. Entry captures and prior read-interaction tests; actual recipient/device delivery remains separate. |
| Modules / leave / transport / library / inventory | Existing compact adaptive hub and semantic operations layouts. Leave form now responsive/inset-aware. Unsupported circulation/approval actions remain limitations. |
| Search / AI | Existing semantic layouts, keyboard-aware input and honest unavailable server AI. Entry captures. No claim of live model deployment. |
| Reports | Current data totals and explicitly illustrative charts; no invented export. Entry captures; physical-device chart accessibility remains unverified. |
| Profile / settings | Persisted mutually exclusive Light/Dark/System, default Light. Existing appearance logic tests; light/dark captures. Device process restart/System configuration changes still require acceptance. |

## Verification rules

- Every batched capture must wait for Compose idle; nested capture tests assert the intended state before taking the image. Earlier local captures caught showing the preceding dialog are superseded.
- Screenshot fixtures use synthetic local data and simulated UI states. A receipt fixture is not a real Razorpay payment.
- Renders do not establish keyboard-open, TalkBack, system gestures, orientation or all physical devices. List those as incomplete until exercised.
- Full latest APK/test/lint/render results are recorded after CI completion; earlier 81/82 counts are not silently reused.

## Final checked evidence

- [x] Final c29ecd2 Android build, 85 tests and lint 0 errors/69 warnings passed.
- [x] 82 synchronized Compose images generated; required role/workspace light/dark galleries and representative nested forms/receipt inspected.
- [x] Dialog root bounds checked against viewport without suppressing the assertion.
- [x] Hosted read-only audit confirms one created/provider-bound order, zero paid orders and zero receipts.
- [ ] Complete signed-in captured test payment, persisted receipt/balance/inbox and actual callback/webhook replay.
- [ ] Complete physical-device keyboard/gesture/rotation/TalkBack and all nested report/certificate checks.
- [ ] Implement a secure reachable staff marks editor/publication flow separately; no such editor is claimed here.

See CURRENT_DELIVERY.md for exact artifact/hash and DEVICE_PAYMENT_ACCEPTANCE.md for user/operator steps.
