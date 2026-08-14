# API 01 — Authentication

## Purpose

Implement the first API module for the School Management System.

This module handles:

- User registration
- Login
- Logout
- Access token
- Refresh token
- Forgot password
- Reset password
- Current authenticated user

This is the foundation for all other API modules.

Base URL:

    /api/v1/auth

---

# 1. Architecture

The authentication flow is:

    Static Website
          │
          │ Sign Up Now
          ▼
    System Web App
          │
          ▼
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

Login flow:

    Login
      │
      ▼
    Authenticate User
      │
      ▼
    Access Token
      │
      ▼
    Dashboard

Forgot password:

    Forgot Password
          │
          ▼
      Check Email
          │
          ▼
      Reset Password
          │
          ▼
         Login

---

# 2. Authentication Database

For the first implementation, create these tables:

    users
    refresh_tokens
    password_reset_tokens

Do NOT create:

    students
    teachers
    parents
    classes
    subjects
    attendance
    fees

yet.

Those belong to later API modules.

---

# 3. Users Table

Table:

    users

Fields:

| Field | Type | Required | Description |
|---|---|---:|---|
| id | UUID / BIGINT | Yes | Primary key |
| name | VARCHAR(150) | Yes | User display/full name |
| email | VARCHAR(255) | Yes | Login email |
| password_hash | VARCHAR(255) | Yes | Hashed password |
| status | ENUM | Yes | Account status |
| email_verified_at | TIMESTAMP | No | Email verification time |
| last_login_at | TIMESTAMP | No | Last login |
| created_at | TIMESTAMP | Yes | Creation time |
| updated_at | TIMESTAMP | Yes | Last update |
| deleted_at | TIMESTAMP | No | Soft delete |

User status:

    ACTIVE
    INACTIVE
    SUSPENDED

Email must be unique.

Do NOT store plaintext passwords.

Use a secure password hashing algorithm such as:

    BCrypt

or the password hashing mechanism recommended by the framework.

---

# 4. User Role

Do not implement the complete Users & Roles system yet.

For the first authentication API, the user can have a basic role reference if needed.

Recommended future structure:

    users
       │
       ▼
    user_roles
       │
       ▼
    roles

The complete role/permission system will be implemented in:

    API 04 — Users & Roles

Do not hard-code permissions into the authentication module.

---

# 5. Register API

Endpoint:

    POST /api/v1/auth/register

Purpose:

Create a new user account.

Request:

```json
{
  "name": "Sokhin Sing",
  "email": "sokhin@example.com",
  "password": "StrongPassword123!",
  "confirmPassword": "StrongPassword123!"
}