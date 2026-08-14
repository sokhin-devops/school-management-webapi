# Student API — TODO

Follow-up work identified while building the initial Student CRUD API (2026-08-14). None of this is implemented yet.

## Foundational (repo-wide, not student-specific)
- [ ] Fix `group = 'com.'` in `build.gradle` — looks like a placeholder/typo, should probably be `com.school_management_webapi`.
- [ ] Move the DB password out of `application.properties` (currently committed in plain text) and into an env var or secrets manager.
- [ ] Add Spring Security / authentication — all endpoints are currently unauthenticated.
- [ ] Decide whether to adopt MapStruct for mapping once more entities exist; mappers are currently written by hand.

## Student module
- [ ] Add unit tests for `StudentServiceImpl` (create/update duplicate-detection branches, not-found branches).
- [ ] Add integration/web-layer tests for `StudentController` (validation errors, 404/409 paths).
- [ ] Add search/filter query params to `GET /api/v1/students` (e.g. by name, admission number, active status) beyond plain pagination.
- [ ] Add soft-delete support if deleting a student needs to be reversible (currently a hard delete).
- [ ] Consider relating `Student` to other entities once they exist (e.g. `ClassRoom`, `Guardian`, `Enrollment`) — no relationships are modeled yet.
- [ ] Add a `@Version` optimistic-locking column if concurrent updates to the same student become a concern.
- [ ] Confirm phone number validation regex against real-world formats used by the school (currently a permissive placeholder pattern).
