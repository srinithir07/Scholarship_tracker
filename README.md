# ScholarTrack — Scholarship Application and Eligibility Tracker

ScholarTrack is a Spring Boot backend (with a plain HTML/CSS/JavaScript dashboard) that lets students apply for
scholarship schemes, automatically checks eligibility, routes eligible applications through manual verification and
tracks disbursement. Business rules are enforced in the **service layer**, not only in the database.

## Features

- Scholarship schemes with eligibility criteria (income limit, minimum marks)
- Automatic eligibility check when a student applies — failures are flagged `INELIGIBLE` with all failing reasons
- Ineligible applications can never enter manual verification
- Verifier approves / rejects with remarks
- Disbursement is only possible after verification is `APPROVED`
- Live application status (dashboard polls the API every 10 seconds)
- Duplicate-application protection, clean JSON validation and error responses
- Sample data (5 students, 4 schemes, 8 applications) loaded on first start
- Postman collection and JUnit tests for the key business rules

## Technology stack

Java 17+ · Spring Boot 3.5 · Spring Web · Spring Data JPA / Hibernate · Jakarta Validation · Maven · MySQL (XAMPP)
· HTML · CSS · Vanilla JavaScript (`fetch`) · IntelliJ IDEA · Postman.
(Automated tests use an in-memory H2 database so they run without MySQL. H2 is `test` scope only.)

## Project structure

```
scholartrack/
├── pom.xml
├── README.md
├── .gitignore
├── database/scholartrack.sql                 # optional: schema + sample data for MySQL
├── postman/ScholarTrack.postman_collection.json
└── src/
    ├── main/
    │   ├── java/com/scholartrack/
    │   │   ├── ScholarTrackApplication.java
    │   │   ├── controller/   # REST controllers (no business logic)
    │   │   ├── service/      # business rules, eligibility engine
    │   │   ├── repository/   # Spring Data JPA repositories
    │   │   ├── entity/       # JPA entities + enums
    │   │   ├── dto/          # request / response records
    │   │   ├── exception/    # custom exceptions + GlobalExceptionHandler
    │   │   └── config/       # CORS config, DataInitializer (sample data)
    │   └── resources/
    │       ├── application.properties
    │       └── static/ (index.html, css/style.css, js/app.js)
    └── test/
        ├── java/com/scholartrack/   # BusinessRulesTest, ApiValidationTest
        └── resources/application.properties   # H2 test configuration
```

## Prerequisites

| Tool | Version |
|------|---------|
| JDK | 17 or 21 (LTS) |
| IntelliJ IDEA | Community or Ultimate (bundles Maven) |
| XAMPP | any recent version (provides MySQL/MariaDB on port 3306) |
| Postman | optional, for API testing |

### Install Java
1. Download a JDK 17 or 21 (for example Eclipse Temurin from https://adoptium.net).
2. Install it and verify in a terminal: `java -version`.

### Install / use IntelliJ IDEA
1. Download IntelliJ IDEA from https://www.jetbrains.com/idea/download (Community edition is enough).
2. Start it, choose **Open**, and select the extracted `scholartrack` folder (the one containing `pom.xml`).
3. IntelliJ detects the Maven project. If asked, choose **Trust Project** and wait for dependencies to download.
4. Set the JDK: **File → Project Structure → Project → SDK** → JDK 17 or 21.

### Install / use XAMPP
1. Download XAMPP from https://www.apachefriends.org and install it.
2. Open **XAMPP Control Panel**.
3. Click **Start** next to **MySQL** (required).
4. Click **Start** next to **Apache** only if you want phpMyAdmin (http://localhost/phpmyadmin).

## Database setup (MySQL through XAMPP)

The application connects to `localhost:3306`, database `scholartrack`, user `root`, empty password (XAMPP default).

1. Open XAMPP Control Panel and start **MySQL** (and Apache for phpMyAdmin).
2. Create the database — choose ONE option:
   - **Automatic:** do nothing. The JDBC URL contains `createDatabaseIfNotExist=true`, so the database is created on start-up.
   - **phpMyAdmin:** open http://localhost/phpmyadmin → **New** → name `scholartrack` → collation `utf8mb4_unicode_ci` → **Create**.
   - **SQL script:** in phpMyAdmin open the **Import** tab and import `database/scholartrack.sql`. It creates the database, all tables and sample data.
3. Tables are created/updated automatically by Hibernate (`spring.jpa.hibernate.ddl-auto=update`).
4. Sample data is inserted on first start only if the `students` and `schemes` tables are empty (no duplicates on restart).
   Disable it with the environment variable `SEED_SAMPLE_DATA=false`.

## Configure `application.properties`

`src/main/resources/application.properties` reads credentials from environment variables with XAMPP defaults:

```properties
spring.datasource.url=jdbc:mysql://${DB_HOST:localhost}:${DB_PORT:3306}/${DB_NAME:scholartrack}?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
spring.datasource.username=${DB_USERNAME:root}
spring.datasource.password=${DB_PASSWORD:}
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
server.port=8080
```

If your MySQL root user has a password: in IntelliJ open **Run → Edit Configurations → ScholarTrackApplication → Environment variables**
and add `DB_PASSWORD=yourpassword` (or edit the default after the colon in `application.properties`).
Other variables: `DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USERNAME`, `SEED_SAMPLE_DATA`.

## Run the application

1. Start **MySQL** in XAMPP.
2. Open the `scholartrack` folder in IntelliJ.
3. **Maven → Reload All Maven Projects** (the circular-arrows icon in the Maven tool window).
4. Open `src/main/java/com/scholartrack/ScholarTrackApplication.java` and click the green **Run** icon.
5. Wait for `Started ScholarTrackApplication`.
6. Open **http://localhost:8080/** in your browser.
7. Use Postman to test the APIs (below).

Command line alternative (requires Maven installed): `mvn spring-boot:run`

Run the tests: `mvn test` (or right-click `src/test` in IntelliJ → **Run Tests**).

## Test with Postman

1. Import `postman/ScholarTrack.postman_collection.json` (Postman → Import).
2. The collection variable `baseUrl` is `http://localhost:8080`.
3. Open **Runner**, select the collection and run it: the three folders execute in order, capture IDs automatically
   and demonstrate eligible/ineligible applications, invalid verification, premature disbursement (HTTP 400),
   approval, successful disbursement and rejection.
   You can also run each request manually in order.

## API endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/students` | Create student |
| GET | `/api/students` | List students |
| GET | `/api/students/{id}` | Get student |
| PUT | `/api/students/{id}` | Update student |
| DELETE | `/api/students/{id}` | Delete student (409 if it has applications) |
| POST | `/api/schemes` | Create scheme |
| GET | `/api/schemes` | List schemes |
| GET | `/api/schemes/active` | List ACTIVE schemes |
| GET | `/api/schemes/{id}` | Get scheme |
| PUT | `/api/schemes/{id}` | Update scheme |
| DELETE | `/api/schemes/{id}` | Delete scheme (409 if it has applications) |
| POST | `/api/applications` | Apply `{ "studentId": 1, "schemeId": 1 }` — runs the eligibility engine |
| GET | `/api/applications` | List applications |
| GET | `/api/applications/{id}` | Get application (real-time status) |
| GET | `/api/applications/student/{studentId}` | Applications of one student |
| GET | `/api/applications/status/{status}` | Filter by application status |
| PUT | `/api/applications/{id}/review` | Move ELIGIBLE application to UNDER_REVIEW |
| PUT | `/api/applications/{id}/disburse` | Mark DISBURSED (only if verification APPROVED) |
| POST | `/api/verifications` | Record decision `{ "applicationId", "verifiedBy", "status": "APPROVED\|REJECTED", "remarks" }` |
| GET | `/api/verifications` | List verifications |
| GET | `/api/verifications/{id}` | Get verification |
| PUT | `/api/verifications/{id}` | Change a decision (not allowed after DISBURSED) |
| GET | `/api/dashboard/stats` | Dashboard counters |

### Request examples

```json
POST /api/students
{ "name": "Asha Nair", "email": "asha@example.com", "phone": "9876543210",
  "annualIncome": 180000, "marks": 82.5, "course": "B.E. Computer Science", "year": 2 }

POST /api/schemes
{ "name": "Merit Scholarship", "description": "For top scorers", "incomeLimit": 300000, "minimumMarks": 75, "status": "ACTIVE" }
```

### Error format

```json
{
  "timestamp": "2026-09-28T12:00:00",
  "status": 400,
  "error": "Business Rule Violation",
  "message": "Disbursement cannot be completed until verification is approved."
}
```

Validation errors additionally contain an `errors` object with one message per invalid field.

| HTTP | Meaning |
|------|---------|
| 400 | Validation failure, malformed JSON, business rule violation (ineligible verification, premature disbursement) |
| 404 | Student / scheme / application / verification not found |
| 409 | Duplicate application or email, invalid status transition, deleting referenced records |
| 500 | Unexpected error (no stack trace is exposed) |

## Database schema

| Table | Columns |
|-------|---------|
| `students` | id, name, email (unique), phone, annual_income, marks, course, study_year, created_at |
| `schemes` | id, name (unique), description, income_limit, minimum_marks, status, created_at |
| `applications` | id, student_id → students, scheme_id → schemes, application_date, eligibility_status, application_status, eligibility_remarks, created_at, updated_at — unique (student_id, scheme_id) |
| `verifications` | id, application_id → applications (unique), verified_by, verification_date, status, remarks |

Relationships: Student 1—N Application, Scheme 1—N Application, Application 1—1 Verification.
Back-references are `@JsonIgnore`d and the API returns DTOs, so there is no infinite JSON recursion.

Enums: `EligibilityStatus` (ELIGIBLE, INELIGIBLE), `ApplicationStatus` (SUBMITTED, UNDER_REVIEW, APPROVED, REJECTED,
DISBURSEMENT_PENDING, DISBURSED), `VerificationStatus` (PENDING, APPROVED, REJECTED), `SchemeStatus` (ACTIVE, INACTIVE).

## Business rules

- **Eligibility:** `annualIncome <= incomeLimit` AND `marks >= minimumMarks`. Otherwise the application is stored with
  `eligibilityStatus = INELIGIBLE` and remarks such as *"Student is not eligible: annual income exceeds the permitted limit and marks are below the minimum requirement."*
  It stays `SUBMITTED` and is flagged before any manual review.
- **Rule 1:** an ineligible application cannot be moved to `UNDER_REVIEW` and cannot be verified (HTTP 400).
- **Rule 2:** `PUT /api/applications/{id}/disburse` returns HTTP 400 unless the verification is `APPROVED`.
- **Duplicates:** a student can have only one application per scheme (HTTP 409).
- Only ACTIVE schemes accept applications.
- Approving a verification moves the application to `DISBURSEMENT_PENDING`; rejecting moves it to `REJECTED`.
- A decision can be changed until the scholarship is `DISBURSED`.

All rules live in `ApplicationService`, `VerificationService` and `EligibilityService`.

## Sample workflow

1. Open http://localhost:8080/ → **Dashboard** shows counters for the sample data.
2. **Applications → Apply for Scholarship**: choose a student and scheme. The result is `ELIGIBLE` or `INELIGIBLE` immediately.
3. On an eligible `SUBMITTED` application click **Start Review** → status becomes `UNDER REVIEW`.
4. Click **Verify** (or **Verification → Review Application**), enter the verifier name, choose Approve/Reject, add remarks.
5. Approved → `DISBURSEMENT PENDING`; click **Disburse** → `DISBURSED`.
6. Try **Start Review** on an ineligible application, or **Disburse** on an application that is only under review — the server answers with a clear error message shown in the UI.

Sample data included: eligible, ineligible (income), ineligible (marks), under review, approved verification, rejected verification, disbursed.

## Troubleshooting

| Problem | Fix |
|---------|-----|
| `Communications link failure` / `Connection refused` | MySQL is not running. Start **MySQL** in XAMPP; check port 3306 (XAMPP shows the port). |
| `Access denied for user 'root'` | Set `DB_PASSWORD` (environment variable) to your MySQL root password. |
| `Port 8080 was already in use` | Stop the other app or change `server.port` in `application.properties`. |
| Maven cannot resolve dependencies | Check internet access, then **Maven → Reload All Maven Projects**. |
| `release version 17 not supported` | Set the project SDK/JDK to 17 or 21 (File → Project Structure). |
| Dashboard shows “Cannot reach the server” | The Spring Boot app is not running, or you are not using http://localhost:8080/. |
| Hibernate dialect / MariaDB warnings | XAMPP ships MariaDB; it works with the MySQL driver. If needed, add `spring.jpa.database-platform=org.hibernate.dialect.MariaDBDialect`. |
| Want a clean database | Drop the `scholartrack` database in phpMyAdmin and restart the app (sample data is re-created). |
| Sample data missing | It only loads when `students` and `schemes` are empty and `SEED_SAMPLE_DATA` is not `false`. |
