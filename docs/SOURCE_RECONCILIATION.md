# Source and evidence reconciliation — 23 October 2026

Starting HEAD for this implementation pass: `b55a57cae82ee8690011789729bf6cbdf3b5a927`. Its latest Android implementation was tested at `b604edd0cc56bf962d541f823cd12b7cbae42998`; intervening commits updated documentation/hosted evidence. Existing uncommitted README edits were preserved.

| Conflicting claim | What source/evidence actually establishes |
|---|---|
| 81 versus 82 Android tests | Parsed all 22 downloaded JUnit XML suites for the ivory build: **81 tests, 0 failures/errors/skips**. The 82-test references describe an earlier run/build. They must not be combined or presented as the new build count. Final c29ecd2 CI XML now confirms **85 tests, zero failures/errors/skips**. This is the new count after adding workflow/receipt tests. |
| Payment 404 versus deployed | 404 was the initial pre-repair observation. Migrations 6–9 were subsequently applied via SQL Editor and razorpay-payments deployed. After operator secret-name correction, unsigned user POST returns handler 401 and unsigned webhook POST returns 400 invalid signature. This is configuration/rejection evidence, not a verified capture. |
| Live subscriptions versus 30-second updates | SupabaseDataSource starts selectAsFlow collectors with retry **and** a child loop refreshing notifications, assignments, attendance, marks, notices and fees every 30 seconds. Both are implemented. Device websocket/reconnect timing has not been verified. |
| Marks editor/publication available | Datasource methods and SQL permissions exist, but the reachable timetable Scores view is read-only. There is no wired staff marks editor or publish control. Marks entry in the setup checklist is an operator/API acceptance step until a secure editor is implemented. |
| Five distinct production dashboard layouts | Principal, admin and teacher have their own routes; production routes both STUDENT and PARENT to StudentDashboardScreen. ParentDashboardScreen exists but is not the routed parent Home. Parent screenshot fixtures have been corrected to the production component. SQL still enforces separate identities and linked-child access. |
| Build success means hosted payment success | It does not. Local provider mocks and PostgreSQL tests verify contracts/permissions; a signed-in device test must establish order, checkout, capture, receipt, balance and delivery. |

No schema reset, reseed, blind migration replay or permission weakening is part of this implementation pass. Invoice buttons are being renamed Pay while review retains test-mode disclosure. Checkout resource preload is being moved earlier according to the provider's Android sample; performance improvement requires device measurement.

Sources: current MainActivity, SupabaseDataSource, academic screens, migration/function sources, downloaded XML/CI evidence and user-reported checkout behavior. The user reports slow loading inside checkout; this does not prove captured settlement.
