-- ScholarTrack Database Creation Script
-- Execute in MySQL / XAMPP phpMyAdmin

CREATE DATABASE IF NOT EXISTS scholartrack_db;
USE scholartrack_db;

-- Table 1: Students
CREATE TABLE IF NOT EXISTS students (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    phone VARCHAR(50) NOT NULL,
    marks DOUBLE NOT NULL,
    annual_income DOUBLE NOT NULL,
    course VARCHAR(255) NOT NULL,
    college VARCHAR(255) NOT NULL
);

-- Table 2: Schemes
CREATE TABLE IF NOT EXISTS schemes (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT NOT NULL,
    income_limit DOUBLE NOT NULL,
    minimum_marks DOUBLE NOT NULL,
    status VARCHAR(50) DEFAULT 'ACTIVE'
);

-- Table 3: Applications
CREATE TABLE IF NOT EXISTS applications (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    student_id BIGINT NOT NULL,
    scheme_id BIGINT NOT NULL,
    application_date DATETIME DEFAULT CURRENT_TIMESTAMP,
    eligibility_status VARCHAR(50) NOT NULL,
    application_status VARCHAR(50) NOT NULL,
    disbursement_status VARCHAR(50) NOT NULL,
    FOREIGN KEY (student_id) REFERENCES students(id) ON DELETE CASCADE,
    FOREIGN KEY (scheme_id) REFERENCES schemes(id) ON DELETE CASCADE
);

-- Table 4: Verifications
CREATE TABLE IF NOT EXISTS verifications (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    application_id BIGINT NOT NULL,
    verification_status VARCHAR(50) NOT NULL,
    remarks TEXT,
    verified_date DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (application_id) REFERENCES applications(id) ON DELETE CASCADE
);
