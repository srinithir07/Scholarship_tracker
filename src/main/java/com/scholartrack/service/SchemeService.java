package com.scholartrack.service;

import com.scholartrack.dto.SchemeRequest;
import com.scholartrack.entity.Scheme;
import com.scholartrack.entity.SchemeStatus;
import com.scholartrack.exception.ConflictException;
import com.scholartrack.exception.ResourceNotFoundException;
import com.scholartrack.repository.ApplicationRepository;
import com.scholartrack.repository.SchemeRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class SchemeService {

    private final SchemeRepository schemeRepository;
    private final ApplicationRepository applicationRepository;

    public SchemeService(SchemeRepository schemeRepository, ApplicationRepository applicationRepository) {
        this.schemeRepository = schemeRepository;
        this.applicationRepository = applicationRepository;
    }

    @Transactional
    public Scheme create(SchemeRequest request) {
        if (schemeRepository.existsByNameIgnoreCase(request.name().trim())) {
            throw new ConflictException("A scholarship scheme named '" + request.name().trim() + "' already exists.");
        }
        Scheme scheme = new Scheme();
        copy(request, scheme);
        return schemeRepository.save(scheme);
    }

    @Transactional(readOnly = true)
    public List<Scheme> findAll() {
        return schemeRepository.findAll(Sort.by("id"));
    }

    @Transactional(readOnly = true)
    public List<Scheme> findActive() {
        return schemeRepository.findByStatusOrderByIdAsc(SchemeStatus.ACTIVE);
    }

    @Transactional(readOnly = true)
    public Scheme findById(Long id) {
        return schemeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Scholarship scheme not found with id " + id));
    }

    @Transactional
    public Scheme update(Long id, SchemeRequest request) {
        Scheme scheme = findById(id);
        if (schemeRepository.existsByNameIgnoreCaseAndIdNot(request.name().trim(), id)) {
            throw new ConflictException("A scholarship scheme named '" + request.name().trim() + "' already exists.");
        }
        copy(request, scheme);
        return schemeRepository.save(scheme);
    }

    @Transactional
    public void delete(Long id) {
        Scheme scheme = findById(id);
        if (applicationRepository.existsBySchemeId(id)) {
            throw new ConflictException("Cannot delete a scheme that has scholarship applications. Mark it INACTIVE instead.");
        }
        schemeRepository.delete(scheme);
    }

    private void copy(SchemeRequest request, Scheme scheme) {
        scheme.setName(request.name().trim());
        scheme.setDescription(request.description() == null ? null : request.description().trim());
        scheme.setIncomeLimit(request.incomeLimit());
        scheme.setMinimumMarks(request.minimumMarks());
        scheme.setStatus(request.status() == null ? SchemeStatus.ACTIVE : request.status());
    }
}
