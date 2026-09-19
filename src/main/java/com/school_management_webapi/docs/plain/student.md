# Student API

Base path: `/api/v1/students`

All responses are wrapped in the common envelope:

```json
{
  "success": true,
  "message": "string",
  "data": { },
  "timestamp": "2026-08-14T10:15:30"
}
```

## Endpoints

### Create a student
`POST /api/v1/students`

Request body:

```json
{
  "firstName": "Jane",
  "lastName": "Doe",
  "email": "jane.doe@example.com",
  "phoneNumber": "+1 555-123-4567",
  "dateOfBirth": "2012-04-18",
  "gender": "F",
  "address": "123 Main St, Springfield",
  "admissionNumber": "ADM-2026-001",
  "admissionDate": "2026-08-01"
}
```

Response: `201 Created`

```json
{
  "success": true,
  "message": "Student created successfully",
  "data": {
    "id": 1,
    "firstName": "Jane",
    "lastName": "Doe",
    "email": "jane.doe@example.com",
    "phoneNumber": "+1 555-123-4567",
    "dateOfBirth": "2012-04-18",
    "gender": "F",
    "address": "123 Main St, Springfield",
    "admissionNumber": "ADM-2026-001",
    "admissionDate": "2026-08-01",
    "active": true,
    "createdAt": "2026-08-14T10:15:30",
    "updatedAt": "2026-08-14T10:15:30"
  },
  "timestamp": "2026-08-14T10:15:30"
}
```

Validation rules:

| Field | Rules |
|---|---|
| `firstName` | required, max 100 chars |
| `lastName` | required, max 100 chars |
| `email` | required, valid email format, max 150 chars, must be unique |
| `phoneNumber` | optional, must match `^[+0-9 ()-]{7,20}$` when present |
| `dateOfBirth` | required, must be in the past |
| `gender` | optional, max 10 chars |
| `address` | optional, max 255 chars |
| `admissionNumber` | required, max 50 chars, must be unique |
| `admissionDate` | required |

Error responses:
- `400 Bad Request` — validation failed (see [Validation error format](#validation-error-format))
- `409 Conflict` — a student with the same email or admission number already exists

### Get a student by id
`GET /api/v1/students/{id}`

Response: `200 OK` with a single student in `data`, or `404 Not Found` if no student exists with that id.

### List students (paginated)
`GET /api/v1/students?page=0&size=20&sort=lastName,asc`

Standard Spring `Pageable` query parameters are supported (`page`, `size`, `sort`). Response `data` is a Spring `Page` of students:

```json
{
  "success": true,
  "message": "Students retrieved successfully",
  "data": {
    "content": [ { "id": 1, "firstName": "Jane", "...": "..." } ],
    "totalElements": 1,
    "totalPages": 1,
    "number": 0,
    "size": 20
  },
  "timestamp": "2026-08-14T10:15:30"
}
```

### Update a student
`PUT /api/v1/students/{id}`

Request body: same shape as create. Response: `200 OK` with the updated student, `404 Not Found` if the id doesn't exist, `400 Bad Request` on validation failure, `409 Conflict` if the new email/admission number collides with a different student.

### Delete a student
`DELETE /api/v1/students/{id}`

Response: `200 OK` with `data: null`, or `404 Not Found` if the id doesn't exist.

## Validation error format

```json
{
  "success": false,
  "message": "Validation failed",
  "data": {
    "email": "Email must be a valid email address",
    "admissionNumber": "Admission number is required"
  },
  "timestamp": "2026-08-14T10:15:30"
}
```

## Notes
- All endpoints are documented in Swagger UI at `/swagger-ui.html` (see [SwaggerConfig](../config/SwaggerConfig.java)).
- See [docs/todo/student-todo.md](../todo/student-todo.md) for follow-up work planned for this module.




├── 01-authentication.md       ← START HERE
    ├── 02-plans-subscription.md
    ├── 03-school-onboarding.md
    ├── 04-users-roles.md
    ├── 05-school-settings.md
    ├── 06-academic.md
    ├── 07-students.md
    ├── 08-teachers.md
    ├── 09-parents.md
    ├── 10-enrollment.md
    ├── 11-attendance.md
    ├── 12-timetable.md
    ├── 13-exams-grades.md
    ├── 14-finance.md
    ├── 15-notifications.md
    └── 16-reports.md