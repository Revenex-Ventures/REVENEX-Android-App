# Known limitations

This repository is a school ERP demo acceptance candidate. Do not advertise unverified integrations as production-ready.

- A captured Razorpay TEST checkout, receipt/balance update and actual provider webhook delivery on a physical device remain unverified in the last delivery. Mock handler tests and hosted SQL checks do not establish capture.
- Complete two-account Android Auth/Storage/Realtime, reconnect, access revocation and logout/account-switch acceptance is still required. Hosted rollback checks simulate protected roles; separate Principal/Teacher/Student Auth read evidence was recorded on 30 October, but does not establish complete Android transport behavior.
- Historical attendance correction request/approval is unavailable. Direct historical register changes remain denied.
- Hosted AI last returned 404; it needs operator deployment/model configuration. No paid model call is enabled automatically.
- Identity provisioning, protected profile corrections and library circulation remain operator-managed or unavailable. Additional SQL roles do not imply implemented Android portals for every role.
- In-app inboxes are not device push. FCM, push, SMS and email delivery are not configured.
- Cloud sessions are memory-only. There is no offline cloud-write queue or silent fallback to sample data. Complete keyset reads now reject truncated/drifting counts with a 100,000-row ceiling; independent HTTP pages do not provide a transaction-wide snapshot or a production-scale performance guarantee.
- Admin web, live payments, refunds, disputes, partial payments, production accounting and production signing/distribution are outside the configured demo.
- Emulator System UI ANRs occurred during verification before recovery; physical-device stability/performance remains to be assessed.

Use [device testing](DEVICE_TESTING.md) and record sanitized failures. Preserve data and fail closed rather than inventing successful behavior.


## Backend hardening deployment limits — 31 October 2026

- Migrations 20–24 and new protections are committed source, not applied hosted. Current role read evidence reports capability 19; compare actual deployed definitions before rollout.
- File inspection is bounded format/signature/structure validation, not antivirus or content disarm. It does not certify safe rendering; old unchanged published attachments are retained without inventing inspection evidence.
- Absence, exemption and reasoned correction of published results require approved school rules. No unrestricted publication override or automatic unpublication is introduced.
- Privileged MFA, external monitoring delivery, database SSL/network administration, backup/Storage restore rehearsal and real multi-connection/load acceptance are not configured or verified by a source/CI pass.
- Repository branch protection/rulesets and environment approvals require owner confirmation. Branch pushes now trigger quality checks; this is separate from enabling deployment.
- Local Demo has synthetic offline personas. Cloud accounts remain Auth-protected and privately operator-provisioned; no new password reset feature or public demo password is added.

See [backend hardening](BACKEND_HARDENING.md) and [security work](SECURITY.md). These limits must remain visible until actual evidence closes them.
