package com.scholartrack.dto;

import com.scholartrack.entity.VerificationStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record VerificationRequest(
        @NotNull(message = "Application id is required")
        Long applicationId,

        @NotBlank(message = "Verifier name is required")
        @Size(max = 100, message = "Verifier name must not exceed 100 characters")
        String verifiedBy,

        @NotNull(message = "Verification status is required (APPROVED or REJECTED)")
        VerificationStatus status,

        @Size(max = 1000, message = "Remarks must not exceed 1000 characters")
        String remarks) {
}
