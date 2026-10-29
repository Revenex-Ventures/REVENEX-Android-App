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
