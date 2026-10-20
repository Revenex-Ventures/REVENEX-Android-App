# Server-authorized Revenex AI and GitHub delivery

## Implemented boundary

The supplied Revenex AI prompt lives only in `supabase/functions/revenex-ai/prompt.md`. `config.toml` bundles it as a server static file. It is not an Android asset, BuildConfig field or client request.

Android submits only a message and a bounded conversational transcript through the signed-in Supabase Functions client. Transcript role labels are conversation text, never ERP roles. The endpoint rejects client role, school/session IDs, language, permitted student IDs, model configuration and tool-permission fields. It calls Supabase Auth `/auth/v1/user`, reads the authoritative `profiles` row, and derives permitted student IDs through RLS plus parent links/teacher assignments. Each database tool request uses the user's bearer token and explicit school/session filters; the AI datasource never uses a service-role key for reads.

Preferred language comes from `profiles.preferred_language`, default `en`. The authenticated `set_assistant_language(p_language)` RPC can change only the caller's language; it cannot change role, school or session. Model instructions include verified role, school, session, permitted IDs and the server-computed read-tool allowlist. The supplied prompt is supplemented with hard server restrictions; prompt injection cannot add tools or bypass the independent permission checks.

Available read datasets: student summary, attendance, timetable, assignments, notices, published/permitted marks, and fee balances. Contacts, document URLs, payment transaction credentials and raw student payloads are not forwarded. Queries return at most 20 records and explicitly report truncation. Context above 500 rows fails closed pending pagination. Teacher marks are conservatively unavailable in AI until explicit subject permissions exist. Transport/library roles receive only notices; finance access is not inferred.

Chat action tools are intentionally unavailable. Enrollment/payment changes remain in their existing confirmed ERP workflows. The model cannot write attendance, publish marks, change fees or delete records. Every response includes an empty `actions_performed` array. There is no live web source or claim of cross-chat memory. At most eight quoted conversation messages are supplied, with ERP facts rechecked through tools.

## Activation: server secrets only

1. Apply migrations 1-7 already documented for the demo, then `supabase/migrations/202610210008_assistant_security.sql` once. It adds language preferences and protected request counters; anonymous/client quota rewrites are forbidden. Limits are 5 requests/user/minute, 30/school/minute and 100/user/day.
2. Set `OPENAI_API_KEY`, `OPENAI_MODEL`, and `REVENEX_AI_ENABLED=true` directly in Supabase Edge Functions secrets. No model is silently selected and no paid model call occurs without explicit server configuration. The implementation uses OpenAI Responses; swapping providers requires only the server adapter. Supabase supplies its own URL and anon key to the function.
3. Deploy with the pinned Supabase CLI: `supabase functions deploy revenex-ai --project-ref nhaxnuaxyqkgnlkwpqkw --use-api`. `verify_jwt=false` disables only the gateway check; every request is authenticated manually through Auth before any model use.
4. Sign in through Supabase Demo. Test general learning, Hindi/Hinglish, own attendance/fees, unrelated-student denial, teacher finance denial, expired sessions, quotas, provider outage and prompt injection. Local demo keeps its bounded offline assistant and does not pretend it is a live model.

OpenAI requests use `store:false`. This is not a promise of zero provider retention; review provider account controls and school data-processing requirements before enabling school records. No question/answer content is logged by this handler. Model output remains AI-generated; tool provenance and server-authorized reads do not guarantee flawless wording or reasoning.

## CI/CD

`.github/workflows/ci.yml` runs on PRs, pushes to main and manual dispatch. Action revisions are pinned to verified full commit SHAs. Jobs have read-only repository tokens, bounded timeouts and no PR deployment secrets.

- Backend: credential scan including history; locked dependency install; active demo npm audit; Deno type/lint checks; PostgreSQL/RLS suite; AI/payment handler tests; retained legacy backend syntax/authorization tests. Firebase is never deployed.
- Android: JDK 21, SDK 36.1, Gradle wrapper checksum validation, APK assembly, unit/Compose tests and lint. An additional APK scan rejects the server prompt and private server configuration markers. APK and diagnostic artifacts expire after 14 days.
- CD: main-branch deployment depends on BOTH jobs. Set repository variable `ENABLE_SUPABASE_DEPLOY=true` only after migration 8 is applied and `supabase-demo` is configured. Set `SUPABASE_ACCESS_TOKEN` in that GitHub Environment, not the APK or source. Public repo variables are `SUPABASE_PROJECT_REF`, `SUPABASE_URL` and `SUPABASE_PUBLISHABLE_KEY`. The job deploys both Edge Functions and verifies anonymous requests receive 401; it does not run a paid model request or payment.
- Migrations are not blindly replayed: this hosted project previously used SQL Editor without CLI migration history. Reconcile history before any automated `db push`. No database password is requested or committed.
- Rollback: revert the problematic server commit on main, let CI pass and redeploy; disable `REVENEX_AI_ENABLED` on the server for immediate model shutdown. Schema changes in migration 8 are additive and need not be dropped to roll back code.

The current GitHub identity can push but has no repository-admin permission. Required status checks, environment protections and production signing/Play Store delivery require repository-owner configuration. CI produces a debug-signed demo APK, not a production-signed Play release. Deployment secrets/model keys have not been supplied, so successful CI alone does not mean hosted AI or checkout is live. The retained legacy Firebase dependency tree is not the active demo deployment and is not covered by the active Supabase npm audit.

## Authoritative references

- [OpenAI Responses function calling](https://developers.openai.com/api/docs/guides/function-calling): server-executed tools, tool-result round trips and bounded schemas.
- [GitHub Actions security](https://docs.github.com/en/actions/reference/security/secure-use): full action SHA pins and secret/token isolation.
- [Supabase function configuration](https://supabase.com/docs/guides/functions/function-configuration): static prompt bundling and gateway configuration.
- [Supabase deployment](https://supabase.com/docs/guides/functions/deploy): authenticated deployment credentials.
