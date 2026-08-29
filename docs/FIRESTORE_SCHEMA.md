# REVENEX SCHOOL ERP — FIRESTORE DATABASE SCHEMA

## Overview & Tenant Isolation Architecture

All school-owned data is strictly scoped under top-level school documents:
`/schools/{schoolId}/...`

Default school tenant: `revenex_school_001`

---

## Collections & Documents

### 1. School Root Document
- **Path**: `/schools/{schoolId}`
- **Fields**:
  - `name`: String ("Revenex Public School")
  - `affiliation`: String ("CBSE / REV-DELHI-042")
  - `city`: String ("Pune, Maharashtra")
  - `established`: Number (2012)
  - `activeSession`: String ("2026-2027")

### 2. User Profiles (`users`)
- **Path**: `/schools/{schoolId}/users/{userId}`
- **Fields**: `id`, `name`, `email`, `role`, `designation`, `phone`, `avatarInitials`, `associatedClass`, `associatedStudentId`, `associatedChildNames`, `schoolId`

### 3. Students Directory (`students`)
- **Path**: `/schools/{schoolId}/students/{studentId}`
- **Fields**: `id`, `admissionNumber`, `rollNumber`, `name`, `classGrade`, `division`, `dob`, `gender`, `bloodGroup`, `parentName`, `parentPhone`, `parentEmail`, `address`, `admissionDate`, `attendancePercent`, `feeStatus`, `feePendingAmount`, `rank`, `gpa`, `avatarColorHex`, `schoolId`, `status`

### 4. Faculty Directory (`teachers`)
- **Path**: `/schools/{schoolId}/teachers/{teacherId}`
- **Fields**: `id`, `employeeId`, `name`, `department`, `designation`, `qualification`, `email`, `phone`, `assignedClasses` (Array), `subjects` (Array), `weeklyPeriods`, `attendancePercent`, `experienceYears`, `joiningDate`, `schoolId`, `status`

### 5. Class Attendance Records (`attendance`)
- **Path**: `/schools/{schoolId}/attendance/{recordId}`
- **Fields**: `id`, `classGrade`, `division`, `date`, `totalStudents`, `presentCount`, `absentCount`, `lateCount`, `leaveCount`, `markedBy`, `studentList` (Array of objects: `studentId`, `studentName`, `rollNumber`, `status`, `remark`), `schoolId`

### 6. Fee Records & Ledger (`fees`)
- **Path**: `/schools/{schoolId}/fees/{feeId}`
- **Fields**: `id`, `studentId`, `studentName`, `classGrade`, `division`, `tuitionFee`, `examFee`, `transportFee`, `labLibraryFee`, `discountScholarship`, `totalFee`, `paidAmount`, `status`, `dueDate`, `lastPaymentDate`, `transactions` (Array of `FeePaymentTransaction`), `schoolId`

### 7. Leave Applications (`leaves`)
- **Path**: `/schools/{schoolId}/leaves/{leaveId}`
- **Fields**: `id`, `applicantId`, `applicantName`, `applicantRole`, `classOrDept`, `leaveType`, `startDate`, `endDate`, `daysCount`, `reason`, `appliedDate`, `status`, `approverRemark`, `schoolId`

### 8. Homework Assignments (`assignments`)
- **Path**: `/schools/{schoolId}/assignments/{assignmentId}`
- **Fields**: `id`, `title`, `subject`, `classGrade`, `division`, `teacherName`, `assignedDate`, `dueDate`, `instructions`, `attachmentName`, `maxPoints`, `isCompletedByStudent`, `submissionStatus`, `schoolId`

### 9. Study Materials (`study_materials`)
- **Path**: `/schools/{schoolId}/study_materials/{materialId}`
- **Fields**: `id`, `title`, `subject`, `classGrade`, `chapter`, `teacherName`, `uploadDate`, `fileType`, `fileSize`, `description`, `downloadCount`, `schoolId`

### 10. Examination Schedules (`exams`)
- **Path**: `/schools/{schoolId}/exams/{examId}`
- **Fields**: `id`, `title`, `term`, `classGrade`, `startDate`, `endDate`, `isPublished`, `subjects` (Array of `ExamSubject`), `schoolId`

### 11. Report Cards (`report_cards`)
- **Path**: `/schools/{schoolId}/report_cards/{cardId}`
- **Fields**: `id`, `studentId`, `studentName`, `classGrade`, `division`, `rollNumber`, `term`, `academicYear`, `scores` (Array of `SubjectScore`), `attendanceRate`, `rankInClass`, `totalStudents`, `principalRemark`, `issueDate`, `schoolId`

### 12. Class Timetables (`timetable`)
- **Path**: `/schools/{schoolId}/timetable/{slotId}`
- **Fields**: `id`, `dayOfWeek`, `periodNumber`, `startTime`, `endTime`, `subject`, `teacherName`, `roomNumber`, `classGrade`, `division`, `schoolId`

### 13. School Notices & Circulars (`notices`)
- **Path**: `/schools/{schoolId}/notices/{noticeId}`
- **Fields**: `id`, `title`, `content`, `category`, `publishedDate`, `authorName`, `authorRole`, `targetAudience`, `isImportant`, `attachmentName`, `schoolId`

### 14. Notifications (`notifications`)
- **Path**: `/schools/{schoolId}/notifications/{notificationId}`
- **Fields**: `id`, `title`, `message`, `category`, `timestamp`, `isRead`, `targetRole`, `actionableId`, `schoolId`

### 15. Transport Routes (`transport`)
- **Path**: `/schools/{schoolId}/transport/{routeId}`
- **Fields**: `id`, `routeNumber`, `routeName`, `vehicleNumber`, `driverName`, `driverPhone`, `totalCapacity`, `assignedStudents`, `stops` (Array), `pickupStartTime`, `dropStartTime`, `monthlyFare`, `schoolId`

### 16. Digital Library (`library`)
- **Path**: `/schools/{schoolId}/library/{bookId}`
- **Fields**: `id`, `title`, `author`, `isbn`, `category`, `totalCopies`, `availableCopies`, `shelfLocation`, `issuedCount`, `schoolId`

### 17. Campus Inventory (`inventory`)
- **Path**: `/schools/{schoolId}/inventory/{assetId}`
- **Fields**: `id`, `itemName`, `category`, `location`, `quantity`, `condition`, `purchaseDate`, `estimatedValue`, `schoolId`
