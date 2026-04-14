# idatt2106_2026_06

Group members:

Dennis Moe
dennimoe

Christian Petter Remman
chripre

Kasper Østerlie Gladsøy
kasperog

Kristian Ask Selmer
krisase

Ola Syrstad Berg
olasy

Shakti Om Sharma
shaktios

Thomas Oliver Wallin Higgins
tohiggin

---

## Running locally

**Prerequisites:** Docker Desktop, Java 21, Node.js 18+

### 1. Create your `.env` file (required — do this first)

The database and backend need credentials from a `.env` file that is not committed to git.  
Copy the example and use it as-is for local dev:

```bash
# Mac / Linux
cp .env.example .env

# Windows (PowerShell)
Copy-Item .env.example .env
```

You do not need to change any values — the defaults work for local development.

### 2. First-time setup

```bash
docker-compose pull
cd frontend && npm install
```

Or with Make (Mac/Linux):

```bash
make setup
```

### 3. Start (three terminals)

**Terminal 1 — Database**

```bash
docker-compose up -d
```

**Terminal 2 — Backend**

```bash
# Mac / Linux
cd backend && ./mvnw spring-boot:run

# Windows
cd backend && mvnw.cmd spring-boot:run # WRONG it's
```

**Terminal 3 — Frontend**

```bash
cd frontend && npm run dev
```

Or start everything with Make (Mac/Linux):

```bash
make dev
```

Frontend: http://localhost:5173  
Backend API: http://localhost:8080  
Swagger UI: http://localhost:8080/swagger-ui.html

**Stop everything**

```bash
docker-compose down
```

Or: `make stop`

---

### Troubleshooting

**Backend not starting / port 8080 refused**  
Almost always a missing `.env` — go back to step 1.  
To confirm, check the backend terminal for a `Communications link failure` or `Access denied` error from MySQL.

**Mac/Linux: `Permission denied` on `./mvnw`**  
The Maven wrapper needs execute permission after a fresh clone:

```bash
chmod +x backend/mvnw
```

**Windows: `error during connect ... pipe/docker_engine`**  
Docker Desktop is not running, or it's in Windows containers mode.

1. Open Docker Desktop from the Start menu and wait for it to finish loading (whale icon in the system tray stops animating).
2. Right-click the Docker icon → _Switch to Linux containers…_ (MySQL requires Linux containers).

**Windows: `make` not found**  
`make` is not installed by default. Options (pick one):

```powershell
# Chocolatey
choco install make

# Scoop
scoop install make

# winget (built into Windows 10/11)
winget install GnuWin32.Make
```

After installing, restart your terminal. If using winget, also add `C:\Program Files (x86)\GnuWin32\bin` to your PATH.
