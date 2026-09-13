# data/datasource/

Contains the data source abstraction, both implementations (Demo and Production), and the environment singleton.

## Files

### `ErpDataSource.kt`

The interface contract for all data access. Every screen in the app reads data from this interface — never from a concrete class directly.

- Read operations return `Flow<T>` for reactive, real-time updates.
- Write/mutation operations are `suspend fun`s.
- This is the seam that allows seamless switching between Demo and Production at runtime.

---

### `AppEnvironment.kt`

A singleton that holds the current runtime mode:

- `DataMode.DEMO` — uses `LocalDemoDataSource`, fully offline, no credentials needed.
- `DataMode.PRODUCTION` — uses `FirebaseDataSource`, requires a valid Firebase project and configured `google-services.json`.
- Also stores `ServiceIntegrationStatus` flags (Razorpay live, Sentry, etc.). All default to `false` until keys are injected via `.env`.

---

### `LocalDemoDataSource.kt`

The in-memory Demo implementation of `ErpDataSource`.

- Backed entirely by `MutableStateFlow`s — no network, no database.
- Seeded on init from `SampleData.kt` with realistic Indian school data (24 students, 6 teachers, full timetable, fee records, etc.).
- All mutations (mark attendance, pay fee, approve leave, submit marks) update in-memory state immediately and propagate through the flows.
- Safe to use in CI, screenshots tests, and emulator runs with no Firebase setup.

---

### `FirebaseDataSource.kt`

The production implementation of `ErpDataSource` backed by Cloud Firestore.

On initialization it:
1. Attaches real-time `addSnapshotListener` to 15 Firestore collections under `schools/{schoolId}/`.
2. Calls `restoreUserSession()` to resume an active Firebase Auth session.
3. If Firestore is empty on first launch, calls `seedInitialFirestoreData()` to batch-write initial records.

All mutations:
- Write optimistically to the local `MutableStateFlow` first for instant UI feedback.
- Then persist asynchronously to Firestore.

Contains full Kotlin serialization helpers (`studentToMap`, `mapDocToStudent`, etc.) for every domain model.

---

### `FirebaseStorageManager.kt`

Handles binary file uploads to Firebase Storage.

- Upload path: `schools/{schoolId}/{folder}/{timestamp}_{filename}`
- Validates MIME types before uploading — allowed: PDF, JPEG, PNG, WebP, HEIC, Word (.doc/.docx), Excel (.xls/.xlsx), video.
- Max file size: 10 MB (enforced server-side via `storage.rules`).
- Throws `IllegalStateException` when called in Demo mode instead of silently faking uploads.
- Used by `ErpDataRepository.uploadAttachment()`.
