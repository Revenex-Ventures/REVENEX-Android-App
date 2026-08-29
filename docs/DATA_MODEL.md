# REVENEX SCHOOL ERP — DATA MODELS & ENTITY RELATIONSHIPS

## Core Schema Definitions

### 1. Student Entity (`Student`)
- `id`: String (Unique ID, e.g., `stu_1001`)
- `admissionNumber`: String (School unique admission reference)
- `rollNumber`: Int
- `name`: String
- `classGrade`: String ("10", "9", "8"...)
- `division`: String ("A", "B", "C")
- `dob`: String
- `gender`: String
- `bloodGroup`: String
- `parentName`: String
- `parentPhone`: String
- `parentEmail`: String
- `attendancePercent`: Double
- `feeStatus`: FeeStatus (`PAID`, `PARTIAL`, `PENDING`, `OVERDUE`)
- `feePendingAmount`: Double
- `rank`: Int
- `gpa`: Double

### 2. Class Attendance Record (`ClassAttendanceRecord`)
- `id`: String
- `classGrade`: String
- `division`: String
- `date`: String
- `totalStudents`: Int
- `presentCount`: Int
- `absentCount`: Int
- `lateCount`: Int
- `leaveCount`: Int
- `markedBy`: String
- `studentList`: List<StudentAttendance> (`studentId`, `studentName`, `rollNumber`, `status: PRESENT | ABSENT | LATE | LEAVE`, `remark`)

### 3. Fee Record & Payment Transaction (`FeeRecord`, `FeePaymentTransaction`)
- `id`: String
- `studentId`: String
- `studentName`: String
- `classGrade`: String
- `division`: String
- `tuitionFee`: Double
- `examFee`: Double
- `transportFee`: Double
- `labLibraryFee`: Double
- `discountScholarship`: Double
- `totalFee`: Double
- `paidAmount`: Double
- `pendingAmount`: Computed `(totalFee - paidAmount)`
- `status`: FeeStatus
- `dueDate`: String
- `transactions`: List<FeePaymentTransaction> (`transactionId`, `receiptNo`, `amount`, `date`, `method`, `status`, `feeHead`)

### 4. Leave Request (`LeaveRequest`)
- `id`: String
- `applicantId`: String
- `applicantName`: String
- `applicantRole`: UserRole
- `classOrDept`: String
- `leaveType`: String ("Sick Leave", "Family Event", "Duty Leave")
- `startDate`: String
- `endDate`: String
- `daysCount`: Int
- `reason`: String
- `status`: LeaveStatus (`PENDING`, `APPROVED`, `REJECTED`)
- `approverRemark`: String
