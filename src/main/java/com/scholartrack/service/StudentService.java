package com.scholartrack.service;

import com.scholartrack.dto.StudentRequest;
import com.scholartrack.entity.Student;
import com.scholartrack.exception.ConflictException;
import com.scholartrack.exception.ResourceNotFoundException;
import com.scholartrack.repository.ApplicationRepository;
import com.scholartrack.repository.StudentRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class StudentService {

    private final StudentRepository studentRepository;
    private final ApplicationRepository applicationRepository;

    public StudentService(StudentRepository studentRepository, ApplicationRepository applicationRepository) {
        this.studentRepository = studentRepository;
        this.applicationRepository = applicationRepository;
    }

    @Transactional
    public Student create(StudentRequest request) {
        if (studentRepository.existsByEmailIgnoreCase(request.email().trim())) {
            throw new ConflictException("A student with email '" + request.email().trim() + "' already exists.");
        }
        Student student = new Student();
        copy(request, student);
        return studentRepository.save(student);
    }

    @Transactional(readOnly = true)
    public List<Student> findAll() {
        return studentRepository.findAll(Sort.by("id"));
    }

    @Transactional(readOnly = true)
    public Student findById(Long id) {
        return studentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with id " + id));
    }

    @Transactional
    public Student update(Long id, StudentRequest request) {
        Student student = findById(id);
        if (studentRepository.existsByEmailIgnoreCaseAndIdNot(request.email().trim(), id)) {
            throw new ConflictException("A student with email '" + request.email().trim() + "' already exists.");
        }
        copy(request, student);
        return studentRepository.save(student);
    }

    @Transactional
    public void delete(Long id) {
        Student student = findById(id);
        if (applicationRepository.existsByStudentId(id)) {
            throw new ConflictException("Cannot delete a student who has scholarship applications.");
        }
        studentRepository.delete(student);
    }

    private void copy(StudentRequest request, Student student) {
        student.setName(request.name().trim());
        student.setEmail(request.email().trim());
        student.setPhone(request.phone().trim());
        student.setAnnualIncome(request.annualIncome());
        student.setMarks(request.marks());
        student.setCourse(request.course().trim());
        student.setYear(request.year());
    }
}
