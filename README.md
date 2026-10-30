# Revenex School ERP

Native Android school ERP built with Kotlin and Jetpack Compose. It supports school leadership, school administrators, teachers, students and guardians through role-aware portals.

The active cloud backend is Supabase: Auth, PostgreSQL with RLS, private Storage, database inboxes and Realtime. Local Demo is a separate offline experience with synthetic personas. Razorpay is TEST-only and server-verified. Firebase is not part of the application or deployment.

## Project layout

```text
app/
  src/main/java/com/example/
    data/                 # Models, data sources and repository
    ui/                   # Theme, components, navigation and role screens
  src/main/res/           # Bundled brand/photo assets, icons and Android resources
  src/test/               # JVM, Compose interaction and native render tests
  src/review/             # Optional side-by-side review build resources
supabase/
  migrations/             # Ordered schema, RLS and RPC migrations
  functions/              # Server-only payment, AI and document inspection handlers
  tests/                  # Isolated PostgreSQL and handler tests
scripts/                  # Public configuration, security and verification utilities
docs/                     # Current setup, architecture, workflows and limitations
.github/                  # CI/CD and dependency update configuration
gradle/                   # Wrapper and version catalog
AGENTS.md                 # Repository working rules
```

Build output, APKs, logs, caches, local configuration and historical cleanup backups are ignored by Git. Keep the existing package/application ID when updating installed demo devices.

## Build and run

Use JDK 21 and the Android SDK/build tools declared in the Gradle project (compile SDK 36.1, minimum Android API 24). Open the project root in Android Studio or use the wrapper.

1. Copy [supabase.local.properties.example](supabase.local.properties.example) to ignored `supabase.local.properties`.
2. For cloud sign-in, set only `SUPABASE_URL` and `SUPABASE_PUBLISHABLE_KEY`. Without cloud configuration, use Try demo.
3. Build and install:

```powershell
.\gradlew.bat assembleDebug
adb install -r app\build\outputs\apk\debug\app-debug.apk
```

Install as an update to preserve app data. Stop on a signature/package conflict; do not uninstall or reset school data to work around it.

## Documentation

- [Documentation index](docs/README.md)
- [Development and project conventions](docs/DEVELOPMENT.md)
- [Architecture and security boundaries](docs/ARCHITECTURE.md)
- [Supabase setup](supabase/README.md)
- [Razorpay TEST setup](supabase/RAZORPAY_SETUP.md)
- [Testing commands](docs/TESTING.md) and [device acceptance checklist](docs/DEVICE_TESTING.md)
- [Screens and connected workflows](docs/SCREENS_AND_WORKFLOWS.md)
- [Known limitations](docs/KNOWN_LIMITATIONS.md) and [verification evidence](docs/VERIFICATION.md)
- [Visual system and asset attribution](docs/DESIGN_SYSTEM.md)

## Backend change record — 31 October 2026

This is an Android **prototype with synthetic demo data**. UI/frontend is preserved. Local Demo works offline without cloud credentials; school cloud sign-in still uses Supabase Auth. No password is hardcoded, published, or added to this README. Forgotten cloud credentials are handled privately by the authorized operator; a new self-service password-reset flow is outside this change.

Every backend/configuration change, including small changes, must update this record and the relevant directory README with its behavior, validation and remaining limits. Git history provides the exact line-level diff. [Repository rules](AGENTS.md) retain this requirement for future work.

| Source change | Behavior and protection |
|---|---|
| `f03a648`: migration 20 | Future assignments default to DRAFT; new/changed or newly published attachment references require a real stored object, with a transaction lock; unchanged historical published references are preserved. |
| `f03a648`: migration 21 | Operator-defined school/session reporting periods and server attendance aggregates align dashboard/report attendance. Explicit term ranges are inclusive; legacy terms retain clearly identified session-to-date scope. No invented term dates/backfill. |
| `f03a648`: migration 22 | Leadership-only atomic result batches validate eligible roster × required subjects, submitted valid marks, configured exam maxima and expected versions. Zero differs from missing. Submitted edits return to DRAFT; published marks cannot be silently overwritten or automatically unpublished. |
| `ec906b6`: complete reads | Unique-ID keyset pagination and exact counts reject missing/duplicate/truncated/drifting pages and the 100,000-row safety limit. Partial totals are not returned as complete; separate pages are not a database-wide point-in-time snapshot. |
| `ec906b6`: scope/readiness | Shared attendance scope is used by data consumers; result batches carry observed versions; integration configuration is distinguished from verification; historical sample assignments remain explicitly PUBLISHED. |
| `26da5a1`, `900f193`: hosted verification | Hidden local password prompts, role selection, per-run progress reports and sanitized Auth diagnostics; previously recorded Teacher/Student reads passed on capability 19, and Principal console success was supplied by the user. |
| `8624605`: migration 23 and payments | Service-only actor/school/session fixed-minute quotas: create 5, verify 15, status 30, document inspect 5; tenant/operation ceiling 200. Payment work fails closed before provider access if the limiter is unavailable. Denials are deduplicated scoped events without request contents or credentials. Signed webhook settlement retains its own verification/replay controls. |
| `18c0c93`: migration 24 and inspection | New/changed assignment, material, marks and submission references require inspection matching the Storage object UUID/update timestamp. Service-only confirmation records SHA-256. Replacement invalidates approval; old unchanged published references survive. |
| `18c0c93`: private upload handshake | Authenticated inspection downloads with the caller's bearer token; validates ownership/path/size; bounds streams/ZIP expansion and rejects malformed, encrypted or traversing DOCX entries. Uploader returns a stable reference only after explicit confirmation; failure uses existing RLS-limited staging cleanup. |
| `18c0c93`: honest file safety | PDF/JPEG signature checks, PNG chunk integrity and bounded DOCX structure checks are **format inspection only**, not antivirus, content disarm or proof a file is safe to open. |
| `18c0c93`: operations/CI | Read-only counts for abuse, pending orders, missing receipts, stale inspection and staging age; new regression suites and Deno checks; coordinated migration/function/client rollout documented. Deployment additionally requires explicit schema confirmation variable `24`. |
| Current CI follow-up | Checks now run on `main` and `codex/**` pushes, all PRs, and manual dispatch. Previously this branch had no run because only `main` pushes matched and it had no PR. Deployment remains main-only and requires both quality jobs and operator configuration. No checks are skipped to obtain green status. |
| Current runner follow-up | All three jobs use explicit `ubuntu-24.04` rather than a moving OS alias during GitHub's Ubuntu 26.04 migration. Source checks, timeouts and main-only deployment protections remain enforced. This improves reproducibility; it does not prove the cause of a stalled hosted runner. |
| Current documentation follow-up | All four READMEs, security/limitations/testing/deployment guidance and dated verification are synchronized; future updates must record small changes and distinguish source/local/CI/hosted evidence. |

Detailed implementation and safe rollout: [backend remediation](docs/BACKEND_REMEDIATION.md), [backend hardening](docs/BACKEND_HARDENING.md), [Supabase README](supabase/README.md). Undefined absence, exemption and published-result correction rules remain school-policy blockers; no academic behavior is invented.

## Verification and CI/CD

- **Source/push:** backend commits through `18c0c93` were confirmed on the remote working branch on 31 October 2026. Documentation/CI commit `c99df93` was also pushed; its backend job passed in run `41109276114`, while Android reported in progress with no steps before the runner follow-up superseded it. Fresh local checks and current branch CI are distinguished in [dated evidence](docs/VERIFICATION.md).
- **Locally verified baseline:** 142 PostgreSQL policy/schema checks, 22 payment tests, 57 backend regressions, 16 AI tests, 15 focused Kotlin tests and 3 Python tests passed for the hardening patch. This is not a full Android build or hosted acceptance. Current reruns and exact commit/run links belong in the evidence record.
- **Hosted evidence:** the existing demo checkpoint reports migrations 1–19; Principal/Teacher/Student authenticated reads passed as recorded. Migrations **20–24 are prepared, not applied hosted**. New payment protection and uploads require their matching migrations/functions before compatible clients are demonstrated.
- **CI status:** see [Actions](https://github.com/Revenex-Ventures/REVENEX-Android-App/actions) and [CI definition](.github/workflows/ci.yml). Check the exact branch and commit, both backend and Android jobs, and report deployment as skipped when gated. Older green runs do not verify newer backend commits.
- **Protection:** checks remain mandatory in the workflow. Repository-owner branch/ruleset enforcement and deployment-environment approvals must be confirmed separately; writing a workflow does not configure those settings.

## Security requirements and remaining backend work

No system can be promised unhackable. The prototype must still reject unauthorized cloud access. [Security boundaries and prioritized operator work](docs/SECURITY.md) cover tenant/role isolation, draft privacy, private files, secrets, payments, AI and incident handling.

Before a broader demonstration: confirm deployed definitions and Auth/Storage/Realtime isolation with disposable synthetic records; configure privileged-account MFA and restricted operator access; review Supabase security/performance advisors, Auth abuse settings and database network/SSL controls; enable an approved monitoring destination; rehearse backup/Storage restoration in a separate disposable environment; and test real multi-connection concurrency and bounded load. These are tracked requirements, not claims of deployment or tests already completed. See the [official Supabase checklist](https://supabase.com/docs/guides/deployment/going-into-prod).

## Readiness

This is a demo acceptance-test candidate, not a production certification. Hosted SQL permission/workflow checks have passed, but a captured device Razorpay TEST payment, real webhook delivery and full two-account Android transport acceptance remain unverified. Hosted AI is unavailable until an operator configures/deploys it. See the limitations before demonstrating cloud behavior.

Secrets belong only in server secret stores or CI environments. Never commit private credentials or place model, payment, service-role or database secrets in Android.
