# data/

The data layer of the ScholaOS app. Handles all data access, domain models, and state management.

## Directories

| Directory | Purpose |
|---|---|
| `model/` | All domain model classes and enums (students, teachers, fees, attendance, etc.) |
| `datasource/` | Data source interface, Demo and Production implementations, and environment config |
| `repository/` | Central repository singleton that exposes reactive `StateFlow`s to the UI |

## Design

The data layer is built around an `ErpDataSource` interface with two concrete implementations:

- **`LocalDemoDataSource`** — fully in-memory, no network required, seeded with realistic Indian school data.
- **`FirebaseDataSource`** — connects to Firestore with real-time listeners and persists all mutations.

`ErpDataRepository` binds to whichever source is active and re-exposes data as `StateFlow`s consumed by the UI. Switching Demo ↔ Production at runtime rebinds all collectors cleanly.
