# Revenex Student portal — reference implementation

The current Student experience replaces the old shared family presentation with native routed Compose pages matching the supplied Student home and destination board. Principal, Teacher and Parent presentations remain separate. Existing Supabase Auth, repositories, RLS, private document opening and Razorpay server-created-order/verified-settlement logic are retained.

## What changed

- Cool-white canvas, orange actions, peach/mint/blue/lavender surfaces and a consistent colored outline icon family. Full pastel shortcut panels retain the reference's color mapping.
- Home: greeting, real next lesson or honest unavailable state, attendance/task summaries, six school shortcuts, due work and latest school notice.
- Home / Learn / Schedule / Inbox / More navigation; linked assignment, invoice, notice and profile destinations.
- Read-only saved-attendance calendar, class assignments and confirmed completion status, published-only results/PDF access, weekday timetable, fees/invoices/recorded receipts, catalogue, recipient-source inbox, notice detail and personal profile.
- Settings retain the existing persisted Light / Dark / System choice. Student-specific reduced motion, quiet confirmation sound and optional haptics are persisted public presentation preferences; sound/haptics default off.
- Student identity uses the verified linked student ID and school. Changing a presentation-selected child cannot change Student identity. School/session/class filtering supplements existing backend authorization; it does not replace RLS.

## Evidence and delivery

Artifacts: `app/build/student-image-delivery/apk/app-debug.apk`, `manifest.json`, `source.json`, `light-gallery.html`, `dark-gallery.html`, `comparisons.html`, `runtime-gallery.html`, `adaptive-gallery.html` and `runtime/student-motion.mp4`. The manifest identifies the exact local source checkpoint and APK SHA-256. The checked-out branch and existing user changes are preserved; this delivery does not claim a GitHub upload or a new CI run.

Final normal checks: **163 local tests across 43 suites, zero failures/errors/skips; lint zero errors and 80 warnings; debug APK build and no-Firebase dependency gate passed.** The focused final Student screenshot run passed **12 tests**, producing 36 Light/Dark production-Compose renders plus three adaptive renders. The APK was installed with `adb install -r` on the API-34 emulator; all primary Student routes, completion-status save/undo, Local Demo payment guard, theme changes/restart, reduced-motion persistence, keyboard search, and Local Demo logout/account switching were exercised. See the runtime manifest for exact evidence.

Commands:

```powershell
.\gradlew.bat --no-daemon --no-configuration-cache --max-workers=1 assembleDebug testDebugUnitTest lintDebug :app:verifyNoFirebaseDependencies
.\gradlew.bat --no-daemon --no-configuration-cache --max-workers=1 -Proborazzi.test.record=true testDebugUnitTest --tests com.example.StudentImageSpecTest
```

One earlier full screenshot-recording run failed the existing API-28 partial-PDF cleanup assertion on Windows. The unchanged focused PDF test and final normal full suite passed. The failure log is retained; no assertion was skipped or weakened. Screenshot recording is distinguished from the normal complete suite.

The installed-app walkthrough exposed and fixed saved-tab restoration reopening a notice, and a Student More route still pointing at the legacy hub. The final APK uses the new Student renderer at that route. Back navigation and primary landing behavior now have a real NavHost regression test.

See `docs/STUDENT_REFERENCE_CHECKLIST.md` and the packaged `SOURCE_CHANGES.txt` for page coverage, changed files and deliberate differences. Native renders and actual installed-APK captures have separate galleries; compilation is not counted as visual verification.

## Honest integration limits

- The existing assignment API confirms completion status; it has no student submission-file/note persistence contract. The UI does not fabricate upload, note persistence or teacher review success.
- The current library model exposes catalogue/availability, not individual loans or due dates. Borrowed records are explicitly unavailable.
- Notices containing an attachment name without a private file reference cannot offer a fake download link. Existing canonical private document links use the established authorized opener.
- Results remain empty when the actual records are drafts. No illustrative grades or class average are invented.
- Local sample fees do not launch a fake payment. Cloud sign-in is required for Razorpay TEST checkout; only verified server settlement can produce a paid balance/receipt.
- Device push is not configured. Inbox entries are in-app updates from the existing source, with confirmed read-state calls.
- Hosted Student authorization, cross-account notification isolation, real private-file retrieval and an actual Razorpay TEST transaction require a signed-in hosted account/device session. Local tests and emulator captures must not be described as those hosted checks.

No hosted database reset, reseed or deployed migration is part of this UI delivery. No new libraries or private credentials were added to the APK.

## Hosted acceptance after this UI delivery

Use an assigned synthetic Supabase student/parent account on your device. Open Fees → an outstanding invoice → Pay, confirm Razorpay TEST mode, complete the official mock-success flow yourself, then return and use Check payment status. A verified receipt and refreshed server balance are the acceptance criteria. No real/test card credentials, OTPs or private keys belong in chat or Android configuration. This delivery did not observe that hosted transaction.

School-owned identity/photo changes and official certificates require the school's authorized workflow. Legacy Local Demo self-editing/document generation is not presented as proof of a school-issued record. Existing operator/other-role code remains available; no hosted identity-write permission was added.
