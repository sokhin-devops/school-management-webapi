# API 02 — Plans & Subscription

## Purpose

Implement the second API module for the School Management System.

This module manages:

* Subscription plans
* Plan features
* User/school subscription
* Current subscription
* Plan selection during onboarding
* Subscription status
* Subscription changes

This API comes after:

```
API 01 — Authentication
```

The overall onboarding flow is:

```
Register
   │
   ▼
Choose Plan
   │
   ▼
School Setup
   │
   ├── School
   ├── Branch
   └── Academic Year
   │
   ▼
Dashboard
```

---

# 1. Base URL

All endpoints use:

```
/api/v1
```

Plans:

```
/api/v1/plans
```

Subscriptions:

```
/api/v1/subscriptions
```

---

# 2. Important Architecture Decision

The subscription belongs to the **SaaS tenant/account**, not directly to an individual student, teacher, or branch.

For this system, the recommended structure is:

```
User
  │
  ▼
Tenant / Organization
  │
  ├── Subscription
  │
  ├── School
  │
  └── Branches
```

The authenticated user who registers initially becomes the owner of the tenant.

Example:

```
Sokhin
  │
  ▼
Tenant: ABC Education Group
  │
  ├── Subscription: Professional
  │
  └── School
        │
        ├── Main Branch
        └── Secondary Branch
```

Do not tie a subscription directly to `users`.

---

# 3. Tenant

If API 01 does not already create a tenant, add the required tenant structure as part of this module.

Table:

```
tenants
```

Fields:

| Field      | Type          | Required | Description              |
| ---------- | ------------- | -------: | ------------------------ |
| id         | UUID / BIGINT |      Yes | Primary key              |
| name       | VARCHAR(150)  |      Yes | Tenant/organization name |
| status     | ENUM          |      Yes | Tenant status            |
| created_at | TIMESTAMP     |      Yes | Creation time            |
| updated_at | TIMESTAMP     |      Yes | Last update              |
| deleted_at | TIMESTAMP     |       No | Soft delete              |

Tenant status:

```
ACTIVE
INACTIVE
SUSPENDED
```

Relationship:

```
users N ───── N tenants
```

This relationship should eventually be handled through a membership table.

Recommended future structure:

```
users
   │
   ▼
tenant_users
   │
   ▼
tenants
```

For the initial implementation, the registering user must become the tenant owner.

---

# 4. Plans

Plans are SaaS subscription products offered by the platform.

Default plans:

```
Starter
Professional
Enterprise
```

These are platform-level plans.

They are NOT school types.

For example:

```
School Type
├── Primary School
├── Secondary School
├── College
└── Language Center
```

is completely different from:

```
Subscription Plan
├── Starter
├── Professional
└── Enterprise
```

---

# 5. Plans Table

Table:

```
plans
```

Fields:

| Field         | Type          | Required | Description      |
| ------------- | ------------- | -------: | ---------------- |
| id            | UUID / BIGINT |      Yes | Primary key      |
| code          | VARCHAR(50)   |      Yes | Unique plan code |
| name          | VARCHAR(100)  |      Yes | Display name     |
| description   | TEXT          |       No | Plan description |
| price_monthly | DECIMAL       |      Yes | Monthly price    |
| price_yearly  | DECIMAL       |      Yes | Yearly price     |
| currency      | VARCHAR(10)   |      Yes | Currency         |
| max_students  | INT           |       No | Maximum students |
| max_teachers  | INT           |       No | Maximum teachers |
| max_branches  | INT           |       No | Maximum branches |
| status        | ENUM          |      Yes | Plan status      |
| sort_order    | INT           |      Yes | Display order    |
| created_at    | TIMESTAMP     |      Yes | Creation time    |
| updated_at    | TIMESTAMP     |      Yes | Last update      |

Plan status:

```
ACTIVE
INACTIVE
```

Example:

```json
{
  "id": "plan-uuid",
  "code": "professional",
  "name": "Professional",
  "description": "For growing schools",
  "priceMonthly": 49.00,
  "priceYearly": 490.00,
  "currency": "USD",
  "maxStudents": 1000,
  "maxTeachers": 100,
  "maxBranches": 5,
  "status": "ACTIVE"
}
```

Do not hard-code prices inside the frontend.

The backend must be the source of truth for:

* Plan name
* Price
* Limits
* Features
* Availability

---

# 6. Plan Features

Create a separate feature structure.

Table:

```
plan_features
```

Fields:

| Field        | Type          | Required |
| ------------ | ------------- | -------: |
| id           | UUID / BIGINT |      Yes |
| plan_id      | FK → plans.id |      Yes |
| feature_code | VARCHAR(100)  |      Yes |
| feature_name | VARCHAR(150)  |      Yes |
| enabled      | BOOLEAN       |      Yes |
| limit_value  | INT           |       No |
| created_at   | TIMESTAMP     |      Yes |
| updated_at   | TIMESTAMP     |      Yes |

Examples:

```
STUDENT_MANAGEMENT
TEACHER_MANAGEMENT
PARENT_MANAGEMENT
ATTENDANCE
EXAMS
GRADES
FINANCE
REPORTS
MULTIPLE_BRANCHES
CUSTOM_ROLES
NOTIFICATIONS
```

Example:

```json
{
  "featureCode": "STUDENT_MANAGEMENT",
  "featureName": "Student Management",
  "enabled": true
}
```

For limited features:

```json
{
  "featureCode": "MULTIPLE_BRANCHES",
  "featureName": "Multiple Branches",
  "enabled": true,
  "limitValue": 5
}
```

---

# 7. Get Available Plans

Endpoint:

```
GET /api/v1/plans
```

Authentication:

```
No
```

Purpose:

Display plans on the Choose Plan page.

Response:

```json
{
  "success": true,
  "data": [
    {
      "id": "starter-id",
      "code": "starter",
      "name": "Starter",
      "description": "For small schools",
      "priceMonthly": 19.00,
      "priceYearly": 190.00,
      "currency": "USD",
      "maxStudents": 300,
      "maxTeachers": 30,
      "maxBranches": 1,
      "features": [
        {
          "code": "STUDENT_MANAGEMENT",
          "name": "Student Management",
          "enabled": true
        },
        {
          "code": "ATTENDANCE",
          "name": "Attendance",
          "enabled": true
        }
      ]
    }
  ]
}
```

Only return:

```
ACTIVE
```

plans.

---

# 8. Get Plan Details

Endpoint:

```
GET /api/v1/plans/{planId}
```

Authentication:

```
No
```

Purpose:

Show complete information about one plan.

Example:

```
GET /api/v1/plans/professional
```

Response:

```json
{
  "success": true,
  "data": {
    "id": "professional-id",
    "code": "professional",
    "name": "Professional",
    "description": "For growing schools",
    "priceMonthly": 49.00,
    "priceYearly": 490.00,
    "currency": "USD",
    "maxStudents": 1000,
    "maxTeachers": 100,
    "maxBranches": 5,
    "features": []
  }
}
```

---

# 9. Subscription

A tenant can have a subscription.

Table:

```
subscriptions
```

Fields:

| Field         | Type            | Required | Description         |
| ------------- | --------------- | -------: | ------------------- |
| id            | UUID / BIGINT   |      Yes | Primary key         |
| tenant_id     | FK → tenants.id |      Yes | Tenant              |
| plan_id       | FK → plans.id   |      Yes | Selected plan       |
| status        | ENUM            |      Yes | Subscription status |
| billing_cycle | ENUM            |      Yes | Monthly/yearly      |
| start_at      | TIMESTAMP       |      Yes | Subscription start  |
| end_at        | TIMESTAMP       |       No | Subscription end    |
| trial_end_at  | TIMESTAMP       |       No | Trial end           |
| canceled_at   | TIMESTAMP       |       No | Cancellation time   |
| created_at    | TIMESTAMP       |      Yes | Creation time       |
| updated_at    | TIMESTAMP       |      Yes | Last update         |

Billing cycle:

```
MONTHLY
YEARLY
```

Subscription status:

```
TRIALING
ACTIVE
PAST_DUE
CANCELED
EXPIRED
```

Relationship:

```
tenants 1 ───── N subscriptions
```

However, normally only one subscription should be active at a time.

---

# 10. Select Plan

Endpoint:

```
POST /api/v1/subscriptions
```

Authentication:

```
Yes
```

Purpose:

Select a plan after registration.

Request:

```json
{
  "planId": "professional-id",
  "billingCycle": "MONTHLY"
}
```

Process:

```
Authenticated User
      │
      ▼
    Tenant
      │
      ▼
   Validate Plan
      │
      ▼
Create Subscription
      │
      ▼
  Choose Plan ✓
```

````

Response:

```json
{
  "success": true,
  "message": "Subscription created successfully.",
  "data": {
    "id": "subscription-id",
    "plan": {
      "id": "professional-id",
      "code": "professional",
      "name": "Professional"
    },
    "billingCycle": "MONTHLY",
    "status": "ACTIVE",
    "startAt": "2026-08-14T08:00:00Z"
  }
}
````

Do not allow the frontend to submit:

```
price
maxStudents
maxTeachers
maxBranches
```

Those values must come from the selected plan in the database.

---

# 11. Current Subscription

Endpoint:

```
GET /api/v1/subscriptions/current
```

Authentication:

```
Yes
```

Response:

```json
{
  "success": true,
  "data": {
    "id": "subscription-id",
    "status": "ACTIVE",
    "billingCycle": "MONTHLY",
    "startAt": "2026-08-14T08:00:00Z",
    "endAt": null,
    "plan": {
      "id": "professional-id",
      "code": "professional",
      "name": "Professional",
      "priceMonthly": 49.00,
      "priceYearly": 490.00,
      "currency": "USD",
      "maxStudents": 1000,
      "maxTeachers": 100,
      "maxBranches": 5
    }
  }
}
```

---

# 12. Subscription History

Endpoint:

```
GET /api/v1/subscriptions
```

Authentication:

```
Yes
```

Purpose:

Return the tenant's subscription history.

Response:

```json
{
  "success": true,
  "data": [
    {
      "id": "subscription-id",
      "plan": {
        "code": "starter",
        "name": "Starter"
      },
      "billingCycle": "MONTHLY",
      "status": "CANCELED",
      "startAt": "2026-01-01T00:00:00Z",
      "endAt": "2026-06-01T00:00:00Z"
    },
    {
      "id": "subscription-id-2",
      "plan": {
        "code": "professional",
        "name": "Professional"
      },
      "billingCycle": "MONTHLY",
      "status": "ACTIVE",
      "startAt": "2026-06-01T00:00:00Z",
      "endAt": null
    }
  ]
}
```

---

# 13. Change Plan

Endpoint:

```
PATCH /api/v1/subscriptions/current
```

Authentication:

```
Yes
```

Request:

```json
{
  "planId": "enterprise-id",
  "billingCycle": "YEARLY"
}
```

The backend must determine whether the change is:

```
Upgrade
Downgrade
```

Do not trust the frontend to determine this.

The backend must validate:

* Plan exists
* Plan is active
* Tenant is allowed to use the plan
* Existing subscription exists
* Billing cycle is valid

Response:

```json
{
  "success": true,
  "message": "Subscription updated successfully.",
  "data": {
    "id": "subscription-id",
    "plan": {
      "code": "enterprise",
      "name": "Enterprise"
    },
    "billingCycle": "YEARLY",
    "status": "ACTIVE"
  }
}
```

---

# 14. Cancel Subscription

Endpoint:

```
POST /api/v1/subscriptions/current/cancel
```

Authentication:

```
Yes
```

Request:

```json
{
  "reason": "No longer needed"
}
```

Response:

```json
{
  "success": true,
  "message": "Subscription cancellation scheduled successfully.",
  "data": {
    "status": "CANCELED",
    "canceledAt": "2026-08-14T08:00:00Z"
  }
}
```

Do not immediately delete the subscription.

Keep the subscription history.

---

# 15. Subscription Limits

The subscription determines what the tenant can use.

Example:

```
Professional
   │
   ├── maxStudents = 1000
   ├── maxTeachers = 100
   └── maxBranches = 5
```

When the tenant creates a student:

```
Current students = 1000
Plan limit = 1000
```

Result:

```
Reject creation
```

Example error:

```json
{
  "success": false,
  "message": "Student limit reached for your current subscription.",
  "errorCode": "STUDENT_LIMIT_REACHED"
}
```

Recommended limit error codes:

```
STUDENT_LIMIT_REACHED
TEACHER_LIMIT_REACHED
BRANCH_LIMIT_REACHED
FEATURE_NOT_AVAILABLE
SUBSCRIPTION_REQUIRED
SUBSCRIPTION_EXPIRED
```

The actual enforcement will be used by later modules.

---

# 16. Feature Access

Do not check plan names throughout the application.

Avoid:

```text
if plan == "professional"
```

Instead use feature/limit checks:

```text
hasFeature("ATTENDANCE")
hasFeature("CUSTOM_ROLES")
checkLimit("STUDENTS")
checkLimit("BRANCHES")
```

This allows plans to change without rewriting business logic.

---

# 17. Example Feature Access

Example:

```text
Tenant
  │
  ▼
Professional Plan
  │
  ├── STUDENT_MANAGEMENT ✓
  ├── TEACHER_MANAGEMENT ✓
  ├── ATTENDANCE ✓
  ├── EXAMS ✓
  ├── FINANCE ✓
  ├── CUSTOM_ROLES ✓
  └── MULTIPLE_BRANCHES ✓
              │
              └── limit: 5
```

---

# 18. Choose Plan UI Flow

The frontend should use:

```
GET /api/v1/plans
```

Then display:

```text
┌───────────────────────────────────────────────┐
│                 CHOOSE PLAN                   │
│                                               │
│  ┌────────────┐ ┌────────────────┐ ┌────────┐│
│  │  STARTER   │ │ PROFESSIONAL ⭐ │ │ENTERPRISE│
│  │            │ │                │ │        │
│  │ $19 / month│ │ $49 / month    │ │ Custom │
│  │            │ │                │ │        │
│  │ ✓ Students │ │ ✓ Students     │ │ ✓ All  │
│  │ ✓ Teachers │ │ ✓ Teachers     │ │        │
│  │ ✓ Basic    │ │ ✓ Attendance   │ │        │
│  │            │ │ ✓ Finance      │ │        │
│  │            │ │ ✓ Reports      │ │        │
│  │            │ │                │ │        │
│  │ [Choose]   │ │ [Choose]      │ │[Choose]│
│  └────────────┘ └────────────────┘ └────────┘
└───────────────────────────────────────────────┘
```

The UI must obtain the actual values from:

```
GET /api/v1/plans
```

Do not hard-code prices or features.

---

# 19. Billing Provider

Do not tightly couple the database to a specific payment provider yet.

The subscription module should be designed so a payment provider can be added later.

Recommended abstraction:

```
PaymentProvider
     │
     ├── createCheckout()
     ├── handleWebhook()
     ├── cancelSubscription()
     └── updateSubscription()
```

Possible future providers:

```
Stripe
ABA PayWay
KHQR
Other Cambodian payment providers
```

Do not implement a real payment gateway unless explicitly requested.

For the initial API implementation, plan selection can create an internal subscription record.

---

# 20. Payment vs Subscription

Keep these concepts separate.

Subscription:

```
What plan does the tenant have?
```

Payment:

```
How did the tenant pay?
```

Future structure:

```
subscriptions
     │
     ▼
  invoices
     │
     ▼
  payments
```

Do not put payment transaction fields directly into `subscriptions`.

---

# 21. Authorization

Only authorized tenant users can access:

```
GET /subscriptions/current
GET /subscriptions
POST /subscriptions
PATCH /subscriptions/current
POST /subscriptions/current/cancel
```

The user must belong to the tenant.

Subscription management should eventually be restricted to:

```
Owner
Admin
```

The detailed role/permission implementation belongs to:

```
API 04 — Users & Roles
```

For now, create the basic authorization hook/interface without implementing the complete permission tree.

---

# 22. Multi-Tenant Isolation

Every subscription query must be scoped to the authenticated tenant.

Never do:

```text
find subscription by ID only
```

Prefer:

```text
find subscription
WHERE id = ?
AND tenant_id = authenticatedTenantId
```

A user from Tenant A must never be able to access Tenant B's subscription.

---

# 23. API Summary

Base URL:

```
/api/v1
```

Public:

| Method | Endpoint        | Auth |
| ------ | --------------- | ---- |
| GET    | /plans          | No   |
| GET    | /plans/{planId} | No   |

Authenticated:

| Method | Endpoint                      | Auth |
| ------ | ----------------------------- | ---- |
| POST   | /subscriptions                | Yes  |
| GET    | /subscriptions/current        | Yes  |
| GET    | /subscriptions                | Yes  |
| PATCH  | /subscriptions/current        | Yes  |
| POST   | /subscriptions/current/cancel | Yes  |

---

# 24. Error Responses

Use the same response format established by API 01.

Example:

```json
{
  "success": false,
  "message": "Plan not found.",
  "errorCode": "PLAN_NOT_FOUND"
}
```

Recommended errors:

```
PLAN_NOT_FOUND
PLAN_INACTIVE
SUBSCRIPTION_NOT_FOUND
SUBSCRIPTION_ALREADY_EXISTS
SUBSCRIPTION_REQUIRED
SUBSCRIPTION_EXPIRED
FEATURE_NOT_AVAILABLE
STUDENT_LIMIT_REACHED
TEACHER_LIMIT_REACHED
BRANCH_LIMIT_REACHED
INVALID_BILLING_CYCLE
UNAUTHORIZED
FORBIDDEN
VALIDATION_ERROR
```

---

# 25. Database Relationships

```text
┌───────────────┐
│    TENANTS    │
│               │
│ id            │
│ name          │
│ status        │
└───────┬───────┘
        │
        │ 1
        │
        │ N
        ▼
┌──────────────────┐
│  SUBSCRIPTIONS   │
│                  │
│ id               │
│ tenant_id        │
│ plan_id          │
│ status            │
│ billing_cycle     │
└────────┬─────────┘
         │
         │ N
         │
         │ 1
         ▼
┌──────────────────┐
│      PLANS       │
│                  │
│ id               │
│ code             │
│ name             │
│ price_monthly    │
│ price_yearly     │
└────────┬─────────┘
         │
         │ 1
         │
         │ N
         ▼
┌──────────────────┐
│  PLAN_FEATURES   │
│                  │
│ id               │
│ plan_id          │
│ feature_code     │
│ enabled          │
│ limit_value      │
└──────────────────┘
```

---

# 26. Implementation Order

Implement this module in the following order.

## Step 1 — Tenant

Create:

```
tenants
```

If tenant creation is already implemented in API 01, reuse it.

## Step 2 — Plans

Create:

```
plans
```

## Step 3 — Plan Features

Create:

```
plan_features
```

## Step 4 — Seed Default Plans

Create:

```
Starter
Professional
Enterprise
```

Use database seed data.

Do not hard-code these plans in controllers/services.

## Step 5 — Subscription

Create:

```
subscriptions
```

## Step 6 — Get Plans

Implement:

```
GET /api/v1/plans
```

## Step 7 — Select Plan

Implement:

```
POST /api/v1/subscriptions
```

## Step 8 — Current Subscription

Implement:

```
GET /api/v1/subscriptions/current
```

## Step 9 — Subscription History

Implement:

```
GET /api/v1/subscriptions
```

## Step 10 — Change Plan

Implement:

```
PATCH /api/v1/subscriptions/current
```

## Step 11 — Cancel

Implement:

```
POST /api/v1/subscriptions/current/cancel
```

## Step 12 — Feature/Limit Service

Create a reusable service such as:

```
SubscriptionService
FeatureAccessService
SubscriptionLimitService
```

Later modules should be able to call:

```
hasFeature(...)
checkLimit(...)
```

---

# 27. Testing Requirements

## Plans

* Return active plans
* Do not return inactive plans
* Get plan details
* Invalid plan ID
* Inactive plan

## Subscription

* Select valid plan
* Invalid plan
* Create subscription
* Get current subscription
* Get subscription history
* Change plan
* Change billing cycle
* Cancel subscription

## Security

* Tenant A cannot access Tenant B subscription
* Unauthorized user cannot access subscription
* Non-owner/admin cannot modify subscription once roles are implemented

## Limits

* Student limit is enforced
* Teacher limit is enforced
* Branch limit is enforced
* Feature availability is enforced

---

# 28. What NOT to Implement Yet

Do not implement these in API 02:

```
School
Branch
Academic Year
Students
Teachers
Parents
Programs
Levels
Classes
Sections
Subjects
Rooms
Attendance
Exams
Grades
Fees
Payments
Reports
Notifications
Complete Roles & Permissions
Real Payment Gateway
```

These belong to later API modules.

---

# 29. Expected Result

After API 02 is complete:

```text
REGISTER
   │
   ▼
USER + TENANT
   │
   ▼
GET /api/v1/plans
   │
   ▼
CHOOSE PLAN
   │
   ▼
POST /api/v1/subscriptions
   │
   ▼
SUBSCRIPTION ACTIVE
   │
   ▼
API 03 — SCHOOL ONBOARDING
```

The backend must now know:

```
Who is the user?
Which tenant does the user belong to?
Which subscription does the tenant have?
Which plan is active?
What features are available?
What limits apply?
```

The next module after this is:

```
API 03 — School Onboarding
```

which will implement:

```
Create School
    ↓
Create Branch
    ↓
Create Academic Year
    ↓
Setup Complete
    ↓
Dashboard
```
