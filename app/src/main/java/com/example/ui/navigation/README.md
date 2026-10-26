# ui/navigation/

Defines all navigation destinations and role-based bottom navigation configuration.

## Files

### `NavDestination.kt`

Defines the `Screen` sealed class hierarchy with route patterns:

| Screen | Route | Description |
|---|---|---|
| `Auth` | `auth` | Login and authentication entry point |
| `Loading` | `loading` | Session restoration and route redirection |
| `Dashboard` | `dashboard` | Dynamic role dashboard (Principal, Admin, Teacher, Student, Parent) |
| `Profile` | `profile` | User profile, account info, and app settings |
| `Students` | `students` | Enrolled students directory |
| `StudentDetail` | `student_detail/{studentId}` | 360° student profile with attendance, marks, fees |
| `Teachers` | `teachers` | Faculty directory |
| `TeacherDetail` | `teacher_detail/{teacherId}` | Faculty profile, assigned classes, timetable |
| `TeacherProfile` | `teacher_profile` | Teacher's own profile view |
| `Attendance` | `attendance?classGrade={classGrade}&division={division}` | Roll call register with status toggles |
| `PrincipalAttendance` | `principal_attendance` | School-wide attendance overview and drilldown |
| `Fees` | `fees` | Financial ledger, invoice status, and test checkout |
| `Timetable` | `timetable` | Weekly schedule by day and period |
| `Exams` | `exams` | Exam schedule, syllabus, and answer keys |
| `ReportCard` | `report_card` | Term evaluation cards and CBSE marks calculation |
| `Homework` | `homework` | Assignment tracker with managed file attachments |
| `StudyMaterial` | `study_material` | Digital library of class study documents |
| `Notices` | `notices` | School-wide circulars and announcements |
| `Notifications` | `notifications` | Personal database notification inbox |
| `Leaves` | `leaves` | Leave request submission and approval workflow |
| `Transport` | `transport` | Bus routes, stops, and fleet tracking |
| `Library` | `library` | Book catalog and circulation registry |
| `Inventory` | `inventory` | Campus equipment and facility assets |
| `Reports` | `reports` | Analytics and exportable operational reports |
| `AiAssistant` | `ai_assistant` | Role-bounded study and ERP guidance assistant |
| `GlobalSearch` | `global_search` | Search across students, faculty, circulars, and tasks |
| `OperationsHub` | `operations_hub` | Quick-access grid for operational ERP modules |

### `NavConfig.getBottomNavItems(role)`

Returns role-specific bottom navigation tabs (5 items per role):
- **Principal**: Overview (`dashboard`), Students (`students`), Attendance (`attendance`), Fees (`fees`), ERP Hub (`operations_hub`)
- **Admin**: Control (`dashboard`), Admissions (`students`), Schedule (`timetable`), Finance (`fees`), Modules (`operations_hub`)
- **Teacher**: My Class (`dashboard`), Attendance (`attendance`), Homework (`homework`), Timetable (`timetable`), More (`operations_hub`)
- **Student & Parent**: Home (`dashboard`), Schedule (`timetable`), Homework (`homework`), Resources (`operations_hub`), Results (`report_card`)
*(Note: Parent portal is unified into the Student portal with guardian links).*
