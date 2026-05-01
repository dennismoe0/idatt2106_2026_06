# Database

MySQL 8. All schema changes go through Flyway numbered migration scripts (`V1__` through `V30__`) located in `backend/src/main/resources/db/migration/`. Never alter the database manually; always create a new migration file.

---

## Migrations

30 migrations from V1 to V30. Each file is named `V{n}__{description}.sql`. Flyway runs outstanding migrations on application startup. The `flyway_schema_history` table tracks which migrations have been applied.

To add a schema change:
1. Create `V{next_number}__{short_description}.sql` in `db/migration/`.
2. Write the DDL/DML.
3. Restart the application; Flyway applies it automatically.

Never edit an already-applied migration file.

---

## Tables

### users
Core user account. Both teachers and students are stored here.

| Column | Type | Notes |
|--------|------|-------|
| id | BIGINT PK | |
| email | VARCHAR | Unique; used by teachers for login |
| username | VARCHAR | Used by students (simulated Feide) |
| password_hash | VARCHAR | BCrypt strength 12 |
| first_name | VARCHAR | |
| last_name | VARCHAR | |
| role | ENUM | `STUDENT` / `TEACHER` |
| created_at | TIMESTAMP | |

---

### stops
The 7 game locations.

| Column | Type | Notes |
|--------|------|-------|
| id | BIGINT PK | |
| name | VARCHAR | Display name |
| description | TEXT | |
| order_index | INT | 1–7, defines unlock order |
| location_x | FLOAT | Map coordinate |
| location_y | FLOAT | Map coordinate |

---

### tasks
All tasks for all stops.

| Column | Type | Notes |
|--------|------|-------|
| id | BIGINT PK | |
| stop_id | BIGINT FK → stops | |
| task_type | ENUM | `LEARN`, `FAKE_NEWS`, `AI_PHOTO`, `PHISHING_EMAIL`, `MARKETPLACE`, `SOCIAL_MEDIA`, `PASSWORD`, `CLUE_RIDDLE`, `FINAL_BOSS` |
| order_index | INT | Order within the stop |
| content_json | JSON | Full task content, structure varies by type |
| xp_reward | INT | XP awarded on correct answer |
| star_reward | INT | Stars awarded on correct answer |

---

### student_progress
Tracks which tasks each student has completed, in which classroom.

| Column | Type | Notes |
|--------|------|-------|
| id | BIGINT PK | |
| student_id | BIGINT FK → users | |
| task_id | BIGINT FK → tasks | |
| classroom_id | BIGINT FK → classrooms | |
| completed_at | TIMESTAMP | |
| is_correct | BOOLEAN | |

---

### medals
All medals in the system (seeded by DataLoader).

| Column | Type | Notes |
|--------|------|-------|
| id | BIGINT PK | |
| name | VARCHAR | |
| description | TEXT | |
| icon_key | VARCHAR | Frontend asset key |
| stop_id | BIGINT FK → stops (nullable) | Stop this medal is tied to, if any |

---

### student_medals
Which medals a student has earned.

| Column | Type | Notes |
|--------|------|-------|
| id | BIGINT PK | |
| student_id | BIGINT FK → users | |
| medal_id | BIGINT FK → medals | |
| earned_at | TIMESTAMP | |

---

### classrooms
A classroom created and managed by a teacher.

| Column | Type | Notes |
|--------|------|-------|
| id | BIGINT PK | |
| name | VARCHAR | |
| description | TEXT | |
| join_code | VARCHAR | Unique 6-character code |
| music_muted | BOOLEAN | Teacher-controlled music toggle |
| school_id | BIGINT FK → schools (nullable) | |
| created_at | TIMESTAMP | |

---

### classroom_students
Enrolment of a student in a classroom.

| Column | Type | Notes |
|--------|------|-------|
| id | BIGINT PK | |
| classroom_id | BIGINT FK → classrooms | |
| student_id | BIGINT FK → users | |
| display_name | VARCHAR | Student's chosen name within this classroom |
| status | ENUM | `PENDING`, `APPROVED`, `KICKED` |
| joined_at | TIMESTAMP | |

---

### classroom_teachers
Association of a teacher to a classroom (supports co-teaching).

| Column | Type | Notes |
|--------|------|-------|
| id | BIGINT PK | |
| classroom_id | BIGINT FK → classrooms | |
| teacher_id | BIGINT FK → users | |
| is_owner | BOOLEAN | Whether this teacher created the classroom |

---

### schools
A school that groups multiple classrooms.

| Column | Type | Notes |
|--------|------|-------|
| id | BIGINT PK | |
| name | VARCHAR | |
| join_code | VARCHAR | Unique code for teachers to join |
| created_at | TIMESTAMP | |

---

### avatars
One-to-one with users (STUDENT only). Stores the student's current avatar configuration.

| Column | Type | Notes |
|--------|------|-------|
| id | BIGINT PK | |
| user_id | BIGINT FK → users (unique) | |
| gender | VARCHAR | |
| skin | VARCHAR | |
| eyes | VARCHAR | |
| hair | VARCHAR | |
| outfit | VARCHAR | |
| hat | VARCHAR (nullable) | |
| accessory | VARCHAR (nullable) | |
| color_picker_unlocked | BOOLEAN | |

---

### avatar_shop_items
Catalogue of purchasable avatar options.

| Column | Type | Notes |
|--------|------|-------|
| id | BIGINT PK | |
| option_type | VARCHAR | e.g. `HAT`, `OUTFIT`, `ACCESSORY` |
| option_value | VARCHAR | Asset key |
| star_cost | INT | Price in stars |
| medal_id | BIGINT FK → medals (nullable) | Required medal, if any |

---

### unlocked_avatar_options
Which shop items a student has purchased.

| Column | Type | Notes |
|--------|------|-------|
| id | BIGINT PK | |
| user_id | BIGINT FK → users | |
| option_type | VARCHAR | |
| option_value | VARCHAR | |
| purchased_at | TIMESTAMP | |

---

### notebook_entries
Student notebook entries.

| Column | Type | Notes |
|--------|------|-------|
| id | BIGINT PK | |
| student_id | BIGINT FK → users | |
| stop_id | BIGINT FK → stops (nullable) | Null for general entries |
| entry_type | ENUM | `CLUE`, `TIP`, `REFLECTION`, `GENERAL` |
| content | TEXT | |
| created_at | TIMESTAMP | |
| updated_at | TIMESTAMP | |

---

### notifications
In-app notifications for teachers (e.g. new student joined, mystery submitted).

| Column | Type | Notes |
|--------|------|-------|
| id | BIGINT PK | |
| teacher_id | BIGINT FK → users | |
| message | TEXT | |
| is_read | BOOLEAN | |
| created_at | TIMESTAMP | |

---

### student_xp_log
Audit log of XP transactions for a student.

| Column | Type | Notes |
|--------|------|-------|
| id | BIGINT PK | |
| student_id | BIGINT FK → users | |
| classroom_id | BIGINT FK → classrooms | |
| amount | INT | Can be negative for corrections |
| reason | VARCHAR | Human-readable reason |
| created_at | TIMESTAMP | |

---

### weekly_mysteries
Student-submitted mystery scenarios.

| Column | Type | Notes |
|--------|------|-------|
| id | BIGINT PK | |
| submitted_by | BIGINT FK → users | |
| classroom_id | BIGINT FK → classrooms | |
| title | VARCHAR | |
| description | TEXT | |
| answer | TEXT | Correct answer (teacher-set or from submission) |
| status | ENUM | `PENDING`, `APPROVED`, `REJECTED`, `ACTIVE` |
| xp_reward | INT | |
| star_reward | INT | |
| created_at | TIMESTAMP | |

---

### student_mystery_completions
Tracks which students have solved which mysteries.

| Column | Type | Notes |
|--------|------|-------|
| id | BIGINT PK | |
| student_id | BIGINT FK → users | |
| mystery_id | BIGINT FK → weekly_mysteries | |
| completed_at | TIMESTAMP | |
| is_correct | BOOLEAN | |

---

### word_filter
Blocked words / phrases for user-generated content moderation (display names, notebook entries, mystery submissions).

| Column | Type | Notes |
|--------|------|-------|
| id | BIGINT PK | |
| word | VARCHAR | Lowercased |
| added_at | TIMESTAMP | |

---

## Entity relationships (summary)

```
users ──< classroom_students >── classrooms ──< classroom_teachers >── users
users ──< student_progress >── tasks >── stops
users ──< student_medals >── medals
users ─── avatars
users ──< unlocked_avatar_options
users ──< notebook_entries >── stops
users ──< student_xp_log
users ──< weekly_mysteries
users ──< student_mystery_completions >── weekly_mysteries
classrooms >── schools
avatar_shop_items ── medals (optional lock)
```
