package com.scholartrack.service;

import com.scholartrack.dto.VerificationRequest;
import com.scholartrack.dto.VerificationResponse;
import com.scholartrack.dto.VerificationUpdateRequest;
import com.scholartrack.entity.Application;
import com.scholartrack.entity.ApplicationStatus;
import com.scholartrack.entity.EligibilityStatus;
import com.scholartrack.entity.Verification;
import com.scholartrack.entity.VerificationStatus;
import com.scholartrack.exception.BusinessRuleException;
import com.scholartrack.exception.InvalidStatusTransitionException;
import com.scholartrack.exception.ResourceNotFoundException;
import com.scholartrack.repository.ApplicationRepository;
import com.scholartrack.repository.VerificationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class VerificationService {

    private final VerificationRepository verificationRepository;
    private final ApplicationRepository applicationRepository;

    public VerificationService(VerificationRepository verificationRepository,
                               ApplicationRepository applicationRepository) {
        this.verificationRepository = verificationRepository;
        this.applicationRepository = applicationRepository;
    }

    /** Verifier records an APPROVED / REJECTED decision for an application that is UNDER_REVIEW. */
    @Transactional
    public VerificationResponse create(VerificationRequest request) {
        Application application = applicationRepository.findById(request.applicationId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Application not found with id " + request.applicationId()));

        requireEligible(application);
        requireDecision(request.status());

        ApplicationStatus current = application.getApplicationStatus();
        if (current == ApplicationStatus.SUBMITTED) {
            throw new InvalidStatusTransitionException(
                    "Application must be moved to UNDER_REVIEW before it can be verified.");
        }
        if (current != ApplicationStatus.UNDER_REVIEW) {
            throw new InvalidStatusTransitionException("Application is already " + current
                    + " and cannot be verified again. Update the existing verification instead.");
        }

        Verification verification = verificationRepository.findByApplicationId(application.getId())
                .orElseGet(Verification::new);
        verification.setApplication(application);
        return VerificationResponse.from(decide(application, verification, request.verifiedBy(),
                request.status(), request.remarks()));
    }

    /** Lets a verifier change a decision, but never after the scholarship has been disbursed. */
    @Transactional
    public VerificationResponse update(Long id, VerificationUpdateRequest request) {
        Verification verification = getVerification(id);
        Application application = verification.getApplication();

        requireEligible(application);
        requireDecision(request.status());

        if (application.getApplicationStatus() == ApplicationStatus.DISBURSED) {
            throw new InvalidStatusTransitionException(
                    "Verification cannot be changed after the scholarship has been disbursed.");
        }
        return VerificationResponse.from(decide(application, verification, request.verifiedBy(),
                request.status(), request.remarks()));
    }

    @Transactional(readOnly = true)
    public List<VerificationResponse> findAll() {
        return verificationRepository.findAllByOrderByIdDesc().stream().map(VerificationResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public VerificationResponse findById(Long id) {
        return VerificationResponse.from(getVerification(id));
    }

    private Verification decide(Application application, Verification verification, String verifiedBy,
                                VerificationStatus status, String remarks) {
        verification.setVerifiedBy(verifiedBy.trim());
        verification.setStatus(status);
        verification.setRemarks(remarks == null || remarks.isBlank() ? null : remarks.trim());
        verification.setVerificationDate(LocalDateTime.now());

        application.setApplicationStatus(status == VerificationStatus.APPROVED
                ? ApplicationStatus.DISBURSEMENT_PENDING
                : ApplicationStatus.REJECTED);

        Verification saved = verificationRepository.save(verification);
        application.setVerification(saved);
        applicationRepository.save(application);
        return saved;
    }

    private void requireEligible(Application application) {
        if (application.getEligibilityStatus() != EligibilityStatus.ELIGIBLE) {
            throw new BusinessRuleException("Ineligible applications cannot be verified. "
                    + application.getEligibilityRemarks());
        }
    }

    private void requireDecision(VerificationStatus status) {
        if (status == VerificationStatus.PENDING) {
            throw new BusinessRuleException("Verification decision must be APPROVED or REJECTED.");
        }
    }

    private Verification getVerification(Long id) {
        return verificationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Verification not found with id " + id));
    }
}
