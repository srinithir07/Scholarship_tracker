package com.scholartrack.service;

import com.scholartrack.dto.ApplicationRequest;
import com.scholartrack.dto.ApplicationStatusResponse;
import com.scholartrack.dto.DisbursementUpdateRequest;
import com.scholartrack.entity.*;
import com.scholartrack.exception.BusinessRuleException;
import com.scholartrack.exception.ResourceNotFoundException;
import com.scholartrack.repository.ApplicationRepository;
import com.scholartrack.repository.SchemeRepository;
import com.scholartrack.repository.StudentRepository;
import com.scholartrack.repository.VerificationRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ApplicationService {

    private final ApplicationRepository applicationRepository;
    private final StudentRepository studentRepository;
    private final SchemeRepository schemeRepository;
    private final VerificationRepository verificationRepository;

    public ApplicationService(ApplicationRepository applicationRepository,
                              StudentRepository studentRepository,
                              SchemeRepository schemeRepository,
                              VerificationRepository verificationRepository) {
        this.applicationRepository = applicationRepository;
        this.studentRepository = studentRepository;
        this.schemeRepository = schemeRepository;
        this.verificationRepository = verificationRepository;
    }

    public List<Application> getAllApplications() {
        return applicationRepository.findAll();
    }

    public Application getApplicationById(Long id) {
        return applicationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Application not found with ID: " + id));
    }

    /**
     * Rule 1: Apply for scholarship with automatic eligibility check.
     */
    public Application applyForScholarship(ApplicationRequest request) {
        Student student = studentRepository.findById(request.getStudentId())
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with ID: " + request.getStudentId()));

        Scheme scheme = schemeRepository.findById(request.getSchemeId())
                .orElseThrow(() -> new ResourceNotFoundException("Scholarship scheme not found with ID: " + request.getSchemeId()));

        Application application = new Application();
        application.setStudent(student);
        application.setScheme(scheme);
        application.setDisbursementStatus(DisbursementStatus.NOT_STARTED);

        // Eligibility logic enforcement
        boolean marksEligible = student.getMarks() >= scheme.getMinimumMarks();
        boolean incomeEligible = student.getAnnualIncome() <= scheme.getIncomeLimit();

        if (marksEligible && incomeEligible) {
            application.setEligibilityStatus(EligibilityStatus.ELIGIBLE);
            application.setApplicationStatus(ApplicationStatus.SUBMITTED);
        } else {
            application.setEligibilityStatus(EligibilityStatus.NOT_ELIGIBLE);
            application.setApplicationStatus(ApplicationStatus.REJECTED);
        }

        return applicationRepository.save(application);
    }

    /**
     * Rule 2: Disbursement cannot be marked COMPLETED unless verification has been APPROVED.
     */
    public Application updateDisbursementStatus(Long applicationId, DisbursementUpdateRequest request) {
        Application application = getApplicationById(applicationId);
        DisbursementStatus targetStatus = request.getDisbursementStatus();

        if (targetStatus == DisbursementStatus.COMPLETED) {
            Optional<Verification> latestVerification = verificationRepository.findTopByApplicationIdOrderByVerifiedDateDesc(applicationId);
            
            boolean isApproved = latestVerification.isPresent() && 
                    latestVerification.get().getVerificationStatus() == VerificationStatus.APPROVED;

            if (!isApproved && application.getApplicationStatus() != ApplicationStatus.APPROVED) {
                throw new BusinessRuleException("Disbursement cannot be marked COMPLETED unless verification has been APPROVED.");
            }
        }

        application.setDisbursementStatus(targetStatus);
        return applicationRepository.save(application);
    }

    /**
     * Real-time Application Status endpoint summary logic.
     */
    public ApplicationStatusResponse getApplicationStatus(Long applicationId) {
        Application application = getApplicationById(applicationId);
        Optional<Verification> latestVerification = verificationRepository.findTopByApplicationIdOrderByVerifiedDateDesc(applicationId);

        ApplicationStatusResponse response = new ApplicationStatusResponse();
        response.setApplicationId(application.getId());
        response.setApplicationDate(application.getApplicationDate());

        // Student Details
        Student student = application.getStudent();
        response.setStudentId(student.getId());
        response.setStudentName(student.getName());
        response.setStudentEmail(student.getEmail());
        response.setStudentMarks(student.getMarks());
        response.setStudentAnnualIncome(student.getAnnualIncome());

        // Scheme Details
        Scheme scheme = application.getScheme();
        response.setSchemeId(scheme.getId());
        response.setSchemeName(scheme.getName());
        response.setSchemeIncomeLimit(scheme.getIncomeLimit());
        response.setSchemeMinimumMarks(scheme.getMinimumMarks());

        // Statuses
        response.setEligibilityStatus(application.getEligibilityStatus());
        response.setApplicationStatus(application.getApplicationStatus());
        response.setDisbursementStatus(application.getDisbursementStatus());

        if (latestVerification.isPresent()) {
            response.setVerificationStatus(latestVerification.get().getVerificationStatus());
            response.setVerificationRemarks(latestVerification.get().getRemarks());
        } else {
            response.setVerificationStatus(VerificationStatus.PENDING);
            response.setVerificationRemarks(application.getEligibilityStatus() == EligibilityStatus.NOT_ELIGIBLE ? 
                    "Failed basic eligibility criteria" : "Awaiting verifier review");
        }

        return response;
    }
}
