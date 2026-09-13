# screens/search/

Global search across all app data.

## Files

### `GlobalSearchScreen.kt`

Full-text search interface that queries across multiple data domains simultaneously.

- Single search input field with debounced queries (avoids firing on every keystroke).
- Results grouped by category: Students, Teachers, Notices, Assignments, Study Materials.
- Each result is tappable and navigates directly to the relevant detail screen.
- Search scope is role-restricted — Teachers cannot find records they are not authorized to view.
- Empty state with suggested quick actions when the search field is empty.

Accessible to: All roles (results filtered by role permissions).
