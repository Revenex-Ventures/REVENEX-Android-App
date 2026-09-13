# screens/reports/

Analytics dashboard and reports.

## Files

### `ReportsAnalyticsScreen.kt`

Data-driven reports and analytics for the Principal and Admin roles.

- **Attendance Analytics**: Class-wise and month-wise attendance trend charts. Highlights classes with below-threshold attendance.
- **Fee Collection Report**: Total collected vs. outstanding by class and by month. Overdue summary.
- **Academic Performance**: Subject-wise average scores across classes. Top performers and at-risk student flags.
- **Audit Trail**: Filterable audit log table (by category: ADMISSION, ROLL_CALL, FEE_PAYMENT, GRADEBOOK, BROADCAST). Each entry shows timestamp, actor, action, and affected record.
- Export buttons to generate PDF/Excel snapshots of each report section (calls `FirebaseStorageManager` in production).

Accessible to: Principal, Admin only.
