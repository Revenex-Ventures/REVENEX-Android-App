# Revenex — approved presentation delivery

25 October 2026. This is the current consolidated delivery record; earlier README/build reports are historical.

## Application and artifacts

- Tested application commit: `88db806681929e6794062c52445f8ab65adc21f6` on `codex/revenex-reference-redesign`.
- CI merge commit: `e93156609414af754aeeaa05abc14b555251a3a3`.
- Passing independent run: [40123461452](https://github.com/Revenex-Ventures/REVENEX-Android-App/actions/runs/40123461452).
- [Side-by-side review APK](app/build/approved-board-delivery/apk/Revenex-ERP-review-88db806.apk), package `com.example.review`.
- [Standard APK](app/build/approved-board-delivery/apk/Revenex-ERP-demo-88db806.apk), package `com.example`.
- [Light gallery](app/build/approved-board-delivery/light-gallery.html), [Dark gallery](app/build/approved-board-delivery/dark-gallery.html), [actual runtime gallery and recordings](app/build/approved-board-delivery/runtime-gallery.html).
- [Actual final-CI launch recording](app/build/approved-board-delivery/runtime/final-ci-launch-demo.mp4): launcher icon → RV/REVENEX animation → photo entry → explicit Supabase school sign-in → separate Local Demo → Principal dashboard. No hosted login or payment occurs in this video.
- [Principal comparison](app/build/approved-board-delivery/principal-latest-comparison.png), [Teacher comparison](app/build/approved-board-delivery/teacher-latest-comparison.png), [Attendance comparison](app/build/approved-board-delivery/attendance-latest-comparison.png).
- [Screen/flow checklist](docs/APPROVED_SCREEN_CHECKLIST.md), [implementation and research](docs/APPROVED_BOARD_IMPLEMENTATION.md), [cumulative changed files](app/build/approved-board-delivery/CHANGED_FILES.txt), [checksums](app/build/approved-board-delivery/final-artifact-checksums.json).

Review APK SHA-256: `1321cc37d3ae33d19bedd8049f341bd3674f79a664564100ed14877312d040e2`.
Standard APK SHA-256: `c95ddb9ebaa6bef5abfd0e9a81e41f79852a227256315ef19409eb912b3dce53`.

The final CI review APK was installed and exercised on the isolated Android emulator. Numbered runtime captures 14 onward use the local build; all 92 application Kotlin/resource files match the tested application contents after CRLF normalization. `ci-*` images and `final-ci-*.mp4` recordings use the exact downloaded CI review binary. See [provenance](app/build/approved-board-delivery/PROVENANCE.md).

Application changes are pushed. Local checkout HEAD remains the preserved `ce80da2` baseline with working changes; the remote delivery commit above is the tested source. Do not reset or pull over existing work to reconcile this. Other user README edits were preserved. Build artifacts are local/CI artifacts rather than committed APKs or screenshots.

## What existed and what changed

This continuation began with an existing Kotlin/Jetpack Compose app, Supabase adapters and secure Razorpay test-payment code. It was not Flutter. Earlier Firebase removal and prior backend deployment work predate this batch. Supabase data was not reset, reseeded or migrated again.

The presentation previously retained oversized shared-card structure and scattered modules. The approved board now drives actual routed layouts: ivory/white surfaces, charcoal text, accessible orange actions, restrained lavender/sage/blue/yellow semantics, fine separators and compact spacing. Official RV/company-card lettering is retained, including the distinct dark launch stage. No invented portraits, growth percentages, schedules, charts or active AI were added to fill the reference.

- Principal: compact context/header, three metrics, labeled saved-attendance chart, grouped exceptions/actions, honest events and management/activity sections.
- Teacher: compact identity, real lesson timeline, attendance/assignment actions, class summaries and assignments. Attendance opens the displayed cohort.
- Admin: operational context, metrics/exceptions, actions, eight grouped module areas, actual academic hub, faculty/student tabs and distinct audit destination. Attendance overview now has its missing NavHost registration.
- Student/Parent: shared identity with distinct roles; greeting/assistant/next lesson/shortcuts/tasks for students, permitted child selection/statistics/updates/balance/events for parents.
- Attendance: class/date/slot selection, compact summary, searchable aligned P/A/L/E register, accessible controls and anchored Save with confirmed status. Existing Late/Excused meanings are retained.
- Assignments/resources/exams/timetable: tabs, subject/status filters, pastel concise rows, expandable full details, attachments and readable schedules. Search includes assignments.
- Fees: one projection from fee records instead of contradictory demo invoice fixtures, selected-child scope, concise balances, Pending/Paid tabs, expanded invoice/fee-head review, history, Pay/status/receipt actions.
- Students/faculty/profiles: continuous compact directories and grouped labeled details, with responsive detail panes and retained forms/documents.
- Inbox/notices/audit: compact chronology, timestamps, categories/read indicators, full messages and working destinations. In-app transport limitations are contextual, not repeated large banners.
- Results/reports: reachable private draft marks and publication review, responsive numeric fields, saved subjects, original reports/PDF/certificates, scoped attendance periods and labeled session financial totals.
- Settings/assistant/forms/dialogs: grouped account/settings rows, one persistent Light/Dark/System selector, compact forms and buttons, honest server availability, source-labeled chat replies and no artificial reply delay.

Primary navigation anchors at Dashboard, not the removed Auth destination. Saved navigation is cleared only for registered routes on account exit; this fixes an actual runtime sign-out crash. Nested destinations keep their semantic primary selection. Large text keeps navigation labels rather than hiding them. Phone insets/keyboard handling and tablet rail/drawer behavior remain intact.

## Focused data/workflow corrections

These are narrow fixes, not an unnecessary backend rewrite:

- Confirmed writes/RPCs are bound to the initiating session revision. Logout/account changes cannot produce a successful confirmation or load another account's data. An already-sent server request can still commit; no fictitious rollback or duplicate retry is claimed.
- Cloud parent assignment status follows the selected permitted child. Scope is still derived from authenticated backend records and RLS; portal selection is not authorization.
- Home, Finance and reports use the same existing fee records. Family fees/history follow the selected child.
- Persistent reconciliation-success banners were removed from steady-state screens; pending/error feedback and brief confirmed-save feedback remain.

No permission relaxation, schema replay, paid service, model key, payment secret, Firebase SDK or device-push integration was introduced. Secure invoice ownership/amount checks, integer paise, capture/signature verification and duplicate-safe settlement remain on the server.

## Actual checks

| Scope | Result |
|---|---|
| CI `assembleDebug assembleReview testDebugUnitTest lintDebug lintReview verifyNoFirebaseDependencies` with Roborazzi recording | Passed; 134 tests, 0 failures/errors/skipped |
| Lint Debug and Review | 0 errors/fatal findings, 78 warnings per variant; warnings not suppressed |
| PostgreSQL schema/RLS test harness | 111 assertions passed; this is local policy testing, not all hosted services |
| AI/payment handler tests | 31 passed, 0 failed; mocked provider/dependencies |
| Deno entrypoint/prompt loading | 1 passed, 0 failed |
| Credential/private-config/APK boundaries and dependency audit gates | Passed in CI |
| Final local build, tests, both lint variants and no-Firebase gate | Passed; log under `app/build/approved-board-delivery/final-local-checks.log` |
| Actual routed regression | Admin People → Faculty → Students → Academics → Assignments → Back → Activity → More → Attendance overview → Home → Settings → Sign out passed |
| Actual emulator behavior | Account entry, Supabase sign-in entry, separate demo, all five local roles, child Fees, invoice expansion, inbox destination/read feedback and Student assignments inspected |
| Local attendance action | Marked Aarav absent; saved 4 present/2 absent with confirmed Local Demo feedback |
| Appearance/layout | Light/Dark selected, Dark restored after process restart in matching local build, System reacted to Android night yes/no, 1.5× text, tablet settings, keyboard-open/scrolled sign-in inspected |
| Exact final CI APK | Installed without removing either app; cold launch recorded; warm resume retained Principal without observed brand replay; reduced-motion cold entry reached account entry |

Fresh images are under `latest-ci/`. Legacy preview-only ScholaOS images are excluded from the galleries. Production-body screenshots are labeled fixture renders, not full NavHost/hosted-account evidence. Full-scroll gate captures include Principal management/activity and Teacher tools. One emulator System UI wait prompt occurred in the initial older local-build inspection; debug emulator cold-launch time is not a physical-device performance guarantee.

Historical 81/82/85/119 test counts belong to earlier commits. The tested current total is 134. Source 14c5a0d passed CI but was superseded after runtime found the sign-out crash; its APK is not the recommended artifact.

## Hosted/backend evidence and limits

- Last SQL Editor SELECT-only setup check: public tables without RLS = 0, profiles = 4, four named workflow RPCs present. This checks presence, not a successful private login or every policy.
- Fresh 25 October unauthenticated payment-function probe: HTTP 401, `Sign in to the school account first.` The function responds; the historical 404 is not its current observed response.
- Fresh anonymous student endpoint probe: HTTP 401, `permission denied for table students`. This is access rejection, not evidence that authorized student reads fail.
- No order, payment, hosted data mutation, SQL migration or secret change was made by these probes.
- Realtime subscriptions coexist with 30-second reconciliation and reconnect handling. This is an in-app database inbox; no FCM/background device push is configured or claimed.
- Payment security tests cover unauthenticated calls, client amount/actor overrides, live-key rejection, invalid signature/order, authorized-not-captured pending state, verified capture, forged/signed webhooks, failure and late-event handling. They do not prove a real Razorpay TEST transaction or current secret validity.

## Remaining acceptance gaps

Actual hosted two-account assignment delivery, saved attendance, draft-before-publication privacy, recipient-isolated read state, reconnect/account switching and private storage require signed-in acceptance. Hosted Admin provisioning was not verified. No physical phone was connected. No real Razorpay TEST capture, receipt, balance update, principal inbox or duplicate webhook acceptance was observed in this continuation.

Expanded exam/resource details and every long report/certificate scroll position do not all have separate native-device captures. Rotation, TalkBack and all device/font combinations remain unverified. The screen checklist distinguishes layout implementation from these inspection gaps. No pixel-identical claim is made; actual records, accessible controls and honest unavailable states take priority over illustrative reference content.

Cloud library circulation, certificate issuance, report export and external machine/SMS/payroll/transport integrations remain existing explicit operator/integration boundaries. Local Demo data/actions are simulations; its shared fixtures are not a substitute for hosted per-recipient acceptance.

## Install and complete the payment acceptance

1. Use `Revenex-ERP-review-88db806.apk` for a separate `com.example.review` installation. It preserves the standard app's data. A compatible update uses `adb install -r`; never uninstall/clear data to resolve a signing mismatch. If an existing review package has a different certificate, stop and retain its data until the matching signing key is available.
2. Open Student / Guardian and sign in with the assigned synthetic Supabase school account. **Try demo is offline simulation and cannot perform secure Razorpay checkout.** Do not send passwords or private keys in chat.
3. Student Home → Fees, or Parent Home → School fees → View invoice. In Fees → Pending, expand an outstanding invoice and tap **Pay**.
4. Confirm Razorpay TEST mode and use its official mock Success flow yourself. Return to the same signed-in Revenex account and use **Check status** if needed. Do not repay while verification is pending.
5. Accept only a server-verified receipt and updated balance. Then check Principal inbox and that a fully paid invoice left Pending. Capture/callback/webhook duplicates must not credit twice.
6. If it fails, report only the first stage and sanitized on-screen status/error. No credentials, OTPs, card details, authorization headers or private screenshots are required.

## Research used

- [Fedena process flow](https://fedena.freshdesk.com/support/solutions/articles/266503-process-flow-start-using-fedena) and [mobile roles](https://fedena.com/mobile_app): task grouping and role priorities rather than a crowded desktop menu.
- [Frappe admissions](https://docs.frappe.io/education/student_admission), [enrolment](https://docs.frappe.io/education/program-enrollment), [attendance](https://docs.frappe.io/education/student-attendance), [student portal](https://docs.frappe.io/education/student-portal), [fee schedules](https://docs.frappe.io/education/fee-schedule): connected record/context and family-task organization.
- [Android adaptive layouts](https://developer.android.com/develop/ui/compose/build-adaptive-apps) and [Compose accessibility defaults](https://developer.android.com/develop/ui/compose/accessibility/api-defaults): navigation adaptation, readable hierarchy and accessible interaction targets.

The supplied board is the composition authority. Other supplied references contribute concise forms, grouped tasks, category accents and compact settings; unrelated reference branding/promotion was not copied.
