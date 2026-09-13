# ui/theme/

Material Design 3 theme definition for ScholaOS.

## Files

### `Color.kt`

Brand color palette constants. Defines the primary, secondary, tertiary, surface, and error colors used throughout the app. Do not hardcode colors in screen files — reference these tokens instead.

---

### `Theme.kt`

`ScholaTheme` — the root `MaterialTheme` composable wrapper. Applies color scheme, typography, and shapes. All screens are wrapped in this theme via the root composable in `MainActivity.kt`.

---

### `Tokens.kt`

Design token objects for consistent spacing, elevation, motion, corner radius, and role-based accent colors:

| Token object | Contents |
|---|---|
| `Spacing` | Standard spacing scale (4dp, 8dp, 12dp, 16dp, 24dp, 32dp, etc.) |
| `Radius` | Corner radius values (small, medium, large, full) |
| `Elev` | Elevation levels for cards, dialogs, and navigation surfaces |
| `Motion` | Animation duration and easing constants |
| `RoleAccent` | Per-role accent color map (Principal → blue, Teacher → teal, Student → purple, Parent → orange) |

---

### `Type.kt`

Typography scale and `TypeTokens`. Defines the full `Typography` object passed to `MaterialTheme`. Use `TypeTokens` for semantic text style references (e.g. `TypeTokens.labelSmall`) rather than hardcoded `TextStyle`s.
