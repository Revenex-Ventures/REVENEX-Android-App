# Backend remediation — 30 October 2026

Baseline: `dae188c1ad30b1071f3c79884916863c0e8a131b` on `codex/project-review-2026-10-29`.
There were no source changes since that commit; the preceding audit report was untracked and is preserved separately.
No UI source, existing hosted school record, existing RLS policy, deployment or hosted migration was changed.

## Fixed in source

- Dashboard and report cards use the same structured attendance definition: PRESENT/LATE/EXCUSED divided by all recorded roll-call sessions, including ABSENT/LEAVE. School and academic session must match. Future dates are excluded using Asia/Kolkata school time.
- Reporting periods are explicit operator-managed dates, not inferred from names such as “Term 1”. Empty term configures session bounds; named periods narrow those bounds inclusively. Legacy reports without an explicit period retain session-to-date attendance and carry `attendanceScope`/period metadata; they do not pretend to be a configured term aggregate. No historical percentage/publication field is backfilled.
- The `attendance_summaries` security-invoker view calculates counts in PostgreSQL over all RLS-visible records before HTTP pagination. The Android source uses it when capability-advertised; schema19 can still use complete structured session rows. A failed capability probe does not produce a supposedly complete snapshot.
- All multirow Android school snapshot/refresh queries page by unique server ID with exact remaining counts. Short pages caused by a smaller server cap continue loading. Missing counts, repeated IDs, early empty pages, changed cardinality or the explicit 100,000-row safety limit reject the entire read; the old complete snapshot remains stale/error state. Final counts are checked again. This detects cardinality/order failures, not a globally serializable multi-HTTP snapshot; aggregate queries themselves use one database statement.
- Integration readiness now uses explicit local-only, missing configuration, sign-in-required and not-verified states. Public configuration/Auth presence cannot certify provider readiness. The unused misleading Razorpay/Gemini booleans were replaced; the actual assistant provider is OpenAI.
- Stored-object triggers check the private bucket and lock actual object rows for new/changed references and newly published references. Metadata alone is insufficient. Historical unchanged published references are preserved, including legacy objects subsequently lost. No historical publication state or submission is rewritten.
- Future assignment defaults are DRAFT in SQL and the Kotlin model. Existing sample/seed fixtures explicitly retain their historical PUBLISHED state. Source fixture edits were not applied to hosting. Existing class/subject authoring and draft-file RLS restrictions remain in force.
- New result publication requires a configured school/session/class/exam scope, a current complete non-archived roster, all required subjects independently defined in the exam/catalog, valid numeric maxima, submitted marks, and the observed scope/mark versions. Zero is valid; missing is not zero. The transaction locks scope, roster, exam and student-term edits, then publishes the intended grid atomically. Replay is idempotent and does not duplicate result events.
- The legacy per-student endpoint cannot bypass completeness or unpublish history. It supports an authorized no-op only for already-published historical results. Editing submitted unpublished marks already returns them to DRAFT and requires resubmission. Published edits/unpublication remain blocked because no reasoned correction workflow is defined.
- Android publication submits the entire configured batch with observed versions; no frontend change or new selection widget was introduced. Missing/ambiguous scope produces an explicit configuration error. New batches require schema22; configuration cannot silently fall back to the old publishing API.

## Prepared migrations; none applied to hosting

| Migration | Purpose |
| --- | --- |
| [20 — stored objects and draft/review boundary](../supabase/migrations/202610300020_backend_integrity.sql) | New assignment default; stored-object checks; transitional submitted-mark publication checks |
| [21 — attendance reporting](../supabase/migrations/202610300021_attendance_reporting.sql) | Operator-managed reporting bounds, RLS-preserving server aggregate view, additive capabilities |
| [22 — atomic result batches](../supabase/migrations/202610300022_result_batch_publication.sql) | Explicit eligible roster/exam subjects, optimistic versions, atomic leadership publication, legacy no-op/correction denial |

These were applied only to fresh isolated PGlite fixtures. No reset, reseed or deployment was performed against the existing project. Do not replay old migrations or use the seed script against hosting.

The existing policy suite explicitly exercises legacy migration1–21 behaviors, applies migration22 afterwards, and asserts tightened publication endpoints plus preserved school/student isolation. The new latest-schema batch suite tests the changed publication contract independently. Older rollback SQL remains legacy regression material, not an instruction to unpublish current records.

## School-policy/configuration gaps intentionally fail closed

- No absence/exemption grading rule exists in the current model. Numeric zero remains a scored zero. Missing marks and non-SCORED assessment markers cannot be published through an invented rule.
- No reasoned correction/review model exists for published results. There is no unrestricted override, automatic unpublication or silent overwrite.
- Marks identify a term but have no independent exam ID. A result scope therefore permits one exam per school/session/class/term. Distinct exams cannot silently share the same marks; they need distinct configured terms or a separately designed exam-identity migration.
- Required subjects/maxima and the eligible roster must be configured from authoritative school exam/roster policy. No configuration was fabricated/backfilled for existing schools.
- Explicit term/session reporting dates are not available in old schemas; the legacy metric is explicitly session-to-date until the operator defines dates.

## Secure hosted setup — one checklist

1. In the existing authorized Supabase dashboard, confirm the project is the hostname configured in the two public Android properties. Confirm the intended records are synthetic `revenex_demo` / `demo_2026`; do not change roles or provision replacement accounts to obtain a pass.
2. From an interactive local PowerShell terminal in this repository, run the **existing credential-entry workflow**, now with role selection:

   ```powershell
   python supabase/verify_hosted.py --roles PRINCIPAL TEACHER STUDENT --required-schema 19
   ```

   It requires the existing synthetic Principal, Teacher and Student account passwords, entered locally through hidden `getpass` prompts. No password, access token or private key belongs in chat, command arguments, report files or Git. The session is held in memory and signed out after each role.
3. The command verifies backend-derived role/school/session, reads the schema capability version, performs complete paginated roster/fee/class/assignment/mark/attendance/inbox reads, and checks scope/recipient isolation. It does not write school records, download documents, call AI or trigger payments. Its sanitized output is `app/build/reports/supabase-hosted.json`. Capability presence is not a definition checksum.
4. An authorized operator can separately run [the read-only preflight](../supabase/remediation_preflight_readonly.sql) to inspect synthetic markers, schema presence and function fingerprints. Compare actual definitions/RLS/history with source before approving any migration. No hosted migration is authorized merely by a successful capability check. After explicitly approved migration/configuration, the same credential command can use `--required-schema 22` to check the new capabilities.

No credentials were available to the agent, and no authenticated hosted success is claimed. The noninteractive workflow was not supplied credentials. Browser restrictions were not bypassed.

## Disposable synthetic connected-write plan — prepared, not executed

Preconditions: successful read-only tenant/schema verification; explicit authorization of the following mutations; an existing disposable synthetic class with already authorized P/T/S accounts, complete non-archived synthetic roster and canonical required subjects. If this class/account context does not already exist, stop for an operator plan rather than modifying real allocations or roles.

- Use `remed_20261030_<random UUID>` identifiers and a unique exam/term. Never reuse existing attendance/result identifiers. The operator configures the test exam, required subject/maxima scope, eligible roster and reporting bounds; this is configuration, not a permission expansion.
- T creates DRAFT assignment with a clearly synthetic document; S cannot read draft or download it. T explicitly publishes; S reads/submits; assigned T reviews. Retry the same observed version and verify one event per intended recipient.
- For attendance, use an explicitly authorized disposable synthetic class/date/slot with no preexisting register. Save every eligible pupil with observed versions. Verify S's record and P's dashboard/report aggregates for the same period; try a stale save and a foreign pupil denial. Never overwrite an existing school register.
- Enter every roster × required-subject mark, including a legitimate zero; show that missing, unsubmitted, stale and unsupported absence/exemption cases fail without partial publication. P publishes the configured batch once; S/G see only their own authorized results. Replay the same request and verify stable versions/inbox counts. Confirm published edits/unpublication are denied.
- Publish a uniquely targeted notice and verify only intended recipients/read flags. Test private file access using synthetic bytes only; do not download or include private student documents in evidence.
- Retain sanitized operation/role alias/record identifier/time/outcome and disclose durable audit/inbox effects. No cleanup deletes are authorized here; rollback cannot erase external or audit history. No payment, actual push/email/SMS or model inference is included.

## Verification record

Current Node results: the policy/upgrade suite passed **142 checks**; **20** payment handler tests, **16** AI handler tests, and **30** dedicated stored-object/reporting/result-batch tests passed. These are local SQL role emulation/mocked tests, not Auth/Storage/Realtime transport.

SQL reproductions were recorded before the fixes: stored-object/default/review regressions failed on the baseline; reporting tests failed without the new reporting schema. The original JVM reproduction run stalled in KSP before producing results; it is not counted as a successful RED run.

Android verification encountered stale shared Gradle journal/project locks owned by the earlier terminated daemon. Isolating user/project caches then failed while compiling the unchanged `settings.gradle.kts` (`plugins`/`id` unresolved at line15). These are preserved in ignored `app/build/remediation-*-focused.log` files. No checks were skipped or source/settings changed to suppress the failure. A direct cached-Kotlin compiler/JUnit check of changed backend sources is recorded separately if successful; it is not a full APK/Android suite certification.

The changed backend Kotlin sources compiled successfully with cached Kotlin **2.2.10** and the serialization plugin, including the actual updated Supabase data source, models, attendance, pagination, readiness and result-batch helpers. **14 JUnit regression tests passed** across BackendRemediationTest, CompletePaginationTest and SupabaseStudentSummaryTest. This direct check used existing cached dependencies/previously built unchanged classes; it does not replace a clean complete Android build.

Executed commands and evidence:

```powershell
npm test --prefix supabase/tests
node --test supabase/tests/assistant-edge.test.mjs
python app/build/remediation-direct-compile.py
python -m py_compile supabase/verify_hosted.py
python scripts/check_docs.py
python scripts/check_secrets.py --history
git diff --check
```

The direct compiler command is a local ignored diagnostic artifact, not a new supported build system. Its exact Java/compiler/classpath arguments and output are under `app/build/remediation-direct/`; successful output is `Kotlin compiler exit: 0` and `OK (14 tests)`. Backend outputs are `app/build/remediation-backend-final.log` and `app/build/remediation-ai-final.log`. Documentation links and credential/history scans passed; `git diff --check` passed. No UI file differs from the baseline.

Pagination API signatures were checked against cached Supabase SDK 3.2.3 and the [official Kotlin reference](https://supabase.com/docs/reference/kotlin/select). Once the local Gradle/settings environment is repaired, run the normal full project gate, not just the direct check:

```powershell
.\gradlew.bat --no-daemon --no-configuration-cache --max-workers=1 assembleDebug testDebugUnitTest lintDebug verifyNoFirebaseDependencies
```

At initial remediation completion there was no authenticated hosted evidence. Migrations 20–22 remain prepared only; no hosted mutation was attempted. The following later user-run checks supersede that initial access blocker for authenticated reads only.

## Authenticated hosted read evidence received

The user securely entered credentials locally and ran the verifier against `nhaxnuaxyqkgnlkwpqkw.supabase.co`. Saved reports confirm synthetic tenant `revenex_demo` / session `demo_2026` and the following outcomes on 30 October 2026:

| Role alias | Verification time (IST) | Evidence | Result |
| --- | --- | --- | --- |
| Principal | Exact completion time not retained | User-provided console output from the earlier multi-role run | Auth login, protected profile, scoped reads and inbox passed; the older script did not save progress before the later Teacher failure |
| Teacher | 20:28:39 | `app/build/reports/supabase-hosted-20261030T145822415879Z.json` | Complete selected-role run; capability version 19; 1 visible pupil and 2 recipient inbox rows; scoped reads/teacher fee denial checks passed |
| Student | 20:29:24 | `app/build/reports/supabase-hosted-20261030T145911096361Z.json` | Complete selected-role run; capability version 19; 1 authorized pupil and 2 recipient inbox rows; scoped reads passed |

These are current HOSTED AUTH reads, not local mocks. The role reports' `complete=true` applies to each invocation's selected role, not a complete backend acceptance test. The latest `supabase-hosted.json` contains Student only; timestamped reports preserve Teacher separately.

The reports explicitly record `schema_definitions_compared=false`. The backend advertises capability version 19; that is not proof of exact deployed migration/function definitions, nor evidence that the prepared remediation is deployed. No authenticated school write, private Storage HTTP delivery, websocket reconnect, result-batch publication, payment capture or AI execution was verified by these commands. The remaining next step is the operator's read-only deployed-definition/preflight comparison before any separately approved migration or disposable connected-write test.
