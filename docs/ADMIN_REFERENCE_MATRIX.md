# Admin reference implementation matrix — 24 October 2026

Starting source: 419f352. This is a live checklist, not a claim of complete ERP coverage.

## Reference inventory and translation

The approved nine-screen board sets typography, compact white/ivory surfaces, orange actions, pastel category wells, register rows, invoice lists and inbox hierarchy. Its displayed names, dates, balances and counts are synthetic examples; they must not become production facts. The web screenshot supplies functional breadth, not its crowded navigation.

Visible web menu labels: Home, Admissions, Notification Center, Attendance, Reports, HRM, Expense, Academics, Timetable, Inventory, Counselling, Fees, Scholarship, Transport, Gallery, Settings, DOC, Counter Billing, Support, Website, Class Test, New CCE, Nirgam Document, Event Management, Library, Tally, Calendar, Voucher, Assignments, Hostel, Changelog, Question Bank Easy DB, Campaign Fees, Help, PTA, Our Products, Principal Dashboard, Admin Dashboard, Doc Repository, Update Password, Staff Shifts, Dynamic Form, LMS Admin, Quiz, New Nirgam Document, Question Bank Easy, School Info, Logout. Top controls: module/student/staff/name/ID search, Profile, Admission, Fees, Next Year Fees. Side controls: callback, support, sync and chat.

Attendance reference controls: Attendance, Machine Attendance, Holiday Record, Weekly Holiday, Leave Management, Machine Status, RFID Punch Report tabs; App/Web/Email/SMS destinations; academic year, date, class, division and sort selectors; student search, select-all-absent, roll/name/gender register; calendar with taken/not-taken/holiday/weekend legend; attendance report/settings/help links. A label alone does not establish a business rule or available integration.

## Implementation / operational boundaries

| Area | Current connected scope | Remaining boundary |
|---|---|---|
| Navigation | Searchable role-filtered directory in six groups; inbox, scoped search, profile, ordinary destinations and context actions | UI visibility is not authorization; backend policies remain authoritative |
| Admin home | Scoped students/faculty, outstanding invoices, pending leave, today's recorded registers, activity, working shortcuts | No invented admission queue or predicted attendance percentage |
| Admissions / people | Existing enrolment RPC, duplicate validation, student directory/detail, class allocation on admission | Auth provisioning and guardian relinking require trusted operator workflow; cloud faculty writes unsupported |
| Academics | Existing timetable, assignments/submissions, resources, examinations and report views | No timetable allocation editor, scholarship, class-test bank or question-bank workflow |
| Marks | Existing RLS and source read/write/publication methods | Staff editor/publication remains to be implemented and verified; family draft protection must remain |
| Attendance | Class/date/session register, bulk actions, confirmed save, loaded historical review and overview | Historical correction, staff register, configurable holidays and device ingestion not implemented |
| Fees | Invoice balance/list, TEST checkout, server capture/signature validation, status lookup and receipt UI | Real test capture/receipt acceptance pending; no structures, next-year fee configuration, concessions, expenses/vouchers or offline collection workflow |
| Communication | Notices, persistent recipient inbox, read state; Realtime plus 30-second reconciliation | Real two-device cross-account delivery/reconnect still needs acceptance; no background push/SMS/email configured |
| Operations | Library catalogue, transport routes, inventory, operational summaries, profile/appearance | Some mutations remain unsupported in cloud mode; do not treat catalogue buttons as proof of circulation |
| Documents / certificates | Existing authorized attachments and profile/report document views | Official certificate issuance, general repository and dynamic forms not implemented |
| Integration boundary | Expandable admin directory explanation for hardware/vendor services | RFID/machine attendance, Tally, payroll/staff shifts, website/gallery management require separate design/provider setup |
| Unspecified vendor modules | Names inventoried above | DOC, New CCE, Nirgam variants, Campaign Fees, Question Bank Easy/DB, Our Products require product clarification; no fabricated destinations |
| Additional school modules | Not represented as operational modules | Hostel, counselling, PTA, LMS/quizzes, scholarships and event/calendar editing require defined workflows and permissions |

## Verification ledger for this increment

- [ ] Focused directory role/search tests.
- [ ] New admin/directory rendered light/dark, phone/tablet/large text interaction.
- [ ] All Android build/test/lint gates and backend regression tests.
- [ ] Latest APK path, source commit and SHA-256 recorded in delivery notes.
- [ ] Hosted captured TEST payment, persisted receipt/balance/inbox and duplicate replay.

Older delivery artifacts and their test counts remain historical evidence. They do not verify this increment.
