package com.scholartrack.controller;

import com.scholartrack.dto.SchemeRequest;
import com.scholartrack.entity.Scheme;
import com.scholartrack.service.SchemeService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/schemes")
public class SchemeController {

    private final SchemeService schemeService;

    public SchemeController(SchemeService schemeService) {
        this.schemeService = schemeService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Scheme create(@Valid @RequestBody SchemeRequest request) {
        return schemeService.create(request);
    }

    @GetMapping
    public List<Scheme> findAll() {
        return schemeService.findAll();
    }

    @GetMapping("/active")
    public List<Scheme> findActive() {
        return schemeService.findActive();
    }

    @GetMapping("/{id}")
    public Scheme findById(@PathVariable Long id) {
        return schemeService.findById(id);
    }

    @PutMapping("/{id}")
    public Scheme update(@PathVariable Long id, @Valid @RequestBody SchemeRequest request) {
        return schemeService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        schemeService.delete(id);
    }
}
