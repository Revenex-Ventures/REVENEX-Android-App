# Student portal reference checklist

The supplied Student home (`f5d3e9ba...png`) and twelve-screen board (`c2dbc4c8...png`) define this presentation. This is native Compose in the real routed application, not a prototype. Existing Principal, Teacher and Parent presentations and Supabase/payment services are preserved.

## Color and icon contract

| Meaning | Icon / surface |
|---|---|
| Assignments | Orange clipboard; peach panel |
| Attendance | Teal calendar / people; mint panel |
| Results | Purple outlined ascending bars; lavender panel |
| Timetable | Blue outlined clock; pale-blue panel |
| Fees | Orange outlined wallet; peach panel |
| Library | Teal outlined open book; mint panel |
| Subject rows | Mathematics orange document, language purple book, science blue flask, social studies teal building |
| Confirmed completion | Green/mint Submitted badge |

Canvas #F8F9FC, white surfaces, charcoal #202735, muted #647084, fine #E7EBF1 borders. Icon and selected-control orange remains #E85B0B; filled actions use #C94B08 with white labels. Accessible orange text uses #C24807 on the cool canvas and #A33B07 on peach. These are semantic text shades; pastel fills and icon colors are retained. Dark uses deliberate charcoal, green, blue, purple and peach-brown surfaces rather than inversion.

Measured ratios are recorded in the delivery's `palette-contrast.json`: body/canvas 14.22:1, secondary/canvas 4.76:1, white/action 4.67:1, action text/canvas 4.72:1, orange text/peach 5.92:1; colored non-text orange icons/peach 3.18:1.

## Routed screens

| Screen / state | Implemented presentation | Evidence / remaining dependency |
|---|---|---|
| Home | Greeting, actual next lesson or unavailable state, two summaries, six pastel shortcuts, due work, notice | Light/Dark native renders; installed-APK top/lower captures |
| Learn | Assignments, results, library, resources, examinations | Native renders and runtime navigation |
| More | Personal profile and existing Student-permitted destinations | Native renders; scrollable list, no staff actions |
| Assignment list | All / Due / Done; class/subject/deadline and semantic status | Native renders and runtime |
| Assignment detail | Full instructions, authorized attachment opener, deadline/status, guarded confirmed status save/undo | Local Demo save/undo tested; genuine pending/error UI; cloud error/retry not exercised |
| File / note submission | Explicit unavailable state | No student upload/note repository contract; no fabricated upload or review success |
| Attendance | Read-only calendar, saved-session summary/history and day-details dialog | Future, school and session filtering tested. 48dp day targets; horizontal pan where a narrow viewport cannot fit seven targets |
| Results / report | Published own reports, term selector, subject marks, guarded existing PDF exporter | Native populated renders and draft/other-school/session filtering tests; hosted report/private-file retrieval unverified |
| Timetable | Day selector, actual times/subjects/rooms and empty day | Native Sunday-empty and runtime Monday populated captures |
| Fees | Actual outstanding total and Pending / Paid invoice rows | Integer-paise formatting tested; own-student scope tested |
| Invoice / receipts | Recorded line items, total/paid/balance, real successful transaction metadata, test-payment guard/check action | Local sample never launches a fake checkout; cloud payment/receipt settlement unverified this task |
| Library | Borrowed / Catalog; read-only title/author/availability | Catalog runtime; own-loan records and return dates unavailable in current model |
| Inbox | All / Unread, compact rows, timestamps, indicators, confirmed read/read-all and validated destinations | Header badge shares the same school/role presentation filter; backend remains recipient authority |
| Notices / detail | School notices, category, content, publisher/date and authorized-link availability | Native detail and runtime list/detail. Names without canonical private references are not fake links |
| Profile | Own linked identity, academic/context fields, masked guardian contact, explicit dialer action | Other presentation-selected student cannot replace identity; correction action explains the authorized manual school process |
| Settings | Light / Dark / System; reduced motion, optional sounds/haptics; retained app lock, inbox/documents, support/account details and sign out | Native phone/large-font renders; runtime persistence checks recorded separately |
| Resources | Class notes and established authorized opener | Native renders; named files lacking a reference are unavailable |
| Examinations | Published school/session/class schedules only | Native populated render; no marks editor/publication controls |
| Search | Own identity/invoices, permitted class assignments and school notices | Other loaded people excluded; keyboard runtime capture |
| AI / transport | Existing working destinations retained with Student theme | No assistant capability, assigned route or backend result is fabricated; these existing secondary layouts were not separately rebuilt from an absent reference |

## Navigation, motion and accessibility

- Regular Home / Learn / Schedule / Inbox / More bar on phones; existing adaptive rail/drawer on larger widths. No central decorative AI action.
- A real runtime regression was found when a primary tab restored a previously saved notice stack. `navigatePortalPrimary` now restores presentation state then returns Student tabs to their landing route. Its real NavHost regression test exercises More → notice → Inbox → More.
- Insets remain owned by the shared shell. Lists scroll; narrow 150% text layouts stack summaries, use two-column shortcuts and wrap navigation instead of reducing font size.
- 20dp standard margins, 24dp sections, 12dp cards and at least 48dp controls. Meaning is conveyed by text/status as well as color. Tabs expose selected state and icon-only actions have labels.
- Press 100ms/.98; selection 160ms; forward/reverse navigation 200ms. Reduced motion removes scale/travel; no animated financial or academic numbers.
- Sounds/haptics default off. The 120ms WAV is original local synthesis (880Hz with a quiet 1320Hz harmonic and envelope). Confirmation runs only after awaited submission-status success, respects ringer/volume/audio focus, and releases playback. API 24–25 use the guarded legacy focus path. Audible output/hardware vibration require physical-device verification.

## Verification boundaries

Use `app/build/student-image-delivery/manifest.json` for exact source, APK SHA-256 and final counts. Native Robolectric renders, installed API-34 emulator captures and hosted verification are distinct evidence categories. Reference comparisons preserve real data, official RV branding, readable controls and honest unavailable states; no pixel-perfect assertion is made.

No hosted reset, reseed, migration, RLS change, private key in Android, new library or paid integration was introduced. Hosted Student login, two-account recipient isolation/reconnect, private attachment retrieval, real Razorpay TEST settlement, TalkBack operation and older/physical-device behavior remain separate acceptance checks. Existing source tests do not prove those hosted outcomes.
