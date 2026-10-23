> Previous ivory delivery record. Superseded for the latest implementation/APK by [CURRENT_DELIVERY.md](CURRENT_DELIVERY.md). Retained as historical evidence; do not reuse its test count or APK hash for newer source.

# Revenex ivory/orange delivery — 2026-10-23

## Delivered application

Tested source: `b604edd0cc56bf962d541f823cd12b7cbae42998`, branch `codex/revenex-reference-redesign`, draft PR #11.
CI: https://github.com/Revenex-Ventures/REVENEX-Android-App/actions/runs/39963704791

APK: `app/build/revenex-ivory-release/app-debug.apk` (25,831,912 bytes).
SHA-256: `5CE4EC90DAC550AA39D284098706C16DA4E4FBDA7E5DB5E559015FB544D2A458`.
This is a debug artifact, not a production signing/release claim. Existing installations signed with another key can reject an update; do not uninstall user data merely to bypass this.

## Actual UI scope

One shared ivory/white, charcoal and orange identity, with sage/lavender/blue/butter supporting surfaces and a coordinated warm dark palette. Existing Light/Dark/System preference persists and defaults to Light. No new UI framework or library.

Composition changes: compact dashboard task panels and metric tiles; role/name context; large-text-safe navigation; compact adaptive module rows; focused authentication; assignment subject/status/deadline hierarchy; dated attendance register with view-only history and persistent save feedback; exact fee totals and paid-invoice receipt lookup; grouped student contact sections; inbox category/unread metadata; selected appearance and attendance states; semantic dark-mode admin/report/leave controls.

Shared changes reach principal, admin, teacher, student and parent portals plus secondary screens. Rendered entry coverage includes authentication, all five dashboards, modules, students/details, teachers/details, attendance, assignments, timetable, examinations/results, fees, notices/inbox, AI chat, search, reports, profile/settings, admission form and operations pages. This is not a claim that every nested interaction received a fresh composition rewrite. See `IVORY_REDESIGN_CHECKLIST.md` for outstanding states.

Changed implementation groups: `ui/theme/{Color,SemanticColors,Theme,Tokens}.kt`; `ui/components/{CommonComponents,DesignSystem,Dialogs}.kt`; auth, academics, attendance, communication, admin/student dashboards, fees, operations, profile, reports and student detail screen files; native theme XML; payment error/receipt wiring in MainActivity and RazorpayPayments. Theme contrast, receipt dialog and payment contract tests accompany those changes. Other locally edited README files were preserved.

## Verification and limits

- CI command: `./gradlew '-Dorg.gradle.jvmargs=-Xmx3g -Dfile.encoding=UTF-8' --no-daemon --no-configuration-cache --no-parallel --max-workers=2 -Proborazzi.test.record=true assembleDebug testDebugUnitTest lintDebug verifyNoFirebaseDependencies --console=plain` — passed.
- 81 Android tests: zero failures/errors/skips. Lint: zero errors, 69 warnings. APK private-key/server-prompt boundary and Firebase dependency checks passed.
- `npm test --prefix supabase/tests` — 105 PostgreSQL/PGlite schema/RLS checks passed, rerun after hosted setup work. These simulate multiple SQL identities; they are not two real Android accounts.
- `node --test supabase/tests/razorpay-edge.test.mjs` — 15 passed, rerun. Covers unsigned callers, amount overrides, invalid signatures, captured/pending/failed payments, forged webhooks and replay-related behavior with mocked provider/database dependencies.
- Rendered evidence: `app/build/revenex-ivory-verification/reports/redesign/` (five portal light/dark pairs, 46 secondary light/dark captures, auth/settings/admission, 320dp at 1.5 font scale and 800dp admin). Images were inspected; large-font dock and dark admin contrast were refined using these renders. Before/after comparisons: `app/build/reports/ivory-delivery/`.
- Emulator install and Activity launch succeeded using a locally debug-signed copy whose non-signature ZIP entries matched the CI APK. Subsequent ADB capture failed due to host process/resource errors. No current physical-device or keyboard-open screenshot proof is claimed.
- Still unverified: every nested dialog/sheet/error/IME state, TalkBack traversal, rotation, process-restart appearance on device, true two-account Android assignment/inbox/reconnect/account-switch flow, hosted payment success/cancel/failure/webhook replay, and physical-device gesture navigation.
- Unsupported existing workflows remain honest limitations: staff Android marks entry/publication editor, some profile/faculty/operator mutations and library/leave/certificate workflows. Additional SQL roles do not imply additional implemented Android portals. No background push integration is claimed.

## Hosted repair performed

Before repair, authenticated SQL checks found the teacher Auth account confirmed, four provisioned demo profiles, zero public tables without RLS, but no payment schema or enrollment RPC. Edge Functions had no deployed functions and no custom secrets.

Applied the existing reviewed migrations 6, 7, 8 and 9 in order via SQL Editor. Each transaction returned success. No reset, seed replay, school-data deletion or RLS weakening was performed. Follow-up query confirmed payment schema, enrollment RPC, assistant quota schema and published-result notification trigger all exist; zero public tables lack RLS. SQL Editor application does not populate CLI migration history automatically; do not blindly rerun these migrations.

Deployed the repository's `razorpay-payments/index.ts` and `core.mjs` together. With the user's action-time approval, disabled the legacy gateway JWT check to support HMAC-verified webhooks; the handler retains Supabase Auth verification for user actions. Anonymous POST now returns HTTP 401 with the handler's sign-in message. Payment relation now returns 401 rather than 404. Auth health is 200. AI function remains undeployed (404).

Server custom secrets were initially absent. The operator entered them directly and corrected a trailing comma in the key-secret name. All three exact Razorpay secret names are now present. Unsigned empty user requests return 401 and unsigned empty webhook requests return 400 `Invalid webhook signature.`, replacing the earlier missing-config 503. Values were not opened or displayed. These probes cannot establish credential pairing or provider connectivity. Provider order creation, capture and webhook delivery remain unverified. A successful Android callback never directly credits a fee. Only server-verified capture settles the database transaction.

CI build/security gates are operational; the automated deployment job was skipped by its branch/environment gate. Manual payment deployment above is separate from proving automated deployment. No merge to main was performed.

## Sources informing implementation

- Android Compose Material 3: https://developer.android.com/develop/ui/compose/designsystems/material3
- Compose accessibility defaults: https://developer.android.com/develop/ui/compose/accessibility/api-defaults
- Material segmented control API: https://developer.android.com/reference/kotlin/androidx/compose/material3/SegmentedButtonDefaults
- Razorpay server order and signature requirements: https://razorpay.com/docs/payments/payment-gateway/android-integration/standard/integration-steps/
- Supabase function authentication and secrets: https://supabase.com/docs/guides/functions/auth and https://supabase.com/docs/guides/functions/secrets

User images informed composition and visual direction; provider documentation informed API/security details. Neither compilation nor anonymous HTTP rejection establishes complete hosted workflow acceptance.
