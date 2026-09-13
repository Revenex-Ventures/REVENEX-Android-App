# data/model/

Contains all domain model classes and enums used throughout the app.

## Files

### `Models.kt`

Single file defining every domain type in the system. All models are plain Kotlin data classes.

| Model | Description |
|---|---|
| `UserRole` | Enum: `PRINCIPAL`, `TEACHER`, `STUDENT`, `PARENT` |
| `UserProfile` | Logged-in user with role, name, class association, and `schoolId` |
| `Student` | Full student record — admission number, class, attendance %, fee status, GPA, rank |
| `Teacher` | Faculty record — employee ID, subjects taught, assigned classes, experience |
| `AttendanceStatus` | Enum: `PRESENT`, `LATE`, `ABSENT`, `EXCUSED`, `LEAVE` |
| `StudentAttendance` | Per-student attendance entry within a single roll call session |
| `ClassAttendanceRecord` | Full class roll call — date, present/absent counts, list of student attendance entries |
| `LeaveRequest` / `LeaveStatus` | Leave application model with workflow states: `PENDING`, `APPROVED`, `REJECTED` |
| `FeeStatus` | Enum: `PAID`, `PARTIAL`, `PENDING`, `OVERDUE` |
| `FeeRecord` | Detailed fee ledger per student including transaction history |
| `FeePaymentTransaction` | Individual payment receipt with timestamp and payment mode |
| `LedgerInvoice` | Invoice model with Razorpay/payment channel metadata |
| `GradebookEntry` | Single assessment score entry (student, subject, marks obtained/max) |
| `SubjectScore` | Subject score within a report card with computed CBSE grade |
| `ReportCard` | Full term report card — aggregates subject scores, computes overall CBSE grade |
| `HomeworkAssignment` | Homework assignment with attachment URL and per-student completion status |
| `StudyMaterial` | Uploaded learning resource with file type metadata |
| `CheckedPaper` / `AnswerKey` | Exam paper management — checked paper scans and answer key attachments |
| `SchoolNotice` | School circular with category and importance flag |
| `ErpNotification` | In-app notification with category and target role |
| `AuditLogEntry` | Audit trail entry — categories: `ADMISSION`, `ROLL_CALL`, `FEE_PAYMENT`, `GRADEBOOK`, `BROADCAST` |
| `SchoolEvent` | School calendar event |
| `TimetableSlot` | Single period in the weekly timetable |
| `TransportRoute` | Bus route with stops, driver name, and capacity |
| `LibraryBook` | Library catalog entry with available and issued counts |
| `InventoryAsset` | School asset record (equipment, IT hardware, sports gear) |

## Notes

- All models are used directly as state in `StateFlow`s — keep them immutable (`val` fields, `data class`).
- CBSE grade computation logic lives inside `SubjectScore` and `ReportCard`.
- Serialization/deserialization to/from Firestore maps is handled in `FirebaseDataSource.kt`, not here.
