# Verification utilities

| Script | Purpose |
|---|---|
| `write_public_android_config.py` | Write only public Android Supabase configuration from CI variables |
| `check_secrets.py` | Redacted credential scan, optionally including Git history |
| `check_apk_boundary.py` | Reject server prompt/private configuration markers in an APK |
| `check_docs.py` | Check maintained documentation links |
| `check_integration_health.py` | Read-only public service reachability; not authenticated acceptance |
| `check_deployed_functions.py` | Read-only unsigned POST probes require HTTP 401 from AI, payments and inspect-document after gated deployment; no provider calls or login |

Run commands from the repository root. See [testing](../docs/TESTING.md). Scripts must not print secret values, migrate/reset hosted data or invent successful provider verification.


## Commands and evidence

```powershell
python scripts/check_docs.py
python scripts/check_secrets.py --history
python -m unittest discover -s supabase/tests -p test_verify_hosted.py
```

The hosted verification tool lives at [supabase/verify_hosted.py](../supabase/verify_hosted.py), not this directory. Use the exact command, roles, hidden local credential-entry instructions and read-only limits in the [Supabase README](../supabase/README.md). Generated progress files stay ignored under `app/build/reports/`; never commit private reports.

For GitHub verification, authenticated `gh run list --branch codex/project-review-2026-10-29` identifies runs; `gh run view RUN_ID` shows backend/Android/deployment conclusions. Inspect failures and commit identity before claiming success. A missing branch run is not green CI. Network or credentials problems must be reported without printing tokens.

31 October update: scripts documentation now covers all three deployed handlers and separates anonymous rejection from authenticated acceptance. Update this README and the [root change record](../README.md) whenever a script, flag, output contract or verification step changes, including small changes. See [pipeline configuration](../.github/workflows/ci.yml) and [verification history](../docs/VERIFICATION.md).
