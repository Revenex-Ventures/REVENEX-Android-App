# scripts/

Utility scripts for project maintenance, automated security scanning, preflight health verification, and CI/CD pipelines.

## Files

### `scan-secrets.js`

Pre-commit credential scanner. Walks the working tree and scans text files for accidentally committed secrets.

- **Hard findings (exit code 1 — blocks commit):** Private keys, AWS keys, GitHub tokens, Stripe/Razorpay live secret keys, Slack tokens, raw JWTs.
- **Review-level findings (warning only):** Generic `password =` or `apiKey` literals.
- **Usage:**
  ```bash
  node scripts/scan-secrets.js
  ```

---

### `check_secrets.py`

Advanced repository and Git history scanner:
- Scans both working tree and optional Git history (`--history`) for model API credentials, GitHub tokens, Supabase private keys/service-role JWTs, and private credential literals.
- Reports exact locations while never outputting sensitive values.
- **Usage:**
  ```bash
  python scripts/check_secrets.py
  ```

---

### `check_apk_boundary.py`

Post-build APK verification:
- Inspects assembled APK package entries to assert that server-only prompts, OpenAI/Gemini keys, Razorpay secret keys, or Supabase service-role keys were never packaged into client artifacts.
- **Usage:**
  ```bash
  python scripts/check_apk_boundary.py app/build/outputs/apk/debug/app-debug.apk
  ```

---

### `check_deployed_functions.py`

Edge function deployment checker:
- Verifies HTTP status of deployed Supabase Edge Functions (`razorpay-payments`, `revenex-ai`).
- **Usage:**
  ```bash
  python scripts/check_deployed_functions.py
  ```

---

### `check_integration_health.py`

Preflight integration health checker:
- Inspects public endpoint status, auth health, and configuration availability without requiring private keys.
- **Usage:**
  ```bash
  python scripts/check_integration_health.py
  ```

---

### `write_public_android_config.py`

CI configuration generator:
- Validates public `SUPABASE_URL` and `SUPABASE_PUBLISHABLE_KEY` environment variables.
- Strictly validates that keys are public (`sb_publishable_...`) and writes `supabase.local.properties` for Android build pipelines. Rejects private service-role keys.

