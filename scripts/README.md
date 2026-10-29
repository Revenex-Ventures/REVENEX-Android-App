# Verification utilities

| Script | Purpose |
|---|---|
| `write_public_android_config.py` | Write only public Android Supabase configuration from CI variables |
| `check_secrets.py` | Redacted credential scan, optionally including Git history |
| `check_apk_boundary.py` | Reject server prompt/private configuration markers in an APK |
| `check_docs.py` | Check maintained documentation links |
| `check_integration_health.py` | Read-only public service reachability; not authenticated acceptance |
| `check_deployed_functions.py` | CI check that deployed handlers reject unsigned requests |

Run commands from the repository root. See [testing](../docs/TESTING.md). Scripts must not print secret values, migrate/reset hosted data or invent successful provider verification.
