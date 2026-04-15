# Docker Launch System — Design Spec
**Date:** 2026-04-15
**Project:** Nettdetektivene (IDATT2106 Team 6)
**Status:** Approved

---

## Goal

Single-command launch for the full stack (MySQL + Spring Boot + Vue 3) that works on Windows, Mac, and Linux with no installation required beyond Docker. A separate fast-iteration dev path runs backend and frontend on the host for full hot reload, requiring only Docker + Java 21 + Node 20.

---

## Two Workflows

### Full Docker (Kasper + graders)

No Java or Node required on the host. Everything runs in containers.

```bash
cp .env.example .env         # defaults work as-is for local use
docker compose up --build    # builds images, starts db → backend → frontend
# open http://localhost:5173
```

First run: ~3–5 minutes (Maven downloads deps, npm builds). Subsequent runs: fast (Docker layer cache).

### Dev (everyone with Java 21 + Node 20)

Full Vite HMR and Spring Boot devtools. Run commands separately — no `make` required.

```bash
# Start DB (once per session)
docker compose -f docker-compose.dev.yml up -d --wait

# Terminal 1 — backend
./backend/mvnw spring-boot:run          # Mac/Linux
backend\mvnw.cmd spring-boot:run        # Windows

# Terminal 2 — frontend
cd frontend && npm run dev
```

`make` shortcuts are available on Mac/Linux as convenience aliases but are never required.

---

## Files

### New

| File | Purpose |
|------|---------|
| `backend/Dockerfile` | Multi-stage: Maven build → eclipse-temurin:21-jre-alpine runtime |
| `frontend/Dockerfile` | Multi-stage: node:20-alpine build → nginx:alpine serve |
| `frontend/nginx.conf` | SPA routing (`try_files`) + `/api/` proxy to backend container |
| `docker-compose.dev.yml` | DB-only compose for dev path |

### Modified

| File | Change |
|------|--------|
| `docker-compose.yml` | Add `backend` and `frontend` services alongside existing `db` |
| `backend/src/main/resources/application.yml` | `localhost` → `${DB_HOST:localhost}` (one line) |
| `.env.example` | Add `DB_HOST=db`, `FRONTEND_PORT=5173` |
| `Makefile` | Cleaner targets; optional convenience only |
| `README.md` | Setup section for both paths |

---

## Service Architecture (Full Docker)

```
Browser → http://localhost:${FRONTEND_PORT:-5173}
           │
           ▼
      nginx (frontend container)
           │  /api/* → proxy_pass http://backend:8080
           │  /*     → serve /usr/share/nginx/html (Vue SPA)
           │
           ▼
      Spring Boot (backend container) :8080
           │
           ▼
      MySQL 8 (db container) :3306
```

Dependency chain enforced by healthchecks:
- `backend` waits for `db` healthy before starting
- `frontend` waits for `backend` healthy (TCP check on port 8080) before nginx starts

### Why not expose the backend port publicly?

In full Docker mode, the browser never calls the backend directly — all API traffic goes through nginx. The backend port is internal to the Docker network only. This avoids CORS configuration in the production compose and mirrors how a real deployment would work.

In dev mode, the backend runs on `localhost:8080` and Vite's dev server is configured with a proxy to it.

---

## Environment Variables

### Root `.env` (shared by both workflows)

| Variable | Default | Purpose |
|----------|---------|---------|
| `DB_ROOT_PASSWORD` | `rootpassword` | MySQL root password |
| `DB_NAME` | `nettdetektivene` | Database name |
| `DB_USERNAME` | `appuser` | App DB user |
| `DB_PASSWORD` | `apppassword` | App DB password |
| `JWT_SECRET` | *(must set)* | JWT signing key — change before use |
| `MYSQL_PORT` | `3306` | Host-mapped MySQL port |
| `DB_HOST` | `db` | DB hostname — `db` for Docker, `localhost` for dev host |
| `FRONTEND_PORT` | `5173` | Host port for nginx frontend |

### `frontend/.env` (dev only, not used in Docker build)

| Variable | Default | Purpose |
|----------|---------|---------|
| `VITE_API_URL` | `http://localhost:8080` | Axios base URL in dev |

In the Docker build, `VITE_API_URL` is set to empty string via `--build-arg`. This makes all Axios calls relative (`/api/...`), which nginx then proxies to the backend container. Zero changes to Vue source code.

---

## Dockerfiles

### `backend/Dockerfile`

```dockerfile
# Stage 1: Build
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline -q
COPY src ./src
RUN mvn package -DskipTests -q

# Stage 2: Runtime
FROM eclipse-temurin:21-jre-alpine
RUN apk add --no-cache curl
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]
```

Dependency pre-download (`dependency:go-offline`) means repeated builds only re-run the compile stage when source changes — not a full dep download every time.

### `frontend/Dockerfile`

```dockerfile
# Stage 1: Build
FROM node:20-alpine AS build
WORKDIR /app
COPY package*.json ./
RUN npm ci
COPY . .
ARG VITE_API_URL=""
ENV VITE_API_URL=$VITE_API_URL
RUN npm run build

# Stage 2: Serve
FROM nginx:alpine
COPY --from=build /app/dist /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf
EXPOSE 80
```

### `frontend/nginx.conf`

```nginx
server {
    listen 80;
    root /usr/share/nginx/html;
    index index.html;

    location /api/ {
        proxy_pass http://backend:8080/api/;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
    }

    location / {
        try_files $uri $uri/ /index.html;
    }
}
```

---

## docker-compose.yml (updated)

All three services. `db` service unchanged from current. Added:

```yaml
backend:
  build: ./backend
  environment:
    DB_HOST: db
    DB_PORT: 3306
    DB_NAME: ${DB_NAME}
    DB_USERNAME: ${DB_USERNAME}
    DB_PASSWORD: ${DB_PASSWORD}
    JWT_SECRET: ${JWT_SECRET}
  depends_on:
    db:
      condition: service_healthy
  healthcheck:
    test: ["CMD-SHELL", "wget -qO- http://localhost:8080/swagger-ui/index.html || exit 1"]
    interval: 15s
    timeout: 10s
    retries: 8
    start_period: 60s

frontend:
  build:
    context: ./frontend
    args:
      VITE_API_URL: ""
  ports:
    - "${FRONTEND_PORT:-5173}:80"
  depends_on:
    backend:
      condition: service_healthy
```

## docker-compose.dev.yml (new)

```yaml
services:
  db:
    image: mysql:8.0
    command: --default-authentication-plugin=mysql_native_password --character-set-server=utf8mb4 --collation-server=utf8mb4_unicode_ci
    environment:
      MYSQL_ROOT_PASSWORD: ${DB_ROOT_PASSWORD}
      MYSQL_DATABASE: ${DB_NAME}
      MYSQL_USER: ${DB_USERNAME}
      MYSQL_PASSWORD: ${DB_PASSWORD}
    ports:
      - "${MYSQL_PORT:-3306}:3306"
    volumes:
      - mysql_data:/var/lib/mysql
    healthcheck:
      test: ["CMD", "mysqladmin", "ping", "-h", "localhost", "-u", "root", "-p${DB_ROOT_PASSWORD}"]
      interval: 10s
      timeout: 5s
      retries: 10
      start_period: 30s

volumes:
  mysql_data:
```

---

## application.yml Change

Single line change — DB host made configurable:

```yaml
# Before
url: jdbc:mysql://localhost:${MYSQL_PORT:3306}/${DB_NAME:nettdetektivene}?...

# After
url: jdbc:mysql://${DB_HOST:localhost}:${MYSQL_PORT:3306}/${DB_NAME:nettdetektivene}?...
```

Dev: `DB_HOST` not set → defaults to `localhost`. Docker: compose sets `DB_HOST=db`.

---

## Makefile (convenience only, Mac/Linux)

```makefile
.PHONY: dev-db backend frontend start stop logs

dev-db:
	docker compose -f docker-compose.dev.yml up -d --wait

backend:
	./backend/mvnw spring-boot:run

frontend:
	cd frontend && npm run dev

start:
	docker compose up --build

stop:
	docker compose down
	docker compose -f docker-compose.dev.yml down

logs:
	docker compose logs -f
```

---

## Out of Scope

- Hot reload inside Docker containers (Vite HMR with volume mounts) — not needed since dev path runs on host
- Multi-environment configs (staging, production remote) — local only per project requirements
- Docker Swarm / Kubernetes — overkill for a course project
- Automated `.env` generation — user copies `.env.example` manually

---

## Success Criteria

- `docker compose up --build` starts all three services from a clean clone, no extra tools needed
- `http://localhost:5173` loads the Vue app and API calls succeed
- Dev path starts DB in Docker and backend/frontend on host with full hot reload
- Works on Windows (PowerShell / CMD), Mac, and Linux
- Kasper can run the full stack without Java or Node installed
