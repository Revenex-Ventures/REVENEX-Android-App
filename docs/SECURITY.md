# Security boundaries

- Android accepts only public Supabase URL/publishable configuration. Payment IDs returned for checkout are public; model, gateway, webhook, service-role and database secrets remain server-only.
- The protected profile and explicit teacher allocations/guardian links determine scope. UI role switching is only Local Demo presentation and cannot authorize cloud reads/writes.
- PostgreSQL RLS applies to every public ERP table. Sensitive mutations use validated RPCs rather than trusting client-supplied role, owner, author, amount or publication flags.
- Files use a private bucket, owned metadata, managed recipient grants and short-lived signed links. A previously issued signed link remains usable until expiry; do not treat revocation as retroactive URL invalidation.
- Marks drafts remain private; only authorized leadership can publish. Version checks prevent stale writes. Attendance saves are whole-class transactions; historical changes require a correction process that is not yet implemented.
- Payment success requires server/provider verification. Keep test credentials paired, use integer paise, preserve pending/failure states and reconcile unresolved references rather than creating replacement charges blindly.
- Inbox read state belongs to a recipient. Clients cannot forge recipients or settlement notifications.
- Logout/account switches must erase account-scoped caches. Reconciliation and permission revocation need actual transport/device acceptance in addition to isolated tests.

Run `python scripts/check_secrets.py --history` before pushing and `python scripts/check_apk_boundary.py <apk>` before delivering. The scanner reports locations, not credential values. These checks are boundaries, not a full security audit.

Do not reset/reseed an existing school project, weaken RLS, clear user data to bypass bugs, or blindly replay SQL Editor migrations. Provision identities and secret values directly through the appropriate operator consoles. Do not put passwords, tokens or payment credentials in Git issues, screenshots or chat.


## Backend follow-up and abuse cases — 31 October 2026

Source controls below depend on migrations 20–24 and corresponding handler/client deployment; they are not claims of current hosted enforcement. [Hardening details](BACKEND_HARDENING.md) and [Supabase setup](../supabase/README.md) give rollout prerequisites.

| Threat/boundary | Source handling | Remaining verification/action |
|---|---|---|
| Foreign school/session or forged portal role | Protected profiles, scoped RLS/RPCs, subject allocations and own-child visibility | Verify authenticated negative cases, account switching and revoked membership against the deployed schema |
| Draft assignment/result leakage | Server publication transitions; drafts and draft attachments excluded from students/guardians; recipient notification uniqueness | Connected role/file delivery checks, including copied IDs and retries |
| Incomplete/stale result publication | Eligible roster × required subjects, submitted valid marks, optimistic versions and atomic batch transaction | Define absence/exemption/correction policy and test real independent concurrent sessions |
| False totals/truncated reads | Exact counts and keyset completeness, scoped attendance reporting periods/aggregates | Configure intended term ranges; measure realistic staging load and query plans |
| Payment/inspection abuse | Server-only fixed actor/tenant quotas and deduplicated sanitized denials; payment provider work starts only after quota reservation | Tune against approved demo traffic; verify anonymous/Auth-endpoint abuse protection at the provider/gateway separately |
| Missing/replaced/forged file | Owned manifest, actual object existence, object-version inspection with bounded bytes/archives, private grants and signed delivery | Antivirus/CDR if untrusted real documents become in scope; verify actual private HTTP delivery and replacement races |
| Credential/provider leakage | Server-only secrets, sanitized errors, credential/history/APK checks, pinned Actions and dependency audit | Operator secret rotation/incident response; audit scanners are not proof that no secret exists |
| AI prompt/data abuse | Auth-derived context, bounded scoped read tools and no academic/payment write tools | Hosted prompt injection, expiry and budget checks only after authorized model configuration |

Local Demo requires no cloud password. Cloud Auth remains enabled; no credentials are embedded in the prototype or README, and no self-service password reset flow is introduced. Forgotten cloud credentials go through the authorized operator's private identity administration process. Do not bypass Auth, share credentials in chat or weaken RLS for a demo.

## Operator security backlog

| Priority | Required work | Completion evidence |
|---|---|---|
| Before hosted rollout | Confirm project/tenant and deployed definitions; verify backup and apply only missing migrations with coordinated handlers/client | Dated definition comparison, migration result and disposable synthetic acceptance |
| Before wider account access | MFA for privileged Supabase/GitHub operators; least-privilege access; owner-reviewed branch/ruleset and deployment-environment approvals | Operator confirmation of settings and recovery access; never export secrets |
| Before untrusted traffic | Review Security/Performance Advisors, Auth rate limits and email confirmation, database SSL/network controls and allowed redirect configuration | Sanitized settings/check results; no assumption that source configuration changed hosted controls |
| Before real records/documents | Data retention/minimization, approved monitoring destination, backup plus private Storage restoration, file-safety requirements | Isolated restore and authorized alert-delivery tests with measured recovery results |
| Before performance claims | Real multi-connection quota/publication conflicts, timeouts, expected-volume query plans and bounded staging load | Target, budget, concurrent-client count, p95 latency/error rates and rollback outcomes |
| On suspected exposure | Revoke affected sessions/access, rotate compromised secrets through the operator store, preserve sanitized incident evidence and recheck tenant isolation | Incident record with affected scope and verification; no destructive school-data reset |

These tasks follow the [official Supabase production checklist](https://supabase.com/docs/guides/deployment/going-into-prod). No system is guaranteed unhackable; document demonstrated controls and open risks honestly. Prototype status does not authorize access to another school's data. Do not enable paid services, external messaging, production load or destructive recovery without the existing scope/authorization.
