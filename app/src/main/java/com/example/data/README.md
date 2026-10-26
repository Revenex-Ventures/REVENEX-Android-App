# data/

The data layer of the REVENEX School ERP. Handles all data access, domain models, and state management.

## Directories

| Directory | Purpose |
|---|---|
| `model/` | All domain model classes and enums (students, teachers, fees, attendance, etc.) |
| `datasource/` | Data source interface, Local Demo and Supabase Demo implementations, and cloud adapters |
| `repository/` | Central repository singleton (`ErpDataRepository`) that exposes reactive `StateFlow`s to the UI |

## Design

The data layer is built around an `ErpDataSource` interface with two active implementations:

- **`LocalDemoDataSource`** — fully in-memory, no network required, seeded with realistic Indian school data.
- **`SupabaseDataSource`** — connects to Supabase PostgreSQL with Row Level Security, Realtime subscriptions, and private document storage via `SupabaseFileStore`.

`ErpDataRepository` binds to whichever source is active and re-exposes data as `StateFlow`s consumed by the UI. Switching between Local Demo and Supabase Demo cleanly rebinds all collectors. Firebase backend and adapters have been retired and removed.
