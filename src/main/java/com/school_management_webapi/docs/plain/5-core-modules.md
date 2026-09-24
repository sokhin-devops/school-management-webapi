# API 05 — Core Modules

## Purpose

The modules the web app runs on once a tenant is onboarded: Academic, People,
Finance, Attendance, Assessments, Access control and the Dashboard.

Written to match the shapes the web app already uses (`core/models` and the
sixteen record forms), so a screen and its endpoint agree on field names without
a translation layer in between.

    Register → Choose Plan → Onboarding → ┬── Academic   (programmes, levels, subjects, rooms, classes)
                                          ├── People     (students, teachers, parents)
                                          ├── Finance    (fees, payments, expenses)
                                          ├── Attendance (per record, and per register)
                                          ├── Assessments(assessments, mark sheets)
                                          ├── Settings   (roles, users)
                                          └── Dashboard  (counts and trends)

## Scope

Everything here hangs off a **branch**. The web app has a branch switcher and
every list in it is a list for one branch, so `branch_id` is a column on each of
these tables rather than something derived through the school.

`BranchScopeService` answers the same three questions for every one of them:
which tenant is calling, which branches may it see, and is this branch one of
them. A branch outside the caller's tenant is reported as **404**, not 403, so
ids cannot be probed across tenants.

## Endpoints

Each of the twelve record modules has the same six:

    GET    /api/v1/{module}            list, paged, with filters and search
    GET    /api/v1/{module}/{id}       one record
    POST   /api/v1/{module}            create
    PUT    /api/v1/{module}/{id}       full replacement
    PATCH  /api/v1/{module}/{id}       partial change
    DELETE /api/v1/{module}/{id}       archive (soft delete)

| Module | Path | Unique within a branch |
| --- | --- | --- |
| Programmes | `/programs` | `code` |
| Subjects | `/subjects` | `code` |
| Levels | `/levels` | — |
| Rooms | `/rooms` | `code` |
| Classes | `/classes` | `code` |
| Teachers | `/teachers` | `employeeNumber` |
| Parents | `/parents` | — |
| Fees | `/fees` | — |
| Payments | `/payments` | `reference` |
| Expenses | `/expenses` | — |
| Assessments | `/assessments` | — |
| Attendance | `/attendance` | — |

Beyond the six:

    GET  /api/v1/attendance/register              one class, one sitting, with tallies
    PUT  /api/v1/attendance/register              mark a whole class at once
    GET  /api/v1/assessments/{id}/scores          the mark sheet, with its average
    PUT  /api/v1/assessments/{id}/scores          replace the mark sheet
    GET  /api/v1/roles                            roles, seeding the five defaults on first use
    POST /api/v1/roles                            create a custom role
    PUT  /api/v1/roles/{id}                       replace a custom role
    DELETE /api/v1/roles/{id}                     delete a custom role nobody holds
    GET  /api/v1/users                            everyone with access to the tenant
    POST /api/v1/users                            invite someone
    PUT  /api/v1/users/{userId}                   change name, role, branches or status
    DELETE /api/v1/users/{userId}                 remove access, leaving the account
    GET  /api/v1/dashboard/summary                counts, today's attendance, money trend

## Deliberate decisions

- **Status is sent on create, not defaulted.** Every form in the web app offers
  a status, and a record saved as inactive must not come back active.

- **A register and a mark sheet are replaced, not merged.** Attendance is taken
  a class at a time and marking is done a sheet at a time; a student left out is
  a student with no mark, which is what removing a row from the sheet in front
  of you should mean. Merging would make that impossible to express.

- **`averageScore` and `graded` are stored on the assessment.** Every list of
  assessments shows them and a mark sheet is written far less often than it is
  read, so they are recomputed on save rather than counted on every read. An
  unmarked student is left out of the average rather than counted as zero, which
  would make a half-marked sheet look worse than it is.

- **A day with no register is absent from the trend, not zero percent.** A
  weekend nobody marked is not a day everybody missed.

- **Default roles are fixed** (64-users-and-roles.md), and the type is stored
  rather than inferred from the name — a renamed role would otherwise stop being
  protected. They are seeded the first time a tenant asks for its roles, which
  covers tenants created before this module existed without a migration.

- **A role still held by someone cannot be deleted**, and nobody can remove or
  deactivate their own access.

- **An invitation carries no password.** The account is created with a random
  one that is thrown away, and the person sets theirs through the existing
  forgot-password flow, so no credential is ever put in an email or in a
  response.

- **A teacher's subjects and a parent's children are element collections**, not
  join entities: they are lists owned by the record, replaced whole on save, and
  never navigated from the other end.

## Known gaps

- [ ] **Students are still keyed by school, not branch.** `branch_id` and
  `class_group_id` were added to `students` as nullable columns so the
  branch-scoped modules can join to them, but a student created before this has
  neither, and nothing yet backfills them. The dashboard and the register treat
  a student without a class as unplaced rather than as an error.

- [ ] **Permissions are not enforced.** `Role` stores the grid and
  `TenantUser.roleId` says who holds which, but no endpoint checks it yet —
  `TenantAuthorizationService` still only confirms tenant membership. The
  checking layer plugs in there.

- [ ] **Uniqueness of an attendance sitting is enforced in the service, not the
  database.** The natural key is five columns wide; the register endpoint
  replaces by that key, but a direct POST to `/attendance` can still create a
  second record for the same student and sitting.

- [ ] **No reports module.** 50-reports.md asks for student, attendance,
  academic and financial reports; the dashboard summary covers the landing page
  only.

- [ ] **`SchoolType` was realigned to the web app's list.** Rows written before
  that need a one-off migration, because `ddl-auto=update` never rewrites stored
  enum values or drops the check constraint built from the old ones:

      ALTER TABLE schools DROP CONSTRAINT IF EXISTS schools_type_check;
      UPDATE schools SET type = type || '_SCHOOL' WHERE type IN ('PRIMARY', 'SECONDARY');
      ALTER TABLE schools ADD CONSTRAINT schools_type_check CHECK (type IN
        ('PRIMARY_SCHOOL','SECONDARY_SCHOOL','HIGH_SCHOOL','COLLEGE','UNIVERSITY',
         'LANGUAGE_CENTER','TRAINING_CENTER','VOCATIONAL_SCHOOL','OTHER'));

- [ ] **Registering and logging in within the same second returns 409.** The JWT
  `iat` and `exp` claims have one-second resolution, so a login that lands in the
  same second as the register that preceded it mints a byte-identical refresh
  token and trips the unique index on its hash. Pre-existing, in the auth module;
  a jti claim or a unique salt per token would settle it.
