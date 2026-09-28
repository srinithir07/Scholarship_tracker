-- =====================================================================
-- ScholarTrack - MySQL / MariaDB (XAMPP) setup script
-- Creates the database, tables and demo data.
-- Safe to run more than once (IF NOT EXISTS / INSERT IGNORE).
-- Import through phpMyAdmin (Import tab) or run: mysql -u root < scholartrack.sql
-- NOTE: The Spring Boot app also creates the tables automatically
--       (spring.jpa.hibernate.ddl-auto=update) and only seeds demo data when the
--       students/schemes tables are empty, so running this script first is optional.
-- =====================================================================

CREATE DATABASE IF NOT EXISTS scholartrack
    CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE scholartrack;

CREATE TABLE IF NOT EXISTS students (
    id            BIGINT        NOT NULL AUTO_INCREMENT,
    name          VARCHAR(100)  NOT NULL,
    email         VARCHAR(150)  NOT NULL,
    phone         VARCHAR(20)   NOT NULL,
    annual_income DECIMAL(14,2) NOT NULL,
    marks         DECIMAL(5,2)  NOT NULL,
    course        VARCHAR(100)  NOT NULL,
    study_year    INT           NOT NULL,
    created_at    DATETIME(6)   NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_students_email (email)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS schemes (
    id            BIGINT        NOT NULL AUTO_INCREMENT,
    name          VARCHAR(150)  NOT NULL,
    description   VARCHAR(1000) NULL,
    income_limit  DECIMAL(14,2) NOT NULL,
    minimum_marks DECIMAL(5,2)  NOT NULL,
    status        VARCHAR(20)   NOT NULL,
    created_at    DATETIME(6)   NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_schemes_name (name)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS applications (
    id                  BIGINT       NOT NULL AUTO_INCREMENT,
    student_id          BIGINT       NOT NULL,
    scheme_id           BIGINT       NOT NULL,
    application_date    DATE         NOT NULL,
    eligibility_status  VARCHAR(20)  NOT NULL,
    application_status  VARCHAR(30)  NOT NULL,
    eligibility_remarks VARCHAR(500) NULL,
    created_at          DATETIME(6)  NOT NULL,
    updated_at          DATETIME(6)  NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_application_student_scheme (student_id, scheme_id),
    CONSTRAINT fk_applications_student FOREIGN KEY (student_id) REFERENCES students (id),
    CONSTRAINT fk_applications_scheme  FOREIGN KEY (scheme_id)  REFERENCES schemes (id)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS verifications (
    id                BIGINT        NOT NULL AUTO_INCREMENT,
    application_id    BIGINT        NOT NULL,
    verified_by       VARCHAR(100)  NULL,
    verification_date DATETIME(6)   NULL,
    status            VARCHAR(20)   NOT NULL,
    remarks           VARCHAR(1000) NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_verifications_application (application_id),
    CONSTRAINT fk_verifications_application FOREIGN KEY (application_id) REFERENCES applications (id)
) ENGINE=InnoDB;

-- ---------------------------------------------------------------------
-- Sample students (5)
-- ---------------------------------------------------------------------
INSERT IGNORE INTO students (id, name, email, phone, annual_income, marks, course, study_year, created_at) VALUES
 (1, 'Aarav Krishnan', 'aarav.krishnan@example.com', '9876543210', 150000.00, 88.50, 'B.Tech AI & ML', 3, NOW(6)),
 (2, 'Priya Sharma',   'priya.sharma@example.com',   '9876501234', 240000.00, 92.00, 'B.E. Computer Science', 2, NOW(6)),
 (3, 'Karthik Rajan',  'karthik.rajan@example.com',  '9876512345', 620000.00, 81.00, 'B.E. Mechanical Engineering', 4, NOW(6)),
 (4, 'Meena Lakshmi',  'meena.lakshmi@example.com',  '9876523456',  90000.00, 54.00, 'B.Sc Data Science', 1, NOW(6)),
 (5, 'Rahul Verma',    'rahul.verma@example.com',    '9876534567', 180000.00, 76.00, 'B.Tech Information Technology', 3, NOW(6));

-- ---------------------------------------------------------------------
-- Sample scholarship schemes (4)
-- ---------------------------------------------------------------------
INSERT IGNORE INTO schemes (id, name, description, income_limit, minimum_marks, status, created_at) VALUES
 (1, 'Merit Excellence Scholarship', 'Rewards high-scoring students from families with an annual income up to 5,00,000.', 500000.00, 80.00, 'ACTIVE', NOW(6)),
 (2, 'Need-Based Education Grant', 'Financial support for students from low-income families who maintain good academic standing.', 200000.00, 60.00, 'ACTIVE', NOW(6)),
 (3, 'STEM Achievers Scholarship', 'Encourages students in engineering and science programmes with strong academic records.', 350000.00, 70.00, 'ACTIVE', NOW(6)),
 (4, 'First-Generation Learner Award', 'Supports first-generation college students from economically weaker families.', 120000.00, 50.00, 'ACTIVE', NOW(6));

-- ---------------------------------------------------------------------
-- Sample applications (8)
--  1 Aarav / Merit      ELIGIBLE    DISBURSED
--  2 Aarav / Need-Based ELIGIBLE    SUBMITTED
--  3 Priya / Merit      ELIGIBLE    DISBURSEMENT_PENDING (verification approved)
--  4 Priya / STEM       ELIGIBLE    UNDER_REVIEW (verification pending)
--  5 Karthik / Need     INELIGIBLE  (income above limit)
--  6 Meena / STEM       INELIGIBLE  (marks below minimum)
--  7 Rahul / STEM       ELIGIBLE    REJECTED (verification rejected)
--  8 Rahul / Merit      INELIGIBLE  (marks below minimum)
-- ---------------------------------------------------------------------
INSERT IGNORE INTO applications (id, student_id, scheme_id, application_date, eligibility_status, application_status, eligibility_remarks, created_at, updated_at) VALUES
 (1, 1, 1, DATE_SUB(CURDATE(), INTERVAL 30 DAY), 'ELIGIBLE',   'DISBURSED',            'Student is eligible for the scholarship.', NOW(6), NOW(6)),
 (2, 1, 2, DATE_SUB(CURDATE(), INTERVAL 2 DAY),  'ELIGIBLE',   'SUBMITTED',            'Student is eligible for the scholarship.', NOW(6), NOW(6)),
 (3, 2, 1, DATE_SUB(CURDATE(), INTERVAL 12 DAY), 'ELIGIBLE',   'DISBURSEMENT_PENDING', 'Student is eligible for the scholarship.', NOW(6), NOW(6)),
 (4, 2, 3, DATE_SUB(CURDATE(), INTERVAL 5 DAY),  'ELIGIBLE',   'UNDER_REVIEW',         'Student is eligible for the scholarship.', NOW(6), NOW(6)),
 (5, 3, 2, DATE_SUB(CURDATE(), INTERVAL 8 DAY),  'INELIGIBLE', 'SUBMITTED',            'Student is not eligible: annual income exceeds the permitted limit.', NOW(6), NOW(6)),
 (6, 4, 3, DATE_SUB(CURDATE(), INTERVAL 6 DAY),  'INELIGIBLE', 'SUBMITTED',            'Student is not eligible: marks are below the minimum requirement.', NOW(6), NOW(6)),
 (7, 5, 3, DATE_SUB(CURDATE(), INTERVAL 20 DAY), 'ELIGIBLE',   'REJECTED',             'Student is eligible for the scholarship.', NOW(6), NOW(6)),
 (8, 5, 1, DATE_SUB(CURDATE(), INTERVAL 3 DAY),  'INELIGIBLE', 'SUBMITTED',            'Student is not eligible: marks are below the minimum requirement.', NOW(6), NOW(6));

-- ---------------------------------------------------------------------
-- Sample verifications (approved x2, pending x1, rejected x1)
-- ---------------------------------------------------------------------
INSERT IGNORE INTO verifications (id, application_id, verified_by, verification_date, status, remarks) VALUES
 (1, 1, 'Dr. S. Ramesh Kumar', DATE_SUB(NOW(6), INTERVAL 1 DAY), 'APPROVED', 'All documents verified. Income certificate and mark sheet are authentic.'),
 (2, 3, 'Prof. Anitha Devi',   DATE_SUB(NOW(6), INTERVAL 1 DAY), 'APPROVED', 'Documents verified. Approved for disbursement.'),
 (3, 4, NULL, NULL, 'PENDING', NULL),
 (4, 7, 'Dr. S. Ramesh Kumar', DATE_SUB(NOW(6), INTERVAL 1 DAY), 'REJECTED', 'Income certificate is not attested by a competent authority.');
