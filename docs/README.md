# Project documentation

Use these documents as the current entry points. Historical delivery reports and duplicated directory READMEs were removed from the active tree; Git history and the ignored local cleanup backup retain them.

| Document | Purpose |
|---|---|
| [Development](DEVELOPMENT.md) | Tools, structure, build commands and conventions |
| [Architecture](ARCHITECTURE.md) | Android/backend boundaries and synchronization |
| [Testing](TESTING.md) | Automated checks and verification rules |
| [Device testing](DEVICE_TESTING.md) | Manual cross-portal acceptance checklist |
| [Screens and workflows](SCREENS_AND_WORKFLOWS.md) | Routed actions, permissions and honest gaps |
| [Backend remediation](BACKEND_REMEDIATION.md) | Attendance, complete reads, stored-object and publication fixes; synthetic hosted-test plan |
| [Backend hardening](BACKEND_HARDENING.md) | Quotas, object-version inspection, rollout order and remaining operator work |
| [Deployment](DEPLOYMENT.md) | Exact CI triggers, quality/deployment gates and operator prerequisites |
| [Security](SECURITY.md) | Credentials, RLS, private files and payment boundaries |
| [Known limitations](KNOWN_LIMITATIONS.md) | Unfinished and unverified capabilities |
| [Verification](VERIFICATION.md) | Dated implementation/test evidence |
| [Design system](DESIGN_SYSTEM.md) | Shared Compose presentation and appearance |
| [Assets](ASSETS.md) | Photograph attribution and original motion references |
| [Architecture decision](decisions/001-separate-school-administrator-role.md) | Principal versus administrator responsibilities |

Backend-specific operator instructions live beside their implementation: [Supabase](../supabase/README.md), [payments](../supabase/RAZORPAY_SETUP.md), [server AI and deployment](DEPLOYMENT.md).


## Documentation maintenance — 31 October 2026

The [root README change record](../README.md) is the A-to-Z backend/configuration change index. Update it and the relevant directory README with every change, including small behavior, command, prerequisite, compatibility or limitation changes. Link detailed technical evidence here instead of duplicating conflicting claims. Keep historical checkpoints dated; source implementation, local checks, exact-commit GitHub CI and authenticated hosted verification are different states.

Current distinction: migrations 20–24 are prepared only; the prior hosted checkpoint remains 1–19 with later role read evidence. [Verification](VERIFICATION.md) records reruns/failures and GitHub run links. [Known limits](KNOWN_LIMITATIONS.md) and [security](SECURITY.md) track pending rollout, transport, restore, MFA, alerting and school-policy work. No frontend/UI edits are part of this backend follow-up.
