# screens/profile/

User profile and app settings.

## Files

### `ProfileScreen.kt`

Displays the logged-in user's profile and account settings.

- Profile photo, display name, role badge, and school name.
- Role-specific details:
  - Principal: employee ID, designation, contact.
  - Teacher: employee ID, subjects, assigned classes.
  - Student: admission number, class, section, parent contact.
  - Parent: linked children list with quick-switch capability.
- **Demo / Production mode toggle** — switches `AppEnvironment.currentMode` and rebinds the repository data source.
- **Sign Out** button — calls Firebase Auth `signOut()` and navigates back to `AuthScreen`.
- App version and build info displayed at the bottom.

Accessible to: All roles (each user sees only their own profile).
