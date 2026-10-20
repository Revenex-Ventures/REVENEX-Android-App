# Supabase demo migration

Scope: retain local demo and existing UI, add opt-in Supabase demo. Legacy Firebase source stays available for later comparison but is not initialized or selected by either demo mode. Payments and push are disabled in demo builds.

1. Public-only ignored configuration, Kotlin SDK, configuration validation, build without Google services.
2. Transactional schema/RLS/storage/realtime migrations and isolated synthetic seed; provision Auth users through the dashboard, never embed passwords.
3. Authenticate before binding cloud state. Map relational rows to existing models, persist allowed writes, subscribe to scoped changes, show sync/error state. No sample-data fallback in cloud mode.
4. Android regression checks and executable database authorization tests. Hosted verification requires applying migrations and creating Auth accounts; public keys cannot administer the project.

Security boundary: PostgreSQL policies are authoritative, profiles are provisioned by the project operator, child/class access uses relations, tenants/sessions use composite foreign keys. Clients cannot grant roles, create parent links, mutate audit logs, or mark somebody else's notifications read. Files are private and addressed by tenant/session/owner. No privileged key is needed by Android.

Acceptance: local demo works without any cloud config; Supabase fails visibly if unconfigured; no secret is imported from .env; no Firebase/FCM/Razorpay calls from demo; authenticated records never leak across role, school, session or logout. Database tests exercise real RLS, rather than imitating policy logic in Kotlin.
