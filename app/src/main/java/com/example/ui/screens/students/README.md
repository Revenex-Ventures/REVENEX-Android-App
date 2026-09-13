# screens/students/

Student directory and individual student profile screens.

## Files

### `StudentListScreen.kt`

Displays all students enrolled in the school.

- Searchable and filterable list by class/grade and section.
- Each list item shows student name, admission number, class, and attendance %.
- Tapping a student navigates to `StudentDetailScreen`.
- Principal and Teachers can tap the FAB to open `AddStudentDialog`.

Accessible to: Principal, Teachers.

---

### `StudentDetailScreen.kt`

360° view of a single student. Tabbed layout covering:

- **Overview** — photo, basic info, contact details, parent info.
- **Attendance** — term-wise attendance history and monthly breakdown.
- **Academics** — gradebook entries, report cards, checked paper downloads.
- **Fees** — fee ledger, payment history, outstanding dues.
- **Homework** — submitted and pending assignments.

Accessible to: Principal, Teachers (full view); Student/Parent (own record only).
