package com.scholartrack.service;

import com.scholartrack.entity.Scheme;
import com.scholartrack.exception.ResourceNotFoundException;
import com.scholartrack.repository.SchemeRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SchemeService {

    private final SchemeRepository schemeRepository;

    public SchemeService(SchemeRepository schemeRepository) {
        this.schemeRepository = schemeRepository;
    }

    public List<Scheme> getAllSchemes() {
        return schemeRepository.findAll();
    }

    public Scheme getSchemeById(Long id) {
        return schemeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Scholarship scheme not found with ID: " + id));
    }

    public Scheme createScheme(Scheme scheme) {
        return schemeRepository.save(scheme);
    }

    public Scheme updateScheme(Long id, Scheme updatedScheme) {
        Scheme existing = getSchemeById(id);
        existing.setName(updatedScheme.getName());
        existing.setDescription(updatedScheme.getDescription());
        existing.setIncomeLimit(updatedScheme.getIncomeLimit());
        existing.setMinimumMarks(updatedScheme.getMinimumMarks());
        existing.setStatus(updatedScheme.getStatus());
        return schemeRepository.save(existing);
    }

    public void deleteScheme(Long id) {
        Scheme existing = getSchemeById(id);
        schemeRepository.delete(existing);
    }
}
