# ADR-001: Separate the school administrator from the principal

> Android portal scope superseded on 27 October 2026: see [mobile access removal](../ADMIN_MOBILE_REMOVAL.md). The backend ADMIN role and policies remain.
## Status

Accepted — 19 October 2026.

## Context

The original demo treated `PRINCIPAL` and `ADMIN` as one persona. A real school assigns those responsibilities to different people: the principal owns academic direction and high-impact approvals, while an appointed school administrator keeps the ERP operational. Combining them made the demo unclear and made it too easy to over-grant access.

The reference system demonstrates a broad ERP module set—admissions, attendance, fees, HR, academics, timetable, inventory, communication and configuration—but exposes it through a dense menu. The new experience needs those capabilities without turning the mobile application into a wall of navigation.

## Decision

Add an explicit `ADMIN` role named **School Administrator**. It receives an operational control-room dashboard and a compact navigation path for admissions, schedules, finance and the module directory. Its dashboard only links to implemented modules; it does not pretend that unimplemented settings, imports, exports, GPS, analytics or payment reconciliation already work.

The two management roles have deliberately different primary jobs:

| Role | Primary responsibility | Examples of intended authority |
|---|---|---|
| Principal | Academic leadership and executive review | Institutional outcomes, high-impact approvals, academic review and executive reporting |
| School Administrator | Day-to-day ERP operation | Admissions records, staff directory, classes/sections, timetable, notices, operational module setup and audit review |
| Teacher | Assigned academic work | Attendance for assigned classes, homework, marks workflow and own profile |
| Parent / Student | Linked learner data | Own timetable, homework, notices, results and fee status only |

The client role is only a presentation hint. Supabase Auth identities, protected school/session profiles, RLS and scoped RPCs remain the enforcement point. Every mutable production route must validate scope and capability again. A future permissions model will use additive capability grants for delegated work such as bursar, transport manager, librarian and registrar; it will not introduce a browser-like superuser menu for every staff member.

## Interaction model

The interface uses a restrained neutral surface with charcoal structure and terracotta as the primary action colour. It avoids the reference system's multi-row module strip. The administrator sees, in order:

1. a concise operational summary;
2. frequent actions such as admissions, faculty and notice creation;
3. truthful counts derived from the active repository;
4. a small module directory grouped by actual work; and
5. an explicit access-and-change-control reminder.

Motion is limited to short entrance and content transitions already supplied by the design system. Three-dimensional decoration, constant pulsing and animation that delays work are excluded because they reduce clarity in a high-frequency school workflow.

## Consequences

- Demo users can now evaluate a fourth, realistic school-operations persona.
- Production identity provisioning must explicitly provision a protected `ADMIN` profile for approved administrator accounts; client selection must never grant that role.
- The existing principal-only checks are being widened only where the administrator needs the same operational screen. Backend and PostgreSQL RLS policy remain the final authority.
- System configuration, user provisioning UI, imports, exports, real notification delivery, live payments and change-approval workflows still require their own vertical slices and acceptance tests before they are advertised as complete.

## Alternatives considered

### Keep `Principal / Admin` as one role

Rejected because it cannot explain who maintains the ERP after handover and makes least-privilege access harder to demonstrate.

### Copy the reference application's full menu

Rejected because it overwhelms mobile users, hides priorities and would imply functionality that the demo does not yet implement.

### Add a local-only administrator bypass

Rejected because it would teach the wrong security model. Demo mode can select the persona, but production access must originate from verified claims and server authorization.
