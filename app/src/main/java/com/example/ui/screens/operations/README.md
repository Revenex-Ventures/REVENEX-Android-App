# screens/operations/

School operations hub: transport, library, inventory, and leave management.

## Files

### `OperationsScreens.kt`

A hub screen with sub-navigation into four operational modules:

**Operations Hub:**
- Grid of tiles linking to Transport, Library, Inventory, and Leave Management.

**Transport:**
- List of bus routes with route number, driver name, vehicle number, capacity, and stop list.
- Transport Manager can add/edit routes.
- Students/Parents can view their assigned bus route and real-time stop information.

**Library:**
- Book catalog with title, author, ISBN, available copies, and issued copies.
- Librarian can update available/issued counts when books are checked out or returned.
- Students can search the catalog.

**Inventory:**
- Asset registry: IT hardware, sports equipment, lab equipment, furniture.
- Each asset has a condition status (Good / Needs Repair / Condemned) and assigned location.
- Principal and Admin can update asset status.

**Leave Management:**
- For Teachers/Students: submit a new leave request (opens `ApplyLeaveDialog`), view request history and current status.
- For Principal/Class Teacher: review pending leave requests and approve or reject them.
- Status workflow: PENDING → APPROVED or REJECTED.

Accessible to: All roles (scoped by role for edit operations).
