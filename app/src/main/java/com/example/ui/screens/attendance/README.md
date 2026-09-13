# screens/attendance/

Attendance recording (teacher) and school-wide attendance overview (principal).

## Files

### `AttendanceScreen.kt`

The teacher's roll call screen.

- Teacher selects class, section, and date.
- Displays the full student roster for that class.
- Each student row has a toggle cycling through: PRESENT → LATE → ABSENT → EXCUSED.
- On submit, calls `recordRollCallSession()` on the repository which saves the `ClassAttendanceRecord` and writes an audit log entry.
- Prevents duplicate submissions for the same class + date combination.

Accessible to: Teachers (their assigned classes only), Principal (any class).

---

### `PrincipalAttendanceScreen.kt`

School-wide attendance oversight for the Principal.

- **Level 1**: Animated progress ring showing overall school attendance average.
- **Level 2**: Class-wise breakdown — each class shows its attendance % for the selected date.
- **Level 3**: Tapping a class expands the student-level view with individual percentages and a chronological log of each student's attendance history.
- Date picker to browse historical records.

Accessible to: Principal only.
