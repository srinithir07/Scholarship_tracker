package com.scholartrack.service;

import com.scholartrack.dto.ApplicationRequest;
import com.scholartrack.dto.ApplicationResponse;
import com.scholartrack.entity.Application;
import com.scholartrack.entity.ApplicationStatus;
import com.scholartrack.entity.EligibilityStatus;
import com.scholartrack.entity.Scheme;
import com.scholartrack.entity.SchemeStatus;
import com.scholartrack.entity.Student;
import com.scholartrack.entity.Verification;
import com.scholartrack.entity.VerificationStatus;
import com.scholartrack.exception.BusinessRuleException;
import com.scholartrack.exception.ConflictException;
import com.scholartrack.exception.InvalidStatusTransitionException;
import com.scholartrack.exception.ResourceNotFoundException;
import com.scholartrack.repository.ApplicationRepository;
import com.scholartrack.repository.SchemeRepository;
import com.scholartrack.repository.StudentRepository;
import com.scholartrack.repository.VerificationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ApplicationService {

    private final ApplicationRepository applicationRepository;
    private final StudentRepository studentRepository;
    private final SchemeRepository schemeRepository;
    private final VerificationRepository verificationRepository;
    private final EligibilityService eligibilityService;

    public ApplicationService(ApplicationRepository applicationRepository,
                              StudentRepository studentRepository,
                              SchemeRepository schemeRepository,
                              VerificationRepository verificationRepository,
                              EligibilityService eligibilityService) {
        this.applicationRepository = applicationRepository;
        this.studentRepository = studentRepository;
        this.schemeRepository = schemeRepository;
        this.verificationRepository = verificationRepository;
        this.eligibilityService = eligibilityService;
    }

    /**
     * Rule 1: eligibility is evaluated at submission time, before any manual review,
     * and an ineligible application is flagged INELIGIBLE immediately.
     */
    @Transactional
    public ApplicationResponse apply(ApplicationRequest request) {
        Student student = studentRepository.findById(request.studentId())
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with id " + request.studentId()));
        Scheme scheme = schemeRepository.findById(request.schemeId())
                .orElseThrow(() -> new ResourceNotFoundException("Scholarship scheme not found with id " + request.schemeId()));

        if (scheme.getStatus() != SchemeStatus.ACTIVE) {
            throw new BusinessRuleException("Scholarship scheme '" + scheme.getName() + "' is not accepting applications.");
        }
        if (applicationRepository.existsByStudentIdAndSchemeId(student.getId(), scheme.getId())) {
            throw new ConflictException("Student already has an application for this scholarship scheme.");
        }

        EligibilityService.Result result = eligibilityService.evaluate(student, scheme);

        Application application = new Application();
        application.setStudent(student);
        application.setScheme(scheme);
        application.setEligibilityStatus(result.status());
        application.setEligibilityRemarks(result.remarks());
        application.setApplicationStatus(ApplicationStatus.SUBMITTED);
        return ApplicationResponse.from(applicationRepository.save(application));
    }

    @Transactional(readOnly = true)
    public List<ApplicationResponse> findAll() {
        return applicationRepository.findAllByOrderByIdDesc().stream().map(ApplicationResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public ApplicationResponse findById(Long id) {
        return ApplicationResponse.from(getApplication(id));
    }

    @Transactional(readOnly = true)
    public List<ApplicationResponse> findByStudent(Long studentId) {
        if (!studentRepository.existsById(studentId)) {
            throw new ResourceNotFoundException("Student not found with id " + studentId);
        }
        return applicationRepository.findByStudentIdOrderByIdDesc(studentId).stream()
                .map(ApplicationResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public List<ApplicationResponse> findByStatus(ApplicationStatus status) {
        return applicationRepository.findByApplicationStatusOrderByIdDesc(status).stream()
                .map(ApplicationResponse::from).toList();
    }

    /**
     * Moves an ELIGIBLE, SUBMITTED application into manual verification (UNDER_REVIEW)
     * and opens a PENDING verification record for the verifier.
     */
    @Transactional
    public ApplicationResponse startReview(Long id) {
        Application application = getApplication(id);

        if (application.getEligibilityStatus() != EligibilityStatus.ELIGIBLE) {
            throw new BusinessRuleException("Ineligible applications cannot proceed to manual verification. "
                    + application.getEligibilityRemarks());
        }
        if (application.getApplicationStatus() != ApplicationStatus.SUBMITTED) {
            throw new InvalidStatusTransitionException("Application cannot move to UNDER_REVIEW from status "
                    + application.getApplicationStatus() + ".");
        }

        application.setApplicationStatus(ApplicationStatus.UNDER_REVIEW);

        Verification verification = new Verification();
        verification.setApplication(application);
        verification.setStatus(VerificationStatus.PENDING);
        application.setVerification(verificationRepository.save(verification));

        return ApplicationResponse.from(applicationRepository.save(application));
    }

    /**
     * Rule 2: disbursement can only be completed when verification has been APPROVED.
     */
    @Transactional
    public ApplicationResponse disburse(Long id) {
        Application application = getApplication(id);
        Verification verification = application.getVerification();

        boolean approved = application.getEligibilityStatus() == EligibilityStatus.ELIGIBLE
                && verification != null
                && verification.getStatus() == VerificationStatus.APPROVED;
        if (!approved) {
            throw new BusinessRuleException("Disbursement cannot be completed until verification is approved.");
        }
        if (application.getApplicationStatus() == ApplicationStatus.DISBURSED) {
            throw new ConflictException("Application has already been disbursed.");
        }
        if (application.getApplicationStatus() != ApplicationStatus.DISBURSEMENT_PENDING) {
            throw new InvalidStatusTransitionException("Application cannot be disbursed from status "
                    + application.getApplicationStatus() + ".");
        }

        application.setApplicationStatus(ApplicationStatus.DISBURSED);
        return ApplicationResponse.from(applicationRepository.save(application));
    }

    private Application getApplication(Long id) {
        return applicationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Application not found with id " + id));
    }
}
