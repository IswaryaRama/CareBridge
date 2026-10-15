# CarePoint Hospital Management System (Learning Project)

A Java Spring Boot web application with a responsive browser UI and a persistent local SQLite database.

## Features
- Dashboard metrics: patients, doctors, appointments, unpaid bills and collected revenue
- Patient registration, list, update and delete
- Doctor creation, listing and delete
- Appointment scheduling, status changes (Scheduled / Completed / Cancelled), delete
- Billing, payment status updates (Paid / Unpaid), delete
- Input checks and HTTP error responses
- SQLite database file created automatically as `hospital.db`

## Requirements
- JDK 17 or newer (Java 17 or 21 LTS is recommended for beginner projects)
- Maven 3.9+ (or use the Maven path shown below)
- Internet connection for the first build to download dependencies
- No MySQL or MySQL Workbench required

## Run on Windows PowerShell
1. Extract the ZIP.
2. Open PowerShell in the folder that contains `pom.xml`.
3. Confirm `dir pom.xml` shows the file.
4. Run `mvn -version`.
5. Start the application:
   ```powershell
   mvn spring-boot:run
   ```
   If Maven is not on PATH, use your installation path, for example:
   ```powershell
   & "C:\Tools\apache-maven-3.10.0\bin\mvn.cmd" spring-boot:run
   ```
6. Open http://localhost:8080
7. Stop the app with Ctrl+C. Data is saved to `hospital.db` in the folder where you started the app.

## First-time use
1. Open Doctors and add at least one doctor.
2. Open Patients and register a patient.
3. Open Appointments and schedule a visit.
4. Open Billing and create a bill for the patient.
5. Mark the bill as paid to see collected revenue update on the dashboard.

## REST API overview
- `GET /api/dashboard`
- `GET, POST /api/patients`; `PUT, DELETE /api/patients/{id}`
- `GET, POST /api/doctors`; `PUT, DELETE /api/doctors/{id}` (the UI currently exposes create/list/delete)
- `GET, POST /api/appointments`; `PUT /api/appointments/{id}/status`; `DELETE /api/appointments/{id}`
- `GET, POST /api/bills`; `PUT /api/bills/{id}/payment`; `DELETE /api/bills/{id}`

## Important limitations
This is an educational local project, not a production clinical system. It does not yet include authentication/authorization, automated tests, audit logs, encrypted backups, consent/privacy workflows, or production deployment hardening. Do not enter real patient medical data. SQLite is useful for a local demo; a multi-user production deployment usually needs a managed server database and security review.
