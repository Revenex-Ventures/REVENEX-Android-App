# REVENEX SCHOOL ERP — AUTHENTICATION & IDENTITY ARCHITECTURE

## 1. Authentication Architecture
Revenex School ERP implements a multi-tiered authentication layer:
- **Local Demo Mode**: Provides 1-tap persona switching between Principal, Faculty, Student, and Guardian without requiring live credentials.
- **Production Mode (Firebase / Google Workspace)**: Integrates with Firebase Authentication and Google Identity Services (`CredentialManager`).

## 2. Authentication Flow
```
               ┌───────────────────────┐
               │      AuthScreen       │
               │ (Role + Email + Pass) │
               └───────────┬───────────┘
                           │
             ┌─────────────┴─────────────┐
             ▼                           ▼
    [DEMO ACCOUNT]             [GOOGLE / EMAIL AUTH]
  Loads Sample Profile        Authenticates via Firebase
             │                           │
             └─────────────┬─────────────┘
                           ▼
             ┌───────────────────────────┐
             │ UserProfile & Role Set    │
             └─────────────┬─────────────┘
                           ▼
           ┌───────────────┴───────────────┐
           ▼                               ▼
    [ROLE ROUTING]                 [DATA SCOPE]
 Principal  → Principal Hub     Teachers see assigned classes
 Teacher    → Teacher Desk      Parents see registered children
 Student    → Student Desk      Students see own grade & marks
 Parent     → Parent Portal
```

## 3. Session Management & Security
- Passwords and tokens are never logged or stored in plain text.
- Profile states are reactive via `StateFlow<UserProfile>`.
- Token revocation and logout instantly clear session context and reset the UI stack.
