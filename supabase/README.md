# Supabase demo backend

Supabase Auth, PostgreSQL/RLS, private Storage, a recipient-specific database inbox and Realtime are the active cloud path. Local Demo remains separate and offline. No Firebase configuration or billing is required.

## Existing demo project

The last operator-verified checkpoint on 26 October 2026 reports migrations 1–19 applied. Hosted assignment, connected-workflow and versioned-marks rollback acceptance passed. **Do not rerun deployed migrations or seed this project.** Inspect actual schema/history before any new deployment. Older APKs using direct writes must be updated; do not restore broad write grants to accommodate them.

## New empty demo project only

1. Create a dedicated synthetic project and enable Auth, PostgreSQL, Storage and Realtime.
2. Apply the files in [migrations](migrations) once in filename order, 1–19. Keep `erp_private` out of Data API exposed schemas. Public ERP reads/writes require a protected authenticated profile and restrictive policies.
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

Capability metadata reports available functions only; it grants no access. School/session, teacher allocation and guardian links remain server authority. Cloud failures must remain visible instead of falling back to sample success.

## Operator diagnostics and acceptance

- [diagnose_setup.sql](diagnose_setup.sql): setup booleans/counts; never share passwords/tokens.
- [verify_hosted.py](verify_hosted.py): inspect its options; authenticated checks need local operator configuration.
- [assignment rollback acceptance](verify_assignment_workflows_rollback.sql), [connected workflows](verify_connected_workflows_rollback.sql), [versioned marks](verify_versioned_marks_rollback.sql) and [staff workflows](verify_staff_workflows_rollback.sql): review preconditions and use only authorized synthetic records. Their temporary writes roll back.
- [payment acceptance read-only SQL](payment_acceptance_readonly.sql): inspect recorded payment state without changing balances.

Run isolated tests through [tests/package.json](tests/package.json). See [testing commands](../docs/TESTING.md), [device checklist](../docs/DEVICE_TESTING.md), [verification evidence](../docs/VERIFICATION.md) and [known limits](../docs/KNOWN_LIMITATIONS.md).

Payment/model/service-role/webhook/database secrets belong only in server/CI secret stores. See [Razorpay TEST setup](RAZORPAY_SETUP.md) and [deployment](../docs/DEPLOYMENT.md). No actual captured TEST payment or physical-device end-to-end acceptance is claimed by this setup guide.
