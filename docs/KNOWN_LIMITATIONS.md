# Known limitations

This repository is a school ERP demo acceptance candidate. Do not advertise unverified integrations as production-ready.

- A captured Razorpay TEST checkout, receipt/balance update and actual provider webhook delivery on a physical device remain unverified in the last delivery. Mock handler tests and hosted SQL checks do not establish capture.
- Complete two-account Android Auth/Storage/Realtime, reconnect, access revocation and logout/account-switch acceptance is still required. Hosted rollback checks simulate protected roles; they are not password login or transport tests.
- Historical attendance correction request/approval is unavailable. Direct historical register changes remain denied.
- Hosted AI last returned 404; it needs operator deployment/model configuration. No paid model call is enabled automatically.
- Identity provisioning, protected profile corrections and library circulation remain operator-managed or unavailable. Additional SQL roles do not imply implemented Android portals for every role.
- In-app inboxes are not device push. FCM, push, SMS and email delivery are not configured.
- Cloud sessions are memory-only. There is no offline cloud-write queue, silent fallback to sample data or production-scale pagination guarantee.
- Admin web, live payments, refunds, disputes, partial payments, production accounting and production signing/distribution are outside the configured demo.
- Emulator System UI ANRs occurred during verification before recovery; physical-device stability/performance remains to be assessed.

Use [device testing](DEVICE_TESTING.md) and record sanitized failures. Preserve data and fail closed rather than inventing successful behavior.
