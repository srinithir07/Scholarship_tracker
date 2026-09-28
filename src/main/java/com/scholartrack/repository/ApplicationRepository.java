package com.scholartrack.repository;

import com.scholartrack.entity.Application;
import com.scholartrack.entity.ApplicationStatus;
import com.scholartrack.entity.EligibilityStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ApplicationRepository extends JpaRepository<Application, Long> {

    List<Application> findAllByOrderByIdDesc();

    List<Application> findByStudentIdOrderByIdDesc(Long studentId);

    List<Application> findByApplicationStatusOrderByIdDesc(ApplicationStatus applicationStatus);

    boolean existsByStudentIdAndSchemeId(Long studentId, Long schemeId);

    boolean existsByStudentId(Long studentId);

    boolean existsBySchemeId(Long schemeId);

    long countByEligibilityStatus(EligibilityStatus eligibilityStatus);

    long countByApplicationStatus(ApplicationStatus applicationStatus);
}
