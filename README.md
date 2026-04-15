# idatt2106_2026_06

| Username   | Name                         |
|------------|------------------------------|
| dennimoe   | Dennis Moe                   |
| chripre    | Christian Petter Remman      |
| kasperog   | Kasper Østerlie Gladsøy      |
| krisase    | Kristian Ask Selmer          |
| olasy      | Ola Syrstad Berg             |
| shaktios   | Shakti Om Sharma             |
| tohiggin   | Thomas Oliver Wallin Higgins |

---

## Running locally

**Prerequisites:** [Docker Desktop](https://www.docker.com/products/docker-desktop/), **Java 21**, Node.js 18+

> **Java 21 is required.** Java 17 and 25 will not work — the project will fail to start.
> - Arch Linux: `sudo archlinux-java set java-21-openjdk`
> - Mac: `brew install openjdk@21`
> - Windows: download from [adoptium.net](https://adoptium.net/)

---

### 1. Create your `.env` file

Credentials are not committed to git. Copy the example once and leave the values as-is:

```bash
# Mac / Linux
cp .env.example .env

# Windows (PowerShell)
Copy-Item .env.example .env
```

---

### 2. First-time setup

```bash
docker-compose pull
cd frontend && npm install
```

Or with Make (Mac/Linux only): `make setup`

---

### 3. Start

Open three terminals in the project root:

**Terminal 1 — Database**
```bash
docker-compose up -d
```

**Terminal 2 — Backend** (wait a few seconds for the database first)
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

**Terminal 3 — Frontend**
```bash
cd frontend && npm run dev
```

Mac/Linux shortcut: `make dev`

| URL | |
|---|---|
| Frontend | http://localhost:5173 |
| Backend API | http://localhost:8080 |
| Swagger UI | http://localhost:8080/swagger-ui.html |

**Stop:** `docker-compose down` or `make stop`

---

### Troubleshooting

**`Access denied for user appuser` or `Communications link failure`**
The `.env` file is missing, or Docker initialized the database before `.env` existed. Reset:
```bash
docker-compose down -v
cp .env.example .env
docker-compose up -d
```

**`Port 8080 already in use`**
A previous backend instance is still running.
```bash
# Mac / Linux
pkill -f "spring-boot:run"

# Windows (PowerShell)
Get-Process -Name java | Stop-Process
```

**`Port 3306 already in use`**
Another MySQL is running locally (common on Mac with Homebrew).
```bash
brew services stop mysql   # Mac — kill -9 won't stick, launchd restarts it
```
Or set `MYSQL_PORT=3307` in your `.env` and restart Docker.

**`UnsupportedClassVersionError` / `class file version 65.0`**
You are running the wrong Java version. The project requires Java 21 exactly.
```bash
java -version              # check current version
sudo archlinux-java set java-21-openjdk   # Arch Linux
brew install openjdk@21                   # Mac
```
Open a new terminal after switching.

**`Permission denied` on `./mvnw` (Mac/Linux)**
```bash
chmod +x backend/mvnw
```

**Docker `pipe/docker_engine` error (Windows)**
Docker Desktop is not running or is set to Windows containers mode.
1. Open Docker Desktop and wait for it to finish loading.
2. Right-click the whale icon in the system tray → **Switch to Linux containers…**

**`make` not found (Windows)**
```powershell
choco install make        # Chocolatey
scoop install make        # Scoop
winget install GnuWin32.Make  # winget — also add C:\Program Files (x86)\GnuWin32\bin to PATH
```
