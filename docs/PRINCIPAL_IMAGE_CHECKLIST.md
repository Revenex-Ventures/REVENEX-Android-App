# Principal reference implementation checklist

25 October 2026. App source: `c8be45a92a6cb2dae4975fd9a57df12af14bf2f0`, local ref `codex/principal-local-build`. Other user changes and the normal checkout were preserved. No remote upload occurred.

The two supplied Principal images define the layouts. The real NavHost routes to these presentations, rather than unused previews. Existing repository/state interfaces remain connected.

| Screen | Implemented structure | Evidence |
|---|---|---|
| Home | Brand/search/inbox/account; greeting/session; three metrics; six School modules; attention; upcoming | Native render and installed navigation |
| Students | Search, class/status filters, compact rows, details | Render; query/filter survive detail/back |
| Staff | Teaching directory, department/search, status labels, Allocations | Render and installed navigation |
| Attendance | Date/saved slot, summary, class progress, student history, report | Render and class/history navigation |
| Academics | Six shortcuts, actual review counts, Subjects/Calendar | Render and installed navigation |
| Fees | Due/Collected, Dues/Receipts, compact invoice review | Render; Principal profile fee action retains data mode |
| Reports | Categories, scoped tables, recent files, real CSV export | Render; CSV escaping and paise tests; earlier native export |
| Approvals | Pending/Reviewed, actual leave rows | Render and installed cancellation |
| Approval detail | Identity, fields, comment, anchored confirmed decisions | Render; confirmed-save/cancel/unavailable tests |
| School & account | Compact settings, one Light/Dark/System selector, sign out | Render; stored Dark restart and System changes |
| More | Searchable compact modules and retained creation actions | Light/Dark native render |

Shared: semantic palette/spacing, line icons, compact rows/summary/tabs/selects, accessible actions, five-item phone navigation, tablet rail and two-column directory. At 320dp/150% text, navigation uses two rows to retain readable labels. Dialogs/forms remain scrollable with keyboard/system insets.

## Evidence and intentional differences

- Galleries: `app/build/principal-image-delivery/`: Light, Dark, reference comparisons, installed runtime and adaptive layouts.
- Production Compose Robolectric renders: 393dp/font 1.0, Home 820dp and destinations 640dp high. Adaptive: 320dp/150% text and 800dp tablet. Installed runtime: API 34 emulator, 360dp logical width. These are different evidence types.
- Official company RV/REVENEX artwork is retained. Sample photos, counts and events are not fabricated. Empty states intentionally differ from sample content.
- No fake non-teaching directory, staff attendance, corrections, admission/concession approvals, permission editor or session switch: those require actual backend support.
- Existing leaf screens (student/staff details/forms, timetable, assignments, exams/results, notices/inbox, AI/search and operations) remain reachable. They were not all rebuilt or visually reverified in this Principal batch. This is not an app-wide visual-completion claim.
- No physical device was connected. Hosted sign-in, two-account propagation and captured Razorpay TEST receipt were not reverified. No backend/RLS/data/migration changes.

## Checks

Local `assembleDebug testDebugUnitTest lintDebug verifyNoFirebaseDependencies` passed with one worker: **145 tests / 41 suites, 0 failures/errors/skips; lint 0 errors / 76 warnings**. An earlier combined screenshot-recording run had five Compose idling timeouts; focused and subsequent full normal runs passed without weakened assertions. Screenshot recording and normal test evidence are separate. No CI result is claimed for this local source.
