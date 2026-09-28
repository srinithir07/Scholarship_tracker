package com.scholartrack.controller;

import com.scholartrack.dto.ApplicationRequest;
import com.scholartrack.dto.ApplicationResponse;
import com.scholartrack.entity.ApplicationStatus;
import com.scholartrack.service.ApplicationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/applications")
public class ApplicationController {

    private final ApplicationService applicationService;

    public ApplicationController(ApplicationService applicationService) {
        this.applicationService = applicationService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApplicationResponse apply(@Valid @RequestBody ApplicationRequest request) {
        return applicationService.apply(request);
    }

    @GetMapping
    public List<ApplicationResponse> findAll() {
        return applicationService.findAll();
    }

    @GetMapping("/{id}")
    public ApplicationResponse findById(@PathVariable Long id) {
        return applicationService.findById(id);
    }

    @GetMapping("/student/{studentId}")
    public List<ApplicationResponse> findByStudent(@PathVariable Long studentId) {
        return applicationService.findByStudent(studentId);
    }

    @GetMapping("/status/{status}")
    public List<ApplicationResponse> findByStatus(@PathVariable ApplicationStatus status) {
        return applicationService.findByStatus(status);
    }

    @PutMapping("/{id}/review")
    public ApplicationResponse startReview(@PathVariable Long id) {
        return applicationService.startReview(id);
    }

    @PutMapping("/{id}/disburse")
    public ApplicationResponse disburse(@PathVariable Long id) {
        return applicationService.disburse(id);
    }
}
