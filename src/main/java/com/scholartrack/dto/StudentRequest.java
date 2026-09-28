package com.scholartrack.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record StudentRequest(
        @NotBlank(message = "Student name is required")
        @Size(max = 100, message = "Student name must not exceed 100 characters")
        String name,

        @NotBlank(message = "Email is required")
        @Email(message = "Email must be a valid email address")
        @Size(max = 150, message = "Email must not exceed 150 characters")
        String email,

        @NotBlank(message = "Phone number is required")
        @Pattern(regexp = "^\\+?[0-9]{10,13}$", message = "Phone must contain 10 to 13 digits (optional leading +)")
        String phone,

        @NotNull(message = "Annual income is required")
        @PositiveOrZero(message = "Annual income cannot be negative")
        BigDecimal annualIncome,

        @NotNull(message = "Marks are required")
        @DecimalMin(value = "0.0", message = "Marks must be between 0 and 100")
        @DecimalMax(value = "100.0", message = "Marks must be between 0 and 100")
        BigDecimal marks,

        @NotBlank(message = "Course is required")
        @Size(max = 100, message = "Course must not exceed 100 characters")
        String course,

        @NotNull(message = "Year is required")
        @Min(value = 1, message = "Year must be between 1 and 6")
        @Max(value = 6, message = "Year must be between 1 and 6")
        Integer year) {
}
