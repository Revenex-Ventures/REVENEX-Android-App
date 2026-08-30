# REVENEX School ERP — STRICT AUDIT (PART 1)

Scope/truth basis: this repository is a **native Android app** (Kotlin + Jetpack Compose, Material 3, AGP 9.1.1 / Gradle 9.3.1). There is **no server, no backend API, no database migration tool, no web frontend**. "Backend" = Firebase (Auth, Firestore, Storage). "Demo mode" = in-memory `LocalDemoDataSource`. All findings cite actual files/lines retrieved from disk on this clone.

---

## 1. Repository enumeration

### Root
| Entry | Purpose / status |
|---|---|
| `app/` | Android application module (all code). |
| `assets/`, `build/`, `.gradle/`, `.kotlin/` | Build/dependency caches (generated). |
| `docs/` | 23 markdown reports (ARCHITECTURE, AUDIT, RUNBOOK, PHASE_2/3/4 reports, etc.). Self-published; some overstate reality (e.g. README claims "Google Workspace Sign-In", code has a mock). |
| `firestore.rules`, `storage.rules` | Firestore / Storage security rules (only security layer that exists). See §4. |
| `.env` / `.env.example` | Razorpay/Gemini/Cloudflare config. `.env` is git-ignored; contains only a 23-char `rzp_test_…` placeholder. See §4. |
| `.gitignore`, `local.properties` | Dev config (SDK path) — committed. |
| `debug.keystore` | **Committed debug keystore** (dev signing only; release signing uses env `KEYSTORE_PATH`). Flag: should not live at repo root. |
| `app-debug.apk`, `build_output.txt`, `test_output.txt`, `metadata.json`, `revenex_school_erp_source.zip` | Committed build artifacts — should be git-ignored, not evidence of health. |
| `gradlew`, `gradlew.bat`, `settings.gradle.kts`, `build.gradle.kts`, `gradle.properties` | Standard Gradle wrapper. |

### `app/src/main/java/com/example/`
| File | Purpose | Verdict |
|---|---|---|
| `MainActivity.kt` (574 l) | NavHost + Scaffold + Razorpay activity listener | Core. Payment-on-success is client-side (§3.3). |
| `data/datasource/AppEnvironment.kt` (33 l) | `DataMode` enum + `getIntegrationStatus()` | **Stub** — always returns `false` for every integration (lines 24-32), no runtime readiness signal. |
| `data/datasource/ErpDataSource.kt` | Data-source interface (17 methods incl. `switchRole`) | OK. |
| `data/datasource/LocalDemoDataSource.kt` (550 l) | In-memory demo store | **The entire demo backend.** All "works in demo" claims trace here. |
| `data/datasource/FirebaseDataSource.kt` (1719 l) | Firestore/Storage-backed store | Dead in this clone: requires `google-services.json` (absent; `missingGoogleServicesStrategy=WARN`, app/build.gradle.kts:74). Runtime guarded, but **no test coverage and non-atomic writes** (§3.5). |
| `data/datasource/FirebaseStorageManager.kt` | File upload manager | OK (upload only). |
| `data/model/Models.kt` (409 l) | All data classes | Money = `Double` (§3.4). No session on fees/attendance (§3.5). |
| `data/repository/ErpDataRepository.kt` (638 l) | Facade + StateFlows bound to active source | Payment proxy (§3.3). |
| `data/repository/SampleData.kt` (1292 l) | Hardcoded seed data | Demo-only. ~30 students across classes 1-10; receipts `REC-2026-0842` etc. hardcoded (lines 691-747). |
| `ui/components/CommonComponents.kt`, `DesignSystem.kt`, `Dialogs.kt` | Shared UI + dialogs | OK. `Dialogs.kt` contains the real payment dialog & receipt UI (no PDF). |
| `ui/navigation/NavDestination.kt` | Routes + role bottom-nav | Role nav is UI-only gating. |
| `ui/screens/…` (23 files) | Screens | Mostly healthy demo-mode Read UI; several "admin" actions gated only in UI. |

### `app/src/test|androidTest`
15 tests all pass (see §2). Coverage is thin for the surface: `ErpProductionUnitTest` (8), `ExampleRobolectricTest` (5), `ExampleUnitTest` (1), `GreetingScreenshotTest` (1). No security-rule tests, no payment-flow tests, no money-math tests.

### Dead / stub / duplicated
- `AppEnvironment.kt` — status stub.
- `ParentDashboardScreen.kt` — PARENT role is programmatically folded into STUDENT at AuthScreen:185 (`if (role == PARENT) STUDENT`); the screen is effectively unreachable in normal flow.
- `firestore.rules` `isStudent()` — see §3.2.
- Docs claim Google Workspace sign-in / SMS / confirmations — none exist (§2/§3).

---

## 2. RUN report (what actually happened)

1. First run `:app:compileDebugKotlin` → **green**.
2. `assembleDebug testDebugUnitTest` → **BUILD FAILED**:
   ```
   app/build/generated/.../BuildConfig.java:12: error: illegal start of expression
     public static final String CLOUDFLARE_R2_BUCKET_URL = ;
     public static final String CLOUDFLARE_WORKER_URL = ;
   ```
   **Root cause (FINDING #1):** `.env.example` ships `CLOUDFLARE_WORKER_URL=` and `CLOUDFLARE_R2_BUCKET_URL=` with **empty values**; the Google Secrets Gradle plugin (app/build.gradle.kts:68-72) generates those as BuildConfig fields, and the empty value is emitted unquoted → invalid Java. The fields are **never referenced** anywhere in source (only `BuildConfig.RAZORPAY_KEY_ID` at MainActivity:102 and `APPLICATION_ID` in a test).
   **Fix applied (config, not feature code):** added `CLOUDFLARE_WORKER_URL` and `CLOUDFLARE_R2_BUCKET_URL` to the secrets `ignoreList` (app/build.gradle.kts:71-74).
3. Re-run `assembleDebug testDebugUnitTest` → **BUILD SUCCESSFUL**. APK `app/build/outputs/apk/debug/app-debug.apk` (29,782,394 bytes). Unit tests: **15/15 pass, 0 failures** (ErpProductionUnitTest 8, ExampleRobolectricTest 5, ExampleUnitTest 1, GreetingScreenshotTest 1).
4. **Install/run on device: NOT performed — no emulator/device is attached in this environment.** Additional blockers to a "production" run:
   - `app/google-services.json` **absent** (builds with WARN only; Firebase runtime self-guards).
   - Demo mode is the default `DataMode.DEMO` (LocalDemoDataSource.kt); production requires external Firebase project config.
   - Real Razorpay/Gemini keys require external secrets (test key placeholder present).

**FINDING #1 statement:** a fresh clone does not build out of the box (empty env default values break `compileDebugJavaWithJavac`). Fix applied above; must be re-verified in Part 2 P0.

---

## 3. Critical-path traces (actual code)

### 3.1 Auth / sessions
- Login → `ErpDataRepository.loginWithFirebase(email, pass)` (ErpDataRepository.kt:227-234) → `FirebaseAuth.signInWithEmailAndPassword`. **JWT issue/verify/refresh are entirely Firebase SDK internal**; there is no app-side JWT code, no refresh-token handling, no password hashing in-app. (That's fine — Firebase Identity does it — but there is **no wallet/cert-pinning or key-attestation**, and session restore relies purely on FirebaseAuth persistence.)
- Firestore user lookup: `users.whereEqualTo("email", email)` → first doc → `switchRole(role)` (ErpDataRepository.kt:238-263). **No phone/DOB/employeeId challenge on the principal path.**
- Demo path: `LocalDemoDataSource.login()` (LocalDemoDataSource.kt:84-111) sets a synthetic `UserProfile`; `logout()` (113-115) resets to `defaultProfiles[0]` (Principal).
- **README disproven:** "Principal Login: Google Workspace Sign-In" is a mock — AuthScreen.kt:66 comment *"fallback email/pass until real Google integration"*, and :217-228 calls `loginWithFirebase(principalEmail, principalPassword)`. Teacher and Student logins match Employee ID + DOB / Admission No + DOB **against client-visible in-memory data** (AuthScreen.kt:240-302) — authenticatable by anyone who reads the seeded data.

### 3.2 RBAC — enforcement is NOT server-side
There is no backend. The two enforcement mechanisms are:
1. **UI/gating** (demo): OperationsHub module `allowedRoles` filter (OperationsScreens.kt:55-77); teacher class-scoping of the student roster (StudentListScreen.kt:46-73); bottom-nav per role (NavDestination.kt:51-84). All bypassable in code; **Demo mode has zero authorization** — any call to `activeSource` mutates in-memory state.
2. **Firestore rules** (production only; unverified against a live project):
   - `isStudent()` **returns `isAuthenticated()`** — firestore.rules:37-39. Any signed-in user is a "student".
   - Role functions infer role from `request.auth.token.role` OR **email substring regexes** (`.*principal.*`, `.*teacher.*`, `.*parent.*`) — rules:15-19,24-27,32-34. A user registering `foo@bar-principal-any.com` becomes a principal to the rules. The app never sets custom claims at sign-up.
   - **Unprotected (spoofable) mutation surfaces:** `students` write = `isPrincipal()` (rules:55) — escapable via email heuristic; `fees` create/update = `isPrincipal() || isParent()` (rules:74) with **no per-student ownership check** → a parent may write any student's fee record; `attendance` create/update = teacher/principal (rules:67, 69); `assignments`, `study_materials`, `exams`, `report_cards` writeable by teacher/principal (rules:88-107).
   - **Over-read:** any authenticated user can read ALL of: students (rules:54), teachers (60), attendance (66), fees (73), report cards (106), suburbs of `/schools/{schoolId}` base read (43). No peer/guardian/owner scoping despite Student model carrying parent phone/email/address (Models.kt:34-37).
   - **Storage:** any authenticated user can write any `schools/{schoolId}/...` path and read any file (storage.rules:6-12); no writer/owner separation (student could overwrite teacher answer-key folder).
   - **Notifications** create/update/delete by any authed user (rules:123-127).
   - Unprotected execution happens in-app too: FirebaseDataSource updates student `feePendingAmount`/`feeStatus` directly (FirebaseDataSource.kt:636-646) on a write path that, under the rules, **only principals may perform** — a parent-driven payment would be *denied by the rules*, i.e. the PRODUCTION payment write is broken end-to-end.

### 3.3 Fees & Razorpay — success is decided by the client
- Checkout: `MainActivity.startRazorpayPayment()` (MainActivity.kt:93-132). Key from env/BuildConfig (102); guarded (103-109). Amount = `(amount * 100).toInt()` paise (118).
- **Success signal = client callback**: `PaymentResultWithDataListener.onPaymentSuccess` (MainActivity.kt:134-153) immediately calls `repository.processFeePayment(...)` with `paymentData?.signature`. There is **no webhook handler anywhere** (grep: zero matches). The signature is accepted as a parameter and **never verified** — repo `processFeePayment` (ErpDataRepository.kt:444-477) and `FirebaseDataSource.processFeePayment` (FirebaseDataSource.kt:590-664) both ignore it.
- Amount correctness: repository forces `correctAmount = pendingAmount` (repo:453) → **partial payment impossible; over-credits the full dues for any amount paid**. FirebaseDataSource uses `correctAmount = minOf(amountPaid, pending)` (:600-601) → partial payments allowed only down to 0, but still no server-authoritative amount.
- Receipts: `receiptNo = "REC-2026-${(1000..9999).random()}"` (repo:458; FirebaseDataSource:606; LocalDemoDataSource:258) — **random 4-digit, not serial-numbered, collision-prone, no uniqueness constraint**.
- Receipt PDF / serial-number / export: **none** — no `PdfDocument`/share/print anywhere (grep). Receipt is a static dialog (Dialogs.kt:1425 `ReceiptRow("Razorpay ID", ...)`).

### 3.4 Money math — `Double` everywhere
- `FeeRecord` fields `tuitionFee…totalFee, paidAmount` = `Double` (Models.kt:151-162); `FeePaymentTransaction.amount` `Double` (:138); `pendingAmount = (totalFee - paidAmount)` (:164); `Student.feePendingAmount` `Double` (:41); report percentages `Double` (:217); `CheckedPaper.marksObtained/maxMarks` `Double` (:273-274). **No integer-paise, no fixed-point, no BigDecimal — every currency and marks computation is IEEE-754 float.** Partial-payment accumulation in FirebaseDataSource:619 (`record.paidAmount + correctAmount`) drifts over time. This is the money-math defect the spec flags.
- No fines, no concessions (only a single `discountScholarship` field), no installment model — all absent from Models.kt.

### 3.5 Academic year / data integrity
- **Session scoping:** only `ReportCard` carries `academicYear` (Models.kt:206), hardcoded `"2026-2027"` in seeds (SampleData.kt:767,790) and even in a live screen (ReportCardScreen.kt:50). `FeeRecord`, `ClassAttendanceRecord`, `HomeworkAssignment`, `SchoolNotice`, `TransportRoute` **have no session/year field**. Query-by-session impossible; **no rollover** of students/fees/attendance; attendance identity is `classGrade-division-date` (LocalDemoDataSource.kt:207, FirebaseDataSource.kt:543) so history and session mixes are indistinguishable.
- **Atomicity:** a payment writes fee record (`.set`, FirebaseDataSource.kt:651), student record (`.update`, :640-642), then a notification (:657) in **three separate operations with no Firestore `batch`/`transaction`**; failure between them leaves ledger/student balance divergent. Same pattern for attendance (attendance `.set` :574 + per-student `update` :563) and `addStudent` (demo only, but also fee creation at LocalDemoDataSource.kt:120-145). No unique constraints anywhere (Firestore rules cannot enforce uniqueness); `receiptNo`/`transactionId` shown above are non-unique.

### 3.6 Data integrity — relational guarantees
No foreign keys (NoSQL). References are string IDs (`studentId`, `studentName` denormalized). `StudentListScreen` teacher-scoping joins Teacher-by-email to `assignedClasses` (StudentListScreen.kt:46-53) — correct in demo, fragile in prod. Attendance per-class roll-ups derive from `classAttendance` maps.

---

## 4. Security sweep

| Item | Status |
|---|---|
| Hardcoded secrets/API keys in source | **None found.** `rzp_test_` placeholder lives only in git-ignored `.env`; BuildConfig guarded (MainActivity:102-109). |
| `debug.keystore` + `local.properties` + build artifacts committed | Repo hygiene defect (root). Release signing needs env `KEYSTORE_PATH/STORE_PASSWORD/KEY_PASSWORD` (app/build.gradle.kts). |
| SQL injection | N/A — no SQL. (Firestore uses parameterized queries by design.) |
| Injection/validation | `obtainedMarks` bounded 0..max (AcademicsScreens.kt:821-827). Money amounts, names, admissions, DOB are client-authoritative with no server validation. |
| Files served without auth | Firestore returns download URLs that Storage rules gate to *authenticated* users, but any authenticated user may read **any** student's checked papers/answer keys (storage.rules:6). No per-user ACL. |
| CORS | N/A (no server). |
| Debug mode | `android:allowBackup="true"` (AndroidManifest.xml) — cloud backup of sensitive school data on unencrypted backup channel. No network security config (cleartext policy unset). No `android:debuggable` in main manifest (debug variant only — normal). |
| Firestore rules | Privilege escalation via email heuristics + `isStudent()==isAuthenticated()` (§3.2). **This is the single most severe finding.** |
| Audit logging | **Absent.** No audit trail on any financial or grade mutation (notifications are the only "log", and they target readers, not an immutable ledger). |
| Backups | No backup/export mechanism in code. |


## 5. AUDIT MATRIX (honest grading)

Grades: **L0** missing · **L1** UI only · **L2** happy path works (demo) · **L3** production-ready.
Every "cloud" row is capped at L1 by the missing `google-services.json` + unverified rules this clone can't run. Specific defects below each grade.

| # | Module | Grade | Specific defect (file, behavior, missing case) |
|---|---|---|---|
| 1 | Auth/RBAC | **L1** | Principal "Google sign-in" is email/password mock (AuthScreen:66,217-228). Teacher/Student identity validated against *client-visible seeded data* (AuthScreen:240-302). `isStudent()` = any authed user (firestore.rules:37-39); role inferred from email regex (rules:15-19); fees writeable by any parent, any student record (rules:74); all reads open to any authed user (rules:54,60,66,73,106). Demo mode: zero server enforcement. No custom-claims provisioning. |
| 2 | Academic sessions | **L0/L1** | `academicYear` only on `ReportCard` hardcoded (Models.kt:206; ReportCardScreen.kt:50). Fees/attendance/homework/notices have no session. No rollover. |
| 3 | Classes/Sections/Subjects | **L2** | Hardcoded seeds classes 1-10 (SampleData.kt). No CRUD for classes/sections/subjects; timetable slots denormalized (TimetableSlot). |
| 4 | Admissions & enquiries | **L1** | Manual `AddStudentDialog` + `addStudent` (LocalDemoDataSource:120-145). No admission pipeline, no enquiry module. |
| 5 | Student profiles + docs + ID cards | **L2** | Rich 360 view (StudentDetailScreen). Docs = storage uploads only via FirebaseStorageManager (produces URLs); **no ID card generation**; reads not role-scoped server-side. |
| 6 | CSV import | **L0** | Not present (grep: no csv). |
| 7 | Fee structures | **L1** | One hardcoded structure per student seed; only `discountScholarship`; no configurable heads/terms; `Double` money (Models.kt:151-162). |
| 8 | Concessions/fines/installments | **L0** | Not modeled. |
| 9 | Razorpay online payment | **L1** | Client `onPaymentSuccess` decides outcome (MainActivity:134-153); no webhook, signature unverified (repo:444-477; FirebaseDataSource:590-664); amount logic forces full dues on partial pay (repo:453) or clamps (FirebaseDataSource:601); no idempotency key; production Firestore write would be denied by rules (§3.2). |
| 10 | Offline cash/cheque payments | **L1** | `FeePaymentDialog` shows UPI/Card/NetBanking/Cash options (Dialogs.kt) but all go through the same `processFeePayment`; no bank/cheque fields, no offline verification workflow. |
| 11 | Receipts (PDF, serial-numbered) | **L0/L1** | UI static receipt dialog only (Dialogs.kt:1425). Receipt # random 4-digit (Models.kt:137; repo/Firebase/LocalDemo:458/606/258). No PDF, no serial, no share/download (grep: no PdfDocument). |
| 12 | Fee reports & defaulters | **L2** | ReportsAnalyticsScreen computes in-memory aggregates; statuses PAID/PARTIAL/PENDING/OVERDUE derived live. No historical reconciliation; no defaulter list that cross-checks with ledger (double math). |
| 13 | Student attendance + parent notification | **L2** | Marking + rollups OK in demo (AttendanceScreen; LocalDemoDataSource). "Parent notification" = in-app ErpNotification only (no SMS/FCM). Case-insensitive date-keyed records; no session scoping. |
| 14 | Staff attendance & leave | **L2** | Leave cycle implemented (apply/approve/reject) demo-only. Staff attendance is a % number, not a registry; no leave balance/encashment. |
| 15 | Timetable + clash detection | **L1** | Display + live period highlight exist; **no clash detection**; no admin editor. |
| 16 | Exams & marks entry | **L2** | Marks entry validated 0..max (AcademicsScreens:821-827); no grade-book persistence per term-scoped record beyond snapshot; double marks math. |
| 17 | Report cards (PDF, bulk) | **L1** | On-screen report card only (ReportCardScreen). No PDF, no bulk, hardcoded session (ReportCardScreen:50). |
| 18 | Promotion/rollover | **L0** | Not present. |
| 19 | Homework & submissions | **L2** | Assign/list/toggle, attachment upload via Storage manager. Submission status Pending/Submitted/Graded; no camera capture inside app (picker only); no per-student submission records. |
| 20 | Study material | **L2** | Upload wired (CreateAssignment similar); storage rules allow any authed write (§3.2). |
| 21 | Notices | **L2** | Publish/read/filter + URGENT pin + read receipts UI (CommunicationScreens). No era-scoped filtering; principal-only write under rules; in demo mode unprotected. |
| 22 | Push notifications | **L0** | FCM dependency absent; "SMS & Email confirmation" is a hardcoded string (Dialogs.kt:1436). |
| 23 | SMS abstraction | **L0** | Not present (only cosmetic strings). |
| 24 | Parent-teacher messaging | **L0** | Not present. Parent portal folded to student (AuthScreen:185). |
| 25 | Complaints | **L0** | Not present (leave requests are the closest analogue). |
| 26 | Transport | **L2** | Routes/stops/fares seeded + detail UI; **no fee linkage**, no seat/stop allocation, `Double` fares. |
| 27 | Library | **L2** | Catalog + issue counts seeded (LibraryScreen); no lending/returns transactions. |
| 28 | Inventory | **L2** | Asset list + condition (InventoryScreen); no stock movements, no depreciation. |
| 29 | Certificates (TC/Bonafide/Character) | **L0** | Not present (grep verified). |
| 30 | Audit logs | **L0** | Not present. |
| 31 | Data export | **L0** | Not present (AI assistant claims "detailed export" — canned text only, AiAssistantScreen:100). |
| 32 | Backups | **L0** | Not present in code. |
| 33 | Deployment readiness | **L1** | Docs (DEPLOYMENT.md/RUNBOOK.md) exist; live deploy impossible without `google-services.json`, real keys, verified rules, signed release, and backend for webhooks/SMS. |

---

## Audit verdict (headline)

- **The app is a well-built demo (L2) single-APK Android app.** From a fresh clone it does **not** build (FINDING #1, fixed above). With the fix it builds and all 15 unit tests pass; it can run only in Demo mode (no device/emulator here to install on).
- **Nothing in this repo satisfies the production expectations of the spec**: no webhook-verified payments (client callback decides success, signature never checked), no server-side RBAC (Firestore rules are spoofable via email heuristics + `isStudent()==isAuthenticated()`), money is IEEE-754 `Double` end-to-end, no academic-session scoping, no serial-numbered PDF receipts, no atomic multi-write transactions, no audit log, no SMS/FCM, no CSV import, no certificates, no rollover.
- Security: no secrets committed; biggest exposures are Firestore rules privilege escalation + storage any-authed-write + client-authoritative financial success.

PART 1 complete. Part 2 (fix & complete) is deliberately not started — per instructions, work halts here pending review of this audit.