# data/repository/

Contains the central repository singleton (`ErpDataRepository`) and static sample dataset.

## Files

### `ErpDataRepository.kt`

The single source of truth for the UI layer. Access is managed via a thread-safe singleton pattern (`getInstance()`).

**Key responsibilities:**
- Exposes domain entities and UI state as Kotlin `StateFlow<T>` streams (students, faculty, attendance, fees, notices, timetable, etc.).
- `bindDataSource(source: ErpDataSource)` — attaches reactive collectors that pipe underlying data source flows into repository `MutableStateFlow`s.
- `setDataMode(mode: DataMode)` — switches between `DataMode.DEMO` (local in-memory) and `DataMode.SUPABASE_DEMO` (authenticated Supabase cloud). Cancels existing collectors, resets session state, and cleanly rebinds.
- `loginWithSupabase(email, password)` — authenticates against Supabase Auth, retrieves the tenant-isolated profile, and restores user session.
- Business logic operations: recording roll calls with audit entries, processing fee invoices, calculating student attendance percentages, and resolving document references via `SupabaseFileStore`.

---

### `SampleData.kt`

Static dataset used to seed `LocalDemoDataSource` with realistic school ERP data.
- Includes students across multiple grades, faculty members, timetables, and demo fee ledgers.
- Used strictly in Local Demo mode and tests; never written to or blended with cloud data.

