package com.scholartrack.controller;

import com.scholartrack.dto.VerificationRequest;
import com.scholartrack.entity.Verification;
import com.scholartrack.service.VerificationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/verifications")
public class VerificationController {

    private final VerificationService verificationService;

    public VerificationController(VerificationService verificationService) {
        this.verificationService = verificationService;
    }

    @GetMapping
    public ResponseEntity<List<Verification>> getAllVerifications() {
        return ResponseEntity.ok(verificationService.getAllVerifications());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Verification> getVerificationById(@PathVariable Long id) {
        return ResponseEntity.ok(verificationService.getVerificationById(id));
    }

    @PostMapping
    public ResponseEntity<Verification> createVerification(@Valid @RequestBody VerificationRequest request) {
        Verification created = verificationService.createVerification(request);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Verification> updateVerification(
            @PathVariable Long id,
            @Valid @RequestBody VerificationRequest request) {
        Verification updated = verificationService.updateVerification(id, request);
        return ResponseEntity.ok(updated);
    }
}
