# REVENEX SCHOOL ERP — SECURITY RULES & POLICIES

## Security Architecture

Revenex ERP implements a multi-tenant backend authorization model enforcing least-privilege access control at both the Firestore database layer and Firebase Storage layer.

Rules source files:
- Firestore: [`firestore.rules`](file:///c:/Users/Prasanna/Downloads/revenex-school-erp/firestore.rules)
- Storage: [`storage.rules`](file:///c:/Users/Prasanna/Downloads/revenex-school-erp/storage.rules)

---

## Role-Based Access Control (RBAC) Matrix

| Collection | Principal / Admin | Teacher | Student | Parent | Unauthenticated |
|:---|:---:|:---:|:---:|:---:|:---:|
| `users` | Read / Write | Read Own | Read Own | Read Own | Denied |
| `students` | Full Access | Read | Read Own | Read Child | Denied |
| `teachers` | Full Access | Read | Read | Read | Denied |
| `attendance` | Full Access | Create / Update | Read Own | Read Child | Denied |
| `fees` | Full Access | Denied | Read Own | Read / Pay | Denied |
| `leaves` | Approve / Reject | Apply / Read | Apply / Read | Apply / Read | Denied |
| `assignments` | Full Access | Create / Edit | Read / Submit | Read Child | Denied |
| `study_materials` | Full Access | Upload / Edit | Read | Read | Denied |
| `exams` | Full Access | Create / Edit | Read | Read | Denied |
| `report_cards` | Full Access | Create / Edit | Read Own | Read Child | Denied |
| `notices` | Create / Edit | Read | Read | Read | Denied |
| `library` | Full Access | Read / Issue | Read | Read | Denied |
| `inventory` | Full Access | Read | Denied | Denied | Denied |

---

## Deploying Security Rules

To deploy these security rules to your active Firebase project:

```bash
# Install Firebase CLI if needed
npm install -g firebase-tools

# Login to Google Cloud / Firebase
firebase login

# Deploy rules
firebase deploy --only firestore:rules,storage:rules
```
