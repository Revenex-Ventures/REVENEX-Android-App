# Supabase demo backend

Supabase Auth, PostgreSQL/RLS, private Storage, a recipient-specific database inbox and Realtime are the active cloud path. Local Demo remains separate and offline. No Firebase configuration or billing is required.

## Existing demo project

The 26 October operator checkpoint reports migrations 1–19 applied. Principal/Teacher/Student authenticated reads were recorded on 30 October; capability 19 is not a comparison of deployed definitions. Migrations 20–24 are prepared source only, not applied hosted by this work. Hosted assignment, connected-workflow and versioned-marks rollback acceptance passed. **Do not rerun deployed migrations or seed this project.** Inspect actual schema/history before any new deployment. Older APKs using direct writes must be updated; do not restore broad write grants to accommodate them.

## New empty demo project only

1. Create a dedicated synthetic project and enable Auth, PostgreSQL, Storage and Realtime.
2. Apply the files in [migrations](migrations) once in filename order, 1–24 after reviewing prerequisites. Existing projects must instead compare actual history/definitions and apply only confirmed missing versions. Keep `erp_private` out of Data API exposed schemas. Public ERP reads/writes require a protected authenticated profile and restrictive policies.
3. Create the four synthetic Auth users below in the operator console, choosing passwords privately and confirming their emails. Do not commit or send those passwords.
4. Run [seed.sql](seed.sql) once on this new empty demo. It requires those Auth users and links protected profiles, school/session, a class, teacher/subject allocation and a guardian-child relation.
5. Copy [the public Android template](../supabase.local.properties.example) to ignored `supabase.local.properties` and configure only the HTTPS URL/publishable key.
6. Use school sign-in. Selecting a portal never provisions a user or grants its role.

| Synthetic Auth email | Protected role |
|---|---|
| principal@revenex.demo | PRINCIPAL |
| teacher@revenex.demo | TEACHER |
| student@revenex.demo | STUDENT |
| parent@revenex.demo | PARENT |

An Administrator cloud account or other role requires explicit operator provisioning; the offline persona selector is not authorization. Additional enum roles do not imply implemented Android portals.

## Migration areas

| Versions | Purpose |
|---|---|
| 1–5 | Schema/RLS, private storage/events, document grants, integrity and student privacy |
| 6–10 | TEST payment settlement, enrollment/actions, assistant security, result notifications and confirmed staff workflows |
| 11–14 | Leadership publication boundary, private submissions/reviews, versioned assignment drafts/publication and subject-scoped marks |
| 15–19 | Versioned marks/review, capability metadata, atomic/versioned attendance, targeted notices and confirmed leave requests |
| 20 | DRAFT defaults, actual stored-object validation and historical publication preservation |
| 21 | Operator-defined reporting periods and scoped server attendance aggregates |
| 22 | Roster/subject-complete, submitted, atomic/versioned leadership result publication |
| 23 | Service-only actor/tenant quotas and sanitized scoped denial events |
| 24 | Object-version inspection guards and server-managed staging timestamps |

Capability metadata reports available functions only; it grants no access. School/session, teacher allocation and guardian links remain server authority. Cloud failures must remain visible instead of falling back to sample success.

## Operator diagnostics and acceptance

- [diagnose_setup.sql](diagnose_setup.sql): setup booleans/counts; never share passwords/tokens.
- [verify_hosted.py](verify_hosted.py): inspect its options; authenticated checks need local operator configuration.
- [assignment rollback acceptance](verify_assignment_workflows_rollback.sql), [connected workflows](verify_connected_workflows_rollback.sql), [versioned marks](verify_versioned_marks_rollback.sql) and [staff workflows](verify_staff_workflows_rollback.sql): review preconditions and use only authorized synthetic records. Their temporary writes roll back.
- [payment acceptance read-only SQL](payment_acceptance_readonly.sql): inspect recorded payment state without changing balances.

Run isolated tests through [tests/package.json](tests/package.json). See [testing commands](../docs/TESTING.md), [device checklist](../docs/DEVICE_TESTING.md), [verification evidence](../docs/VERIFICATION.md) and [known limits](../docs/KNOWN_LIMITATIONS.md).

Payment/model/service-role/webhook/database secrets belong only in server/CI secret stores. See [Razorpay TEST setup](RAZORPAY_SETUP.md) and [deployment](../docs/DEPLOYMENT.md). No actual captured TEST payment or physical-device end-to-end acceptance is claimed by this setup guide.

## Current operator commands and rollout

From the repository root in PowerShell:

```powershell
Set-Location -LiteralPath 'D:\VS Code\Revenex Android'
python .\supabase\verify_hosted.py --roles PRINCIPAL TEACHER STUDENT --required-schema 19
npm test --prefix supabase/tests
```

The first command requires the corresponding synthetic Principal, Teacher and Student accounts. It enters passwords through hidden local prompts and performs read-only Auth/profile/scoped data/inbox checks. No passwords, tokens or private keys belong in command arguments, README, reports or chat. It does not prove newer schema definitions, hosted writes, file HTTP delivery or two-device Realtime. Local Demo needs no cloud password; do not disable cloud Auth to mimic it.

Before hosted changes, run [read-only schema/tenant preflight](remediation_preflight_readonly.sql) as the authorized project operator and compare actual definitions/history with source. Confirm project `nhaxnuaxyqkgnlkwpqkw`, tenant `revenex_demo`, session `demo_2026` and the synthetic marker. Keep current school data, RLS and historical publications intact.

Follow [hardening rollout](../docs/BACKEND_HARDENING.md) and the [disposable synthetic-record plan](../docs/BACKEND_REMEDIATION.md). Migrations 23/24, the payment/inspection functions and compatible Android uploader must be coordinated: missing server protections deliberately fail closed. A capability value alone does not certify deployment. [Read-only monitoring](security_monitor_readonly.sql) requires 23/24 and creates no cleanup scheduler or external alerts.

The new file handler performs format inspection only; antivirus/content-disarm service is not configured. Undefined absence/exemption/correction rules, privileged MFA, external alerting, restore rehearsal, concurrent PostgreSQL load and actual private delivery remain explicit operator/testing work. See [security](../docs/SECURITY.md).

## Change documentation

Update this README's migration/command/compatibility entries and the [root change record](../README.md) for every backend change. Record exact-commit tests separately from authenticated hosted evidence; never label prepared migrations as applied. CI/push changes are explained in [deployment](../docs/DEPLOYMENT.md).


## Latest security review

[31 October findings](../docs/BACKEND_SECURITY_REVIEW.md): payment/webhook, AI and inspection now use a shared bounded reader with a 15-second total read deadline, existing byte limits, 16,384-chunk limit and cancellation that cannot hold the error response open. AI quota requires explicit boolean true. These are handler source fixes, requiring coordinated redeployment; no additional SQL migration is prepared or applied.

Fresh anonymous hosted probes reject students/fees/orders/payment requests with 401, but **AI and inspect-document return 404**. Auth health is reachable. No current hosted AI execution, successful new inspection upload, captured payment or authenticated write/Realtime acceptance is established. The source-level CI pass does not close these integration gaps.
