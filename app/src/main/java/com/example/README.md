# app/src/main/java/com/example/

Root Kotlin package for the ScholaOS Android app.

## Structure

```
com/example/
├── MainActivity.kt       — Single Activity entry point
├── data/                 — Data layer: models, data sources, repository
└── ui/                   — UI layer: screens, components, navigation, theme
```

## Architecture

The app follows a **Repository pattern** with a clean separation between data and UI:

- The `data/` layer defines domain models, abstracts data access behind an interface, and exposes reactive `StateFlow` streams.
- The `ui/` layer consumes those streams via `collectAsState()` in Jetpack Compose screens.
- `MainActivity.kt` is the single host Activity that wires navigation, handles Razorpay payment callbacks, and renders the root `ScholaErpApp` composable.

## `MainActivity.kt`

The app's single Activity. Responsibilities:
- Sets up edge-to-edge display
- Pre-loads the Razorpay `Checkout` SDK on startup
- Hosts the `NavHost` with animated screen transitions (fade + slide)
- Implements `PaymentResultWithDataListener` to receive Razorpay success/failure callbacks
- Renders all global modal dialogs (Add Student, Add Teacher, Create Notice, Create Assignment, Apply Leave, Fee Payment) as overlay composables
