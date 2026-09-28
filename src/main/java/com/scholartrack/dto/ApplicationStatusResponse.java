package com.scholartrack.dto;

import com.scholartrack.entity.ApplicationStatus;
import com.scholartrack.entity.DisbursementStatus;
import com.scholartrack.entity.EligibilityStatus;
import com.scholartrack.entity.VerificationStatus;

import java.time.LocalDateTime;

public class ApplicationStatusResponse {

    private Long applicationId;
    private LocalDateTime applicationDate;
    
    // Student Info
    private Long studentId;
    private String studentName;
    private String studentEmail;
    private Double studentMarks;
    private Double studentAnnualIncome;

    // Scheme Info
    private Long schemeId;
    private String schemeName;
    private Double schemeIncomeLimit;
    private Double schemeMinimumMarks;

    // Statuses
    private EligibilityStatus eligibilityStatus;
    private ApplicationStatus applicationStatus;
    private VerificationStatus verificationStatus;
    private String verificationRemarks;
    private DisbursementStatus disbursementStatus;

    public ApplicationStatusResponse() {
    }

    // Getters and Setters
    public Long getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(Long applicationId) {
        this.applicationId = applicationId;
    }

    public LocalDateTime getApplicationDate() {
        return applicationDate;
    }

    public void setApplicationDate(LocalDateTime applicationDate) {
        this.applicationDate = applicationDate;
    }

    public Long getStudentId() {
        return studentId;
    }

    public void setStudentId(Long studentId) {
        this.studentId = studentId;
    }

    public String getStudentName() {
        return studentName;
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }

    public String getStudentEmail() {
        return studentEmail;
    }

    public void setStudentEmail(String studentEmail) {
        this.studentEmail = studentEmail;
    }

    public Double getStudentMarks() {
        return studentMarks;
    }

    public void setStudentMarks(Double studentMarks) {
        this.studentMarks = studentMarks;
    }

    public Double getStudentAnnualIncome() {
        return studentAnnualIncome;
    }

    public void setStudentAnnualIncome(Double studentAnnualIncome) {
        this.studentAnnualIncome = studentAnnualIncome;
    }

    public Long getSchemeId() {
        return schemeId;
    }

    public void setSchemeId(Long schemeId) {
        this.schemeId = schemeId;
    }

    public String getSchemeName() {
        return schemeName;
    }

    public void setSchemeName(String schemeName) {
        this.schemeName = schemeName;
    }

    public Double getSchemeIncomeLimit() {
        return schemeIncomeLimit;
    }

    public void setSchemeIncomeLimit(Double schemeIncomeLimit) {
        this.schemeIncomeLimit = schemeIncomeLimit;
    }

    public Double getSchemeMinimumMarks() {
        return schemeMinimumMarks;
    }

    public void setSchemeMinimumMarks(Double schemeMinimumMarks) {
        this.schemeMinimumMarks = schemeMinimumMarks;
    }

    public EligibilityStatus getEligibilityStatus() {
        return eligibilityStatus;
    }

    public void setEligibilityStatus(EligibilityStatus eligibilityStatus) {
        this.eligibilityStatus = eligibilityStatus;
    }

    public ApplicationStatus getApplicationStatus() {
        return applicationStatus;
    }

    public void setApplicationStatus(ApplicationStatus applicationStatus) {
        this.applicationStatus = applicationStatus;
    }

    public VerificationStatus getVerificationStatus() {
        return verificationStatus;
    }

    public void setVerificationStatus(VerificationStatus verificationStatus) {
        this.verificationStatus = verificationStatus;
    }

    public String getVerificationRemarks() {
        return verificationRemarks;
    }

    public void setVerificationRemarks(String verificationRemarks) {
        this.verificationRemarks = verificationRemarks;
    }

    public DisbursementStatus getDisbursementStatus() {
        return disbursementStatus;
    }

    public void setDisbursementStatus(DisbursementStatus disbursementStatus) {
        this.disbursementStatus = disbursementStatus;
    }
}
