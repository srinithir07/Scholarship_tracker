package com.scholartrack.service;

import com.scholartrack.dto.VerificationRequest;
import com.scholartrack.entity.*;
import com.scholartrack.exception.BusinessRuleException;
import com.scholartrack.exception.ResourceNotFoundException;
import com.scholartrack.repository.ApplicationRepository;
import com.scholartrack.repository.VerificationRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class VerificationService {

    private final VerificationRepository verificationRepository;
    private final ApplicationRepository applicationRepository;

    public VerificationService(VerificationRepository verificationRepository, ApplicationRepository applicationRepository) {
        this.verificationRepository = verificationRepository;
        this.applicationRepository = applicationRepository;
    }

    public List<Verification> getAllVerifications() {
        return verificationRepository.findAll();
    }

    public Verification getVerificationById(Long id) {
        return verificationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Verification record not found with ID: " + id));
    }

    public Verification createVerification(VerificationRequest request) {
        Application application = applicationRepository.findById(request.getApplicationId())
                .orElseThrow(() -> new ResourceNotFoundException("Application not found with ID: " + request.getApplicationId()));

        if (application.getEligibilityStatus() == EligibilityStatus.NOT_ELIGIBLE) {
            throw new BusinessRuleException("Application fails eligibility criteria and cannot proceed to manual verification.");
        }

        Verification verification = new Verification();
        verification.setApplication(application);
        verification.setVerificationStatus(request.getVerificationStatus());
        verification.setRemarks(request.getRemarks());
        verification.setVerifiedDate(LocalDateTime.now());

        // Update application status based on verification outcome
        if (request.getVerificationStatus() == VerificationStatus.APPROVED) {
            application.setApplicationStatus(ApplicationStatus.APPROVED);
        } else if (request.getVerificationStatus() == VerificationStatus.REJECTED) {
            application.setApplicationStatus(ApplicationStatus.REJECTED);
        } else {
            application.setApplicationStatus(ApplicationStatus.UNDER_REVIEW);
        }

        applicationRepository.save(application);
        return verificationRepository.save(verification);
    }

    public Verification updateVerification(Long id, VerificationRequest request) {
        Verification verification = getVerificationById(id);
        Application application = verification.getApplication();

        if (application.getEligibilityStatus() == EligibilityStatus.NOT_ELIGIBLE) {
            throw new BusinessRuleException("Application fails eligibility criteria and cannot proceed to manual verification.");
        }

        verification.setVerificationStatus(request.getVerificationStatus());
        verification.setRemarks(request.getRemarks());
        verification.setVerifiedDate(LocalDateTime.now());

        if (request.getVerificationStatus() == VerificationStatus.APPROVED) {
            application.setApplicationStatus(ApplicationStatus.APPROVED);
        } else if (request.getVerificationStatus() == VerificationStatus.REJECTED) {
            application.setApplicationStatus(ApplicationStatus.REJECTED);
        } else {
            application.setApplicationStatus(ApplicationStatus.UNDER_REVIEW);
        }

        applicationRepository.save(application);
        return verificationRepository.save(verification);
    }
}
