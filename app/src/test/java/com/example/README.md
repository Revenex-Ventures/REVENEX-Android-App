# app/src/test/java/com/example/

Kotlin test classes for unit, Robolectric, and Roborazzi screenshot tests.

## Files

### `ExampleUnitTest.kt`

Basic JUnit sanity test. Verifies the test environment is configured correctly. Run this first when setting up a new dev machine.

---

### `ErpProductionUnitTest.kt`

Unit tests for core business logic in the production data layer:

- Attendance percentage calculation (`calculateStudentAttendancePercentage`)
- CBSE grade computation from raw marks (`SubjectScore`, `ReportCard`)
- Fee payment status transitions
- Role-based permission checks
- Leave request workflow state transitions

---

### `ExampleRobolectricTest.kt`

Robolectric tests that require an Android `Context` but run on the JVM without a device.

- Tests that need `Context` for string resources, shared preferences, or Activity lifecycle.
- Significantly faster than instrumented tests on a real device or emulator.

---

### `GreetingScreenshotTest.kt`

Roborazzi screenshot test for the greeting/login screen composable.

- Renders the `AuthScreen` composable in isolation using `ComposeContentTestRule`.
- Captures a PNG and compares it against the golden image in `../screenshots/`.
- Fails the build if the rendered output differs from the golden image beyond the configured threshold.

---

### `ScholaOSPreviewScreenshotTest.kt`

Roborazzi screenshot tests covering all major screens. **10 test cases**, each with a committed golden PNG:

| Test | Screen |
|---|---|
| `principalDashboard` | Principal dashboard with stat tiles |
| `studentsDirectory` | Student list with search bar |
| `studentDetail` | 360° student profile (tabbed) |
| `attendanceMatrix` | Attendance roll call screen |
| `feeLedger` | Fee management with invoice list |
| `academicsTimetable` | Academics screen — Timetable tab |
| `auditTrailNotices` | Notices and audit log |
| `commandPalette` | Command palette modal overlay |
| `tabletExecutiveDashboard` | Principal dashboard on tablet (navigation rail layout) |
| `tabletScholarsSplitPane` | Student list + detail split-pane on tablet |

To regenerate golden images after an intentional UI change:

```bash
.\gradlew.bat recordRoborazziDebug
```
