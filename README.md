# QueueCare — Clinic Token & Queue System

A full-stack clinic queue assignment built with **Java 21, Spring Boot 3, Spring Data JPA, MySQL, React, Vite and JavaScript**. This project uses a locally installed MySQL server; **Docker is not used or required**.

## Features
- Register patients with name, age, phone and doctor; each doctor has its own token sequence that restarts daily.
- Doctor actions: Call Next, Mark Done, Skip and Recall skipped patients.
- Waiting-room display: current token and next three waiting patients for each doctor, auto-refreshing every 8 seconds.
- Estimated wait time and queue position for waiting patients, based on that doctor's average completed consultation duration (10-minute default before the first completion).
- Daily report: registered, seen, waiting, skipped and average consultation duration per doctor.
- Age 60+ priority patients with alternating priority/regular selection when both groups are waiting.
- Phone-number search and call notification sound.
- Data persists in MySQL; three starter doctors are inserted automatically on first backend start.

## Requirements
1. Java JDK 21
2. Apache Maven 3.9+ (or use your IDE's Maven support)
3. MySQL Server 8.x and MySQL Workbench (optional GUI)
4. Node.js 20+ and npm

## Step 1 — Create the MySQL database (no Docker)
1. Open MySQL Workbench and connect to your local MySQL server.
2. Open `database/queuecare.sql` and execute it, or run:

```sql
CREATE DATABASE IF NOT EXISTS queuecare CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

3. Open `backend/src/main/resources/application.properties` and set the username/password for your local MySQL installation. The defaults are `root` and an empty password. Example if your MySQL root password is `admin123`:

```properties
spring.datasource.username=root
spring.datasource.password=admin123
```

Do not include quotes around the password. You can alternatively set environment variables `DB_USERNAME` and `DB_PASSWORD`.

Hibernate automatically creates/updates the `doctors` and `patients` tables. Three sample doctors are inserted the first time the backend starts.

## Step 2 — Run Spring Boot (Java 21)
Open a terminal in the project folder:

```bash
cd backend
mvn spring-boot:run
```

Confirm Java with `java -version` and Maven with `mvn -version`. Both should show Java 21. Backend API: `http://localhost:8080/api/health`.

## Step 3 — Run React
Open a second terminal:

```bash
cd frontend
npm install
npm run dev
```

Open the local URL printed by Vite, normally `http://localhost:5173`.

## Common local MySQL issues
- `Access denied for user`: correct the username/password in `application.properties`.
- `Unknown database 'queuecare'`: run `database/queuecare.sql` in Workbench.
- `Communications link failure`: start the MySQL80/MySQL server service and confirm port 3306.
- `Port 8080 already in use`: stop the other process or change `server.port`.

## API endpoints
- `GET /api/health`
- `GET /api/doctors`
- `POST /api/patients` — JSON: `{ "name":"Asha", "age":28, "phone":"9876543210", "doctorId":1 }`
- `GET /api/queue`
- `POST /api/doctors/{doctorId}/call-next`
- `POST /api/patients/{id}/done`
- `POST /api/patients/{id}/skip`
- `POST /api/patients/{id}/recall`
- `GET /api/display`
- `GET /api/reports/daily`
- `GET /api/patients/search?phone=9876543210`

## Folder structure
- `backend/` — Spring Boot REST API, JPA entities, repositories, services and controllers.
- `frontend/` — React/Vite user interface.
- `database/queuecare.sql` — SQL database creation script for MySQL Workbench.

## Demo checklist
1. Register 2–3 patients for different doctors.
2. Open Doctor screen and Call Next.
3. Mark one patient Done, skip one patient, and Recall the skipped patient.
4. Open Waiting room and verify the current and next tokens update.
5. Open Daily report and verify counts and average consultation duration.

This is an assignment/demo app, not a production-ready medical records system. Add authentication, role-based access, auditing, backups and appropriate privacy controls before using real patient data.
