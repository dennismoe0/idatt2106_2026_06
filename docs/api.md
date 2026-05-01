# API Reference

Base URL: `http://localhost:8080`

Interactive documentation is available at **http://localhost:8080/swagger-ui/index.html** when the backend is running.

All endpoints under `/api/**` (except auth) require a valid JWT token in the `Authorization: Bearer <token>` header.

Role annotations: `[TEACHER]` = requires TEACHER role, `[STUDENT]` = requires STUDENT role, `[ANY]` = any authenticated user.

---

## Auth — `/api/auth`

No authentication required on these endpoints.

### POST /api/auth/register
Teacher self-registration.

**Request body:**
```json
{
  "email": "teacher@example.com",
  "password": "hunter2",
  "firstName": "Ola",
  "lastName": "Nordmann"
}
```

**Response:** `201 Created` with user details.

---

### POST /api/auth/login
Teacher login.

**Request body:**
```json
{
  "email": "teacher@example.com",
  "password": "hunter2"
}
```

**Response:**
```json
{
  "token": "<jwt>",
  "role": "TEACHER",
  "userId": 1
}
```

---

### POST /api/auth/student-login
Simulated Feide student login (username only, no password).

**Request body:**
```json
{
  "username": "student123"
}
```

**Response:**
```json
{
  "token": "<jwt>",
  "role": "STUDENT",
  "userId": 42
}
```

---

## Game — `/api/game`

### GET /api/game/stops/meta `[TEACHER]`
Returns metadata for all stops (no progress).

---

### GET /api/game/stops?classroomId=X `[STUDENT]`
Returns all stops with their locked/unlocked status for the authenticated student in the given classroom.

---

### GET /api/game/stops/{stopId}/tasks?classroomId=X `[STUDENT]`
Returns all tasks for a stop, including completion status for the student.

---

### GET /api/game/tasks/{taskId}?classroomId=X `[STUDENT]`
Returns a single task with its full `content_json` payload.

---

### POST /api/game/tasks/{taskId}/submit `[STUDENT]`
Submit an answer to a task.

**Request body:**
```json
{
  "answer": { "...task-type-specific fields..." },
  "classroomId": 7
}
```

**Response:** Correct/incorrect result, XP awarded, any medals earned.

---

### GET /api/game/progress?classroomId=X `[STUDENT]`
Returns completion statistics: stops completed, tasks completed, total XP, medals earned.

---

### GET /api/game/profile `[STUDENT]`
Returns the student's current XP, level, and star balance.

---

### POST /api/game/stops/{stopId}/claim-xp `[STUDENT]`
Claim the weekly bonus XP for completing a stop. One-time per stop per week.

---

## Classrooms — `/api/classrooms`

### POST /api/classrooms `[TEACHER]`
Create a new classroom.

**Request body:**
```json
{
  "name": "5A",
  "description": "Fifth grade class A"
}
```

**Response:** `201 Created` with classroom details including join code.

---

### GET /api/classrooms `[TEACHER]`
List all classrooms owned by the authenticated teacher.

---

### GET /api/classrooms/{id} `[TEACHER]`
Get a single classroom by ID.

---

### DELETE /api/classrooms/{id} `[TEACHER]`
Delete a classroom. Only the owning teacher can delete.

---

### POST /api/classrooms/join `[STUDENT]`
Join a classroom using a join code.

**Request body:**
```json
{
  "code": "ABC123",
  "displayName": "Detektiv Mikkel"
}
```

**Response:** Enrollment created with `PENDING` status until the teacher approves.

---

### GET /api/classrooms/{id}/students `[TEACHER]`
List all students enrolled in the classroom with their status (PENDING / APPROVED / KICKED).

---

### GET /api/classrooms/{id}/student-progress `[TEACHER]`
Returns progress summary for each approved student: stops completed, tasks completed, XP.

---

### GET /api/classrooms/{id}/stops `[TEACHER]`
Returns all stops associated with the classroom.

---

### PUT /api/classrooms/{id}/students/{sid} `[TEACHER]`
Approve or kick a student.

**Request body:**
```json
{
  "status": "APPROVED"
}
```

Allowed values: `APPROVED`, `KICKED`.

---

### GET /api/classrooms/{id}/leaderboard `[ANY]`
Returns the classroom leaderboard ranked by XP.

---

### GET /api/classrooms/{id}/school-leaderboard `[ANY]`
Returns the leaderboard for all classrooms in the same school, ranked by average class XP.

---

### GET /api/classrooms/{id}/global-leaderboard `[ANY]`
Returns the global leaderboard across all classrooms.

---

### PUT /api/classrooms/{id}/my-displayname `[STUDENT]`
Update the student's display name within this classroom.

**Request body:**
```json
{
  "displayName": "Detektiv Lena"
}
```

---

### GET /api/classrooms/mine `[STUDENT]`
Returns the classroom the authenticated student is enrolled in.

---

### GET /api/classrooms/{id}/my-status `[STUDENT]`
Returns the student's approval status in the classroom plus the `musicMuted` flag.

---

### PUT /api/classrooms/{id}/music-muted `[TEACHER]`
Toggle background music for the classroom.

**Request body:**
```json
{
  "musicMuted": true
}
```

---

## Avatars — `/api/avatars`

### GET /api/avatars/me `[STUDENT]`
Returns the authenticated student's current avatar configuration.

---

### PUT /api/avatars/me `[STUDENT]`
Update avatar fields (gender, skin, eyes, hair, outfit, hat, accessory).

**Request body:** Partial or full avatar field map.

---

### GET /api/avatars/options `[STUDENT]`
Returns all available avatar options with flags:
- `purchased` — already owned
- `medalLocked` — requires a specific medal
- `colorPickerUnlocked` — whether the color picker has been unlocked

---

### GET /api/avatars/shop `[STUDENT]`
Returns the full shop catalog with `purchased` flag on each item.

---

### POST /api/avatars/shop/purchase `[STUDENT]`
Purchase an avatar option using stars.

**Request body:**
```json
{
  "optionType": "HAT",
  "optionValue": "detective_hat"
}
```

**Response:** Updated star balance.

---

## Medals — `/api/medals`

### GET /api/medals/all `[ANY]`
Returns all medals defined in the system with descriptions and unlock conditions.

---

### GET /api/medals/mine `[STUDENT]`
Returns all medals earned by the authenticated student.

---

## Notebook — `/api/notebook`

### GET /api/notebook `[STUDENT]`
Returns all notebook entries for the authenticated student (clues, tips, and reflections).

---

### POST /api/notebook `[STUDENT]`
Create a reflection tied to a specific stop.

**Request body:**
```json
{
  "stopId": 3,
  "content": "Jeg lærte at..."
}
```

---

### POST /api/notebook/general `[STUDENT]`
Create a general (non-stop) notebook entry.

**Request body:**
```json
{
  "content": "Min egen notis"
}
```

---

### PUT /api/notebook/{id} `[STUDENT]`
Edit an existing notebook entry.

**Request body:**
```json
{
  "content": "Oppdatert tekst"
}
```

---

### DELETE /api/notebook/{id} `[STUDENT]`
Delete a notebook entry.

---

### GET /api/notebook/student/{studentId} `[TEACHER]`
View all notebook entries for a specific student (teacher read-only access).

---

## Notifications — `/api/notifications`

### GET /api/notifications `[TEACHER]`
List all notifications for the authenticated teacher.

---

### GET /api/notifications/count `[TEACHER]`
Returns the count of unread notifications.

**Response:**
```json
{
  "count": 3
}
```

---

### PUT /api/notifications/{id}/read `[TEACHER]`
Mark a single notification as read.

---

### PUT /api/notifications/read-all `[TEACHER]`
Mark all notifications as read.

---

### DELETE /api/notifications/{id} `[TEACHER]`
Delete a single notification.

---

### DELETE /api/notifications/old `[TEACHER]`
Delete all notifications older than a threshold (cleanup).

---

## Schools — `/api/schools`

### POST /api/schools `[TEACHER]`
Create a new school.

**Request body:**
```json
{
  "name": "Solberg skole"
}
```

**Response:** `201 Created` with school details including join code.

---

### POST /api/schools/join `[TEACHER]`
Join an existing school using a join code.

**Request body:**
```json
{
  "code": "SCH42"
}
```

---

### GET /api/schools/mine `[TEACHER]`
Returns the school the authenticated teacher belongs to.

---

### GET /api/schools/mine/classrooms `[TEACHER]`
Returns all classrooms belonging to teachers in the same school.

---

## Weekly Mysteries — `/api/weekly-mysteries`

### POST /api/weekly-mysteries/submissions `[STUDENT]`
Submit a new mystery idea.

**Request body:**
```json
{
  "title": "Den mystiske e-posten",
  "description": "Noen sendte en veldig merkelig e-post til rektoren..."
}
```

---

### GET /api/weekly-mysteries/active?classroomId=X `[STUDENT]`
Returns the currently active mystery for the classroom. Returns `204 No Content` if there is no active mystery.

---

### POST /api/weekly-mysteries/active/complete `[STUDENT]`
Submit an answer to the active mystery.

**Request body:**
```json
{
  "answer": "Personen er...",
  "classroomId": 7
}
```

**Response:** Correct/incorrect result, stars and XP awarded if correct.

---

### GET /api/weekly-mysteries/submissions?classroomId=X `[TEACHER]`
Returns all student mystery submissions for the classroom, with moderation status.

---

### PUT /api/weekly-mysteries/{id} `[TEACHER]`
Edit a mystery's content or status.

**Request body:** Fields to update (title, description, answer, status).

---

### PUT /api/weekly-mysteries/{id}/activate?classroomId=X `[TEACHER]`
Set a mystery as the active mystery for the classroom.

---

### PUT /api/weekly-mysteries/{id}/reject `[TEACHER]`
Reject a student mystery submission.
