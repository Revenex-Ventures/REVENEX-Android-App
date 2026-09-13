# screens/academics/

Timetable, homework, study materials, exam schedules, and report cards.

## Files

### `AcademicsScreens.kt`

A tabbed screen with four sub-sections:

| Tab | Description |
|---|---|
| **Timetable** | Weekly class schedule. Teachers see their teaching periods; students see their class timetable. |
| **Homework** | Assignment list with subject, due date, and completion status. Teachers can create assignments (opens `CreateAssignmentDialog`) and attach PDF/DOCX files. Students can mark assignments complete and download attachments. |
| **Study Materials** | Uploaded resources (PDFs, videos) shared by teachers. Students can view and download materials. |
| **Exams** | Exam schedule with date, subject, and syllabus. Teachers can upload answer keys; students can download them. |

---

### `ReportCardScreen.kt`

Full term report card for a student.

- Displays subject-wise marks, obtained vs. maximum, and computed CBSE grade for each subject.
- Shows overall aggregate score and overall CBSE grade.
- Download/share button to export the report card as a PDF.
- Teachers can tap a subject row to upload or view the scanned checked paper for that student.

Accessible to: Principal (any student); Teacher (their subjects); Student/Parent (own record).
