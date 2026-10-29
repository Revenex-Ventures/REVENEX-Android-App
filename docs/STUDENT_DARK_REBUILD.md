# Student dark presentation — 27 October 2026

Student DARK presentation only. Student Light, other portals, authentication,
launch assets and backend behavior are retained. Principal and Teacher dark work
follows this checkpoint separately. Current official RV/REVENEX assets remain.

## Visual system

Canvas `#0B1220`, cards `#1C2B43`, navigation `#162237`, blue `#92BAF2`,
mint `#A6D9BF`, orange `#FF743D`. Flat fills; light text on dark surfaces,
dark ink on colored surfaces. Student-only semantic tokens, editorial hierarchy,
compact rows and Home / Learn / Schedule / Inbox / You navigation. Tablet You
opens the same Settings destination. Existing Android platform sans-serif is used;
no font asset, app library or backend dependency was added.

## Reference-to-route checklist

| Views | Actual screen/state | Status |
|---|---|---|
| 01 Home | StudentHome | Rebuilt, rendered, emulator-routed |
| 02 Learn | StudentLearn | Rebuilt, rendered, emulator-routed |
| 03–04 Assignments/detail | StudentAssignments / StudentAssignment | Rebuilt and rendered; existing submit behavior retained |
| 05 Attendance | StudentAttendance | Mint summary, saved-session calendar/history; rendered |
| 06 Results | StudentResults | Published-only summary and actual subject progress; rendered, privacy tests |
| 07 Timetable | StudentTimetable | Date chips, aligned cards, expandable teacher detail, live India-time stroke; rendered and emulator-routed |
| 08–09 Fees/invoice | StudentFees / StudentInvoice | Balance, account summary, invoice rows, breakdown and existing payment callbacks; rendered |
| 10 Library | StudentLibrary | Real catalogue panels/search; rendered, keyboard tested |
| 11–12 Inbox/notice | StudentInbox / StudentNotices | Recipient read state and existing destinations retained; rendered |
| 13 Profile/settings | StudentIdentity / StudentSettings | Protected identity, actual appearance choice; rendered and emulator-routed |
| 14–15 Submission/feedback | StudentCloudSubmission inline states | Existing private upload, version, review locks and feedback retained; submitted/review panels styled; hosted device flow not reverified |
| 16–17 Resources/detail | StudentResources | Search, filters, metadata and existing document opening; list/detail rendered, Back tested |
| 18–19 Book/my books | StudentLibrary detail/unavailable state | Catalogue detail rendered; personal loans explicitly unavailable |
| 20–22 Receipt/payment states | Existing payment controller/dialog | Four rendered presentation fixtures; server-confirmed state remains required |
| 23 Report | StudentResults(reportId) | Existing authorized published PDF workflow retained |
| 24 Correction | StudentIdentity dialog | Honest school-contact state rendered; no fake submitted request |
| 25 Preferences | StudentSettings | Existing persistent Light/Dark/System and motion/sound/haptic choices retained |

Secondary pages remain reachable and rendered: More/tools, Search, Examinations,
notice list, AI assistant and Transport. AI/Transport retain their existing compact
composition under the scoped palette; request/data behavior was not rewritten.

## Verification and evidence

The delivery metadata records exact source state, APK SHA-256 and final check counts.
Native API 34 captures cover 20 screens in both palettes, nested states, a 320dp
phone at 1.5× text and an 800dp tablet. Captures use actual production Composables
and the responsive shell, not generated mockups. Payment receipt captures are
explicit presentation fixtures, not proof of a provider transaction.

Focused tests cover result/invoice privacy, confirmed local completion, India-time
schedule behavior, contrast, role/theme isolation, tablet You navigation, document
Back behavior and payment lookup/cancellation guards. Build, tests, lint, docs links,
credential scan and APK server/private-configuration boundary were checked. Do not
substitute historical full-suite totals for this focused run.

Fifteen of the original 18 Student Light captures are pixel-identical. Home,
attendance and timetable differ in current dates/time/schedule records.

Installed on the emulator with `adb install -r`, retaining data. Actual Home → Learn
→ Assignments → Schedule → You navigation passed. DARK was stored in public
appearance preferences. Keyboard-open library search kept navigation above the IME.
An actual silent navigation tour was recorded (about 45 seconds).

A local Gradle cache-tagging stall was diagnosed from thread dumps. Verification
used an ignored workspace-local Gradle home with marking disabled, following
[Gradle documentation](https://docs.gradle.org/current/userguide/directory_layout.html).
No application quality gate was disabled.

## Limitations

- No physical Android device was connected; manual TalkBack and every orientation
  were not exhaustively checked. A further emulator restart check timed out in
  automatic approval review and is not counted as verified.
- Hosted Supabase sign-in/private upload/feedback/Realtime and a real captured
  Razorpay TEST transaction were not reverified in this UI task. Existing policies,
  repositories, migrations and settlement logic are unchanged.
- Personal loan history/renewals and submitted profile-correction requests are not
  exposed by the current backend. The submission model has no numeric review score.
  Receipt PDF export is not added. Unsupported document links remain unavailable.
- Real long titles and financial breakdowns wrap/scroll. Reference sample values,
  records and comparison statistics are never substituted for authorized data.

Generated delivery: `app/build/student-dark-delivery/` (ignored by Git): APK,
rendered/runtime galleries, navigation recording and source/verification metadata.
