package com.scholartrack.controller;

import com.scholartrack.dto.VerificationRequest;
import com.scholartrack.dto.VerificationResponse;
import com.scholartrack.dto.VerificationUpdateRequest;
import com.scholartrack.service.VerificationService;
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
@RequestMapping("/api/verifications")
public class VerificationController {

    private final VerificationService verificationService;

    public VerificationController(VerificationService verificationService) {
        this.verificationService = verificationService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public VerificationResponse create(@Valid @RequestBody VerificationRequest request) {
        return verificationService.create(request);
    }

    @GetMapping
    public List<VerificationResponse> findAll() {
        return verificationService.findAll();
    }

    @GetMapping("/{id}")
    public VerificationResponse findById(@PathVariable Long id) {
        return verificationService.findById(id);
    }

    @PutMapping("/{id}")
    public VerificationResponse update(@PathVariable Long id,
                                       @Valid @RequestBody VerificationUpdateRequest request) {
        return verificationService.update(id, request);
    }
}
