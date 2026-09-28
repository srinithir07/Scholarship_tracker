package com.scholartrack.repository;

import com.scholartrack.entity.Scheme;
import com.scholartrack.entity.SchemeStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SchemeRepository extends JpaRepository<Scheme, Long> {

    List<Scheme> findByStatusOrderByIdAsc(SchemeStatus status);

    boolean existsByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCaseAndIdNot(String name, Long id);
}
