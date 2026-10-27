# CI/CD and server deployment

The GitHub Actions workflow builds/tests the Android debug/review variants and runs backend security, PostgreSQL/RLS, Deno and handler checks. It scans credentials/history, checks APK server-only boundaries and uploads time-limited verification/APK artifacts. No production store release is configured.

Function deployment is gated by both quality jobs, the main branch, the `supabase-demo` environment and `ENABLE_SUPABASE_DEPLOY=true`. Deployment credentials belong in that GitHub Environment, not source or Android. Public repository variables are `SUPABASE_URL`, `SUPABASE_PUBLISHABLE_KEY` and `SUPABASE_PROJECT_REF`; the deployment environment requires `SUPABASE_ACCESS_TOKEN`.

Schema migrations are not automatically replayed. The configured demo used SQL Editor application; reconcile actual function/table definitions and migration history before planning any database deployment. CI does not reset/reseed school data. Branch protection and environment approvals require repository-owner configuration.

## Optional server AI

The implementation is [supabase/functions/revenex-ai](../supabase/functions/revenex-ai), with the prompt bundled as a static server file. The server verifies Auth and computes protected school/session/role/student/tool/language context. Android-supplied roles do not grant permissions. Tools are bounded reads; chat actions cannot modify attendance, results, fees or school records.

Only an operator may configure `OPENAI_API_KEY`, `OPENAI_MODEL` and `REVENEX_AI_ENABLED` directly in the server secret store. No model is silently selected and no paid integration is activated by building the app. Deploy only after authorization/configuration and test privacy, quotas, expiry, provider failures and prompt-injection boundaries. The last hosted probe returned 404; a local type check or CI pass does not mean AI is deployed.

See [Supabase setup](../supabase/README.md), [Razorpay TEST setup](../supabase/RAZORPAY_SETUP.md), [security](SECURITY.md) and [known limitations](KNOWN_LIMITATIONS.md). Revert a problematic code change through normal review and redeploy after checks; do not weaken protections or drop data to roll back a handler.
