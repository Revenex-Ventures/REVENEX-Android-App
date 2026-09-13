# functions/test/

Test helpers for the Firebase backend.

## Files

### `debug-token.js`

Firebase App Check debug token helper for local development and CI.

- Generates or reads a debug App Check token from the environment.
- Used when running functions locally against the Firebase emulator, where real App Check attestation is not available.
- Set `FIREBASE_APP_CHECK_DEBUG_TOKEN` in your `.env` and this helper will inject it.

---

### `run-rules-tests.js`

Firestore security rules unit test runner.

- Loads `firestore.rules` and runs a suite of tests against the Firebase emulator using the `@firebase/rules-unit-testing` library.
- Covers: role-based read/write access, multi-tenant isolation, session scoping, parent-child data access, and backend-only write enforcement on sensitive collections (fees, attendance, marks, audit_log).

Run with:

```bash
# From project root, with emulator running
node functions/test/run-rules-tests.js
```

Or via the npm test script:

```bash
cd functions && npm test
```
