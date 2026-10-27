> Historical record. The active approved-board implementation and final evidence are in `docs/APPROVED_BOARD_IMPLEMENTATION.md` and `docs/APPROVED_SCREEN_CHECKLIST.md`. Artifact/source claims below belong to their stated earlier builds.

> Superseded visual direction: the user subsequently requested the original black/orange UI. The original palette and carbon/ember shared components from `6efa23f` are restored. Supabase login, truthful loading/error/data states, administrator access, safe insets and reduced-motion detection remain. The green design rationale below is historical and does not describe the new APK.

# Revenex visual direction and screen register

20 October 2026. Scope: presentation only. No repository, backend, rules, records, or authorisation changes.

## Design rationale

1. **Academic evergreen:** a calm, task-oriented school workspace. A warm paper canvas and opaque white surfaces replace translucent glass and heavy gradients. Deep forest headers establish identity without making every card dark. The official Reply example informed navigation and hierarchy, not a copied layout.
2. **Palette:** primary evergreen `#14675C` with white foreground; forest `#244B40` anchors navigation/header; pale green `#DCEFE5` identifies selection; warm neutral `#F5F6F2` is the canvas; ink `#1C3028` is body text; muted `#52635B` is secondary text. Amber indicates pending, red errors, green success. Status text remains visible alongside color. Existing token names are retained for compatibility.
3. **Navigation:** existing role-specific routes and permissions are preserved. Phones use a labelled Material NavigationBar with at most five items; the teacher faculty profile is accessible from More, while account settings stay in the avatar menu. Larger windows keep the existing rail/drawer, now scrollable, with full labels. Content is width-capped; the student directory retains its list-detail treatment. Module columns adapt from one to two to three instead of stretching phone cards.
4. **Density:** 16dp page/card rhythm, 12dp compact sections, 8dp related spacing, 14–16dp corners, zero routine card shadow. Metrics are visible immediately. Heroes are summaries with one action, not animated splash screens. Labels use the Material type roles and restrained tracking. Touch targets remain at least 48dp for shared actions. Repeated 80–120dp bottom padding is removed where the shared scaffold already reserves the dock.
5. **Feedback:** permission status is distinct from actual push delivery. The inbox remains readable with alerts disabled; the permission/settings actions are contextual. Badges describe unread count. Existing loading, empty, errors, validation, success and assistant-unavailable states inherit the same surfaces/type. This UI pass does not invent offline/sync states that data APIs do not expose.
6. **Accessibility/motion:** Material buttons/navigation supply focus, selected and disabled semantics; custom pointer-only buttons are removed. Headers, navigation, dialogs and chat respect safe/keyboard insets. Reduced-motion preference is read from Android animator scale; shared fades become instantaneous and skeletons static. Ring introductions, numerical count-ups, navigation bounce/audio and card sheen are removed. Existing non-UI navigation transitions are outside this change.

## Research and application

- [Material 3 theming and Reply example](https://developer.android.com/develop/ui/compose/designsystems/material3): semantic foreground/background roles, typography and shared shapes.
- [Adaptive navigation](https://developer.android.com/develop/adaptive-apps/guides/build-adaptive-navigation): compact bar and larger-window rail. Existing Compose Material3 components are used without adding dependencies.
- [List-detail layouts](https://developer.android.com/develop/ui/compose/layouts/adaptive/list-detail): preserve the existing student two-pane view; do not pretend a stretched list is a tablet design.
- [Accessibility defaults](https://developer.android.com/develop/ui/compose/accessibility/api-defaults): semantic controls, meaningful icon descriptions and 48dp actions.
- [Insets](https://developer.android.com/develop/ui/compose/system/insets-ui): consume device insets once; protect IME controls.
- [Cards](https://developer.android.com/develop/ui/compose/components/card), [badges](https://developer.android.com/develop/ui/compose/components/badges), [search](https://developer.android.com/develop/ui/compose/components/search-bar): clear grouping, count semantics, search entry and result hierarchy.
- [Compose animation](https://developer.android.com/develop/ui/compose/animation/quick-guide): short state feedback without delaying tasks.
- [FCM Android requirements](https://firebase.google.com/docs/cloud-messaging/android/get-started): Android notification permission is separate from server delivery; this redesign changes only its presentation.

## Reachable screen inventory

The public screen entry points below were enumerated from the existing UI source. State indicators identify code present in the file, not completed runtime certification. Mobile entry supports Principal, Teacher, Student and Parent. Administrator remains a backend role; its Android entry/dashboard is removed and admin-only identities are blocked with a clear unsupported message.

| Source | Entry points | Existing important states |
|---|---|---|
| academics/AcademicsScreens.kt | TimetableScreen, HomeworkScreen, StudyMaterialScreen, ExamsScreen | empty |
| academics/ReportCardScreen.kt | ReportCardScreen | empty, dialogs/forms |
| ai/AiAssistantScreen.kt | AiAssistantScreen | loading, dialogs/forms |
| attendance/AttendanceScreen.kt | AttendanceScreen | content/detail |
| attendance/PrincipalAttendanceScreen.kt | PrincipalAttendanceScreen | empty, errors/validation, dialogs/forms |
| auth/AuthScreen.kt | AuthScreen | errors/validation, dialogs/forms |
| communication/CommunicationScreens.kt | NoticesScreen, NotificationsScreen | empty |
| dashboard/ParentDashboardScreen.kt | ParentDashboardScreen | errors/validation, dialogs/forms |
| dashboard/PrincipalDashboardScreen.kt | PrincipalDashboardScreen | content/detail |
| dashboard/StudentDashboardScreen.kt | StudentDashboardScreen | empty |
| dashboard/TeacherDashboardScreen.kt | TeacherDashboardScreen | content/detail |
| fees/FeeManagementScreen.kt | FeeManagementScreen, ScholaPaymentCollectionDialog | empty, dialogs/forms |
| operations/OperationsScreens.kt | OperationsHubScreen, LeaveManagementScreen, TransportScreen, LibraryScreen, InventoryScreen | empty, errors/validation, adaptive |
| profile/ProfileScreen.kt | ProfileScreen | errors/validation |
| reports/ReportsAnalyticsScreen.kt | ReportsAnalyticsScreen | content/detail |
| search/GlobalSearchScreen.kt | GlobalSearchScreen | empty |
| students/StudentDetailScreen.kt | StudentDetailScreen, Student360DetailPane | empty |
| students/StudentListScreen.kt | StudentListScreen, ScholarCardItem | empty, adaptive |
| teachers/TeacherDetailScreen.kt | TeacherDetailScreen | empty |
| teachers/TeacherListScreen.kt | TeacherListScreen | content/detail |
| teachers/TeacherProfileScreen.kt | TeacherProfileScreen | dialogs/forms |

Shared state inventory: login demo/live credentials and errors; role selection; school/class/child context; student/staff creation; notice/assignment/leave/payment dialogs; command palette search and no matches; dashboard summaries; notification empty/unread/permission disabled/allowed; assistant preparing/unavailable/demo; loading skeleton; retry/empty components. Offline and sync failures are only displayed where existing APIs provide them. Do not certify every state from a successful build.

## Verification

Build, JVM suite, lint and existing phone/tablet Robolectric screenshot previews are being run. Actual results and visual inspection coverage will be recorded after completion. Dark theme, all-role runtime journeys, rotation, keyboard and large-font checks require explicit verification; they are not inferred from compilation.


### Visual review coverage and corrections

Recorded Compose previews: principal dashboard (phone/tablet), students (phone and tablet list-detail), student details, attendance, fees, timetable, notices, command palette, login. Inspected images include dashboard, directory, fee ledger, notices, command palette and tablet student details. Emulator captures also cover admin and teacher phone dashboards, student phone/tablet dashboard, admission dialog, 130% tablet text and an actual software-keyboard search input. Emulator viewport, density, font and keyboard preferences were restored.

Visual review fixed: header content overlap when a caller omitted the scaffold padding; long-address label/value overlap; teacher action labels breaking mid-word; nested teacher/student hero cards; pale search icons; faded completed schedule text; dark system-bar canvas under dark Android icons; and missing no-match search feedback.

Not verified: every screen for every role, separate live parent login, all dialog submissions, physical-device cutouts/three-button navigation, dark-theme consistency, TalkBack and keyboard-only focus traversal. Live/offline/error states that require backend conditions were not fabricated or certified. No permissions, data calculations, backend or Firebase rules changed. Existing report export remains unavailable and is now visually disabled.

Core text contrast ratios (sRGB/WCAG calculation): primary on white 6.72:1, ink on canvas 12.87:1, muted text on white 6.37:1, selection text on selected surface 8.01:1. This is palette evidence, not a complete screen accessibility certification.


Additional runtime check: teacher landscape rotation switches to the existing scrollable side rail. Phone search accepted a test query with the software keyboard visible and the input remained above it. The no-match query revealed missing feedback; an explicit empty-result message was added. No record mutation was submitted. Keyboard/rotation/font/viewport overrides were restored.


### Final command results

`gradlew.bat --no-daemon --no-configuration-cache :app:assembleDebug :app:testDebugUnitTest :app:lintDebug -Proborazzi.test.record=true --console=plain` completed with **BUILD SUCCESSFUL in 3m 10s** after the final text corrections. Android XML reports: **36 tests, 0 failures, 0 errors, 0 skipped**; these include ten existing Compose screen previews plus the login screenshot test. Lint: **0 errors, 55 warnings**. Screenshot recording writes to `app/build/reports/screenshots`; these are rendered captures, not golden-image comparison assertions. `git diff --check` passed after the whitespace correction (line-ending notices remain).

An earlier verification attempt failed because the C: drive had no free space. Running with JVM/temp files under `app/build/tmp/ui-build` recovered the build; no personal files were removed. The Windows sandbox launcher also became unavailable, so authorised local build/device checks used execution outside it.

Delivered APK: `app/build/outputs/apk/debug/Revenex-UI-Redesign-debug.apk`, SHA256 `6D50BDA0790AF7819792803787F0262FAD5AE3FAEF216CB078331B767310AD61`.
