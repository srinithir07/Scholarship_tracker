package com.scholartrack.dto;

import com.scholartrack.entity.VerificationStatus;
import jakarta.validation.constraints.NotNull;

public class VerificationRequest {

    @NotNull(message = "Application ID is required")
    private Long applicationId;

    @NotNull(message = "Verification status is required")
    private VerificationStatus verificationStatus;

    private String remarks;

    public VerificationRequest() {
    }

    public VerificationRequest(Long applicationId, VerificationStatus verificationStatus, String remarks) {
        this.applicationId = applicationId;
        this.verificationStatus = verificationStatus;
        this.remarks = remarks;
    }

    public Long getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(Long applicationId) {
        this.applicationId = applicationId;
    }

    public VerificationStatus getVerificationStatus() {
        return verificationStatus;
    }

    public void setVerificationStatus(VerificationStatus verificationStatus) {
        this.verificationStatus = verificationStatus;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }
}
