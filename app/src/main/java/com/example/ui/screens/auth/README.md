# screens/auth/

Login and authentication screen.

## Files

### `AuthScreen.kt`

The first screen shown to unauthenticated users.

- **Demo mode**: A single tap on "Enter Demo Mode" logs in with a pre-selected role (Principal/Teacher/Student/Parent) using `LocalDemoDataSource`. No credentials required.
- **Production mode**: Shows email + password fields and calls `loginWithFirebase()` on the repository. On success, reads the user's custom JWT claims to determine their role and navigates to the correct dashboard.
- Includes a toggle to switch between Demo and Production mode.
- Handles loading states and displays Firebase Auth error messages.

Accessible to: all users (unauthenticated).
