# Student light-mode implementation

Scope: Student presentation only. Preserve Student dark, Principal, Teacher, authentication, Supabase policies/data and verified payment controller.

## Visual specification

The supplied full Home board defines hierarchy: approved repository brand; identity/date panel; navy next-class panel; attendance/tasks metrics; circular Learning shortcuts; School essentials; due-work rows; notice; Home/Learn/Schedule/Inbox/You navigation.

White `#FFFFFF`, navy `#202B49`, orange `#C6653C`, powder `#A9C9ED`; derived pale blue/peach/neutral surfaces. No green/purple/teal status fills in Student light. Flat fills, hairline borders, 24dp margins, 28dp section gaps, 12dp related gaps, 16dp internal padding. Shortcuts use white outline icons on navy circles; labels sit below. Brand vectors remain untouched.

## Screen/state checklist

Track implementation and verification separately. Native test renders are not hosted-account or physical-device verification.

| View | Existing entry | Implementation / dependency |
|---|---|---|
| Home | Dashboard | New identity/date, next class, metrics, two shortcut groups, due work, notice composition |
| Learn | StudentLearn | Shared light components; connected academic destinations |
| Assignments | Homework | Existing authorized assignment projection and All/Due/Done filters |
| Assignment | StudentAssignment(id) | Existing instructions, private attachment and submission controller |
| Submission status / feedback | Assignment nested state | Existing confirmed version/review/feedback state; no fabricated feedback |
| Attendance | Attendance | Saved sessions, calendar, explicit statuses; isolated light palette |
| Results / report | ReportCard | Published reports only; existing authorized PDF exporter |
| Timetable | Timetable | Preserve India clock, live rail, gaps, lesson state; no attendance writes |
| Resources / resource detail | StudyMaterial, nested id | Search/filter/detail; original file opener |
| Document / failure | SchoolDocumentButton | Preserve authorized private-file opening and real error/retry |
| Fees / invoice | Fees / StudentInvoice(id) | Scoped ledger, pending/paid, invoice and confirmed payment callbacks |
| Payment pending/interrupted/receipt | Existing payment dialog state | Never infer payment success from appearance or Android callback |
| Library / book / My books | Library, nested state | Catalogue and actual availability. Personal loan/history API unavailable |
| Inbox / notice | Notifications / StudentNotice(id) | Confirmed recipient read-state operations and real destinations |
| Profile / preferences | StudentIdentity / Profile | Existing verified identity, guardian contact, appearance, motion, sound/haptic settings |
| Correction | Identity nested state | Protected correction API unavailable; no fake submission |
| Additional existing destinations | More/Search/Exams/AI/Transport | Retain reachable actions and existing authorization/unavailable behavior |

## Verification

Pending completion of implementation, native rendering, tests, build, lint and emulator checks. Exact evidence will be recorded in the delivery verification file; do not infer verification from this checklist.

## Data limitations

The current Student presentation does not receive a display school name: do not insert the reference's Riverside School into a hosted account. Use verified identity/context rather than an invented school name. Library cover art is not exposed by the existing catalogue; display a neutral title spine rather than an unrelated cover. Personal loans, review-based profile corrections, hosted two-account actions and a real provider TEST capture require their actual backend/device evidence.
