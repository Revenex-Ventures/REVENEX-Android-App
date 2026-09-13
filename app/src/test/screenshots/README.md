# app/src/test/screenshots/

Golden PNG screenshots used by Roborazzi for visual regression testing.

## What these are

Each PNG is a pixel-perfect reference render of a screen composable. When `ScholaOSPreviewScreenshotTest.kt` runs, Roborazzi re-renders each screen and compares it against the corresponding file here. If the output differs beyond the configured tolerance, the test fails and the build is blocked.

## Files (10 golden images)

| File | Screen |
|---|---|
| `principal_dashboard.png` | Principal dashboard |
| `students_directory.png` | Student list |
| `student_detail.png` | 360° student profile |
| `attendance_matrix.png` | Attendance roll call |
| `fee_ledger.png` | Fee management |
| `academics_timetable.png` | Academics — Timetable tab |
| `audit_trail_notices.png` | Notices and audit log |
| `command_palette.png` | Command palette modal |
| `tablet_executive_dashboard.png` | Principal dashboard (tablet) |
| `tablet_scholars_split_pane.png` | Student list + detail (tablet) |

## Updating golden images

After an intentional UI change, regenerate the golden images with:

```bash
.\gradlew.bat recordRoborazziDebug
```

Then commit the updated PNGs along with the code change so reviewers can see the visual diff.
