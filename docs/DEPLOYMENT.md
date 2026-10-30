# CI/CD and server deployment

The GitHub Actions workflow builds/tests the Android debug/review variants and runs backend security, PostgreSQL/RLS, Deno and handler checks. It scans credentials/history, checks APK server-only boundaries and uploads time-limited verification/APK artifacts. No production store release is configured.

Checks run on pushes to `main` and `codex/**`, all pull requests, and manual dispatch. The 31 October follow-up adds working-branch push coverage: the backend branch previously had no run despite a successful push because only main pushes matched and no PR existed. [GitHub trigger/filter documentation](https://docs.github.com/en/actions/how-tos/write-workflows/choose-when-workflows-run/trigger-a-workflow).

Function deployment is gated by both quality jobs, the main branch, the `supabase-demo` environment, `ENABLE_SUPABASE_DEPLOY=true` and `BACKEND_HARDENING_SCHEMA_CONFIRMED=24`. The last variable must only be set after an operator compares deployed migration/function definitions; it is an assertion, not an automatic schema check. Deployment credentials belong in that GitHub Environment, not source or Android. Public repository variables are `SUPABASE_URL`, `SUPABASE_PUBLISHABLE_KEY` and `SUPABASE_PROJECT_REF`; the deployment environment requires `SUPABASE_ACCESS_TOKEN`.

Schema migrations are not automatically replayed. The configured demo used SQL Editor application; reconcile actual function/table definitions and migration history before planning any database deployment. CI does not reset/reseed school data. Branch protection and environment approvals require repository-owner configuration.

## Optional server AI

The implementation is [supabase/functions/revenex-ai](../supabase/functions/revenex-ai), with the prompt bundled as a static server file. The server verifies Auth and computes protected school/session/role/student/tool/language context. Android-supplied roles do not grant permissions. Tools are bounded reads; chat actions cannot modify attendance, results, fees or school records.

Only an operator may configure `OPENAI_API_KEY`, `OPENAI_MODEL` and `REVENEX_AI_ENABLED` directly in the server secret store. No model is silently selected and no paid integration is activated by building the app. Deploy only after authorization/configuration and test privacy, quotas, expiry, provider failures and prompt-injection boundaries. The last hosted probe returned 404; a local type check or CI pass does not mean AI is deployed.

See [Supabase setup](../supabase/README.md), [Razorpay TEST setup](../supabase/RAZORPAY_SETUP.md), [security](SECURITY.md) and [known limitations](KNOWN_LIMITATIONS.md). Revert a problematic code change through normal review and redeploy after checks; do not weaken protections or drop data to roll back a handler.


## Exact-commit CI verification

A successful push is not a test result. Match `git rev-parse HEAD` to the remote branch and the Actions run `headSha`; inspect both backend and Android jobs. Deployment skipped on a working branch is expected, not a failed job. CI must retain failures/artifacts and repair causes; do not remove assertions, suppress lint or skip security checks to obtain green status.

The latest previously inspected green run was [40468041788](https://github.com/Revenex-Ventures/REVENEX-Android-App/actions/runs/40468041788), for a different Dependabot commit. It does not verify the backend hardening commits. Current branch runs and any fixes are recorded in [verification](VERIFICATION.md).

Before enabling CD, confirm the demo target and migrations 20–24, review backups, and coordinate handlers (`razorpay-payments`, `inspect-document`, `revenex-ai`) with the compatible Android client. No database migration runs automatically in CI. The post-deploy check requires anonymous HTTP 401 from all three handlers and cannot prove authenticated behavior. Owner-configured branch/ruleset requirements, restricted environments, deployment secrets and privileged MFA remain operator responsibilities; they are not silently changed here.
