# app/src/test/screenshots/

Golden PNG screenshots used by Roborazzi for visual regression testing.

## What these are

Each PNG is a pixel-perfect reference render of a screen composable. When `ScholaOSPreviewScreenshotTest.kt` runs, Roborazzi re-renders each screen and compares it against the corresponding file here. If the output differs beyond the configured tolerance, the test fails and the build is blocked.

## Files (Golden images)

| File | Screen |
|---|---|
| `greeting.png` | Greeting / auth screen |
| `01_principal_dashboard_mobile.png` | Principal dashboard |
| `02_scholars_directory_mobile.png` | Student list directory |
| `03_student_360_detail_mobile.png` | 360° student profile |
| `04_attendance_matrix_mobile.png` | Attendance roll call matrix |
| `05_fee_management_ledger_mobile.png` | Fee management ledger |
| `06_academics_timetable_mobile.png` | Academics — Timetable tab |
| `07_audit_trail_notices_mobile.png` | Notices and audit log |
| `08_command_palette_modal.png` | Command palette modal |
| `09_tablet_executive_dashboard.png` | Principal dashboard (tablet rail layout) |
| `10_tablet_scholars_split_pane.png` | Student list + detail (tablet split pane) |

## Updating golden images

After an intentional UI change, regenerate the golden images with:

```bash
.\gradlew.bat recordRoborazziDebug
```

Then commit the updated PNGs along with the code change so reviewers can see the visual diff.
