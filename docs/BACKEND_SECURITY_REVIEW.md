# Backend security and integration check — 31 October 2026

This review covers the committed Supabase backend, Android data-layer configuration, tests, dependencies and read-only hosted reachability. UI/frontend, school data, Auth settings and deployed policies are unchanged. It is not a penetration-test certificate or a claim that all hosted integrations work.

## Findings and implemented fixes

| Finding | Impact | Source result |
|---|---|---|
| Payment/AI body readers had byte ceilings without an application read deadline | A stalled or slowly streamed body could retain handler resources; the webhook route reads the unsigned body before signature validation | All three handlers now share a 15-second total read deadline, original byte ceilings and a 16,384-chunk ceiling. Progress does not restart the deadline. |
| Inspection timeout cleanup awaited stream cancellation | A never-settling cancellation could prevent the error response from completing | Cancellation is initiated without awaiting an untrusted completion promise; timers/read locks are cleaned up and a fixed HTTP 408 error is returned. Private object downloads use the same bounded reader. |
| AI quota used truthiness instead of explicit approval | An unexpected RPC object/string could be treated as permission to spend model resources | Only boolean `true` authorizes model work. This is fail-closed response validation, not evidence that an external user could forge the protected RPC. |
| Integration health script omitted inspect-document | The newly integrated endpoint was absent from the operator's reachability report | The read-only probe now covers payments, AI and inspection. A 404 remains unavailable, never successful readiness. |

No SQL migration is needed for these source fixes. Previously prepared migrations 20–24 remain unapplied hosted by this work. Redeploy the changed handlers only through the confirmed demo/schema rollout; this review does not deploy them.

## Local verification

The original stream regressions reproduced missing deadlines/non-completing cancellation before the shared reader was implemented. The AI regression reproduced HTTP 200 for malformed quota approval before requiring boolean true. Red/green logs remain under ignored `app/build/security-review-*` paths.

- Eleven focused stream tests pass: stalled and drip-fed payment/webhook/AI/inspection requests, cancellation that never completes, stalled private object download, exact byte boundaries and tiny-chunk flood.
- Full local rerun passed: 142 PostgreSQL schema/RLS checks, 22 payment tests, 68 combined attendance/publication/quota/document/stream regressions, 17 AI tests and 3 Python verifier tests. The real local Deno entrypoint smoke test also passed. Earlier dated CI evidence is retained separately; the new push needs its own exact-commit CI result.
- Deno checks all three function entrypoints and lints the shared helper as well as each function. npm audit reported zero known vulnerabilities in the committed Node test dependency tree; that does not cover every Android/native/provider component or guarantee absence of vulnerabilities.
- Credential/history and documentation checks run before publication. No private values are included in this report.

## Fresh hosted probe — 31 October 2026, 00:43:44 IST

Existing ignored public configuration was used. No password, privileged key, login, model call, payment or database mutation was performed.

| Endpoint | Result | What it proves |
|---|---|---|
| Auth health | 200 | Reachability only |
| Students, fee records, payment orders | 401 each | These unsigned probes were rejected; not a complete RLS assessment |
| Payment function | 401 | Unsigned payment request rejected; provider secrets/capture remain unverified |
| AI function | 404 | Unavailable; no hosted model execution verified |
| Document inspection function | 404 | Unavailable; compatible uploads cannot complete until coordinated deployment |

Raw sanitized status report: ignored `app/build/reports/integration-health.json`. Previous Principal/Teacher/Student authenticated read evidence remains historical; there is no fresh authenticated write/private-file/Realtime acceptance in this review.

## Remaining security and integration work

1. Confirm actual demo schema/function definitions before applying only missing migrations and coordinating the handlers/client. Current source protections must not be described as already hosted.
2. Provision/deploy AI only with authorized model configuration; deploy inspection with migration 24 and the compatible uploader. Keep payment credentials TEST-only and verify an actual TEST capture/webhook separately.
3. Confirm privileged MFA, owner-controlled branch requirements/deployment approvals, provider Auth/gateway abuse settings, alerts and incident access. Current CLI credentials do not have repository administration permission.
4. Verify signed-in tenant/role/guardian isolation, draft attachment privacy, stale publication conflicts, private delivery and two-device Realtime using the disposable synthetic plan in [remediation](BACKEND_REMEDIATION.md).
5. Rehearse database plus private Storage restoration and actual independent-connection/load tests in an authorized disposable environment. File format checks are not antivirus/content disarm.
6. Obtain approved absence/exemption/published-result correction rules. Preserve historical published records; no unrestricted override is added.

See [security boundaries](SECURITY.md), [hardening rollout](BACKEND_HARDENING.md), [Supabase README](../supabase/README.md) and the [root change record](../README.md). No system can be guaranteed unhackable; close these items with evidence rather than treating a green CI run as hosted security certification.
