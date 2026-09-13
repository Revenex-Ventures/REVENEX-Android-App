# screens/dashboard/

Role-specific home dashboards. Each user role has its own dashboard with relevant KPIs and quick actions.

## Files

### `PrincipalDashboardScreen.kt`

The Principal's command center.

- Summary stat tiles: total students, faculty attendance rate, fee collection amount, pending leave requests.
- Real-time attendance ring showing school-wide attendance percentage.
- Quick navigation to Student List, Attendance Overview, Fee Management, and Reports.
- Recent activity feed (latest audit log entries).

---

### `TeacherDashboardScreen.kt`

The Teacher's home screen.

- Today's timetable periods for the logged-in teacher.
- Upcoming homework deadlines for their classes.
- Quick access to take attendance for the current period.
- Pending leave request approvals (for class teachers).
- Recent notices from the school.

---

### `StudentDashboardScreen.kt`

The Student's home screen.

- Attendance percentage for the current term with a visual indicator.
- Upcoming homework assignments and their due dates.
- Recent marks/gradebook entries.
- Recent school notices.
- Quick link to fee status.

---

### `ParentDashboardScreen.kt`

The Parent's home screen. Mirrors the Student dashboard but:

- Supports switching between multiple linked children (via `studentIds` JWT claim).
- Shows fee dues and payment status prominently.
- Displays leave request status for the selected child.
- Allows initiating a fee payment from the dashboard.
