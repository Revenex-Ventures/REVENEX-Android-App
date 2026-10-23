# Source and evidence reconciliation — 24 October 2026

This upgrade started at 419f352 and preserves earlier work and unrelated README edits. Read CURRENT_DELIVERY.md for the exact delivered source, artifact and current CI totals.

| Conflicting claim | Current source and actual evidence |
|---|---|
| 81 versus 82 versus 85 tests | These were separate historical Android builds. c29ecd2 had 85 passing tests and 82 images; image count is not test count. The staff-workflow upgrade adds tests; use the parsed final XML totals in CURRENT_DELIVERY.md rather than reusing an old number. |
| Payment 404 versus deployed | The initial 404 was repaired by deployed schema/functions. Current protected tables/payment endpoint reject unsigned access with 401; synthetic audit still has one order, zero paid orders and zero receipts. Deployment does not establish capture. |
| Live subscriptions versus 30-second updates | Both exist: selectAsFlow collectors with retry and periodic reconciliation for inbox/assignments/attendance/marks/notices/fees. Device websocket timing and reconnect behavior still need acceptance. |
| Missing marks editor | Superseded. Results now exposes StaffMarksWorkspace to principal/admin/teacher, with private save, review/publish/unpublish and preserved report views. Migration 10 is deployed. Hosted rollback-only draft/publication/replay/recipient-isolation checks passed. Timetable Scores itself remains read-only. |
| Leave approval available in cloud | Previously threw unsupported. Reviewed administrator decisions now use decide_leave, which verifies the protected profile's school/session/role, serializes the decision and sends one recipient update. Local permission/replay tests pass; actual device approval still needs acceptance. |
| Five distinct dashboard layouts | Five Android identities, four distinct production layouts: principal, admin, teacher and shared student/parent Home. The separate ParentDashboardScreen is not production-routed. Child selection is available for multiple authorized children; RLS defines their scope. |
| Attendance present before saving | Staff default-present rows are an explicitly unsaved draft. Family read-only views now render only saved registers; absent records show a no-register state and no staff save/bulk controls. |
| Build success proves live workflows | It does not. Compose fixtures and local SQL/provider tests are distinguished from actual hosted database role tests, Auth/Realtime transport and physical device/provider capture. No captured payment or background push is claimed. |
| All reference modules are active | The directory contains real destinations. Hardware/vendor and unimplemented ordinary modules are explicitly recorded in ADMIN_REFERENCE_MATRIX.md, not fabricated as operational screens. |

Migration 10 was applied once after checking existing schema. No reset, seed replay, older-migration replay or RLS weakening occurred. Private credentials remain server-side. Supabase SQL Editor application does not populate CLI migration history.
