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

**Prerequisites:** Docker, Java 21, Node.js 18+

Open three terminals:

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
cd frontend && npm install && npm run dev
```

Frontend: http://localhost:5173  
Backend API: http://localhost:8080  
Swagger UI: http://localhost:8080/swagger-ui.html

**Stop everything**
```bash
docker-compose down
```
