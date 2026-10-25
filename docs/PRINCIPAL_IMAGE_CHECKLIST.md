# Principal image implementation gate

References: 09d3dfb3 dashboard; 5c8ffde2 nine destination panels. Actual routes are in MainActivity, not legacy previews. Preserve official RV branding, account scope and all backend actions.

| Batch | Routes / actual presentations | Implementation | Render comparison | Interaction |
|---|---|---|---|---|
| A | Dashboard / PrincipalDashboardScreen; students / PrincipalStudents; teachers / PrincipalStaff; principal_attendance / PrincipalAttendanceOverview | In progress | Pending | Pending |
| B | academic_hub / PrincipalAcademics; fees / PrincipalFees; reports / PrincipalReports | Pending | Pending | Pending |
| C | leaves / PrincipalApprovals; approval detail; profile / PrincipalAccount; nested details/dialogs | Pending | Pending | Pending |

Unavailable features must not become fake controls: non-teaching staff, staff attendance, attendance corrections, admission/fee-concession approvals, permission promotion and unsupported session switching. Existing leave RPC, invoice settlement, student/faculty details, report cards, notice and document paths remain connected. No database changes are planned.
