package com.scholartrack.service;

import com.scholartrack.dto.DashboardStats;
import com.scholartrack.entity.ApplicationStatus;
import com.scholartrack.entity.EligibilityStatus;
import com.scholartrack.entity.VerificationStatus;
import com.scholartrack.repository.ApplicationRepository;
import com.scholartrack.repository.SchemeRepository;
import com.scholartrack.repository.StudentRepository;
import com.scholartrack.repository.VerificationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DashboardService {

    private final StudentRepository studentRepository;
    private final SchemeRepository schemeRepository;
    private final ApplicationRepository applicationRepository;
    private final VerificationRepository verificationRepository;

    public DashboardService(StudentRepository studentRepository,
                            SchemeRepository schemeRepository,
                            ApplicationRepository applicationRepository,
                            VerificationRepository verificationRepository) {
        this.studentRepository = studentRepository;
        this.schemeRepository = schemeRepository;
        this.applicationRepository = applicationRepository;
        this.verificationRepository = verificationRepository;
    }

    @Transactional(readOnly = true)
    public DashboardStats stats() {
        return new DashboardStats(
                studentRepository.count(),
                schemeRepository.count(),
                applicationRepository.count(),
                applicationRepository.countByEligibilityStatus(EligibilityStatus.ELIGIBLE),
                applicationRepository.countByEligibilityStatus(EligibilityStatus.INELIGIBLE),
                applicationRepository.countByApplicationStatus(ApplicationStatus.UNDER_REVIEW),
                verificationRepository.countByStatus(VerificationStatus.APPROVED),
                applicationRepository.countByApplicationStatus(ApplicationStatus.DISBURSED));
    }
}
