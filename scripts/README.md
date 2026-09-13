# scripts/

Utility scripts for project maintenance and security tooling.

## Files

### `scan-secrets.js`

Pre-commit credential scanner. Walks the working tree and scans all text files for accidentally committed secrets.

**Hard findings (exit code 1 — blocks commit):**

- Private keys (`-----BEGIN ... PRIVATE KEY-----`)
- AWS access key IDs and secret keys
- GitHub Personal Access Tokens (`ghp_...`)
- Stripe live secret keys (`sk_live_...`)
- Razorpay live secret keys (`rzp_live_...`)
- Slack tokens
- Raw JWTs (three-part base64 strings)

**Review-level findings (warning only):**

- Generic `password =` or `passwd =` literals in code
- Patterns matching `api_key`, `apiKey`, `API_KEY` assignments
- Broad `secret` variable name patterns

**Skipped paths:**

- `node_modules/`, `build/`, `.gradle/`, `.git/`
- `.env` files (intentionally secrets)
- `*.keystore`, `*.jks` binary files
- Known binary/media file extensions

**Usage:**

```bash
node scripts/scan-secrets.js
```

Returns exit code `0` if clean, `1` if hard findings are detected. Integrate into a git pre-commit hook or CI pipeline to prevent secret leakage.
