package com.scholartrack;

import com.scholartrack.dto.ApplicationRequest;
import com.scholartrack.dto.ApplicationResponse;
import com.scholartrack.dto.SchemeRequest;
import com.scholartrack.dto.StudentRequest;
import com.scholartrack.dto.VerificationRequest;
import com.scholartrack.entity.ApplicationStatus;
import com.scholartrack.entity.EligibilityStatus;
import com.scholartrack.entity.Scheme;
import com.scholartrack.entity.SchemeStatus;
import com.scholartrack.entity.Student;
import com.scholartrack.entity.VerificationStatus;
import com.scholartrack.exception.BusinessRuleException;
import com.scholartrack.exception.ConflictException;
import com.scholartrack.service.ApplicationService;
import com.scholartrack.service.SchemeService;
import com.scholartrack.service.StudentService;
import com.scholartrack.service.VerificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@Transactional
class BusinessRulesTest {

    private static final String DISBURSEMENT_MESSAGE =
            "Disbursement cannot be completed until verification is approved.";

    @Autowired
    private StudentService studentService;
    @Autowired
    private SchemeService schemeService;
    @Autowired
    private ApplicationService applicationService;
    @Autowired
    private VerificationService verificationService;

    private Scheme scheme;
    private Student eligibleStudent;

    @BeforeEach
    void setUp() {
        scheme = schemeService.create(new SchemeRequest("Test Merit Scholarship", "Test scheme",
                new BigDecimal("300000"), new BigDecimal("60"), SchemeStatus.ACTIVE));
        eligibleStudent = newStudent("eligible@example.com", "200000", "85");
    }

    @Test
    void eligibleStudentCanApply() {
        ApplicationResponse response = apply(eligibleStudent);

        assertEquals(EligibilityStatus.ELIGIBLE, response.eligibilityStatus());
        assertEquals(ApplicationStatus.SUBMITTED, response.applicationStatus());
        assertEquals("Student is eligible for the scholarship.", response.eligibilityRemarks());
    }

    @Test
    void ineligibleStudentIsFlagged() {
        Student highIncome = newStudent("rich@example.com", "900000", "90");
        Student lowMarks = newStudent("low@example.com", "100000", "40");
        Student bothFail = newStudent("both@example.com", "900000", "40");

        ApplicationResponse income = apply(highIncome);
        ApplicationResponse marks = apply(lowMarks);
        ApplicationResponse both = apply(bothFail);

        assertEquals(EligibilityStatus.INELIGIBLE, income.eligibilityStatus());
        assertTrue(income.eligibilityRemarks().contains("annual income exceeds the permitted limit"));
        assertEquals(EligibilityStatus.INELIGIBLE, marks.eligibilityStatus());
        assertTrue(marks.eligibilityRemarks().contains("marks are below the minimum requirement"));
        assertEquals(EligibilityStatus.INELIGIBLE, both.eligibilityStatus());
        assertTrue(both.eligibilityRemarks().contains("income") && both.eligibilityRemarks().contains("marks"));
        assertEquals(ApplicationStatus.SUBMITTED, income.applicationStatus());
    }

    @Test
    void ineligibleApplicationCannotEnterVerification() {
        ApplicationResponse ineligible = apply(newStudent("rich2@example.com", "900000", "90"));

        assertThrows(BusinessRuleException.class, () -> applicationService.startReview(ineligible.id()));
        assertThrows(BusinessRuleException.class, () -> verificationService.create(
                new VerificationRequest(ineligible.id(), "Verifier", VerificationStatus.APPROVED, "Should fail")));
        assertEquals(ApplicationStatus.SUBMITTED, applicationService.findById(ineligible.id()).applicationStatus());
    }

    @Test
    void disbursementFailsBeforeVerificationApproval() {
        ApplicationResponse submitted = apply(eligibleStudent);

        BusinessRuleException beforeReview = assertThrows(BusinessRuleException.class,
                () -> applicationService.disburse(submitted.id()));
        assertEquals(DISBURSEMENT_MESSAGE, beforeReview.getMessage());

        applicationService.startReview(submitted.id());
        BusinessRuleException duringReview = assertThrows(BusinessRuleException.class,
                () -> applicationService.disburse(submitted.id()));
        assertEquals(DISBURSEMENT_MESSAGE, duringReview.getMessage());
    }

    @Test
    void disbursementFailsWhenVerificationRejected() {
        ApplicationResponse submitted = apply(eligibleStudent);
        applicationService.startReview(submitted.id());
        verificationService.create(new VerificationRequest(submitted.id(), "Verifier",
                VerificationStatus.REJECTED, "Documents incomplete"));

        BusinessRuleException ex = assertThrows(BusinessRuleException.class,
                () -> applicationService.disburse(submitted.id()));
        assertEquals(DISBURSEMENT_MESSAGE, ex.getMessage());
        assertEquals(ApplicationStatus.REJECTED, applicationService.findById(submitted.id()).applicationStatus());
    }

    @Test
    void disbursementSucceedsAfterVerificationApproval() {
        ApplicationResponse submitted = apply(eligibleStudent);
        applicationService.startReview(submitted.id());
        assertEquals(ApplicationStatus.UNDER_REVIEW, applicationService.findById(submitted.id()).applicationStatus());

        verificationService.create(new VerificationRequest(submitted.id(), "Dr. Verifier",
                VerificationStatus.APPROVED, "All documents verified"));
        assertEquals(ApplicationStatus.DISBURSEMENT_PENDING,
                applicationService.findById(submitted.id()).applicationStatus());

        ApplicationResponse disbursed = applicationService.disburse(submitted.id());
        assertEquals(ApplicationStatus.DISBURSED, disbursed.applicationStatus());
        assertEquals(VerificationStatus.APPROVED, disbursed.verificationStatus());
    }

    @Test
    void duplicateApplicationIsRejected() {
        apply(eligibleStudent);
        ConflictException ex = assertThrows(ConflictException.class, () -> apply(eligibleStudent));
        assertEquals("Student already has an application for this scholarship scheme.", ex.getMessage());
    }

    private Student newStudent(String email, String income, String marks) {
        return studentService.create(new StudentRequest("Test Student", email, "9876543210",
                new BigDecimal(income), new BigDecimal(marks), "B.E. Computer Science", 2));
    }

    private ApplicationResponse apply(Student student) {
        return applicationService.apply(new ApplicationRequest(student.getId(), scheme.getId()));
    }
}
