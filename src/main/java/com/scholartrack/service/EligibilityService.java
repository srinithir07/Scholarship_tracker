package com.scholartrack.service;

import com.scholartrack.entity.EligibilityStatus;
import com.scholartrack.entity.Scheme;
import com.scholartrack.entity.Student;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Eligibility engine: a student is eligible only when
 * annualIncome <= incomeLimit AND marks >= minimumMarks.
 */
@Service
public class EligibilityService {

    public record Result(EligibilityStatus status, String remarks) {
    }

    public Result evaluate(Student student, Scheme scheme) {
        List<String> failures = new ArrayList<>();

        if (student.getAnnualIncome().compareTo(scheme.getIncomeLimit()) > 0) {
            failures.add("annual income exceeds the permitted limit");
        }
        if (student.getMarks().compareTo(scheme.getMinimumMarks()) < 0) {
            failures.add("marks are below the minimum requirement");
        }

        if (failures.isEmpty()) {
            return new Result(EligibilityStatus.ELIGIBLE, "Student is eligible for the scholarship.");
        }
        return new Result(EligibilityStatus.INELIGIBLE,
                "Student is not eligible: " + String.join(" and ", failures) + ".");
    }
}
