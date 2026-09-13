# ui/components/

Shared reusable Jetpack Compose components used across multiple screens.

## Files

### `CommandPalette.kt`

Global command palette modal (triggered via a search/command button). Provides keyboard-style search-driven navigation and quick actions across the entire app — similar to ⌘K in desktop apps. Allows jumping to any screen or triggering actions (create notice, add student, etc.) from anywhere.

---

### `CommonComponents.kt`

Library of shared composable building blocks used throughout all screens:

- Stat tile cards (number + label, used on dashboards)
- Role-colored chips and badges
- Section header rows
- Loading skeletons and empty state placeholders
- Reusable list item layouts

---

### `DesignSystem.kt`

Low-level design system primitives. Wraps Material3 components with app-specific defaults (padding, shape, color). Keeps individual screens clean by abstracting repeated styling into named components.

---

### `Dialogs.kt`

All global modal dialogs rendered as overlays from `MainActivity.kt`:

| Dialog | Purpose |
|---|---|
| `AddStudentDialog` | Form to create a new student record |
| `AddTeacherDialog` | Form to onboard a new teacher |
| `CreateNoticeDialog` | Compose and publish a school circular |
| `CreateAssignmentDialog` | Create a new homework assignment with optional attachment |
| `ApplyLeaveDialog` | Submit a leave request (teacher or student) |
| `FeePaymentDialog` | Initiate a Razorpay fee payment for a selected invoice |

These are declared at the Activity level so they can be triggered from any screen via shared state in `ErpDataRepository`.

---

### `ResponsiveScaffold.kt`

Adaptive scaffold that automatically switches layout based on screen size:

- **Phone (< 600dp width)**: Renders a `BottomNavigationBar` with 5 role-specific tabs.
- **Tablet (≥ 600dp width)**: Renders a `NavigationRail` on the left side instead.

All screens are wrapped in this scaffold. It reads the current `UserRole` to display the correct set of navigation items from `NavConfig.getBottomNavItems()`.
