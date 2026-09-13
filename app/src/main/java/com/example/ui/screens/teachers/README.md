# screens/teachers/

Faculty directory and individual teacher profile screens.

## Files

### `TeacherListScreen.kt`

Displays all faculty members.

- Searchable list filtered by subject or department.
- Each item shows name, employee ID, subjects, and assigned classes.
- Tapping a teacher navigates to `TeacherDetailScreen`.
- Principal can tap the FAB to open `AddTeacherDialog`.

Accessible to: Principal only (students cannot view teacher records per Firestore rules).

---

### `TeacherDetailScreen.kt`

Detailed profile for a single teacher:

- Personal and employment details (employee ID, experience, contact).
- List of subjects taught and classes assigned.
- Timetable for the current week.
- Leave history and pending leave requests.

Accessible to: Principal only.
