# Testing

## Android

```powershell
.\gradlew.bat --no-daemon --no-configuration-cache --max-workers=1 assembleDebug testDebugUnitTest lintDebug verifyNoFirebaseDependencies
python scripts/check_apk_boundary.py app/build/outputs/apk/debug/app-debug.apk
```

For available native Compose render checks, enable the existing Roborazzi recording property. Generated images belong under `app/build/reports/` and are not committed:

```powershell
.\gradlew.bat '-Proborazzi.test.record=true' testDebugUnitTest --tests com.example.LaunchEntryTest --tests com.example.StudentImageSpecTest
```

An APK compile is not visual verification. Inspect actual renders or the installed routed app. Identify viewport, font scale, theme, source commit and whether data is synthetic. Retain failed runs; do not skip assertions or raise timeouts to hide a failure.

## Backend

```powershell
npm ci --ignore-scripts --prefix supabase/tests
npm test --prefix supabase/tests
node --test supabase/tests/assistant-edge.test.mjs supabase/tests/razorpay-edge.test.mjs
deno check supabase/functions/revenex-ai/index.ts supabase/functions/razorpay-payments/index.ts supabase/functions/inspect-document/index.ts
deno lint supabase/functions/revenex-ai supabase/functions/razorpay-payments supabase/functions/inspect-document supabase/functions/_shared
deno test --allow-read=supabase/functions --allow-env=SUPABASE_URL,SUPABASE_ANON_KEY,OPENAI_API_KEY,OPENAI_MODEL,REVENEX_AI_ENABLED --allow-net=127.0.0.1 supabase/tests/assistant-entrypoint.test.mjs
```

PGlite uses an isolated local database and its own fixtures. These commands do not migrate/reset hosted Supabase. Node handler tests mock providers; they are not real payments/model calls. Deno's HTTP entrypoint check is separate from Node tests.

## Repository checks

```powershell
python scripts/check_docs.py
python scripts/check_secrets.py --history
```

`scripts/check_integration_health.py` performs read-only anonymous reachability checks using ignored public configuration. A 401 proves unsigned rejection only; 404 is an unavailable endpoint. Never infer a valid signed-in workflow, correct secrets or payment capture from these probes.

Hosted rollback acceptance SQL lives under `supabase/verify_*_rollback.sql`. Review it against the actual schema and use only explicitly authorized synthetic records. Never blindly rerun migrations or seed an existing project.

See [device acceptance](DEVICE_TESTING.md), [evidence](VERIFICATION.md) and [known limits](KNOWN_LIMITATIONS.md).


## Backend follow-up — 31 October 2026

`npm test` includes legacy RLS/publication upgrade, payment, attendance periods, result batch completeness, quota isolation, document handler and document-inspection SQL regressions. The legacy suite intentionally exercises its original migration stage before the strict publication upgrade; new-rule suites separately exercise migrations 23/24. Add tests for changed behavior rather than weakening prior assertions.

Run `python -m unittest discover -s supabase/tests -p test_verify_hosted.py` for hidden-input/sanitized diagnostics regressions. Run the secure role command in the [Supabase README](../supabase/README.md) only through local password entry; no hosted credentials are needed for isolated tests.

The focused cached-dependency Kotlin diagnostic from the prior hardening run is not a replacement for the complete Android CI build/tests/lint/APK-boundary gates. It passed 15 tests; prior local Gradle environment failures are retained. The current exact-commit remote result is recorded in [verification](VERIFICATION.md), including any failure and fix. [Root README](../README.md) distinguishes source/local/CI/hosted status.


The latest security review adds `request-body-security.test.mjs` to `npm test`: eleven fake-clock/stream tests cover total deadlines, drip feeds, never-completing cancellation, private downloads, byte boundaries and chunk floods. AI tests additionally reject malformed quota acknowledgements before model work. No slow-network test performs a real provider call. See [findings](BACKEND_SECURITY_REVIEW.md).


Release acceptance also runs `upstream-security.test.mjs` and `release-preflight.test.mjs` through `npm test`. They verify bounded upstream responses/transport abort plus schema stages 19–24, existing policy fingerprints and non-empty historical publication preservation in isolated fixtures. `python -m unittest discover -s supabase/tests -p 'test_*.py'` includes both existing hidden-account verifier tests and target/inventory-redaction tests. Neither command is authenticated hosted or device acceptance. See [release gates](DEMO_COORDINATED_RELEASE.md).
