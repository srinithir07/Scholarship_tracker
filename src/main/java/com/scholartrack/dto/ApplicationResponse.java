package com.scholartrack.dto;

import com.scholartrack.entity.Application;
import com.scholartrack.entity.ApplicationStatus;
import com.scholartrack.entity.EligibilityStatus;
import com.scholartrack.entity.Verification;
import com.scholartrack.entity.VerificationStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record ApplicationResponse(
        Long id,
        Long studentId,
        String studentName,
        Long schemeId,
        String schemeName,
        LocalDate applicationDate,
        EligibilityStatus eligibilityStatus,
        ApplicationStatus applicationStatus,
        String eligibilityRemarks,
        Long verificationId,
        VerificationStatus verificationStatus,
        String verificationRemarks,
        LocalDateTime createdAt,
        LocalDateTime updatedAt) {

    public static ApplicationResponse from(Application a) {
        Verification v = a.getVerification();
        return new ApplicationResponse(
                a.getId(),
                a.getStudent().getId(),
                a.getStudent().getName(),
                a.getScheme().getId(),
                a.getScheme().getName(),
                a.getApplicationDate(),
                a.getEligibilityStatus(),
                a.getApplicationStatus(),
                a.getEligibilityRemarks(),
                v == null ? null : v.getId(),
                v == null ? null : v.getStatus(),
                v == null ? null : v.getRemarks(),
                a.getCreatedAt(),
                a.getUpdatedAt());
    }
}
