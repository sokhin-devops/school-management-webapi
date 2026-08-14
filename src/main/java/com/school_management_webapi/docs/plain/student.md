# Student Module

## 1. Overview

The Student module manages the complete student/learner lifecycle.

It covers:

- Student profile
- Contact information
- Addresses
- Parents / guardians
- Emergency contacts
- Enrollment
- Academic history
- Subjects
- Attendance
- Exams / assessments
- Fees / payments
- Documents
- Optional student login
- Student status

The system must support different school types:

- Primary School
- Secondary School
- High School
- College
- University
- Language Center
- Training Center
- Vocational School

Do not hard-code the Student module around one school type.

For example:

Primary School:

    Student
      └── Grade 5
            └── Class 5-A

University:

    Student
      └── Computer Science
            └── Year 2
                  └── Section A

Language Center:

    Student / Learner
      └── English Program
            └── Intermediate
                  └── Batch 03


# 2. Student Entity

Table:

    students

Fields:

| Field | Type | Required | Description |
|---|---|---:|---|
| id | UUID / BIGINT | Yes | Primary key |
| school_id | FK | Yes | School owner |
| student_code | VARCHAR | Yes | Unique student identifier |
| first_name | VARCHAR | Yes | First name |
| middle_name | VARCHAR | No | Middle name |
| last_name | VARCHAR | Yes | Last name |
| preferred_name | VARCHAR | No | Preferred/display name |
| gender | ENUM | Yes | Gender |
| date_of_birth | DATE | Yes | Date of birth |
| nationality | VARCHAR | No | Nationality |
| national_id | VARCHAR | No | Government ID |
| photo_url | VARCHAR | No | Profile photo |
| email | VARCHAR | No | Student email |
| phone | VARCHAR | No | Student phone |
| admission_date | DATE | Yes | Date admitted |
| status | ENUM | Yes | Current student status |
| created_at | TIMESTAMP | Yes | Creation time |
| updated_at | TIMESTAMP | Yes | Last update |
| deleted_at | TIMESTAMP | No | Soft delete |

## Student Status

    ACTIVE
    INACTIVE
    GRADUATED
    TRANSFERRED
    SUSPENDED
    WITHDRAWN


# 3. Student Code

`student_code` is the unique identifier used by the school.

Examples:

    STU-000001
    STU-000002
    2026-0001

The exact format should be configurable per school.

Do not use the student's name as the unique identifier.


# 4. Student Name

Use separate fields:

    first_name
    middle_name
    last_name
    preferred_name

Do not store only:

    full_name

because separate names are useful for:

- Searching
- Sorting
- Certificates
- Reports
- Official documents


# 5. Student Address

Do not put every address field inside `students`.

Use:

    student_addresses

Fields:

| Field | Type | Required | Description |
|---|---|---:|---|
| id | PK | Yes | Primary key |
| student_id | FK | Yes | Student |
| address_type | ENUM | Yes | Address type |
| address_line1 | VARCHAR | Yes | Main address |
| address_line2 | VARCHAR | No | Additional address |
| city | VARCHAR | No | City |
| province | VARCHAR | No | Province |
| country | VARCHAR | Yes | Country |
| postal_code | VARCHAR | No | Postal code |
| is_primary | BOOLEAN | Yes | Primary address |
| created_at | TIMESTAMP | Yes | Creation |
| updated_at | TIMESTAMP | Yes | Update |

Address types:

    HOME
    MAILING
    OTHER

Relationship:

    students 1 ───── N student_addresses


# 6. Parents / Guardians

Do not store:

    father_name
    mother_name
    guardian_name

inside `students`.

A student can have multiple parents/guardians.

A parent can have multiple students.

Therefore:

    students
         │
         │ M:N
         ▼
    student_parents
         │
         ▼
    parents


# 7. Parent Entity

Table:

    parents

Fields:

| Field | Type | Required | Description |
|---|---|---:|---|
| id | PK | Yes | Primary key |
| school_id | FK | Yes | School |
| user_id | FK | No | Optional login account |
| first_name | VARCHAR | Yes | First name |
| middle_name | VARCHAR | No | Middle name |
| last_name | VARCHAR | Yes | Last name |
| email | VARCHAR | No | Email |
| phone | VARCHAR | Yes | Phone |
| occupation | VARCHAR | No | Occupation |
| photo_url | VARCHAR | No | Photo |
| status | ENUM | Yes | Status |
| created_at | TIMESTAMP | Yes | Creation |
| updated_at | TIMESTAMP | Yes | Update |


# 8. Student Parent Relationship

Table:

    student_parents

Fields:

| Field | Type | Required | Description |
|---|---|---:|---|
| id | PK | Yes | Primary key |
| student_id | FK | Yes | Student |
| parent_id | FK | Yes | Parent |
| relationship | ENUM | Yes | Relationship |
| is_primary | BOOLEAN | Yes | Primary guardian |
| is_emergency | BOOLEAN | Yes | Emergency contact |
| created_at | TIMESTAMP | Yes | Creation |

Relationship types:

    FATHER
    MOTHER
    GUARDIAN
    STEP_FATHER
    STEP_MOTHER
    OTHER

Example:

    Student A
    ├── Father
    ├── Mother
    └── Guardian

One parent can also have:

    Parent A
    ├── Student A
    ├── Student B
    └── Student C


# 9. Emergency Contacts

Emergency contacts should be separate from parents.

Table:

    student_emergency_contacts

Fields:

| Field | Type | Required | Description |
|---|---|---:|---|
| id | PK | Yes | Primary key |
| student_id | FK | Yes | Student |
| name | VARCHAR | Yes | Contact name |
| relationship | VARCHAR | Yes | Relationship |
| phone | VARCHAR | Yes | Primary phone |
| alternate_phone | VARCHAR | No | Secondary phone |
| is_primary | BOOLEAN | Yes | Primary emergency contact |
| created_at | TIMESTAMP | Yes | Creation |
| updated_at | TIMESTAMP | Yes | Update |


# 10. Enrollment

Do NOT put these directly into `students`:

    class_id
    section_id
    level_id
    academic_year_id

Students change academic placement over time.

Use:

    student_enrollments

Fields:

| Field | Type | Required | Description |
|---|---|---:|---|
| id | PK | Yes | Primary key |
| student_id | FK | Yes | Student |
| school_id | FK | Yes | School |
| branch_id | FK | Yes | Branch |
| academic_year_id | FK | Yes | Academic year |
| program_id | FK | No | Program |
| level_id | FK | No | Level |
| class_id | FK | No | Class |
| section_id | FK | No | Section |
| enrollment_number | VARCHAR | No | Enrollment number |
| enrollment_date | DATE | Yes | Enrollment date |
| status | ENUM | Yes | Enrollment status |
| created_at | TIMESTAMP | Yes | Creation |
| updated_at | TIMESTAMP | Yes | Update |

Enrollment status:

    ACTIVE
    COMPLETED
    TRANSFERRED
    WITHDRAWN
    CANCELLED


# 11. Academic History

The enrollment table provides historical academic placement.

Example:

    Student: Sokhin

    2024-2025
        Primary
        Grade 3
        Class 3-A

    2025-2026
        Primary
        Grade 4
        Class 4-B

    2026-2027
        Primary
        Grade 5
        Class 5-A

Never overwrite old enrollment records.


# 12. Student Subjects

For normal schools, subjects can be inherited from the student's class.

However, some institutions need individual subject enrollment.

Optional table:

    student_subjects

Fields:

| Field | Type | Required | Description |
|---|---|---:|---|
| id | PK | Yes | Primary key |
| student_id | FK | Yes | Student |
| subject_id | FK | Yes | Subject |
| enrollment_id | FK | Yes | Enrollment |
| status | ENUM | Yes | Status |
| created_at | TIMESTAMP | Yes | Creation |
| updated_at | TIMESTAMP | Yes | Update |

Do not require this table for the MVP unless individual subject selection is needed.


# 13. Student Documents

Table:

    student_documents

Fields:

| Field | Type | Required | Description |
|---|---|---:|---|
| id | PK | Yes | Primary key |
| student_id | FK | Yes | Student |
| document_type | ENUM | Yes | Document type |
| document_number | VARCHAR | No | Document number |
| file_id | FK | Yes | Stored file |
| issue_date | DATE | No | Issue date |
| expiry_date | DATE | No | Expiry |
| status | ENUM | Yes | Status |
| created_at | TIMESTAMP | Yes | Creation |
| updated_at | TIMESTAMP | Yes | Update |

Document types:

    BIRTH_CERTIFICATE
    NATIONAL_ID
    PREVIOUS_SCHOOL_CERTIFICATE
    TRANSFER_CERTIFICATE
    PASSPORT
    PHOTO
    OTHER


# 14. Optional Student Medical Profile

Keep medical information separate.

Table:

    student_medical_profiles

Fields:

| Field | Type | Required | Description |
|---|---|---:|---|
| id | PK | Yes | Primary key |
| student_id | FK | Yes | Student |
| blood_group | VARCHAR | No | Blood group |
| allergies | TEXT | No | Allergies |
| medical_conditions | TEXT | No | Conditions |
| medications | TEXT | No | Medications |
| special_notes | TEXT | No | Notes |
| created_at | TIMESTAMP | Yes | Creation |
| updated_at | TIMESTAMP | Yes | Update |

This module should have strict permission control.


# 15. Student Login

Students can optionally have a system account.

Relationship:

    students
        │
        │ user_id
        ▼
    users

`user_id` can be NULL.

Example:

    Student
        user_id = NULL

means the student does not have a login.

If a student receives portal access:

    Student
        │
        ▼
    User Account
        │
        ▼
    Student Role


# 16. Student Attendance

Students should NOT have attendance fields inside `students`.

Use:

    attendance_sessions
             │
             ▼
    attendance_records
             │
             ▼
         students

`attendance_records`:

| Field | Type | Required |
|---|---|---:|
| id | PK | Yes |
| attendance_session_id | FK | Yes |
| student_id | FK | Yes |
| status | ENUM | Yes |
| check_in_time | TIMESTAMP | No |
| check_out_time | TIMESTAMP | No |
| remarks | TEXT | No |
| created_at | TIMESTAMP | Yes |
| updated_at | TIMESTAMP | Yes |

Attendance status:

    PRESENT
    ABSENT
    LATE
    EXCUSED


# 17. Student Exams / Results

Students receive results through:

    assessments
          │
          ▼
    assessment_results
          │
          ▼
       students

Table:

    assessment_results

Fields:

| Field | Type | Required |
|---|---|---:|
| id | PK | Yes |
| assessment_id | FK | Yes |
| student_id | FK | Yes |
| score | DECIMAL | Yes |
| grade | VARCHAR | No |
| remarks | TEXT | No |
| created_at | TIMESTAMP | Yes |
| updated_at | TIMESTAMP | Yes |


# 18. Student Fees

Students can have assigned fees.

Table:

    student_fees

Fields:

| Field | Type | Required |
|---|---|---:|
| id | PK | Yes |
| student_id | FK | Yes |
| fee_structure_id | FK | Yes |
| academic_year_id | FK | Yes |
| amount | DECIMAL | Yes |
| discount_amount | DECIMAL | Yes |
| fine_amount | DECIMAL | Yes |
| final_amount | DECIMAL | Yes |
| due_date | DATE | Yes |
| status | ENUM | Yes |
| created_at | TIMESTAMP | Yes |
| updated_at | TIMESTAMP | Yes |

Status:

    PENDING
    PARTIAL
    PAID
    OVERDUE
    CANCELLED


# 19. Student Payments

Payments belong to student fee assignments.

Table:

    payments

Fields:

| Field | Type | Required |
|---|---|---:|
| id | PK | Yes |
| student_fee_id | FK | Yes |
| student_id | FK | Yes |
| receipt_number | VARCHAR | Yes |
| amount | DECIMAL | Yes |
| payment_method | ENUM | Yes |
| payment_date | TIMESTAMP | Yes |
| reference_number | VARCHAR | No |
| notes | TEXT | No |
| received_by | FK → users.id | Yes |
| created_at | TIMESTAMP | Yes |
| updated_at | TIMESTAMP | Yes |

Payment methods:

    CASH
    BANK
    CARD
    ONLINE
    OTHER


# 20. Student Search

The API should support searching by:

    student_code
    first_name
    last_name
    preferred_name
    email
    phone
    enrollment_number

Example:

    GET /api/v1/students?search=sokhin


# 21. Student Filters

Support:

    school
    branch
    status
    gender
    academic_year
    program
    level
    class
    section

Example:

    GET /api/v1/students
        ?branchId=1
        &academicYearId=3
        &classId=12
        &status=ACTIVE


# 22. Student List UI

The frontend should support:

    [ Search students... ]

    [ Filter ▾ ]

    [ Table | Card ]

Table columns:

    Student
    Student Code
    Gender
    Date of Birth
    Class
    Branch
    Status
    Actions

Card view:

    ┌──────────────────────────────┐
    │       Student Photo         │
    │                              │
    │ Sokhin Sing                  │
    │ STU-000001                   │
    │ Grade 5 • Class A            │
    │                              │
    │ Active                       │
    │                              │
    │ [ View ] [ More ▾ ]          │
    └──────────────────────────────┘


# 23. Student Detail Page

Recommended tabs:

    Overview
    Academic
    Attendance
    Exams & Grades
    Fees & Payments
    Parents
    Documents

Optional:

    Medical
    Transport
    Library


# 24. Student API Endpoints

## List

    GET /api/v1/students

## Detail

    GET /api/v1/students/{id}

## Create

    POST /api/v1/students

## Update

    PUT /api/v1/students/{id}

## Partial Update

    PATCH /api/v1/students/{id}

## Delete / Archive

    DELETE /api/v1/students/{id}

Prefer soft delete/archive instead of physically deleting student records.

## Enrollment

    GET  /api/v1/students/{id}/enrollments
    POST /api/v1/students/{id}/enrollments

## Parents

    GET  /api/v1/students/{id}/parents
    POST /api/v1/students/{id}/parents

## Documents

    GET  /api/v1/students/{id}/documents
    POST /api/v1/students/{id}/documents

## Attendance

    GET /api/v1/students/{id}/attendance

## Results

    GET /api/v1/students/{id}/results

## Fees

    GET /api/v1/students/{id}/fees

## Payments

    GET /api/v1/students/{id}/payments


# 25. Create Student Request

Example:

```json
{
  "studentCode": "STU-000001",
  "firstName": "Sokhin",
  "middleName": null,
  "lastName": "Sing",
  "preferredName": null,
  "gender": "MALE",
  "dateOfBirth": "2010-05-10",
  "nationality": "Cambodian",
  "email": "student@example.com",
  "phone": "012345678",
  "admissionDate": "2026-08-01"
}