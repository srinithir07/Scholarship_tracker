package com.scholartrack.repository;

import com.scholartrack.entity.Verification;
import com.scholartrack.entity.VerificationStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface VerificationRepository extends JpaRepository<Verification, Long> {

    Optional<Verification> findByApplicationId(Long applicationId);

    List<Verification> findAllByOrderByIdDesc();

    long countByStatus(VerificationStatus status);
}
