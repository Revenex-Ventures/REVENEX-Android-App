# Revenex Teacher reference delivery

25 October 2026. This is the Teacher continuation of the Principal image rebuild. Existing user work, Supabase data, repositories, RLS and payment behavior are preserved. No database reset, seed, migration, dependency or framework change was made.

## Application and evidence

- APK: `D:\VS Code\Revenex Android\app\build\teacher-image-delivery\apk\app-debug.apk` (`com.example`, debug signing).
- [Light](app/build/teacher-image-delivery/light-gallery.html), [Dark](app/build/teacher-image-delivery/dark-gallery.html), [equal-width reference comparisons](app/build/teacher-image-delivery/comparisons.html), [installed APK captures](app/build/teacher-image-delivery/runtime-gallery.html), [large text/tablet](app/build/teacher-image-delivery/adaptive-gallery.html).
- [Source commit, checksum and exact checks](app/build/teacher-image-delivery/manifest.json), [changed application files](app/build/teacher-image-delivery/SOURCE_CHANGES.txt), [screen checklist](docs/TEACHER_IMAGE_CHECKLIST.md).
- Source is a local checkpoint on `codex/teacher-image-build`. No upload or new CI result is claimed. The normal checkout and staging area are preserved.

## Implemented presentation

The actual Teacher routes use new native Compose layouts, rather than replacement previews. Home follows the supplied composition: identity/context, next lesson, three compact metrics, a six-module workspace, attention and today's schedule. Classes, class detail, attendance, assignments/details, marks, timetable and own leave requests use compact rows and focused controls. Notifications, settings and existing dialogs inherit the same role palette and keep their working actions.

| Semantic role | Light value |
|---|---|
| Canvas / surfaces | `#F8F9FC` / `#FFFFFF` |
| Primary / pressed orange | `#E85B0B` / `#C94B08` |
| Selected peach | `#FFF0E5` |
| Primary / secondary text | `#202735` / `#647084` |
| Fine borders | `#E7EBF1` |
| Success / error | `#18794E` / `#B42318` |
| Mint / blue / lavender wells | `#EAF6EF` / `#EAF1FC` / `#F0ECFA` |

Dark uses deliberate navy/charcoal surfaces with coordinated accents. The existing persistent Light/Dark/System selector remains in Settings, with Light the default. Orange button labels use dark ink for readability; measured contrast is 5.16:1. Text/action measurements are recorded in `palette-contrast.json` alongside the artifacts.

Screen margins are 20dp, major sections 24dp, related gaps 8–12dp, and controls at least 48dp. Large-font context fields stack, shortcuts switch to two columns, and narrow navigation wraps into two rows. Tablets use the existing navigation rail and a two-column class directory. Scrolling, keyboard and system insets remain intact. Native press feedback and existing short transitions are retained; no decorative animation loop or library was added.

## Workflows and authority

- Class/student presentation uses the loaded teaching allocation. Backend Auth/RLS remains authoritative; portal selection never grants access.
- Attendance retains P/A/L with explicit meanings: Present, Absent, Late. Excused remains available by tapping a student. Search and All present remain accessible. Edits return to Not saved; confirmation follows the existing save callback.
- Assignment creation keeps upload, deadline validation and confirmed publication. Class and subject controls use the teacher's allocation. Details retain instructions and attachment actions.
- Marks use assigned class/subject context and validated private drafts through the existing confirmed API. Multi-row saves report partial confirmation honestly. Published terms stay locked. Teacher publication controls are removed; the Principal publication flow remains tested.
- Leave shows the teacher's own requests and existing request form. No school-wide approval controls are exposed. Teacher student profiles do not offer fee payment.
- Navigation is Home / Classes / Tasks / Schedule / More. Back behavior and saved nested class navigation are retained. Account navigation includes the new routes in logout cleanup.

## Verification and limitations

Final local result: **151 tests / 42 suites passed, zero failures/errors/skips; lint zero errors / 76 warnings**. `assembleDebug`, `testDebugUnitTest`, `lintDebug` and `verifyNoFirebaseDependencies` passed on the final source. Tests include valid/invalid draft entry, persisted private marks, rejected unallocated class access, Teacher publication-control denial and Principal publication.

An earlier screenshot-recording run had one PDF-export cleanup assertion fail on Windows. The unchanged test passed in isolation and in the final full normal run; no assertion was weakened and no unrelated export code was changed. Both logs are retained. This intermittent check remains a testing-environment limitation rather than being concealed.

Screenshot evidence is explicitly separated: native Robolectric production-Composable renders at 393dp/font 1.0 (Home 820dp high, destinations 640dp), 320dp/150% text and 800dp tablet; installed APK captures on an API 34 emulator at 360dp logical width. Runtime checks cover class navigation, a Local Demo attendance save, assignment details/unavailable submissions, assignment/leave forms, keyboard-open layout, timetable, inbox, Light/Dark/System changes, stored Dark after process restart and lower dashboard content. The installed binary's checksum matches the delivered APK. No hosted writes were made.

The current repository interface does **not** expose individual submission files/reviews, received/missing totals, assignment draft/close operations or a submission-for-review action for marks. Those reference controls are not fabricated. Submissions show N/A and an explicit unavailable state. Existing individual report-card presentation is retained rather than falsely claimed as a newly rebuilt page.

Actual synthetic records replace sample image values. No portraits, schedules, marks or counts were invented. Some legacy synthetic subject labels differ from timetable labels; filters show actual matching records and no seed data is rewritten. Official company RV/REVENEX artwork is preserved across portals instead of copying illustrative reference branding.

No physical device was connected. Hosted Supabase login, cross-account updates, file upload, device push and a captured Razorpay TEST transaction were not reverified in this UI batch. Device push is not implied by the in-app inbox. Other portals and backend workflows are preserved; this is not a claim that every page of every portal was rebuilt.

To install without deleting data: `adb install -r "app\build\teacher-image-delivery\apk\app-debug.apk"`. Do not uninstall to bypass a signing conflict when existing data must be retained. For offline visual review: Try demo → Teacher → Continue. For cloud acceptance, use an actual authorized school account; no secrets are required in chat.
