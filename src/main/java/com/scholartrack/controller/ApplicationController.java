package com.scholartrack.controller;

import com.scholartrack.dto.ApplicationRequest;
import com.scholartrack.dto.ApplicationStatusResponse;
import com.scholartrack.dto.DisbursementUpdateRequest;
import com.scholartrack.entity.Application;
import com.scholartrack.service.ApplicationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/applications")
public class ApplicationController {

    private final ApplicationService applicationService;

    public ApplicationController(ApplicationService applicationService) {
        this.applicationService = applicationService;
    }

    @GetMapping
    public ResponseEntity<List<Application>> getAllApplications() {
        return ResponseEntity.ok(applicationService.getAllApplications());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Application> getApplicationById(@PathVariable Long id) {
        return ResponseEntity.ok(applicationService.getApplicationById(id));
    }

    @PostMapping
    public ResponseEntity<Application> applyForScholarship(@Valid @RequestBody ApplicationRequest request) {
        Application created = applicationService.applyForScholarship(request);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @PutMapping("/{id}/disbursement")
    public ResponseEntity<Application> updateDisbursementStatus(
            @PathVariable Long id,
            @Valid @RequestBody DisbursementUpdateRequest request) {
        Application updated = applicationService.updateDisbursementStatus(id, request);
        return ResponseEntity.ok(updated);
    }

    @GetMapping("/{id}/status")
    public ResponseEntity<ApplicationStatusResponse> getApplicationStatus(@PathVariable Long id) {
        ApplicationStatusResponse response = applicationService.getApplicationStatus(id);
        return ResponseEntity.ok(response);
    }
}
