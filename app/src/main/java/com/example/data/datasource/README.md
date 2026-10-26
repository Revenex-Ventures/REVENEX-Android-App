# data/datasource/

Contains the data source abstraction, active implementations (Local Demo and Supabase Demo), and supporting cloud adapters.

## Files

### `ErpDataSource.kt`

The interface contract for all data access across the app. Screens and repositories consume data via this contract:
- Read operations return reactive `Flow<T>` streams.
- Mutation/write operations are defined as `suspend fun`s.
- Enables seamless switching between Local Demo and Supabase Demo at runtime.

---

### `AppEnvironment.kt`

A singleton holding the current runtime environment and service flags:
- `DataMode.DEMO` — in-memory demo using `LocalDemoDataSource`, completely offline with no credentials.
- `DataMode.SUPABASE_DEMO` — authenticated cloud mode using `SupabaseDataSource` and protected profiles.
- Service status flags for optional integrations (e.g. server-side Razorpay test payments).

---

### `LocalDemoDataSource.kt`

The in-memory implementation of `ErpDataSource`.
- Seeded on initialization from `SampleData.kt` with realistic Indian school data.
- Backed by Kotlin coroutine `MutableStateFlow`s for instant state propagation.
- Fully offline and deterministic for development, unit tests, and CI.

---

### `SupabaseDataSource.kt`

The cloud implementation of `ErpDataSource` backed by Supabase:
- Enforces multi-tenant isolation and Row Level Security (RLS) via composite keys (`school_id`, `session_id`).
- Real-time updates via Supabase Realtime channel subscriptions (`supabase_realtime`).
- Maps PostgreSQL records directly into domain models (`Models.kt`).
- Handles database-driven notification feeds and audit logging.

---

### `SupabaseProvider.kt`

Provides the validated public-only Supabase client instance:
- Reads `SUPABASE_URL` and `SUPABASE_PUBLISHABLE_KEY` from Android configuration.
- Manages in-memory Auth sessions. Never stores private keys or service roles in client code.

---

### `SupabaseFileStore.kt` & `DocumentFiles.kt`

Handles private document access and storage:
- Resolves durable `supabase://erp-documents/...` references to temporary, time-bounded signed URLs.
- `DocumentFiles.kt` provides shared metadata structures, MIME type validation, and file size limits.

---

### `RazorpayPayments.kt`

Client-side payment contracts for optional test checkout:
- Calls backend Edge Function `razorpay-payments` for server-verified order creation and capture reconciliation.
- Client never holds secret payment keys or modifies balances directly.

---

### `SupabaseAssistantChat.kt`

Client adapter for the server-bounded AI assistant:
- Sends sanitized user queries and JWT session to `supabase/functions/revenex-ai/`.
- Edge Function evaluates permissions, tenant scope, and context on the server side.

---

### `SupabaseSignInDiagnostics.kt`

Diagnostic utilities for troubleshooting Supabase authentication, connection health, and profile association failures.

