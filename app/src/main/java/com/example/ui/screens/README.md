# ui/screens/

All feature screens grouped by domain. Each subdirectory maps to a navigation destination or a set of related destinations.

## Directories

| Directory | Screens | Description |
|---|---|---|
| `auth/` | `AuthScreen.kt` | Login screen with Demo/Production mode toggle |
| `dashboard/` | 4 role-specific dashboard screens | Home screens for Principal, Teacher, Student, Parent |
| `students/` | `StudentListScreen.kt`, `StudentDetailScreen.kt` | Student directory and 360° student profile |
| `teachers/` | `TeacherListScreen.kt`, `TeacherDetailScreen.kt` | Faculty directory and teacher profile |
| `attendance/` | `AttendanceScreen.kt`, `PrincipalAttendanceScreen.kt` | Teacher roll call and school-wide attendance overview |
| `fees/` | `FeeManagementScreen.kt` | Fee ledger, invoice list, and Razorpay payment trigger |
| `academics/` | `AcademicsScreens.kt`, `ReportCardScreen.kt` | Timetable, homework, study materials, exams, report cards |
| `ai/` | `AiAssistantScreen.kt` | Gemini AI assistant chat interface |
| `communication/` | `CommunicationScreens.kt` | School notices and in-app notifications |
| `operations/` | `OperationsScreens.kt` | Operations hub, transport, library, inventory, leave management |
| `reports/` | `ReportsAnalyticsScreen.kt` | Analytics dashboard and downloadable reports |
| `search/` | `GlobalSearchScreen.kt` | Full-text search across students, teachers, notices, and more |
| `profile/` | `ProfileScreen.kt` | User profile, account details, and app settings |

## Conventions

- Every screen composable takes a `navController: NavHostController` and reads state from `ErpDataRepository` via `collectAsState()`.
- Screens do not hold data state — only ephemeral UI state (dialog visibility, selected tab index, search query string).
- Navigation between screens uses `navController.navigate(Screen.X.route)`.
