# ui/navigation/

Defines all navigation destinations and role-based bottom navigation configuration.

## Files

### `NavDestination.kt`

Defines the `Screen` sealed class with **21 route destinations**:

| Screen | Route | Description |
|---|---|---|
| `Auth` | `auth` | Login screen |
| `PrincipalDashboard` | `principal_dashboard` | Principal home |
| `TeacherDashboard` | `teacher_dashboard` | Teacher home |
| `StudentDashboard` | `student_dashboard` | Student/Parent home |
| `StudentList` | `student_list` | All students directory |
| `StudentDetail` | `student_detail/{id}` | 360° student profile |
| `TeacherList` | `teacher_list` | Faculty directory |
| `TeacherDetail` | `teacher_detail/{id}` | Teacher profile |
| `Attendance` | `attendance` | Teacher roll call screen |
| `PrincipalAttendance` | `principal_attendance` | School-wide attendance overview |
| `FeeManagement` | `fee_management` | Fee ledger + payment |
| `Academics` | `academics` | Timetable, homework, study materials |
| `ReportCard` | `report_card/{id}` | Student term report card |
| `AiAssistant` | `ai_assistant` | Gemini AI assistant |
| `Communication` | `communication` | Notices and notifications |
| `Operations` | `operations` | Transport, library, inventory, leave |
| `Reports` | `reports` | Analytics and reports |
| `GlobalSearch` | `search` | Global search across all data |
| `Profile` | `profile` | User profile and settings |

**`NavConfig.getBottomNavItems(role)`** returns a role-specific list of 5 bottom nav tabs. Each role sees only the tabs relevant to them:
- Principal sees: Dashboard, Students, Attendance, Reports, Operations
- Teacher sees: Dashboard, Attendance, Academics, Communication, Profile
- Student/Parent sees: Dashboard, Academics, Fees, Communication, Profile
