package com.scholartrack.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;

@Entity
@Table(name = "schemes")
public class Scheme {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Scheme name cannot be empty")
    @Column(nullable = false)
    private String name;

    @NotBlank(message = "Description cannot be empty")
    @Column(length = 1000)
    private String description;

    @NotNull(message = "Income limit cannot be null")
    @Positive(message = "Income limit must be positive")
    private Double incomeLimit;

    @NotNull(message = "Minimum marks cannot be null")
    @Min(value = 0, message = "Minimum marks cannot be less than 0")
    @Max(value = 100, message = "Minimum marks cannot exceed 100")
    private Double minimumMarks;

    @NotBlank(message = "Status cannot be empty")
    private String status = "ACTIVE";

    public Scheme() {
    }

    public Scheme(Long id, String name, String description, Double incomeLimit, Double minimumMarks, String status) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.incomeLimit = incomeLimit;
        this.minimumMarks = minimumMarks;
        this.status = status;
    }

    public Scheme(String name, String description, Double incomeLimit, Double minimumMarks, String status) {
        this.name = name;
        this.description = description;
        this.incomeLimit = incomeLimit;
        this.minimumMarks = minimumMarks;
        this.status = status;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Double getIncomeLimit() {
        return incomeLimit;
    }

    public void setIncomeLimit(Double incomeLimit) {
        this.incomeLimit = incomeLimit;
    }

    public Double getMinimumMarks() {
        return minimumMarks;
    }

    public void setMinimumMarks(Double minimumMarks) {
        this.minimumMarks = minimumMarks;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
