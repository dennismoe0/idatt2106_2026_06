# Development Guide

## Branch strategy

```
feature/<name>  →  dev  →  main
```

- All work happens on `feature/<name>` branches.
- PRs target `dev`. At least one review required before merging.
- `main` receives merges from `dev` at release points only. No direct push to `dev` or `main`.
- **Definition of Done:** feature branch → PR → code review by ≥1 person → tests pass → merged to dev.

---

## Code standards

### Backend

- **DTOs always** — JPA entities must never appear in REST responses. One DTO per operation if request and response shapes differ.
- **Service layer holds all business logic** — Controllers validate input and delegate. They contain no `if` statements about business rules.
- **GlobalExceptionHandler** — All error responses go through `@RestControllerAdvice`. Controllers never call `new ResponseEntity(...)` with error status directly.
- **JWT in Authorization header** — `Authorization: Bearer <token>`. Never in cookies, never in query params.
- **No raw SQL** — Use Spring Data JPA method names or `@Query` with JPQL. Parameterise everything. Raw `@NativeQuery` only when JPA cannot express the query, and only with bound parameters.
- **Flyway for all schema changes** — Create a new migration file; never `ALTER` manually.
- **Input validation** — Bean Validation annotations on all request DTOs (`@NotBlank`, `@Email`, `@Size`, etc.).

### Frontend

- **Composition API only** — All components use `<script setup>`. No Options API.
- **One store per domain** — Do not create per-component stores. Stores are: `auth`, `classroom`, `game`, `avatar`, `notebook`, `school`, `audio`, `notification`.
- **Components read from stores** — No direct Axios calls from component `<script setup>`. Call store actions; the store calls the service.
- **Input sanitised on frontend** — Strip or encode user-controlled content before binding to the DOM. Avoid `v-html` with user data; if unavoidable, sanitise with DOMPurify first.
- **CSS tokens** — All colours, spacing, and typography values come from CSS custom properties defined in `tokens.css`. Do not hardcode hex values in component files.

---

## Logging standards

Failures must be traceable without attaching a debugger.

### Backend (SLF4J)

```java
private static final Logger log = LoggerFactory.getLogger(MyService.class);
```

| Level | When to use |
|-------|------------|
| `INFO` | Happy-path milestones: user registered, task submitted, medal awarded |
| `WARN` | Expected failures: bad credentials, resource not found, validation error |
| `ERROR` | Unexpected exceptions: catch blocks that should not normally be reached |

Never log passwords, JWT tokens, or any PII (email addresses, names) at ERROR level or above.

### Frontend (console.*)

| Method | When to use |
|--------|------------|
| `console.log` | Store actions, API calls. Wrap in `import.meta.env.DEV` if the log would be noisy in production. |
| `console.warn` | Recoverable issues: missing optional data, fallback used |
| `console.error` | Every `catch` block must log the full error object: `console.error('Context message', error)` |

Every Axios error handler must log the full error object, not just `error.message`.

---

## Adding a new task type

Follow these steps in order:

1. **Backend: enum** — Add the new value to the `TaskType` enum in `entity/`.

2. **Backend: answer checker** — Create a new class in `service/answer/` that implements the answer-checker interface. The class receives the raw `content_json` object and the student's submitted answer object; it returns a result DTO (correct/incorrect, XP, stars).

3. **Backend: register checker** — Add a case to `GameService.submitAnswer()` that maps the new `TaskType` to the new checker class. One line.

4. **Frontend: task component** — Create a Vue component in `frontend/src/components/student/` named after the task type (e.g. `MyNewTypeTask.vue`). The component receives the task's `contentJson` as a prop, renders the UI, and emits an `answer` event with the student's response object.

5. **Frontend: task runner** — Register the new component in the task runner (TaskView or equivalent) so it is rendered when `taskType === 'MY_NEW_TYPE'`.

6. **Boss fight** — If the new type should appear in the Final Boss stop, add a case to `FinalBossTask.vue`.

7. **Seed data** — Add sample tasks of the new type to `DataLoader.java` so the dev environment has playable content immediately.

8. **Tests** — Write an answer checker unit test in `service/answer/MyNewTypeAnswerCheckerTest.java` and a component test in `frontend/src/__tests__/components/MyNewTypeTask.test.js`.

---

## CSS design system

Tailwind CSS v4 uses a CSS-first configuration. The single source of truth for all design tokens is `frontend/src/assets/tokens.css`.

```css
@theme {
  --color-primary: #1a2e4a;
  --color-secondary: ...;
  /* etc. */
}
```

This makes every token available as:
- A CSS custom property: `var(--color-primary)`
- A Tailwind utility: `bg-primary`, `text-primary`, `border-primary`

**Rules:**
- Never hardcode hex values in component files.
- Never add one-off colours via `bg-[#abc123]` in components. Add the token to `tokens.css` first.
- If a colour is used in more than one place, it must be a token.

---

## Environment variables

Key variables in `.env` (copy from `.env.example`):

| Variable | Description |
|----------|-------------|
| `MYSQL_ROOT_PASSWORD` | Root password for the Docker MySQL container |
| `MYSQL_DATABASE` | Database name |
| `MYSQL_USER` | App DB user |
| `MYSQL_PASSWORD` | App DB password |
| `MYSQL_PORT` | Host port for MySQL (default `3306`) |
| `JWT_SECRET` | HMAC-SHA256 signing secret (min 32 chars) |
| `ALLOWED_ORIGIN` | CORS allowed origin for the frontend (e.g. `http://localhost:5173`) |
| `FRONTEND_PORT` | Host port for the Vite dev server (default `5173`) |

Never commit `.env` — it is in `.gitignore`.

---

## Common tasks

### Run only backend tests
```bash
cd backend && mvn test
```

### Run a single test class
```bash
cd backend && mvn test -Dtest=GameServiceTest
```

### Run only frontend tests
```bash
cd frontend && npm run test
```

### Generate backend coverage report
```bash
cd backend && mvn verify
open backend/target/site/jacoco/index.html
```

### Generate frontend coverage report
```bash
cd frontend && npm run test:coverage
open frontend/coverage/index.html
```

### Reset the local database (wipe all data)
```bash
docker compose down -v
docker compose up --build
```

### Apply a new Flyway migration without restarting Docker
```bash
# Just restart the backend — Flyway runs on startup
cd backend && ./mvnw spring-boot:run
```
