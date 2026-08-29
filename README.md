# REVENEX SCHOOL ERP — ENTERPRISE EDITION

> **Unified Digital Operating System for Modern Educational Institutions**

Revenex School ERP connects Principals, Faculty, Students, and Parents into a singular, high-performance Android mobile application built with Jetpack Compose (Material Design 3), Kotlin Coroutines, and Clean Architecture.

---

## 🎯 Core Goals & Design Philosophy

1. **Academic Trust & Integrity**: Ensure every evaluation metric, marks statement, and attendance registry is accurate, validated, and securely stored without client-side tampering.
2. **Strict Role-Based Access Control (RBAC)**: Ensure that users only access resources corresponding to their roles. Principals oversee the institution; Teachers record attendance and input marks; Students/Parents manage personal details, view academic files, and pay fees.
3. **Seamless Cloud Sync & Offline Fallback**: Dual-mode execution (Production Mode synced with live Google Firebase database / Demo Mode populated with offline mock data) ensures robust testing and staging environments.
4. **Unified Mobile Experience**: Student Portal and Parent Portal are merged into a single portal, allowing parents to switch between multiple child wards instantly.

---

## 🚀 Current Implementation & Features

### 🔐 1. Authentication & Session Lifecycles
* **Principal Login**: Production mode authenticates via Google Workspace Sign-In, checking resolved permissions dynamically from Firestore.
* **Teacher Login**: Authenticates via Employee ID + Date of Birth (DOB) checked against authoritative faculty database records.
* **Student/Parent Login**: Unified portal accessed using the Student's Admission Number + Date of Birth (DOB).
* **Automatic Session Restoration**: Uses FirebaseAuth to restore active sessions automatically upon application launch.

### 📊 2. Principal Dashboard & Attendance Hub
* **Real-time snapshot**: Quick overview of student counts, faculty attendance rate, and collected fees.
* **Hierarchical Attendance Registry**:
  * **Level 1**: Overall school attendance average presented in a visual, animated progress ring.
  * **Level 2**: Class-wise attendance breakdown (Class 10-A, 10-B, 9-A, etc.) dynamically calculated from actual database records.
  * **Level 3**: Student-wise percentages inside the selected class, with clickable overlays showing detailed chronological attendance logs.

### 📝 3. Faculty Marks & Evaluations Desk
* **Marks Entry registry**: Teachers select class, exam term, and subject to record marks for every student, with input limits strictly validated (`obtained <= max`).
* **Checked Papers Upload**: Allows teachers to upload scans or photos of a student's evaluated examination paper. Students/parents can open it directly from their marks sheet.
* **Answer Key Attachment**: Class-wide answers and solution booklets can be uploaded by the teacher and downloaded/viewed by all enrolled students.

### 💳 4. Scoped Fees & Razorpay Gateway
* **Fee Summary**: Personalized parent ledger showing due, paid, and pending balances scoped to the selected child.
* **Razorpay Test Integration**: 1-tap test payment gateway configured inside `MainActivity.kt` that updates collections and outputs transaction receipts on success.

### 📚 5. Homework Tracker
* **Attachments Support**: Supports attaching reference PDF/DOCX booklets to homework assignments.
* **File Downloader**: Students can view files, trigger download progress meters, and read attached document contents directly within the app.

---

## 🛠️ Local Development & CLI Instructions

### Run Unit Tests
Verify core business logic, permissions, and attendance calculations:
```bash
.\gradlew.bat test
```
*Verification status*: **15/15 tests passing successfully** (100% success rate).

### Compile & Build Kotlin Source
Verify source code syntax compile correctness:
```bash
.\gradlew.bat compileDebugKotlin
```

### Clean & Assemble Fresh Debug APK
Build a fresh, updated debug APK reflecting current code changes:
```bash
# Clean previous build artifacts
.\gradlew.bat clean

# Assemble the fresh debug package
.\gradlew.bat assembleDebug
```
*Generated output path*: [`app-debug.apk`](file:///c:/Users/Prasanna/Downloads/revenex-school-erp/app-debug.apk) *(also located under `app\build\outputs\apk\debug\app-debug.apk`)*.

---

## 📈 Future Roadmap & Priorities

1. **Staging & Physical Staging**: Install and test [`app-debug.apk`](file:///c:/Users/Prasanna/Downloads/revenex-school-erp/app-debug.apk) on physical Android devices to verify gesture animations and Razorpay callback responsiveness.
2. **Firebase Rules Deployment**: Test live Production Mode with Cloud Firestore and Firebase Storage using configured security rules.
3. **Push Notifications Integration**: Setup Firebase Cloud Messaging (FCM) to trigger instant alerts for announcements, fee payments, and attendance changes.
