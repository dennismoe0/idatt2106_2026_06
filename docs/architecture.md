# Architecture

## Overview

Nettdetektivene is a full-stack web application. The backend is a Spring Boot 3 REST API; the frontend is a Vue 3 single-page application. They communicate over HTTP using JSON. Authentication is stateless JWT. MySQL is the database; all schema changes go through Flyway migrations.

```
Vue 3 SPA (Vite, port 5173)
  └── Vue Router — client-side routing, role-based guards
  └── Pinia stores — auth, classroom, game, avatar, notebook, school, audio, notification
  └── Services layer — Axios + JWT interceptor → /api/**
  └── Components — common, student, teacher

Spring Boot API (port 8080)
  └── controller/   — thin REST controllers, input validation
  └── service/      — all business logic
  └── service/answer/ — per-task-type answer checkers
  └── repository/   — Spring Data JPA interfaces
  └── entity/       — JPA entities
  └── dto/          — request/response DTOs (entities never exposed directly)
  └── mapper/       — entity ↔ DTO conversion
  └── security/     — JwtAuthFilter, JwtTokenProvider, UserDetailsServiceImpl
  └── config/       — SecurityConfig, OpenApiConfig
  └── exception/    — GlobalExceptionHandler, AuthException, ResourceNotFoundException
  └── seed/         — DataLoader (stops, tasks, medals on startup), DevSeeder

MySQL 8
  └── Flyway migrations (V1–V30) in resources/db/migration/
```

**Package root:** `no.ntnu.idatt2106.nettdetektivene`

---

## Backend

### Stack

| Component | Version / Details |
|-----------|-------------------|
| Java | 21 |
| Spring Boot | 3.2.4 |
| Spring Data JPA / Hibernate | ORM layer |
| MySQL | 8 |
| Flyway | 30 migrations, V1–V30 |
| JWT | HMAC-SHA256, 24 h expiry, jjwt library |
| BCrypt | Strength 12 |
| Rate limiting | Bucket4j — 10 req/min per IP on auth endpoints |
| API docs | SpringDoc OpenAPI (Swagger UI) |

### Layer responsibilities

**controller/** — Receives HTTP requests, validates input with Bean Validation, delegates to services, returns DTOs. Contains no business logic. One controller class per domain.

**service/** — All business logic lives here. Services call repositories and other services. They throw typed exceptions (`ResourceNotFoundException`, `AuthException`) which the global handler maps to HTTP responses.

**service/answer/** — Nine specialised answer-checker classes, one per task type (FAKE_NEWS, AI_PHOTO, PHISHING_EMAIL, MARKETPLACE, SOCIAL_MEDIA, PASSWORD, FINAL_BOSS, LEARN, CLUE_RIDDLE). Each implements a common interface. `GameService.submitAnswer()` dispatches to the correct checker at runtime.

**repository/** — Spring Data JPA interfaces. Custom JPQL queries where needed; no raw SQL.

**dto/** — Separate request and response DTOs per operation. JPA entities are never serialised directly into API responses.

**mapper/** — Converts between entities and DTOs. Keeps controllers and services free of mapping boilerplate.

**security/** — `JwtTokenProvider` generates and validates tokens. `JwtAuthFilter` is a `OncePerRequestFilter` that reads the `Authorization: Bearer` header and populates the `SecurityContext`. `UserDetailsServiceImpl` loads users by email for Spring Security.

**exception/** — `GlobalExceptionHandler` (`@RestControllerAdvice`) catches all typed and unhandled exceptions and returns consistent JSON error responses. Controllers never throw raw exceptions to the client.

**seed/** — `DataLoader` seeds stops, tasks, and medals into the database on startup (idempotent). `DevSeeder` creates sample teacher/student accounts in the `dev` profile only.

### Task content model

All task content is stored as a JSON column (`content_json`) alongside a `task_type` enum column. This avoids separate tables for each task type and allows the frontend to render the correct component dynamically. The answer-checker dispatch in `service/answer/` is the server-side counterpart.

**Task types:** `LEARN`, `FAKE_NEWS`, `AI_PHOTO`, `PHISHING_EMAIL`, `MARKETPLACE`, `SOCIAL_MEDIA`, `PASSWORD`, `CLUE_RIDDLE`, `FINAL_BOSS`

---

## Frontend

### Stack

| Component | Version / Details |
|-----------|-------------------|
| Vue | 3.5.31, Composition API (`<script setup>`) only |
| Pinia | 3 |
| Vue Router | 5 |
| Tailwind CSS | v4, CSS-first `@theme` config in `tokens.css` |
| Vite | Build tool and dev server |
| Axios | HTTP client with JWT interceptor |

### Stores (Pinia)

One store per domain. Stores are the single source of truth for their slice of state; components never call the API directly.

| Store | Responsibility |
|-------|---------------|
| auth | JWT token, current user, login/logout |
| classroom | Classroom data, student lists, join/create flows |
| game | Stops, tasks, progress, XP, task submission |
| avatar | Avatar state, shop catalog, purchase flow |
| notebook | Notebook entries, CRUD |
| school | School info, school-wide classrooms |
| audio | Background music mute state |
| notification | Teacher notifications, unread count |

### Services layer

`api.js` creates the Axios instance, attaches the JWT `Authorization` header on every request via an interceptor, and handles 401 responses (clears auth state, redirects to login). Domain services (`gameService`, `classroomService`, `avatarService`, `notebookService`, `weeklyMysteryService`, `schoolService`, `notificationService`) wrap specific API endpoints and are called from stores.

### Component structure

```
components/
  common/     — HUD, navbar, modals (shared across roles)
  student/    — task components, boss/, avatar/, notebook/, leaderboard
  teacher/    — classroom management, student progress, moderation
```

### Views

```
views/
  student/
    HomeView         — detective easel dashboard (corkboard)
    MapView          — local city map (7 stops)
    WorldMapView     — global world map
    TaskView         — task runner for a stop
    AvatarView       — avatar customisation
    NotebookView     — student notebook
    LeaderboardView  — classroom/school/global leaderboard
    UkasMysteriumView — weekly mystery
  teacher/
    DashboardView         — classroom list
    ClassroomDetailView   — student management + progress
    MysteryModerationView — moderate student mystery submissions
  auth/
    LoginView
    RegisterView
    StudentLoginView
```

### CSS design system

All design tokens live in `tokens.css` inside a Tailwind v4 `@theme {}` block. This makes them available as both CSS custom properties (`var(--color-primary)`) and as Tailwind utility classes (`text-primary`, `bg-primary`). No hex values are hardcoded in component files.

---

## Security model

| Concern | Implementation |
|---------|---------------|
| Authentication | JWT in `Authorization: Bearer <token>` header — never in cookies |
| Session | Stateless — no server-side session state |
| Roles | STUDENT / TEACHER — enforced in `SecurityConfig` and checked in service methods |
| Route guards | Vue Router `beforeEach` checks auth state and role; unauthenticated or wrong-role requests are redirected |
| Rate limiting | Bucket4j, 10 req/min per IP on `/api/auth/**` endpoints |
| CORS | Origin configured via `ALLOWED_ORIGIN` env var; never wildcard in production |
| Passwords | BCrypt strength 12 |
| Input validation | Bean Validation on backend DTOs; frontend sanitises before submission |

---

## Key architectural decisions

**JSON columns for task content** — `content_json` + `task_type` enum avoids a table-per-type explosion and keeps the schema stable as new task types are added. The answer-checker dispatch pattern mirrors this on the service side.

**Answer-checker dispatch** — Each task type has its own checker class in `service/answer/`. Adding a new type means adding a class and one line in `GameService.submitAnswer()` — no branching logic spread across the codebase.

**Flyway for all schema changes** — All DDL goes through numbered migration scripts. The database is never altered manually.

**Tailwind v4 `@theme` as single source of truth** — All colours, spacing, and typography tokens defined once in `tokens.css`. Components reference tokens, not raw values.

**Pinia stores as single source of truth** — Components read from stores, not from component-local state. This prevents duplicate API calls and keeps state consistent across the UI.

**DTOs always** — JPA entities are never exposed in API responses. This decouples the database schema from the API contract and prevents accidental data leakage.
