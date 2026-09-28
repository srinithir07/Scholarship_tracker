package com.scholartrack.repository;

import com.scholartrack.entity.Verification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VerificationRepository extends JpaRepository<Verification, Long> {
    Optional<Verification> findTopByApplicationIdOrderByVerifiedDateDesc(Long applicationId);
    List<Verification> findByApplicationId(Long applicationId);
}
