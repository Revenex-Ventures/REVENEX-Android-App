# screens/communication/

School notices and in-app notification center.

## Files

### `CommunicationScreens.kt`

Two-tab screen:

**Notices tab:**
- List of school circulars and announcements, sorted by date.
- Each notice shows title, category (Academic, Administrative, Events, Emergency), date, and importance flag.
- Important notices are visually highlighted.
- Principal can tap the FAB to open `CreateNoticeDialog` and publish a new circular.
- All roles can read notices.

**Notifications tab:**
- In-app notification feed scoped to the logged-in user's role.
- Categories: fee due reminders, attendance alerts, new assignment posted, leave status updates, new notice.
- Unread count badge shown on the bottom nav tab.
- Tap to mark as read; swipe to dismiss.

Accessible to: All roles (notices are broadcast; notifications are role-scoped).
