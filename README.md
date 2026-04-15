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
| Swagger UI | http://localhost:8080/swagger-ui/index.html |

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
