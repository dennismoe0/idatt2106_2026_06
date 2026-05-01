# Nettdetektivene — IDATT2106 V2026, Team 6

## Live demo

**https://frontend-production-90b8.up.railway.app/**

| Role    | Login                      | Credentials                     |
| ------- | -------------------------- | ------------------------------- |
| Teacher | Email: `grethe@teacher.no` | Password: `password123`         |
| Student | Username: `christian`      | (no password — simulated Feide) |
| Student | Username: `dennis`         | (no password — simulated Feide) |
| Student | Username: `kasper`         | (no password — simulated Feide) |

Students log in via the **"Elev"** button on the login page. After logging in, join classroom with code **`dataing-2`** (Grethe's class) if not already enrolled.

---

Nettdetektivene ("The Net Detectives") is an educational browser game for students aged 8–13. A mysterious criminal has stolen the town's digital budget, and players take on the role of junior detectives. To unmask the culprit, they must investigate 7 locations across the city, each teaching a core internet-safety skill: spotting fake news, identifying phishing emails, recognising AI-manipulated images, evaluating password strength, avoiding online scams, understanding social media manipulation, and a final boss challenge combining all six.

The game is played inside a classroom managed by a teacher. The teacher creates the classroom, students join with a code, and each student's progress is tracked individually. Medals, avatar customisation, a personal notebook, and leaderboards keep students engaged across multiple sessions.

**Target audience:** School students, ages 8–13.
**Course:** IDATT2106 Systemutvikling 2 med smidig prosjekt — NTNU 2026.

---

## Team

| Username | Name                         |
| -------- | ---------------------------- |
| dennimoe | Dennis Moe                   |
| chripre  | Christian Petter Remman      |
| kasperog | Kasper Østerlie Gladsøy      |
| krisase  | Kristian Ask Selmer          |
| olasy    | Ola Syrstad Berg             |
| shaktios | Shakti Om Sharma             |
| tohiggin | Thomas Oliver Wallin Higgins |

---

## Tech stack

| Layer       | Technology                                         |
| ----------- | -------------------------------------------------- |
| Backend     | Spring Boot 3.2.4 / Java 21                        |
| Database    | MySQL 8, Flyway migrations (30 migrations)         |
| Auth        | JWT (HMAC-SHA256, 24 h expiry), BCrypt strength 12 |
| ORM         | Spring Data JPA / Hibernate                        |
| Frontend    | Vue 3.5 (Composition API, `<script setup>`)        |
| State       | Pinia 3                                            |
| Routing     | Vue Router 5                                       |
| Styling     | Tailwind CSS v4 (CSS-first `@theme` config)        |
| Build       | Vite (frontend), Maven (backend)                   |
| HTTP client | Axios with JWT interceptor                         |

---

## Game overview

Students explore 7 stops in order. Each stop has a short learning phase followed by domain-specific tasks, and ends with a clue riddle that advances the story.

| #   | Stop                    | Theme                               |
| --- | ----------------------- | ----------------------------------- |
| 1   | Nyhetskvartalet         | Fake news detection                 |
| 2   | Fotografen              | AI / manipulated images             |
| 3   | Postkontoret            | Phishing emails                     |
| 4   | Markedsplassen          | Scam marketplace                    |
| 5   | Den sosiale møteplassen | Social media manipulation           |
| 6   | Passordbanken           | Password strength                   |
| 7   | Datasenteret            | Final Boss — all six types combined |

Completed stops award XP and medals. Medals unlock avatar options. Stars (secondary currency) are spent in the avatar shop. A classroom leaderboard, school leaderboard, and global leaderboard track progress.

See [docs/game-mechanics.md](docs/game-mechanics.md) for the full game design.

---

## Architecture

The backend is a Spring Boot REST API. The frontend is a Vue 3 SPA that communicates with the API via Axios. MySQL is the database; Flyway manages all schema changes. Authentication is stateless JWT, role-based (STUDENT / TEACHER).

See [docs/architecture.md](docs/architecture.md) for the full architecture document including package structure, security model, and key design decisions.

---

## API

Interactive Swagger UI is available at **http://localhost:8080/swagger-ui/index.html** when the backend is running.

See [docs/api.md](docs/api.md) for the full endpoint reference.

---

## Testing

```bash
# Backend — 225 tests
cd backend && mvn test

# Frontend — 310 tests
cd frontend && npm run test

# E2E (Cypress, headless)
cd frontend && npm run cypress:run
```

See [docs/testing.md](docs/testing.md) for coverage targets, test structure, and E2E flow documentation.

---

## Running locally

Two paths. Pick the one that fits you.

---

### Path A — Full Docker (no Java or Node required)

**Prerequisites:** [Docker Desktop](https://www.docker.com/products/docker-desktop/) only.

```bash
# 1. Copy env file (defaults work as-is for local use)
# Mac / Linux
cp .env.example .env

# Windows (PowerShell)
Copy-Item .env.example .env

# 2. Start everything
docker compose up --build
```

First run takes 3–5 minutes (Maven downloads dependencies, npm builds). Subsequent runs are fast.

Open http://localhost:5173 when all three services show healthy.

**Stop:**

```bash
docker compose down
```

---

### Path B — Dev (Java 21 + Node 20 required)

Full Vite HMR and Spring Boot devtools. Database runs in Docker; backend and frontend run on the host.

**Prerequisites:** Docker Desktop, Java 21, Node 20.

**Step 1 — Start the database (once per session):**

```bash
docker compose -f docker-compose.dev.yml up -d --wait
```

**Step 2 — Start the backend (Terminal 1):**

```bash
# Mac / Linux
cd backend && ./mvnw spring-boot:run

# Windows (CMD)
cd backend
mvnw.cmd spring-boot:run (or potentially the same as Mac / Linux)

# Windows (PowerShell)
cd backend
.\mvnw.cmd spring-boot:run
```

**Step 3 — Start the frontend (Terminal 2):**

```bash
cd frontend && npm run dev
```

Mac/Linux shortcuts: `make dev-db`, `make backend`, `make frontend`

| URL         |                                             |
| ----------- | ------------------------------------------- |
| Frontend    | http://localhost:5173                       |
| Backend API | http://localhost:8080                       |
| Swagger UI  | http://localhost:8080/swagger-ui/index.html |

**Stop:**

```bash
docker compose -f docker-compose.dev.yml down
```

---

## Troubleshooting

**`Access denied for user appuser` or `Communications link failure`**
The `.env` file is missing, or Docker initialized the database before `.env` existed.

```bash
docker compose down -v
cp .env.example .env
docker compose up --build
```

**`Port 3306 already in use`**
Another MySQL is running locally. Set `MYSQL_PORT=3307` in your `.env` and restart Docker.

```bash
brew services stop mysql   # Mac with Homebrew
```

**`Port 5173 already in use`**
Set `FRONTEND_PORT=5174` in your `.env`.

**`Permission denied` on `./mvnw` (Mac/Linux)**

```bash
chmod +x backend/mvnw
```

**Docker `pipe/docker_engine` error (Windows)**
Docker Desktop is not running or is in Windows containers mode.

1. Open Docker Desktop and wait for it to finish loading.
2. Right-click the whale icon → **Switch to Linux containers…**

**`UnsupportedClassVersionError` / `class file version 65.0`**
Wrong Java version. Project requires Java 21.

```bash
java -version
sudo archlinux-java set java-21-openjdk   # Arch Linux
brew install openjdk@21                   # Mac
```
