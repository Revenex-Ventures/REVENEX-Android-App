# ui/theme/

Material Design 3 theme definition for REVENEX School ERP.

## Files

### `Color.kt`

Brand color palette constants. Defines primary (`ScholaTerracotta`), secondary, tertiary, surface, ink, and status colors with accessible contrast across light and dark palettes. Do not hardcode colors in screen files — reference these tokens instead.

---

### `Theme.kt`

`ScholaTheme` — the root `MaterialTheme` composable wrapper.
- Applies color schemes, typography, and shape tokens.
- Supports persisted appearance preferences: `ThemeMode.LIGHT`, `ThemeMode.DARK`, and `ThemeMode.SYSTEM`.
- Sets system bar and navigation bar styling according to active contrast rules.

---

### `Tokens.kt`

Design token objects for consistent layout, rhythm, elevation, and motion:

| Token object | Contents |
|---|---|
| `Spacing` | Standard spacing scale (4dp, 8dp, 12dp, 16dp, 20dp, 24dp, 32dp, 48dp) and semantic padding aliases |
| `Radius` | Corner radius values (`hero` 16dp, `tile` 14dp, `input` 14dp, `pill` 999dp) |
| `Elev` | Hairline border and subtle elevation definitions |
| `Motion` | Spring and tween animation specs |
| `RoleAccent` | Per-role accent colors (Principal, Admin, Teacher, Student, Parent) |

---

### `Type.kt`

Typography scale and `TypeTokens`. Defines the full `Typography` object passed to `MaterialTheme`. Use `TypeTokens` for semantic text style references rather than hardcoded `TextStyle`s.
