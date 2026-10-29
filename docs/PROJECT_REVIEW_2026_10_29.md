# Project review — 29 October 2026

## Scope

Review and publish the existing local school ERP changes, preserving the remote branch history. The application uses Kotlin/Jetpack Compose, role-specific principal/teacher/student/guardian portals, and a Supabase backend. The pending snapshot includes UI/brand rebuilds, administrator mobile exclusion, versioned school workflows, account-scoped reconciliation, tests, and consolidated documentation.

The original local HEAD was `ce80da2`; the remote `codex/revenex-reference-redesign` tip was `8bd8653`, 38 commits ahead. Publish this local snapshot on a separate branch rather than replacing that remote tip. A merge into the existing redesign branch needs a separate reconciliation review.

## Finding and correction

The Android compilation check reproduced a missing experimental-layout opt-in on `StudentLightHome`, which uses Compose `FlowRow`. Add `@OptIn(ExperimentalLayoutApi::class)` to that function. This acknowledges the API contract without changing layout behavior.

The first complete Android run executed 196 tests and found two outdated presentation assertions: principal dark section headings and the student light background token. Align those expectations with the implemented role/theme designs while retaining section/navigation assertions and dark-palette contrast checks. Keep the initial failure log at ignored `app/build/github-review-checks.log` and the corrected run at `app/build/github-review-recheck.log`.

## Verification

- Documentation: 25 maintained documents and 73 local links passed, including this review.
- Credential scan, including Git history: passed.
- PostgreSQL/PGlite schema and RLS: 140 checks passed against an isolated local database.
- Razorpay handler: 20 mocked tests passed.
- AI handler: 16 mocked tests passed.
- Staged whitespace check: passed.
- Debug APK build: passed after the experimental-layout opt-in correction.
- Android JVM/Compose suite: all 196 tests passed, none skipped.
- Android debug lint and Firebase dependency boundary: passed.
- Freshly built debug APK server-prompt/private-configuration boundary: passed.

These checks are not an exhaustive line-by-line security audit or new device acceptance. No hosted migrations or provider transactions were executed for this review.

## Remaining acceptance work

Follow [known limitations](KNOWN_LIMITATIONS.md) and [device testing](DEVICE_TESTING.md). Real-device Razorpay TEST capture/webhook delivery, complete two-account Android Auth/Storage/Realtime acceptance, physical-device performance, and hosted AI availability remain unverified. Historical attendance correction approval and the future administrator web portal remain outside the implemented mobile workflows.
