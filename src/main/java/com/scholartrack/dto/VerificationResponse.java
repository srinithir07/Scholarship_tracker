package com.scholartrack.dto;

import com.scholartrack.entity.Application;
import com.scholartrack.entity.ApplicationStatus;
import com.scholartrack.entity.Verification;
import com.scholartrack.entity.VerificationStatus;

import java.time.LocalDateTime;

public record VerificationResponse(
        Long id,
        Long applicationId,
        String studentName,
        String schemeName,
        String verifiedBy,
        LocalDateTime verificationDate,
        VerificationStatus status,
        String remarks,
        ApplicationStatus applicationStatus) {

    public static VerificationResponse from(Verification v) {
        Application a = v.getApplication();
        return new VerificationResponse(
                v.getId(),
                a.getId(),
                a.getStudent().getName(),
                a.getScheme().getName(),
                v.getVerifiedBy(),
                v.getVerificationDate(),
                v.getStatus(),
                v.getRemarks(),
                a.getApplicationStatus());
    }
}
