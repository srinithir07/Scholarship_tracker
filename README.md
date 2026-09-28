# ScholarTrack — Scholarship Application and Eligibility Tracker

ScholarTrack is a clean, modern, college-level Spring Boot web application for managing scholarship schemes, student applications, automated eligibility checks, verifier document reviews, and fund disbursements.

---

## Key Features

1. **Scholarship Scheme Management**: Define schemes with eligibility criteria (minimum marks and annual family income limit).
2. **Student Applications & Automated Eligibility**: Students apply for schemes. The service layer automatically checks student marks and income against scheme criteria before forwarding for manual verification (Rule 1).
3. **Verifier Review Queue**: Verifiers inspect eligible applications, approve or reject them, and record detailed remarks.
4. **Disbursement Tracking & Strict Enforcement**: Tracks payout status (`NOT_STARTED`, `PROCESSING`, `COMPLETED`). Ensures `COMPLETED` status can ONLY be set after verification approval (Rule 2).
5. **Real-time Application Status Tracking**: Search by Application ID to view instant status summaries across all workflow stages.
6. **Modern Single-Page Dashboard**: Aesthetic UI built with clean HTML, CSS, and Vanilla JavaScript (served directly by Spring Boot).

---

## Business Rules

* **Rule 1 (Eligibility Evaluation)**:
  - If `Student Marks >= Scheme Minimum Marks` AND `Student Annual Income <= Scheme Income Limit`:
    `eligibilityStatus = ELIGIBLE`, `applicationStatus = SUBMITTED`
  - If student fails either condition:
    `eligibilityStatus = NOT_ELIGIBLE`, `applicationStatus = REJECTED`
  - Ineligible applications are flagged BEFORE manual review and cannot proceed to verification.

* **Rule 2 (Disbursement Restriction)**:
  - Attempting to set `disbursementStatus = COMPLETED` while `verificationStatus != APPROVED` is rejected with `400 BAD REQUEST` and a clear error message.

---

## Technology Stack

- **Java Version**: 21
- **Framework**: Spring Boot 3.2.5
- **Spring Modules**: Spring Web, Spring Data JPA, Jakarta Validation
- **Database**: MySQL (XAMPP default configuration: `localhost:3306`, user `root`, no password)
- **In-Memory Database**: H2 (used for instant automated unit & integration test suites)
- **Build Tool**: Apache Maven
- **Frontend**: HTML5, Vanilla CSS3, Vanilla JavaScript (Fetch API)
- **API Testing**: Postman Collection included

---

## Project Structure

```
ScholarTrack/
│
├── pom.xml
├── README.md
├── database/
│   └── schema.sql
├── postman/
│   └── ScholarTrack.postman_collection.json
│
└── src/
    ├── main/
    │   ├── java/
    │   │   └── com/
    │   │       └── scholartrack/
    │   │           ├── ScholarTrackApplication.java
    │   │           ├── config/
    │   │           │   └── DataInitializer.java
    │   │           ├── controller/
    │   │           │   ├── ApplicationController.java
    │   │           │   ├── SchemeController.java
    │   │           │   ├── StudentController.java
    │   │           │   └── VerificationController.java
    │   │           ├── dto/
    │   │           │   ├── ApplicationRequest.java
    │   │           │   ├── ApplicationStatusResponse.java
    │   │           │   ├── DisbursementUpdateRequest.java
    │   │           │   └── VerificationRequest.java
    │   │           ├── entity/
    │   │           │   ├── Application.java
    │   │           │   ├── ApplicationStatus.java
    │   │           │   ├── DisbursementStatus.java
    │   │           │   ├── EligibilityStatus.java
    │   │           │   ├── Scheme.java
    │   │           │   ├── Student.java
    │   │           │   ├── Verification.java
    │   │           │   └── VerificationStatus.java
    │   │           ├── exception/
    │   │           │   ├── BusinessRuleException.java
    │   │           │   ├── ErrorResponse.java
    │   │           │   ├── GlobalExceptionHandler.java
    │   │           │   └── ResourceNotFoundException.java
    │   │           ├── repository/
    │   │           │   ├── ApplicationRepository.java
    │   │           │   ├── SchemeRepository.java
    │   │           │   ├── StudentRepository.java
    │   │           │   └── VerificationRepository.java
    │   │           └── service/
    │   │               ├── ApplicationService.java
    │   │               ├── SchemeService.java
    │   │               ├── StudentService.java
    │   │               └── VerificationService.java
    │   │
    │   └── resources/
    │       ├── application.properties
    │       └── static/
    │           ├── index.html
    │           ├── css/
    │           │   └── style.css
    │           └── js/
    │               └── app.js
    │
    └── test/
        ├── java/
        │   └── com/
        │       └── scholartrack/
        │           └── ScholarTrackBusinessLogicTest.java
        └── resources/
            └── application-test.properties
```

---

## Quick Setup & Execution Guide

### Prerequisites
1. Java 21 JDK installed.
2. XAMPP Control Panel (or MySQL Server running locally).
3. IntelliJ IDEA or standard terminal with Maven.

### Step 1: Database Setup (XAMPP / MySQL)
1. Open XAMPP Control Panel and start **MySQL**.
2. Open phpMyAdmin (`http://localhost/phpmyadmin`) or MySQL CLI.
3. Create database:
   ```sql
   CREATE DATABASE scholartrack_db;
   ```
   *(Note: You can also execute the DDL script in `database/schema.sql` if manual creation is preferred, though Hibernate `spring.jpa.hibernate.ddl-auto=update` will generate tables automatically).*

### Step 2: Build & Run Application
Open project terminal and run:

- **Run Automated Tests**:
  ```bash
  mvn clean test
  ```

- **Run Spring Boot Application**:
  ```bash
  mvn spring-boot:run
  ```

### Step 3: Access Frontend Interface
Open your browser and navigate to:
```
http://localhost:8080
```

---

## REST API Endpoints Summary

| Method | Endpoint | Description |
| :--- | :--- | :--- |
| `GET` | `/api/students` | Get list of all registered students |
| `GET` | `/api/students/{id}` | Get student details by ID |
| `POST` | `/api/students` | Register a new student profile |
| `PUT` | `/api/students/{id}` | Update student details |
| `DELETE` | `/api/students/{id}` | Remove student record |
| `GET` | `/api/schemes` | List all scholarship schemes |
| `POST` | `/api/schemes` | Create a new scholarship scheme |
| `POST` | `/api/applications` | Submit application (Triggers Rule 1 eligibility check) |
| `GET` | `/api/applications` | List all submitted applications |
| `GET` | `/api/applications/{id}/status` | Get detailed summary breakdown of application status |
| `POST` | `/api/verifications` | Approve/Reject application with remarks |
| `PUT` | `/api/applications/{id}/disbursement` | Update disbursement status (Enforces Rule 2) |

---

## Postman Testing

Import `postman/ScholarTrack.postman_collection.json` into Postman to test all endpoints.

---

## Troubleshooting

1. **Access denied for user 'root'@'localhost'**:
   - Verify XAMPP MySQL password settings in `src/main/resources/application.properties`.

2. **Database scholartrack_db does not exist**:
   - Ensure MySQL service is running in XAMPP and run `CREATE DATABASE scholartrack_db;`.

3. **Disbursement update returns 400 Bad Request**:
   - This occurs when trying to mark disbursement as `COMPLETED` before the verifier has `APPROVED` the application. Approve the application first via `/api/verifications`.
