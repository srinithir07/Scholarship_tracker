package com.scholartrack.dto;

import jakarta.validation.constraints.NotNull;

public class ApplicationRequest {

    @NotNull(message = "Student ID is required")
    private Long studentId;

    @NotNull(message = "Scheme ID is required")
    private Long schemeId;

    public ApplicationRequest() {
    }

    public ApplicationRequest(Long studentId, Long schemeId) {
        this.studentId = studentId;
        this.schemeId = schemeId;
    }

    public Long getStudentId() {
        return studentId;
    }

    public void setStudentId(Long studentId) {
        this.studentId = studentId;
    }

    public Long getSchemeId() {
        return schemeId;
    }

    public void setSchemeId(Long schemeId) {
        this.schemeId = schemeId;
    }
}
