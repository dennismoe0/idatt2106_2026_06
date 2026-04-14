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

### First-time setup

Pull the Docker image and install frontend dependencies before running for the first time:

```bash
docker-compose pull
cd frontend && npm install
```

Or with Make:
```bash
make setup
```

---

### Start (three terminals)

**Terminal 1 — Database**
```bash
docker-compose up -d
```

**Terminal 2 — Backend**
```bash
# Mac / Linux
cd backend && ./mvnw spring-boot:run

# Windows
cd backend && mvnw.cmd spring-boot:run
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

### Windows — using `make`

`make` is not installed on Windows by default. Pick one option:

**Option A — Chocolatey** (if you have it):
```powershell
choco install make
```

**Option B — Scoop** (if you have it):
```powershell
scoop install make
```

**Option C — winget** (built into Windows 10/11):
```powershell
winget install GnuWin32.Make
```
Then add `C:\Program Files (x86)\GnuWin32\bin` to your PATH:  
Settings → System → About → Advanced system settings → Environment Variables → Path → New

After installing, restart your terminal and run `make setup` / `make dev` as normal.

---

### Windows — Docker "cannot find pipe" error

If you see an error like `error during connect ... pipe/docker_engine` or similar:

1. **Open Docker Desktop** — it must be running before any `docker` command works. Look for the whale icon in the system tray.
2. **Switch to Linux containers** — right-click the Docker icon in the system tray → *Switch to Linux containers…* (MySQL requires Linux containers; Windows containers mode will not work).
3. **Wait for Docker to finish starting** — the icon animates while it loads. Wait until it's steady before running `docker-compose up -d`.
4. If Docker Desktop is not installed: download from https://www.docker.com/products/docker-desktop/
