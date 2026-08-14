# API 03 — School Onboarding

## Purpose

Implement the third API module for the School Management System.

This module handles the initial setup after a tenant selects a subscription plan.

The onboarding flow is:

    Register
       │
       ▼
    Choose Plan
       │
       ▼
    School Onboarding
       │
       ├── 01 School
       │
       ├── 02 Branch
       │
       └── 03 Academic Year
       │
       ▼
    Setup Complete
       │
       ▼
    Dashboard

This module creates the basic structure required before the tenant can use the School Management System.

---

# 1. Base URL

All endpoints use:

    /api/v1

Onboarding endpoints:

    /api/v1/onboarding

School endpoints:

    /api/v1/schools

Branch endpoints:

    /api/v1/branches

Academic Year endpoints:

    /api/v1/academic-years

---

# 2. Important Architecture

The system is multi-tenant.

The relationship is:

    User
      │
      ▼
    Tenant
      │
      ├── Subscription
      │
      └── School
            │
            ├── Branch
            │
            └── Academic Years

The authenticated user's tenant must always be used.

Never allow the frontend to submit an arbitrary:

    tenantId

for normal onboarding operations.

The backend must determine the tenant from the authenticated user.

---

# 3. Onboarding Flow

The frontend flow is:

```text
┌─────────────────────────────┐
│        CHOOSE PLAN           │
│                              │
│ Starter                      │
│ Professional ⭐              │
│ Enterprise                   │
│                              │
│ [ Choose Plan ]              │
└──────────────┬──────────────┘
               │
               ▼
╔═══════════════════════════════╗
║       SCHOOL SETUP            ║
║                               ║
║  ●━━━━━━━━○━━━━━━━━○          ║
║  01       02       03         ║
║  School    Branch   Academic  ║
║                     Year      ║
╚══════════════╦════════════════╝
               │
               ▼
      ┌──────────────────┐
      │  01 ADD SCHOOL   │
      │                  │
      │ School Name      │
      │ School Type      │
      │ Email            │
      │ Phone            │
      │ Address          │
      │                  │
      │ [ Continue → ]   │
      └────────┬─────────┘
               │
               ▼
      ┌──────────────────┐
      │  02 ADD BRANCH   │
      │                  │
      │ Branch Name      │
      │ Address          │
      │ Phone            │
      │                  │
      │ [ ← Back ]       │
      │ [ Continue → ]   │
      └────────┬─────────┘
               │
               ▼
      ┌──────────────────┐
      │ 03 ACADEMIC YEAR │
      │                  │
      │ Academic Year    │
      │ Start Date       │
      │ End Date         │
      │                  │
      │ [ ← Back ]       │
      │ [ Done ✓ ]       │
      └────────┬─────────┘
               │
               ▼
      ┌──────────────────┐
      │  SETUP COMPLETE  │
      │                  │
      │ [ Go Dashboard ] │
      └────────┬─────────┘
               │
               ▼
          DASHBOARD