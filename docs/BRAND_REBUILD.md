# Revenex brand and presentation rebuild

## Reference decisions

- Image 1: replace the undersized book/wordmark with a prominent RV monogram above the wide REVENEX lettering, on a black brand canvas.
- Image 2: company card is the artwork authority. Native vector contours were traced from the supplied card photograph after perspective analysis; no vector master was supplied. White/charcoal R and restrained gold V variants preserve recognition across themes. The public website currently uses a different R artwork; that asset is not shipped. No card photograph or Netflix artwork/audio is bundled.
- Images 3/4: focused school sign-in, stronger brand/heading hierarchy, clear filled inputs and primary action immediately after credentials. Only configured school email/password sign-in is offered; no fake Google/Apple/sign-up actions. Supabase is explicitly labelled and Local Demo is separate.
- Image 5: accessible two-column persona selection with clear role responsibilities, selected state and a large-text single-column fallback. Student and Parent remain distinct identities.
- Image 6: compact role-specific tasks, semantic pastel accents, register rows, concise invoices and grouped profile/report/settings content. Data-driven counts and unavailable states remain honest.
- Image 7: outgoing brand letters fade on an opaque black stage. The app content remains invisible until that stage is removed, then fades in. There is no wordmark crossfade over Welcome/portal text.

## Scope implemented

Native system splash/launcher icon; shared RV/wordmark in app bars, rails/drawers and account entry; explicit Supabase sign-in; separate offline demo; all routed role dashboards; existing attendance/assignment/fee/inbox/directory/timetable/exam/resource/operations/search/AI presentations; newly rebuilt nested student profile, personal details, academic reports, certificates, marks editor, payment review/pending/error/receipt and search dialog.

Marks save/publish/unpublish, report downloads, attachments, enrollment, invoice/receipt callbacks, Supabase authorization and payment settlement remain attached to the existing interfaces. No schema/RLS reset, reseed, migration replay or server payment change is part of this UI work. Export labels no longer invent a real school affiliation/principal signature; document generation and data inputs are retained.

## Supabase evidence

A read-only request to the configured project's `/auth/v1/settings` returned HTTP 200 with email authentication enabled. This verifies service reachability/configuration, not successful school-profile authentication. The Android entry passes email/password to the existing backend flow; portal choice never grants access. Assigned credentials must still be entered privately on the user's device for hosted acceptance.

## Safe installation

The normal APK keeps package `com.example`. Updating an older differently signed APK still requires its original signing key. An additional `review` build uses `com.example.review` and launcher label **Revenex ERP Review** so it can be installed alongside the old app without deleting its data. Both share the same source, Supabase public configuration and backend security rules; review does not create a separate school/database. No private signing key is put in source or chat. Future updates to an installed review package also need a consistent signing certificate.

CI builds and lints both packages, runs the regular UI/unit workflow tests and verifies both APKs exclude private/server configuration and Firebase dependencies. Final source/artifact/screenshot results are recorded below; local tests and hosted acceptance are deliberately distinguished.

## Final tested application snapshot

- Source branch commit: `e68b414a99bc3b6aa7fd6a3830d1d18e85c888da`.
- CI synthetic merge commit used to produce APKs: `dc5b45b4bf7d23a611a39afe4a842d99ba6ed46c`.
- [Successful CI run 40095052074](https://github.com/Revenex-Ventures/REVENEX-Android-App/actions/runs/40095052074): Android build/tests/lint and server security/policy jobs succeeded. Deployment was skipped by the PR branch guard; this was not a backend deployment.
- Exact Android command: `./gradlew -Dorg.gradle.jvmargs="-Xmx3g -Dfile.encoding=UTF-8" --no-daemon --no-configuration-cache --no-parallel --max-workers=2 -Proborazzi.test.record=true assembleDebug assembleReview testDebugUnitTest lintDebug lintReview verifyNoFirebaseDependencies --console=plain`.
- **119 Android tests; 0 failures, 0 errors, 0 skipped.** Includes real routed Teacher demo, portal hints, explicit Supabase entry, parent identity, no animation replay, reduced motion, tablet/narrow-large-text, preference persistence/System behavior, marks publish workflow, nested report navigation and receipt state contracts.
- Both lint variants: **0 errors/fatal; 78 warnings each**. Warnings remain (dependency updates, existing resource/icon/vector and style findings); lint is not warning-free.
- APK private/server configuration checks ran on both variants. No Firebase dependency or private payment/model/service-role key was introduced.
- 142 PNG artifacts downloaded; 131 in current launch/presentation/redesign/brand-revision suites. Curated galleries exclude the legacy preview suite. These are Compose/Robolectric renders with synthetic data, not proof of hosted transactions.

### Local delivery files

All paths below are relative to the project root `D:/VS Code/Revenex Android`:

| Artifact | Path | SHA-256 |
|---|---|---|
| Side-by-side Review APK (25,119,202 bytes) | `app/build/brand-rebuild-delivery/apk/Revenex-ERP-Review.apk` | `b0e0690e71f0e2c0abaddd1ab45589b72383b8fc9c7bf1021c640d4690177acd` |
| Normal APK (25,955,581 bytes) | `app/build/brand-rebuild-delivery/apk/app-debug.apk` | `fe80ae91a3969d669bc8e922ce35ff1c817edceef697d3721dca69cb75c0f174` |
| All current rendered screens | `app/build/brand-rebuild-delivery/screenshots.html` | Individual full-resolution PNG links |
| Focused galleries | `app/build/brand-rebuild-delivery/{entry,portals,workflows,dark}-gallery.jpg` | Fresh CI renders |
| Machine-readable evidence | `app/build/brand-rebuild-delivery/verification.json` | Source/build/test/lint/APK hashes |
| Emulator captures | `app/build/brand-rebuild-delivery/runtime/` | Separate from fixture renders |

Review identity verified with aapt: `com.example.review`, version `1.0-review`, activity `com.example.MainActivity`, launcher **Revenex ERP Review**. APK signature verification passed; signer SHA-256 `2950aae8a6d49f5021798096387c94bd6a1b30661ebc8af2a8cd3f430b2e17f5`. Both normal and Review installed successfully together on the isolated API34 emulator using `adb install -r`. No uninstall, data clear, account reset or database operation was used. No physical user device was connected during these checks.

### Verified scope and remaining limits

Actual emulator checks reached the new photo entry, Teacher Supabase sign-in, separate demo selection and Teacher dashboard; Settings is reachable through account menu → Settings & profile. Dark selection persisted across process restart to the dark entry screen. The emulator also showed Android System UI/system-process responsiveness dialogs; recording delays are not a reliable app-performance benchmark. Do not interpret synthetic render success as complete physical-device acceptance.

Still requires the user's private device interaction: successful assigned Supabase school login/profile authorization, hosted cross-account writes/inbox reconciliation, and a complete Razorpay TEST captured payment/verified receipt/balance/webhook acceptance. No successful school login, captured payment, production readiness or device push is claimed here. Local Demo remains offline/sample-only and does not perform a real payment. The cloud path remains the existing Supabase integration; role entry is a hint, never authorization.

### Main changed presentation files

`ui/screens/auth/AuthScreen.kt`, `RevenexLaunchHost.kt`; `ui/components/DesignSystem.kt`, `CommonComponents.kt`, `ResponsiveScaffold.kt`, `Dialogs.kt`, `CommandPalette.kt`; `ui/screens/dashboard/TeacherDashboardScreen.kt`; `ui/screens/profile/ProfileScreen.kt`; `ui/screens/academics/ReportCardScreen.kt`, `StaffMarksWorkspace.kt`; theme `Tokens.kt`; native brand/startup resources. Focused MainActivity launch/banner integration and optional Review build/CI packaging are task-required exceptions to the usual UI-only file list. Existing backend logic, permissions and settlement were preserved.

### Actual continuous recordings

- `app/build/brand-rebuild-delivery/runtime/actual-first-use.mp4`: **86.27-second, uncut, silent API34 recording**, untouched normal package on first launch, Light default. Launcher icon → RV native brand and wordmark reveal → photo entry → Principal Supabase sign-in → separate Try demo → actual Principal dashboard. The decoded final frame was inspected. The exact-frame transition storyboard contains the outgoing lettering and separate entry with no overlaid Welcome text.
- `app/build/brand-rebuild-delivery/runtime/actual-launch-entry.mp4`: **108.96-second, uncut, silent API34 recording**, Review package with persisted Dark selection, Teacher sign-in entry → separate Teacher Local Demo → actual Teacher dashboard. Final decoded frame inspected.
- `runtime/09-attendance-dark.png` and `10-warm-resume.png`: actual Teacher attendance navigation and launcher-icon warm resume retained the open register. No save/data mutation was performed.
- `runtime/08-after-restart.png`: actual persisted Dark entry after process restart. `11-first-use-entry-light.png` confirms the other fresh package starts Light.

Both completions use Local Demo, not successful cloud authentication. The emulator experienced System UI/system-process responsiveness dialogs and sparse frame delivery; startup waits and automation pauses are retained. These recordings establish actual routing/content, but **smooth animation timing and startup performance are not accepted on this emulator** and still require a physical-device check. The 1.4-second Compose animation, reduced-motion branch and opaque transition were also checked with controlled render tests. No edited/speeded-up recording or old APK is represented as current evidence.
