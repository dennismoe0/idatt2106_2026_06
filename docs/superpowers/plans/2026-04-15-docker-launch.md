# Docker Launch System Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Single-command `docker compose up --build` launches the full MySQL + Spring Boot + Vue 3 stack with no Java or Node required on the host, while a separate dev path (DB in Docker, backend/frontend on host) gives the team full hot reload.

**Architecture:** Multi-stage Dockerfiles compile both backend (Maven→JRE) and frontend (npm→nginx). nginx serves the Vue SPA and reverse-proxies `/api/` to the backend container, eliminating the need to expose the backend port or configure CORS. A separate `docker-compose.dev.yml` runs DB only so the team can run backend and frontend on the host with devtools/HMR.

**Tech Stack:** Docker Compose v2, eclipse-temurin:21-jre-alpine, node:20-alpine, nginx:alpine, MySQL 8

---

### Task 1: `backend/Dockerfile`

**Files:**
- Create: `backend/Dockerfile`
- Create: `backend/.dockerignore`

- [ ] **Step 1: Create `backend/.dockerignore`**

```
target/
*.md
.git
```

- [ ] **Step 2: Create `backend/Dockerfile`**

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

- [ ] **Step 3: Verify build succeeds**

Run from project root:
```bash
docker build -t nd-backend-test ./backend
```
Expected: image builds, final layer is `eclipse-temurin:21-jre-alpine` with `app.jar`.

- [ ] **Step 4: Commit**

```bash
git add backend/Dockerfile backend/.dockerignore
git commit -m "feat: add backend multi-stage Dockerfile"
```

---

### Task 2: `frontend/Dockerfile` and `frontend/nginx.conf`

**Files:**
- Create: `frontend/Dockerfile`
- Create: `frontend/nginx.conf`
- Create: `frontend/.dockerignore`

- [ ] **Step 1: Create `frontend/.dockerignore`**

```
node_modules/
dist/
*.md
.git
cypress/
```

- [ ] **Step 2: Create `frontend/nginx.conf`**

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

- [ ] **Step 3: Create `frontend/Dockerfile`**

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

- [ ] **Step 4: Verify build succeeds**

Run from project root:
```bash
docker build -t nd-frontend-test ./frontend
```
Expected: image builds, `dist/` copied into nginx, `nginx.conf` in place.

- [ ] **Step 5: Commit**

```bash
git add frontend/Dockerfile frontend/nginx.conf frontend/.dockerignore
git commit -m "feat: add frontend multi-stage Dockerfile with nginx SPA + API proxy"
```

---

### Task 3: `docker-compose.dev.yml` (DB-only for dev path)

**Files:**
- Create: `docker-compose.dev.yml`

- [ ] **Step 1: Create `docker-compose.dev.yml`**

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

- [ ] **Step 2: Verify DB-only compose starts cleanly**

Requires a `.env` file with DB credentials (copy `.env.example` if not present):
```bash
docker compose -f docker-compose.dev.yml up -d --wait
```
Expected: exits with code 0 after MySQL healthcheck passes. Check with:
```bash
docker compose -f docker-compose.dev.yml ps
```
Expected: `db` shows `healthy`.

- [ ] **Step 3: Tear down**

```bash
docker compose -f docker-compose.dev.yml down
```

- [ ] **Step 4: Commit**

```bash
git add docker-compose.dev.yml
git commit -m "feat: add docker-compose.dev.yml for DB-only dev path"
```

---

### Task 4: Update `application.yml` — add `DB_HOST`

**Files:**
- Modify: `backend/src/main/resources/application.yml:3`

Current line 3:
```
url: jdbc:mysql://localhost:${MYSQL_PORT:3306}/${DB_NAME:nettdetektivene}?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC&characterEncoding=UTF-8&useUnicode=true
```

- [ ] **Step 1: Change `localhost` to `${DB_HOST:localhost}`**

New line 3:
```yaml
    url: jdbc:mysql://${DB_HOST:localhost}:${MYSQL_PORT:3306}/${DB_NAME:nettdetektivene}?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC&characterEncoding=UTF-8&useUnicode=true
```

The `:localhost` default means: if `DB_HOST` is not set (dev path), Spring connects to `localhost`. Docker compose will set `DB_HOST=db`.

- [ ] **Step 2: Verify backend still starts in dev mode**

Start the DB first:
```bash
docker compose -f docker-compose.dev.yml up -d --wait
```

Then start backend (from `backend/` directory):
```bash
# Mac/Linux
./mvnw spring-boot:run

# Windows (CMD)
mvnw.cmd spring-boot:run
```
Expected: Spring Boot starts, Flyway migrations run, no datasource errors.

- [ ] **Step 3: Commit**

```bash
git add backend/src/main/resources/application.yml
git commit -m "fix: make DB_HOST configurable so backend works in Docker network"
```

---

### Task 5: Update `.env.example` — add `DB_HOST` and `FRONTEND_PORT`

**Files:**
- Modify: `.env.example`

- [ ] **Step 1: Add two new variables**

Append to `.env.example`:
```
DB_HOST=db
FRONTEND_PORT=5173
```

Full `.env.example` after change:
```
DB_ROOT_PASSWORD=rootpassword
DB_NAME=nettdetektivene
DB_USERNAME=appuser
DB_PASSWORD=apppassword
JWT_SECRET=change-this-to-a-very-long-random-secret-at-least-256-bits-long
MYSQL_PORT=3306
DB_HOST=db
FRONTEND_PORT=5173
```

- [ ] **Step 2: Verify `.env` copy still works for dev**

If you have a local `.env`, it does NOT need `DB_HOST` — the `application.yml` default covers it. The `.env.example` value `DB_HOST=db` is only meaningful inside Docker Compose.

- [ ] **Step 3: Commit**

```bash
git add .env.example
git commit -m "feat: add DB_HOST and FRONTEND_PORT to .env.example"
```

---

### Task 6: Update `docker-compose.yml` — add backend and frontend services

**Files:**
- Modify: `docker-compose.yml`

- [ ] **Step 1: Replace `docker-compose.yml` with full three-service version**

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
      test: ["CMD", "curl", "-f", "http://localhost:8080/swagger-ui/index.html"]
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

volumes:
  mysql_data:
```

- [ ] **Step 2: Verify full stack launches**

Requires `.env` copied from `.env.example` (update `JWT_SECRET` to any non-empty string):
```bash
cp .env.example .env
docker compose up --build
```
Expected output sequence:
1. MySQL starts and passes healthcheck
2. Backend builds (Maven), starts, passes curl healthcheck
3. Frontend builds (npm), nginx starts
4. All three show `healthy` or `running`

Open http://localhost:5173 — Vue app should load.

Open http://localhost:5173/api/auth/login with a POST body — should reach the backend (404 or 405 is fine, 502 means nginx can't reach backend).

- [ ] **Step 3: Commit**

```bash
git add docker-compose.yml
git commit -m "feat: add backend and frontend services to docker-compose.yml"
```

---

### Task 7: Replace `Makefile` with clean convenience targets

**Files:**
- Modify: `Makefile`

- [ ] **Step 1: Replace full Makefile**

```makefile
.PHONY: dev-db backend frontend start stop logs

dev-db:
	docker compose -f docker-compose.dev.yml up -d --wait

backend:
	cd backend && ./mvnw spring-boot:run

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

Note: each recipe line must be indented with a **tab** character, not spaces. The Make build will fail silently with spaces.

- [ ] **Step 2: Verify on Mac/Linux**

```bash
make dev-db
```
Expected: DB starts and passes healthcheck.

```bash
make stop
```
Expected: both compose files torn down.

- [ ] **Step 3: Commit**

```bash
git add Makefile
git commit -m "feat: update Makefile with clean dev/docker convenience targets"
```

---

### Task 8: Rewrite `README.md` setup section

**Files:**
- Modify: `README.md`

- [ ] **Step 1: Replace README.md with updated version**

```markdown
# idatt2106_2026_06 — Nettdetektivene

| Username   | Name                          |
|------------|-------------------------------|
| dennimoe   | Dennis Moe                    |
| chripre    | Christian Petter Remman       |
| kasperog   | Kasper Østerlie Gladsøy       |
| krisase    | Kristian Ask Selmer           |
| olasy      | Ola Syrstad Berg              |
| shaktios   | Shakti Om Sharma              |
| tohiggin   | Thomas Oliver Wallin Higgins  |

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
mvnw.cmd spring-boot:run

# Windows (PowerShell)
cd backend
.\mvnw.cmd spring-boot:run
```

**Step 3 — Start the frontend (Terminal 2):**
```bash
cd frontend && npm run dev
```

Mac/Linux shortcuts: `make dev-db`, `make backend`, `make frontend`

| URL | |
|-----|---|
| Frontend | http://localhost:5173 |
| Backend API | http://localhost:8080 |
| Swagger UI | http://localhost:8080/swagger-ui.html |

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
```
```

- [ ] **Step 2: Verify README renders correctly**

```bash
# View the rendered output (GitHub / GitLab will render this)
cat README.md
```

Check that both paths have copy-pasteable commands and no broken backtick blocks.

- [ ] **Step 3: Commit**

```bash
git add README.md
git commit -m "docs: rewrite README setup section with Full Docker and Dev paths"
```

---

### Task 9: End-to-end smoke test

No new files. Verifies the full system works from a clean state.

- [ ] **Step 1: Tear down any running containers**

```bash
docker compose down -v
docker compose -f docker-compose.dev.yml down -v
```

- [ ] **Step 2: Remove built images**

```bash
docker rmi nd-backend-test nd-frontend-test 2>/dev/null || true
```

- [ ] **Step 3: Run full Docker path from scratch**

```bash
docker compose up --build
```

Wait for all three services to become healthy (watch the logs). Should take 3–5 minutes on first run.

- [ ] **Step 4: Verify Vue app loads**

Open http://localhost:5173 in a browser.
Expected: Vue app loads (login page or homepage).

- [ ] **Step 5: Verify API proxy works**

```bash
curl -s -o /dev/null -w "%{http_code}" http://localhost:5173/api/auth/login -X POST -H "Content-Type: application/json" -d '{}'
```
Expected: `400` or `422` (validation error from Spring) — NOT `502` (which would mean nginx can't reach backend).

- [ ] **Step 6: Tear down**

```bash
docker compose down
```

- [ ] **Step 7: Run dev path smoke test**

```bash
docker compose -f docker-compose.dev.yml up -d --wait
```
Expected: MySQL healthy.

Start backend in background to verify it connects:
```bash
cd backend && ./mvnw spring-boot:run &
sleep 30
curl -s http://localhost:8080/swagger-ui/index.html | grep -c "swagger" && echo "backend OK"
kill %1
```
Expected: prints `backend OK`.

```bash
docker compose -f docker-compose.dev.yml down
```

- [ ] **Step 8: Final commit (if any cleanup needed)**

```bash
git add -A
git status   # verify only expected files
git commit -m "chore: docker launch system complete"
```
