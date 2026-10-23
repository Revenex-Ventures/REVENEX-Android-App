# Ivory/orange redesign — implementation ledger

Primary reference: ivory/orange education triptych → canvas, pills, rounded navigation/settings. Android dashboard → compact metric rows and task hierarchy. Task app → subject/deadline/status assignment cards. Shifty → shortcut grouping and day selection. Four images reviewed; a separate flight-booking reference was not attached. No promotional/brand content copied.

Implemented in this increment:
- [x] Shared ivory/charcoal/orange light palette and coordinated warm dark palette.
- [x] Shared dashboard task panel with side-by-side metrics; compact icon/value metric tiles.
- [x] Orange selected navigation controls, preserved inset/adaptive shell.
- [x] Assignment subject/status header, separate title/instructions and deadline/class metadata.
- [x] Attendance dated register identity, persistent confirmed/error save feedback.
- [x] Fee balance labels and exact rupee totals rather than truncated thousands.
- [x] Existing Light/Dark/System persistence retained.
- [ ] Inspect new renders and refine before claiming completion.
- [ ] Continue composition review: authentication, five dashboards, student/teacher directories/details, profile/settings, reports, timetable, notices/inbox, AI/search, operations, dialogs/forms/sheets and all important states.
- [ ] Complete Razorpay stage-by-stage diagnosis after UI implementation. Existing 404 deployment blockers are not fixed by UI work.

Full reachable route inventory remains in REVENEX_REDESIGN_CHECKLIST.md. A semantic theme pass is not proof of all-screen composition completion.

Second increment implemented: grouped full-width profile contact fields (large-text friendly), honest missing personal data, inbox category/unread metadata, school/user context in shared top bar, compact authentication heading and restrained login elevation, stacked scholar metrics instead of three cramped columns. Existing navigation, dialogs, settings, reports and operations inherit semantic surfaces but still require final rendered review.

Razorpay diagnosis: local demo guard retained; Supabase checkout reaches the order-creation path. Hosted checks on this run: Auth 200, students/fees 401, razorpay_orders 404, razorpay-payments 404. Exact remote blocker is stage 3 (deployment), before provider order creation. No secret inspection or authenticated transaction was possible. Client now maps safe HTTP statuses to actionable errors and never displays private server response text. Local payment-handler suite: 15 passed; actual capture/cancellation/webhook delivery remain unverified. SQL and provider security behavior were preserved.
