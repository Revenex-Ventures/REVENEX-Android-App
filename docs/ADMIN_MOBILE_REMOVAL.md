# Administrator mobile access removed

The Android app supports Principal, Teacher, Student and Parent/Guardian entry.
Administrator work is reserved for the future Admin web portal.

## Dependency boundary

`AdminDashboardScreen` was a standalone presentation and was removed. Its people,
fees, attendance, reports, admission and notice components also serve Principal;
those shared screens and workflows remain. The old shared attendance presentation
is retained for its existing Principal consumers, but no Administrator route uses it.

The account-entry Administrator card and offline persona tile are removed.
Administrator bottom-navigation items and searchable module destinations are empty.
The Android root and shared shell reject a stale or restored admin-only identity
before any workspace or modal can render. Returning to sign-in logs out rather
than impersonating Principal, Teacher or Student.

After Supabase validates Auth and loads the protected profile, an ADMIN profile
receives a fixed unsupported-on-Android message before school records or live
subscriptions load. Existing failure cleanup clears the local session and account
state. Choosing a supported portal cannot override this backend-derived role.

## Preserved

- `UserRole.ADMIN`, existing profiles and sample records.
- Supabase role enums, SQL/RLS, records, private storage and Edge Functions.
- Server administrative enrollment, publication, leave and other workflows for
  the future web portal.
- Principal, Teacher, Student and Guardian navigation and shared actions.
- Approved theme tokens and dark portal screen compositions.

## Verification

Checks cover entry/demo selectors, unsupported admin Auth diagnostics, restored
admin identity, nested shell rejection, return to sign-in, Principal shared
workspaces and supported-role journeys. Native screenshots and the final build
report are packaged under `app/build/admin-removal-delivery` after tests finish.
Hosted ADMIN login is not claimed without an operator-provisioned test account.
No database migration, reset, seed or deployment is required by this change.

This decision supersedes the Android portal portion of the historical
[Administrator-role decision](decisions/001-separate-school-administrator-role.md).
The distinct backend role and its permissions remain valid.
