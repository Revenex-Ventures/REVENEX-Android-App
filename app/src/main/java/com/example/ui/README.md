# ui/

The UI layer of the ScholaOS app. All Jetpack Compose screens, components, navigation, and theming live here.

## Directories

| Directory | Purpose |
|---|---|
| `screens/` | Individual feature screens grouped by domain (auth, dashboard, fees, etc.) |
| `components/` | Shared reusable composables used across multiple screens |
| `navigation/` | Screen route definitions and role-based bottom nav configuration |
| `theme/` | Material Design 3 theme: colors, typography, tokens |

## Architecture

All screens are stateless Composables that receive data via `collectAsState()` from `ErpDataRepository`. No screen holds its own data — it only holds ephemeral UI state (dialog open/closed, selected tab, search query, etc.).

The adaptive `ResponsiveScaffold` renders a bottom navigation bar on phones and a navigation rail on tablets automatically.
