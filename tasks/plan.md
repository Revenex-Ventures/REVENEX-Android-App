# ERP improvement plan

Scope: preserve the Kotlin/Compose Android app; build a credible, explicitly labelled school ERP demo and establish a secure path to customer deployments. No live payments, cloud deployment, or new paid services in this change.

1. Audit architecture, every screen file, integration boundaries, and existing verification. Record evidence and remaining gaps in docs/ERP_AUDIT.md.
2. Close unauthenticated administrative API access using verified Firebase claims, route roles and tenant/session checks. Verify with Node regression tests.
3. Close identified cross-school Firestore and overlapping Storage permission gaps. Extend emulator regression tests.
4. Attempt APK build, unit tests, lint and rules tests; record actual results and environmental blockers.
5. Define the next vertical slices: secure payment reconciliation, honest production data states, notifications, then UI workflow improvements. Avoid a broad cosmetic rewrite before data correctness.

Threat boundaries: untrusted HTTP bodies must never select another tenant or provision roles; staff claims must not grant access outside their school; overlapping Storage grants must not expose student identity documents.

Acceptance: unauthorized API requests fail before mutations; cross-tenant claims fail; permitted school administrators remain supported; health and signed webhook routes retain their independent access model; no secrets are added; remaining risks are explicit.

## Assistant trustworthiness increment — 20 October 2026

## Device navigation spacing — 20 October 2026

The responsive shell owns and consumes safe-drawing insets on every side, including display cutouts and Android navigation bars. The bottom dock reserves space for its raised icon plus side and bottom margins. Keyboard insets protect the shell and assistant composer. Preserve the existing visual tokens. Verify with assembleDebug; gesture/three-button navigation and landscape device interaction remain separate acceptance checks.

Scope: replace the keyword-driven assistant that invents school records with a local, permission-scoped assistant. It may provide general study guidance and navigation help, and may summarize only the signed-in student's or a parent's explicitly linked children's loaded records. It must not perform writes, claim a live provider exists, or expose other people’s records.

Acceptance: deterministic tests cover record scoping, refusal of consequential actions, absence of fabricated school answers, and the provider-not-configured state. The Compose screen must visibly state whether it is using demo data or whether live AI is unavailable.
