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
deno check supabase/functions/revenex-ai/index.ts supabase/functions/razorpay-payments/index.ts
deno lint supabase/functions/revenex-ai supabase/functions/razorpay-payments
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
