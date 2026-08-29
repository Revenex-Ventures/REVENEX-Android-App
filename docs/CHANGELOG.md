# REVENEX SCHOOL ERP — CHANGELOG

All notable changes and production upgrades are documented in this log.

---

## [4.3.0-PHASE2] - 2026-08-28

### Phase 2 Complete — Production Validation & Serialization
- **Exam & Report Card Firestore Mappers**: Implemented `examScheduleToMap()`, `mapDocToExamSchedule()`, `reportCardToMap()`, and `mapDocToReportCard()` in `FirebaseDataSource.kt`.
- **Snapshot Listeners**: Registered snapshot listeners for `exams` and `report_cards` collections in `FirebaseDataSource.kt`.
- **Firestore Seeding**: Added initial exam schedules and report cards to `seedInitialFirestoreData()` batch commit.
- **Phase 2 Audit**: Created [`docs/PHASE_2_AUDIT.md`](file:///c:/Users/Prasanna/Downloads/revenex-school-erp/docs/PHASE_2_AUDIT.md) verifying code integrity across all 24 ERP modules.
- **Feature Completion Matrix**: Created [`docs/FEATURE_COMPLETION_MATRIX.md`](file:///c:/Users/Prasanna/Downloads/revenex-school-erp/docs/FEATURE_COMPLETION_MATRIX.md) providing a 10-column verification matrix for all system capabilities.

---

## [4.2.0-PROD] - 2026-08-28

### Phase 1 Upgrade
- **Build Infrastructure**: Installed missing Gradle wrapper scripts (`gradlew`, `gradlew.bat`, `gradle-wrapper.jar`), added `local.properties` configuration, and extracted `debug.keystore` for debug builds.
- **Repository Architecture**: Complete refactoring of `ErpDataRepository` to eliminate the triple-write problem and race conditions.
- **Attendance Percentage Engine**: Replaced artificial `+0.3` / `-1.2` delta modifications with a real calculation engine based on actual recorded session history in both `LocalDemoDataSource` and `FirebaseDataSource`.
- **Multi-Tenant Isolation**: Added `schoolId` properties to all 17 data models and `status` fields to Student and Teacher models.
- **Examination Module**: Added `ExamSchedule` data flow to `ErpDataSource`, `LocalDemoDataSource`, `FirebaseDataSource`, and `ErpDataRepository`. Created `ExamsScreen` composable and wired `Screen.Exams` route in `MainActivity`.
- **Authentication Resilience**: Removed silent fallback to demo mode on Firebase error. Auth failures now report clean, user-friendly error messages.
- **Manifest & Network Security**: Added `INTERNET` and `ACCESS_NETWORK_STATE` permissions to `AndroidManifest.xml`.
- **Backend Security Rules**: Authored production-grade `firestore.rules` and `storage.rules` for tenant isolation and role authorization.
- **Branding & Documentation**: Removed development platform branding from `README.md`. Created comprehensive documentation suite in `/docs`.
