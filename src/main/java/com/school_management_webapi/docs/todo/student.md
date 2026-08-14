# Student Module - Implementation TODO

Source spec: [student.md](../plain/student.md)

This tracks what's implemented vs. what's left for the full Student module
described in the spec. Phase 1 (core profile CRUD) is done; everything else
depends on modules that don't exist yet in this codebase (School, Branch,
AcademicYear, Program, Level, Class, Section, User/Auth, File storage,
Attendance, Assessments, Fees).

## Phase 1 - Student core profile (DONE)

- [x] `Student` entity (`entity/Student.java`) with `Gender` / `StudentStatus` enums
- [x] Soft delete via `deleted_at` (Hibernate `@SQLDelete` + `@SQLRestriction`)
- [x] `StudentCreateRequest` / `StudentUpdateRequest` / `StudentPatchRequest` (`dto/request`)
- [x] `StudentResponse`, generic `PagedResponse<T>`, `ErrorResponse` (`dto/response`)
- [x] `StudentMapper` (manual - no MapStruct dependency in `build.gradle` yet)
- [x] `StudentRepository` + `StudentSpecification` for dynamic search/filter
- [x] `StudentService` / `StudentServiceImpl` (duplicate `studentCode` per school guarded)
- [x] `StudentController`:
  - `GET /api/v1/students` (search + filters: `schoolId`, `status`, `gender`, pagination)
  - `GET /api/v1/students/{id}`
  - `POST /api/v1/students`
  - `PUT /api/v1/students/{id}`
  - `PATCH /api/v1/students/{id}`
  - `DELETE /api/v1/students/{id}` (soft delete)
- [x] `GlobalExceptionHandler` (404 / 409 / 400 validation errors)

### Notes / decisions made

- `id` is `UUID` (Hibernate `@UuidGenerator`), not `BIGINT` - spec allowed either.
- `schoolId` is stored as a plain `UUID` column with no `@ManyToOne`, since a
  `School` entity doesn't exist yet. Revisit once the School module lands -
  should become a real FK relation.
- Filtering by `academicYear`, `program`, `level`, `class`, `section` (spec
  section 21) is **not** implemented yet - those live on `student_enrollments`,
  which doesn't exist. Filtering will need a join once Phase 5 lands.
- No MapStruct in `build.gradle`, so `StudentMapper` is a plain static-method
  class. Switch to MapStruct if mapping complexity grows across modules.

## Phase 2 - Student Addresses

- [ ] `student_addresses` table / `StudentAddress` entity (`address_type` enum: HOME, MAILING, OTHER)
- [ ] Request/response DTOs, mapper
- [ ] `GET/POST/PUT/DELETE /api/v1/students/{id}/addresses`
- [ ] Enforce single `is_primary` address per student

## Phase 3 - Parents / Guardians

- [ ] `Parent` entity (`parents` table) - has own `status`, optional `user_id`
- [ ] `StudentParent` join entity (`student_parents`) with `relationship` enum
      (FATHER, MOTHER, GUARDIAN, STEP_FATHER, STEP_MOTHER, OTHER), `is_primary`, `is_emergency`
- [ ] Parent CRUD (`/api/v1/parents`)
- [ ] `GET/POST /api/v1/students/{id}/parents`
- [ ] Support one parent linked to multiple students

## Phase 4 - Emergency Contacts

- [ ] `student_emergency_contacts` table / entity
- [ ] `GET/POST/PUT/DELETE /api/v1/students/{id}/emergency-contacts`

## Phase 5 - Enrollment (blocks academic filtering)

- [ ] Depends on: `School`, `Branch`, `AcademicYear`, `Program`, `Level`, `Class`, `Section` entities
- [ ] `student_enrollments` table / entity, `enrollment status` enum
      (ACTIVE, COMPLETED, TRANSFERRED, WITHDRAWN, CANCELLED)
- [ ] Never overwrite old enrollment rows - always insert a new one on promotion/transfer
- [ ] `GET/POST /api/v1/students/{id}/enrollments`
- [ ] Once done: add `academicYearId` / `programId` / `levelId` / `classId` / `sectionId`
      filters to `GET /api/v1/students` (join via current enrollment)

## Phase 6 - Student Subjects (optional, MVP-skippable per spec)

- [ ] `student_subjects` table - only needed if a school requires individual
      subject selection instead of inheriting subjects from class

## Phase 7 - Student Documents

- [ ] Depends on: file storage module (`file_id` FK)
- [ ] `student_documents` table / entity, `document_type` enum
      (BIRTH_CERTIFICATE, NATIONAL_ID, PREVIOUS_SCHOOL_CERTIFICATE,
      TRANSFER_CERTIFICATE, PASSPORT, PHOTO, OTHER)
- [ ] `GET/POST /api/v1/students/{id}/documents`

## Phase 8 - Student Medical Profile

- [ ] `student_medical_profiles` table / entity (1:1 with student)
- [ ] **Strict permission control** required per spec - restrict to specific roles
      once auth/roles exist

## Phase 9 - Student Login (optional portal access)

- [ ] Depends on: User/Auth module
- [ ] Link `students.user_id` (nullable) -> `users`
- [ ] Provisioning endpoint to create a user account + assign a Student role

## Phase 10 - Attendance

- [ ] Depends on: `attendance_sessions` entity
- [ ] `attendance_records` entity, status enum (PRESENT, ABSENT, LATE, EXCUSED)
- [ ] `GET /api/v1/students/{id}/attendance`

## Phase 11 - Exams / Results

- [ ] Depends on: `assessments` entity
- [ ] `assessment_results` entity
- [ ] `GET /api/v1/students/{id}/results`

## Phase 12 - Fees

- [ ] Depends on: `fee_structure`, `AcademicYear`
- [ ] `student_fees` entity, status enum (PENDING, PARTIAL, PAID, OVERDUE, CANCELLED)
- [ ] `GET /api/v1/students/{id}/fees`

## Phase 13 - Payments

- [ ] Depends on: Phase 12, `users` (received_by)
- [ ] `payments` entity, `payment_method` enum (CASH, BANK, CARD, ONLINE, OTHER)
- [ ] `GET /api/v1/students/{id}/payments`

## Testing (not yet started for Phase 1)

- [ ] Unit tests for `StudentServiceImpl` (create/update/patch duplicate-code checks, not-found)
- [ ] `@DataJpaTest` for `StudentRepository` / `StudentSpecification` filtering
- [ ] `@WebMvcTest` / `MockMvc` tests for `StudentController` (validation errors, 404/409 mapping)
- [ ] Integration test covering soft delete (`DELETE` then confirm excluded from `GET` list/detail)

## Frontend (not started)

- [ ] Student list page (table + card view per spec section 22)
- [ ] Student detail page tabs (Overview, Academic, Attendance, Exams & Grades,
      Fees & Payments, Parents, Documents) - most tabs blocked on the phases above
