package com.scholartrack.dto;

import jakarta.validation.constraints.NotNull;

public record ApplicationRequest(
        @NotNull(message = "Student id is required") Long studentId,
        @NotNull(message = "Scheme id is required") Long schemeId) {
}
