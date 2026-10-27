# Revenex APK — device acceptance testing

Use the APK produced by the current successful build or GitHub Actions artifact. This is the latest tested branding/backend candidate, not a production release. Source: 1f05004fbc27b2657abdf56349406ad6d953a96b. The APK is byte-identical to the verified brand-clarity delivery. Local packaged APK details are under ignored build delivery folders. CI artifacts identify their source SHA.

## Install without losing data

Transfer the APK to your phone and open it to update the existing app. With USB debugging enabled, use:

    adb install -r "app/build/outputs/apk/debug/app-debug.apk"

If Android reports a package/signature conflict, stop and keep the existing app/data. Do not uninstall, clear storage or reset Supabase. Use a compatible signed build or a separate test device instead.

## Two distinct ways to test

- Try demo: offline synthetic Principal, Teacher, Student and Guardian navigation. It does not prove shared cloud persistence or Razorpay settlement.
- School sign-in: use your existing synthetic Supabase accounts. Backend records determine role and permitted data. Use this mode for cross-account workflows. Do not send passwords, OTPs, keys or payment credentials in reports.

## Checklist

- [ ] Cold launch: official logo is clear; account entry opens; resume does not replay the intro unnecessarily.
- [ ] Check Principal, Teacher, Student and Guardian navigation, back actions, search, profile and settings.
- [ ] Light/Dark/System: change selection, close/reopen app, check dialogs, keyboard and system navigation margins. Check enlarged text and orientation.
- [ ] Teacher publishes an assignment to an authorized class; the correct Student sees it and another class cannot.
- [ ] Student submits a note/file; Teacher opens that same submission and records feedback; retry does not duplicate it.
- [ ] Teacher saves today's whole-class attendance; Student history and Principal summaries agree after refresh. A stale competing save must fail without partial changes.
- [ ] Teacher saves draft marks: Student cannot see them. Teacher submits for review; authorized Principal publishes; only the appropriate Student/Guardian can view results.
- [ ] Teacher requests leave; Principal decides with a reason; Teacher sees the decision. Retry should retain one request.
- [ ] Principal publishes a targeted notice; only intended recipients receive it. Open its inbox destination; reading it must not mark another recipient's copy read.
- [ ] Disconnect/reconnect network; refresh after a missed update. Log out and switch accounts: no prior account's records should remain.
- [ ] Student timetable shows the connected rail and India-time clock. Past/current/future indicators follow time; they do not automatically submit attendance.
- [ ] Open eligible invoices, history and existing receipts. Principal views must not grant Student/Guardian checkout authority.

## Razorpay TEST acceptance — user interaction required

School sign-in as the synthetic Student/Guardian → Fees → Pending → select an outstanding invoice → Pay. Confirm TEST mode before continuing. Complete Razorpay's official mock Success flow yourself. Return in the same account and select Check payment status.

- [ ] Server-verified receipt appears; balance changes once; the invoice leaves Pending when fully settled; Principal inbox reflects settlement.
- [ ] Repeated status checks do not create a second receipt or debit.
- [ ] Cancellation/failure keeps the fee unpaid and shows an accurate status.
- [ ] If verification remains pending or fails, retain the reference and report only the sanitized status/error. Do not create another payment to bypass an unresolved attempt.

Do not use live payments or real card/UPI details for this demo acceptance test. A captured Razorpay TEST transaction, provider webhook delivery and receipt on a physical device have not yet been verified in this run.

## Known limits

Current build: 13 focused UI checks passed, lint 0 errors / 81 warnings, no Firebase dependencies. The inherited backend checkpoint had 185 passing Android tests, 140 isolated database checks and 36 handler tests; these are not a new full-suite run for the branding-only update. Hosted rollback-only workflow checks passed; they do not prove Android Auth/Storage/Realtime transport.

Historical attendance correction approval is unavailable; historical direct register edits are denied. Hosted AI last returned 404. Identity provisioning, protected profile corrections and library circulation remain operator-managed/unavailable. No device push/email/SMS integration is claimed. Admin web is deferred. Emulator System UI ANRs occurred before recovery; successful runtime captures and current native renders are in the brand-clarity gallery. Physical-device acceptance remains pending.

## Reporting a failure

Record build/source, portal, screen, action, expected/actual result, time, network state and sanitized error text. Use synthetic records. Redact private account/payment details from screenshots. Do not reset/reseed the database or rerun deployed migrations to work around an error.

- [ ] Administrator entry/demo cards are absent. An admin-only identity shows unsupported-on-Android and can return to sign-in without changing roles.
