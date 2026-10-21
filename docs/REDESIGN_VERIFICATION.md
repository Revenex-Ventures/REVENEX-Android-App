# Revenex redesign verification

## Scope and evidence rules

This is the existing Kotlin/Compose app, with no new UI libraries and no database
reset. See [the screen and flow inventory](REVENEX_REDESIGN_CHECKLIST.md).
Screenshot captures are actual app renders; they are not approved pixel baselines.
Local PostgreSQL fixtures do not certify hosted Supabase Auth or Realtime transport.

## Latest completed checks

- Android build, 79 tests, and lint passed in the completed intermediate run
  (`app/build/redesign-final-verification.log`): lint 0 errors, 68 warnings.
- PostgreSQL/PGlite schema and RLS fixtures: **105 passed**.
- AI/payment Edge Function security tests: **31 passed**.
- Working tree and Git history credential scan: passed.
- Theme text-pair contrast test: at least 4.5:1 in both palettes.
- The final expanded suite adds three login/settings/form captures. Final results
  will be recorded here after the isolated local build or clean CI runner completes.

## Commands

```powershell
.\gradlew.bat '-Dorg.gradle.jvmargs=-Xmx3g' --offline --no-daemon --no-configuration-cache --no-parallel --max-workers=1 '-Proborazzi.test.record=true' :app:assembleDebug :app:testDebugUnitTest :app:lintDebug --console=plain
node supabase/tests/rls.test.mjs
node --test supabase/tests/assistant-edge.test.mjs supabase/tests/razorpay-edge.test.mjs
python scripts/check_secrets.py --history
python scripts/check_apk_boundary.py app/build/outputs/apk/debug/app-debug.apk
git diff --check
```

The final local run uses a task-local Gradle user home and a copy of dependencies
excluding lock files. The shared Windows cache stalled in metadata resolution and
then retained a journal lock; no shared cache files were deleted. CI uses the same
Gradle 9.3.1 wrapper with JDK 21 and records the Compose screenshots.

## Rendered coverage and refinements

- Five implemented role dashboards: light/dark, compact phone; admin tablet rail;
  student 320dp/1.5x font; authentication/settings/admission/inbox captures.
- Twenty-three secondary entry screens rendered in both palettes: directories,
  details, academics, attendance, results/profile hub, fees, notices/inbox, module
  hub, leave, transport, library, inventory, reports, search and AI.
- Visual inspection found and corrected dark selected-state contrast, unknown
  badges, clipped chart labels, squeezed attendance actions, a collapsed student
  status badge, unreadable identity wells and narrow-screen metadata wrapping.
- Source-level theme changes also cover nested forms, tabs, dialogs and sheets;
  every nested screen/state has not been independently exercised.

## Actual emulator checks

Local principal login/navigation, all three appearance choices, Dark persistence
through force-stop/relaunch, System reaction to Android night-mode change, and
keyboard-open admission with scrolling were exercised. These were local demo
actions; no student was submitted to the hosted project.

Actual before screenshots came from the exact pre-change CI APK at commit
`2f365b22006491953bc8c4943110ab216a09ca4c`. It was re-signed with the existing local
debug key without code changes so it could be installed on the same isolated
emulator without uninstalling the app. Screenshots are in
`app/build/reports/redesign/device/before-*-head-2f365b2.png`.

## Hosted and product gaps

- No authenticated hosted two-account assignment/inbox/marks/fee test was run.
  A public publishable key cannot establish those sessions.
- Actual WebSocket loss/reconnect and Storage HTTP behavior need hosted testing.
- Migration 9 is supplied and tested locally; it has not been applied remotely.
- Staff marks entry/publication methods exist but have no reachable Android editor
  or publication control. SQL policy tests are not an end-to-end app workflow.
- Inbox navigation opens the relevant module; existing notifications do not carry
  a record target ID. Reconciliation runs every 30 seconds while the process runs.
- Cloud faculty/student profile edits, school-issued certificates, library
  circulation and leave approval remain operator-managed or unsupported. Local
  demo features remain available. Sample charts are labeled illustrative.
- Device push, live provider payments, model inference, TalkBack traversal, all PDF
  child states, every keyboard/orientation and launch-frame timing are unverified.
- Additional SQL roles do not yet have Android portals; no UI role switch grants
  cloud access. Existing backend RLS and school/session/child boundaries remain.
