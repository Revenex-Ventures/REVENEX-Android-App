# REVENEX SCHOOL ERP — ARCHITECTURE BLUEPRINT

## 1. Architectural Pattern
Revenex School ERP follows the modern **Clean Architecture & MVVM** pattern recommended by Android Jetpack, emphasizing separation of concerns, testability, and deterministic state transitions.

```
┌─────────────────────────────────────────────────────────────┐
│                       UI LAYER (M3)                         │
│   • Jetpack Compose Screens (Principal, Teacher, Parent...) │
│   • Reusable Design System Components                       │
│   • Type-safe Navigation (Screen Sealed Hierarchy)          │
└──────────────────────────────┬──────────────────────────────┘
                               │ StateFlow Collection
                               ▼
┌─────────────────────────────────────────────────────────────┐
│                    REPOSITORY LAYER                         │
│   • ErpDataRepository (Singleton State Holder & Coordinator)│
│   • Cross-Module Synchronizers (Attendance ↔ Fees ↔ Leave)  │
│   • AppEnvironment State (Demo Mode vs Production Mode)     │
└──────────────────────────────┬──────────────────────────────┘
                               │
               ┌───────────────┴───────────────┐
               ▼                               ▼
┌─────────────────────────────┐ ┌─────────────────────────────┐
│    LOCAL DEMO DATA SOURCE   │ │    FIREBASE DATA SOURCE     │
│ • Fully populated dataset   │ │ • Firebase Auth             │
│ • In-memory CRUD mutations  │ │ • Cloud Firestore Schema    │
│ • Zero credential required  │ │ • Cloud Storage & Messaging │
└─────────────────────────────┘ └─────────────────────────────┘
```

---

## 2. Key Modules & Layer Responsibilities

### UI & Presentation Layer
- **Composables**: Pure declarative UI without business logic or network access.
- **State Collection**: Uses `collectAsState()` on `StateFlow` primitives to ensure reactive UI re-renders on model changes.
- **Navigation Controller**: Centralized `NavHost` handling route transitions, bottom navigation tabs, role switching, and modals.

### Domain & Repository Layer (`ErpDataRepository`)
- Acts as the single source of truth for the entire application.
- Exposes immutable `StateFlow` streams (`students`, `teachers`, `feeRecords`, `leaveRequests`, etc.).
- Orchestrates multi-entity side effects (e.g. marking attendance updates the class record, the student's individual attendance percentage, and notifies the principal).

### Data Layer Abstraction (`ErpDataSource`)
- **`ErpDataSource`**: Common interface covering every operational entity.
- **`LocalDemoDataSource`**: Provides immediate, friction-free offline capabilities with realistic school data.
- **`FirebaseDataSource`**: Connects directly to Google Cloud Firestore collections when credentials are provisioned.
