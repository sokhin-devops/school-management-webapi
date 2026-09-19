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
---

# 4. Data Model

```
tenants
   │
   ├── subscriptions
   │
   └── schools
         │
         ├── branches
         │
         └── academic_years
```

## schools

| Column     | Type      | Nullable | Notes                                          |
| ---------- | --------- | -------- | ---------------------------------------------- |
| id         | UUID      | No       |                                                |
| tenant_id  | UUID      | No       | FK tenants                                     |
| name       | VARCHAR   | No       | Max 150. Unique per tenant, case-insensitive   |
| type       | VARCHAR   | No       | SchoolType enum                                |
| email      | VARCHAR   | No       | Max 255                                        |
| phone      | VARCHAR   | No       | Max 30                                         |
| address    | TEXT      | No       |                                                |
| status     | VARCHAR   | No       | ACTIVE / INACTIVE                              |
| created_at | TIMESTAMP | No       |                                                |
| updated_at | TIMESTAMP | No       |                                                |
| deleted_at | TIMESTAMP | Yes      | Soft delete marker                             |

`SchoolType`:

```
PRIMARY
SECONDARY
COLLEGE
LANGUAGE_CENTER
OTHER
```

## branches

| Column        | Type      | Nullable | Notes                                        |
| ------------- | --------- | -------- | -------------------------------------------- |
| id            | UUID      | No       |                                              |
| school_id     | UUID      | No       | FK schools                                   |
| name          | VARCHAR   | No       | Max 150. Unique per school, case-insensitive |
| address       | TEXT      | No       |                                              |
| phone         | VARCHAR   | Yes      | Max 30                                       |
| is_main_branch| BOOLEAN   | No       | Exactly one per school                       |
| status        | VARCHAR   | No       | ACTIVE / INACTIVE                            |
| created_at    | TIMESTAMP | No       |                                              |
| updated_at    | TIMESTAMP | No       |                                              |
| deleted_at    | TIMESTAMP | Yes      | Soft delete marker                           |

## academic_years

| Column     | Type      | Nullable | Notes                                        |
| ---------- | --------- | -------- | -------------------------------------------- |
| id         | UUID      | No       |                                              |
| school_id  | UUID      | No       | FK schools                                   |
| name       | VARCHAR   | No       | Max 100. Unique per school, case-insensitive |
| start_date | DATE      | No       |                                              |
| end_date   | DATE      | No       | Must be after start_date                     |
| is_current | BOOLEAN   | No       | Exactly one per school                       |
| created_at | TIMESTAMP | No       |                                              |
| updated_at | TIMESTAMP | No       |                                              |
| deleted_at | TIMESTAMP | Yes      | Soft delete marker                           |

Date ranges of the academic years of one school must never overlap.

---

# 5. Onboarding Status

Endpoint:

```
GET /api/v1/onboarding/status
```

Authentication:

```
Yes
```

The frontend calls this on entry to the setup wizard and after every step, so a
half-finished setup resumes on the right step instead of starting over.

Response:

```json
{
  "success": true,
  "message": "Onboarding status retrieved",
  "data": {
    "subscriptionCompleted": true,
    "schoolCompleted": true,
    "branchCompleted": false,
    "academicYearCompleted": false,
    "completed": false,
    "currentStep": "BRANCH",
    "school": { "id": "school-id", "name": "Sunrise Academy" },
    "branch": null,
    "academicYear": null
  }
}
```

`currentStep`:

```
SUBSCRIPTION     no live subscription - send the user back to Choose Plan
SCHOOL           step 01 not answered
BRANCH           step 02 not answered
ACADEMIC_YEAR    step 03 not answered
COMPLETE         setup done - go to the dashboard
```

`completed` is true only when the subscription and all three steps are done.

---

# 6. Onboarding Steps

The three steps are **idempotent upserts**, not plain inserts.

The wizard has a Back button, so a step is routinely submitted more than once.
Re-submitting a step revises the answer that was already stored instead of
creating a second school / branch / academic year.

```
first submission   -> 201 Created
later submission   -> 200 OK  (same id, revised values)
```

Each step targets the first school of the tenant, and the first branch and first
academic year of that school.

## 6.1 Step 01 - Create School

```
POST /api/v1/onboarding/school
```

Request:

```json
{
  "name": "Sunrise Academy",
  "type": "PRIMARY",
  "email": "hello@sunrise.test",
  "phone": "099111222",
  "address": "12 Main Street"
}
```

Response (201):

```json
{
  "success": true,
  "message": "School setup completed",
  "data": {
    "id": "school-id",
    "tenantId": "tenant-id",
    "name": "Sunrise Academy",
    "type": "PRIMARY",
    "email": "hello@sunrise.test",
    "phone": "099111222",
    "address": "12 Main Street",
    "status": "ACTIVE",
    "createdAt": "2026-08-26T09:00:00",
    "updatedAt": "2026-08-26T09:00:00"
  }
}
```

Requires a live subscription. Without one:

```json
{
  "success": false,
  "message": "Tenant has no active subscription",
  "errorCode": "SUBSCRIPTION_REQUIRED"
}
```

## 6.2 Step 02 - Create Branch

```
POST /api/v1/onboarding/branch
```

Request:

```json
{
  "name": "Main Campus",
  "address": "12 Main Street",
  "phone": "099333444"
}
```

The branch created here is always the main branch of the school.

`schoolId` is never accepted from the frontend - the backend resolves the school
of the tenant.

Fails with `SCHOOL_REQUIRED` (409) when step 01 has not been answered, and with
`BRANCH_LIMIT_REACHED` (403) when the plan allowance is used up.

## 6.3 Step 03 - Create Academic Year

```
POST /api/v1/onboarding/academic-year
```

Request:

```json
{
  "name": "2026-2027",
  "startDate": "2026-09-01",
  "endDate": "2027-06-30"
}
```

The academic year created here is always the current one.

Fails with `SCHOOL_REQUIRED` or `BRANCH_REQUIRED` (409) when an earlier step has
not been answered.

On success the setup is complete and the frontend moves to the dashboard.

---

# 7. School Management

Used by Settings after onboarding, and to add further schools on plans that
allow it.

| Method | Endpoint                 | Purpose               |
| ------ | ------------------------ | --------------------- |
| POST   | /api/v1/schools          | Create a school       |
| GET    | /api/v1/schools          | List schools          |
| GET    | /api/v1/schools/{id}     | Get one school        |
| PUT    | /api/v1/schools/{id}     | Update a school       |
| DELETE | /api/v1/schools/{id}     | Archive a school      |

`POST` requires a live subscription. `DELETE` is a soft delete and cascades the
soft delete to the branches and academic years of the school - otherwise those
rows stay hidden but keep consuming the branch allowance of the plan.

The list is always scoped to the tenant of the authenticated user.

---

# 8. Branch Management

| Method | Endpoint                                | Purpose            |
| ------ | --------------------------------------- | ------------------ |
| POST   | /api/v1/branches                        | Create a branch    |
| GET    | /api/v1/branches?schoolId={id}          | List branches      |
| GET    | /api/v1/branches/{id}                   | Get one branch     |
| PUT    | /api/v1/branches/{id}                   | Update a branch    |
| DELETE | /api/v1/branches/{id}                   | Archive a branch   |

`schoolId` on the list endpoint is optional - omit it to list every branch of the
tenant.

Main branch rules:

```
The first branch of a school is always the main branch.
Marking a branch as main demotes the previous main branch.
The main flag cannot simply be cleared - promote another branch instead
  (MAIN_BRANCH_REQUIRED).
Deleting the main branch promotes the oldest remaining branch.
```

`POST` enforces the `maxBranches` allowance of the plan, counted across every
school of the tenant.

---

# 9. Academic Year Management

| Method | Endpoint                                     | Purpose                 |
| ------ | -------------------------------------------- | ----------------------- |
| POST   | /api/v1/academic-years                       | Create an academic year |
| GET    | /api/v1/academic-years?schoolId={id}         | List academic years     |
| GET    | /api/v1/academic-years/{id}                  | Get one academic year   |
| PUT    | /api/v1/academic-years/{id}                  | Update an academic year |
| DELETE | /api/v1/academic-years/{id}                  | Archive an academic year|

Current year rules:

```
The first academic year of a school is always the current one.
Marking a year as current clears the flag on the previous current year.
The current flag cannot simply be cleared - promote another year instead
  (CURRENT_ACADEMIC_YEAR_REQUIRED).
Deleting the current year promotes the most recent remaining year.
```

Date rules:

```
endDate must be after startDate           -> INVALID_DATE_RANGE (400)
Ranges must not overlap within a school   -> ACADEMIC_YEAR_OVERLAP (409)
```

---

# 10. Validation

| Field                    | Rule                                    |
| ------------------------ | --------------------------------------- |
| school.name              | Required, max 150, unique per tenant    |
| school.type              | Required, valid SchoolType              |
| school.email             | Required, valid email, max 255          |
| school.phone             | Required, max 30                        |
| school.address           | Required, max 1000                      |
| branch.name              | Required, max 150, unique per school    |
| branch.address           | Required, max 1000                      |
| branch.phone             | Optional, max 30                        |
| academicYear.name        | Required, max 100, unique per school    |
| academicYear.startDate   | Required, ISO date                      |
| academicYear.endDate     | Required, ISO date, after startDate     |

Name comparisons are case-insensitive and values are trimmed before storing.

Field errors are returned as:

```json
{
  "success": false,
  "status": 400,
  "message": "Validation failed",
  "fieldErrors": { "name": "name is required" },
  "errorCode": "VALIDATION_FAILED"
}
```

---

# 11. Error Codes

| Code                            | Status | Meaning                                     |
| ------------------------------- | ------ | ------------------------------------------- |
| UNAUTHORIZED                    | 401    | Missing or invalid access token              |
| FORBIDDEN                       | 403    | User does not belong to a tenant             |
| SUBSCRIPTION_REQUIRED           | 403    | No live subscription                         |
| BRANCH_LIMIT_REACHED            | 403    | Plan branch allowance used up                |
| SCHOOL_NOT_FOUND                | 404    | School missing, or outside the tenant        |
| BRANCH_NOT_FOUND                | 404    | Branch missing, or outside the tenant        |
| ACADEMIC_YEAR_NOT_FOUND         | 404    | Academic year missing, or outside the tenant |
| SCHOOL_REQUIRED                 | 409    | Step 01 not answered yet                     |
| BRANCH_REQUIRED                 | 409    | Step 02 not answered yet                     |
| SCHOOL_NAME_ALREADY_EXISTS      | 409    | Duplicate school name in the tenant          |
| BRANCH_NAME_ALREADY_EXISTS      | 409    | Duplicate branch name in the school          |
| ACADEMIC_YEAR_NAME_ALREADY_EXISTS | 409  | Duplicate year name in the school            |
| ACADEMIC_YEAR_OVERLAP           | 409    | Overlapping academic year date range         |
| MAIN_BRANCH_REQUIRED            | 409    | Would leave the school without a main branch  |
| CURRENT_ACADEMIC_YEAR_REQUIRED  | 409    | Would leave the school without a current year |
| INVALID_DATE_RANGE              | 400    | endDate is not after startDate               |
| VALIDATION_FAILED               | 400    | Bean validation rejected the payload         |
| MALFORMED_REQUEST               | 400    | Unparsable body, bad enum or bad date        |
| INVALID_PARAMETER               | 400    | Path or query value of the wrong type        |
| MISSING_PARAMETER               | 400    | Required query parameter absent              |
| DATA_INTEGRITY_VIOLATION        | 409    | Database constraint rejected the write       |
| INTERNAL_ERROR                  | 500    | Unexpected failure - details are logged only |

Every error uses the same envelope:

```json
{
  "success": false,
  "timestamp": "2026-08-26T09:00:00",
  "status": 409,
  "error": "Conflict",
  "message": "Human readable message",
  "path": "/api/v1/onboarding/branch",
  "fieldErrors": null,
  "errorCode": "SCHOOL_REQUIRED"
}
```

---

# 12. Multi-Tenancy Rules

The tenant always comes from the authenticated user:

```
JWT -> userId -> tenant_users -> tenantId
```

Rules:

```
Never accept tenantId from the request body.
Never accept schoolId on an onboarding step - resolve it from the tenant.
Every read and write is filtered by the tenant of the caller.
A record of another tenant is reported as NOT FOUND, never as FORBIDDEN,
  so ids cannot be probed across tenants.
```

A user with no tenant membership gets `FORBIDDEN`, not an empty result.

---

# 13. Subscription Limits

Onboarding runs after plan selection, so the tenant must be on a live
subscription (`TRIALING`, `ACTIVE` or `PAST_DUE`) before a school, branch or
academic year can be created.

Allowances are checked through `SubscriptionLimitService`, never by plan name:

```
checkLimit(tenantId, BRANCHES,  current branch count of the tenant)
checkLimit(tenantId, STUDENTS,  current student count of the tenant)
```

Branch and student counts are tenant-wide, matching how `maxBranches` and
`maxStudents` are defined on the plan.

---

# 14. Testing Requirements

## Onboarding

* Status reports `SUBSCRIPTION` before a plan is chosen
* Status reports the next unanswered step
* Status reports `COMPLETE` once all three steps are answered
* Step 01 is rejected without a live subscription
* Step 01 creates on first submission and updates on re-submission
* Step 02 fails before step 01
* Step 02 marks the first branch as main
* Step 03 fails before step 02
* Step 03 marks the first academic year as current

## School / Branch / Academic Year

* Duplicate names are rejected per tenant / per school
* `endDate` must be after `startDate`
* Overlapping academic year ranges are rejected
* The last main branch cannot be demoted
* The last current academic year cannot be cleared
* Deleting the main branch promotes another branch
* Deleting the current academic year promotes another year
* Deleting a school cascades the soft delete to its children

## Security

* Unauthenticated requests get 401, not 403
* Tenant A cannot read, update or delete a school, branch or academic year of
  tenant B
* Tenant A cannot read or write a student of tenant B

## Limits

* Branch limit is enforced
* Student limit is enforced

---

# 15. Migration Note

`students.student_code` was originally globally unique, while the service treats
it as unique per school. It is now mapped as a composite unique constraint
`uk_students_school_id_student_code`.

`spring.jpa.hibernate.ddl-auto=update` adds the new constraint but never removes
the old one, so an existing database still rejects the same student code in two
different schools. The legacy constraint carries a generated name (for example
`ukcgcf3r5xk73o0etbduc1qxnol`), so look it up before dropping it:

```sql
SELECT conname, pg_get_constraintdef(oid)
FROM   pg_constraint
WHERE  conrelid = 'students'::regclass AND contype = 'u';
```

Drop the row whose definition is `UNIQUE (student_code)` and keep
`uk_students_school_id_student_code`:

```sql
ALTER TABLE students DROP CONSTRAINT <generated_name>;
```

This has already been applied to the local development database.

---

# 16. What NOT to Implement Yet

```
Programs
Levels
Classes
Sections
Subjects
Rooms
Teachers
Parents
Attendance
Exams
Grades
Fees
Payments
Reports
Notifications
Complete Roles & Permissions
```

Roles and permissions land in API 04 - `TenantAuthorizationService` is the hook
those checks plug into.

---

# 17. Expected Result

After API 03 is complete:

```text
REGISTER
   │
   ▼
CHOOSE PLAN
   │
   ▼
GET /api/v1/onboarding/status   -> currentStep = SCHOOL
   │
   ▼
POST /api/v1/onboarding/school
   │
   ▼
POST /api/v1/onboarding/branch
   │
   ▼
POST /api/v1/onboarding/academic-year
   │
   ▼
GET /api/v1/onboarding/status   -> completed = true
   │
   ▼
DASHBOARD
```

The backend now knows:

```
Which school does the tenant run?
Which branches does that school have, and which one is the main branch?
Which academic year is current?
```

The next module after this is:

```
API 04 - Users & Roles
```
