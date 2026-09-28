package com.scholartrack.dto;

import com.scholartrack.entity.SchemeStatus;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record SchemeRequest(
        @NotBlank(message = "Scheme name is required")
        @Size(max = 150, message = "Scheme name must not exceed 150 characters")
        String name,

        @Size(max = 1000, message = "Description must not exceed 1000 characters")
        String description,

        @NotNull(message = "Income limit is required")
        @Positive(message = "Income limit must be greater than zero")
        BigDecimal incomeLimit,

        @NotNull(message = "Minimum marks are required")
        @DecimalMin(value = "0.0", message = "Minimum marks must be between 0 and 100")
        @DecimalMax(value = "100.0", message = "Minimum marks must be between 0 and 100")
        BigDecimal minimumMarks,

        SchemeStatus status) {
}
