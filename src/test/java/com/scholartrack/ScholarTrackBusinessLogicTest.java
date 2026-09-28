package com.scholartrack;

import com.scholartrack.dto.ApplicationRequest;
import com.scholartrack.dto.ApplicationStatusResponse;
import com.scholartrack.dto.DisbursementUpdateRequest;
import com.scholartrack.dto.VerificationRequest;
import com.scholartrack.entity.*;
import com.scholartrack.exception.BusinessRuleException;
import com.scholartrack.repository.ApplicationRepository;
import com.scholartrack.repository.SchemeRepository;
import com.scholartrack.repository.StudentRepository;
import com.scholartrack.repository.VerificationRepository;
import com.scholartrack.service.ApplicationService;
import com.scholartrack.service.VerificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
public class ScholarTrackBusinessLogicTest {

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private SchemeRepository schemeRepository;

    @Autowired
    private ApplicationRepository applicationRepository;

    @Autowired
    private VerificationRepository verificationRepository;

    @Autowired
    private ApplicationService applicationService;

    @Autowired
    private VerificationService verificationService;

    private Scheme meritScheme;
    private Scheme strictScheme;

    @BeforeEach
    void setUp() {
        verificationRepository.deleteAll();
        applicationRepository.deleteAll();
        studentRepository.deleteAll();
        schemeRepository.deleteAll();

        // Scheme 1: Income Limit 200,000, Min Marks 70.0
        meritScheme = schemeRepository.save(new Scheme("Merit Scholarship", "For bright students", 200000.0, 70.0, "ACTIVE"));
        
        // Scheme 2: Income Limit 150,000, Min Marks 85.0
        strictScheme = schemeRepository.save(new Scheme("Strict Scholarship", "High criteria", 150000.0, 85.0, "ACTIVE"));
    }

    @Test
    @DisplayName("TEST 1: Student satisfies income and marks requirements -> ELIGIBLE")
    void test1_StudentSatisfiesEligibility_ShouldBeEligible() {
        // Marks: 75.0 (>= 70), Income: 180,000 (<= 200,000)
        Student student = studentRepository.save(new Student("John Doe", "john@example.com", "9998887770", 75.0, 180000.0, "B.Tech", "ABC College"));

        Application app = applicationService.applyForScholarship(new ApplicationRequest(student.getId(), meritScheme.getId()));

        assertNotNull(app.getId());
        assertEquals(EligibilityStatus.ELIGIBLE, app.getEligibilityStatus());
        assertEquals(ApplicationStatus.SUBMITTED, app.getApplicationStatus());
    }

    @Test
    @DisplayName("TEST 2: Student fails minimum marks -> NOT_ELIGIBLE")
    void test2_StudentFailsMarks_ShouldBeNotEligible() {
        // Marks: 65.0 (< 70), Income: 180,000 (<= 200,000)
        Student student = studentRepository.save(new Student("Mark Smith", "mark@example.com", "9998887771", 65.0, 180000.0, "B.Sc", "XYZ College"));

        Application app = applicationService.applyForScholarship(new ApplicationRequest(student.getId(), meritScheme.getId()));

        assertEquals(EligibilityStatus.NOT_ELIGIBLE, app.getEligibilityStatus());
        assertEquals(ApplicationStatus.REJECTED, app.getApplicationStatus());
    }

    @Test
    @DisplayName("TEST 3: Student fails income limit -> NOT_ELIGIBLE")
    void test3_StudentFailsIncomeLimit_ShouldBeNotEligible() {
        // Marks: 90.0 (>= 70), Income: 300,000 (> 200,000)
        Student student = studentRepository.save(new Student("Rich Student", "rich@example.com", "9998887772", 90.0, 300000.0, "BBA", "LMN College"));

        Application app = applicationService.applyForScholarship(new ApplicationRequest(student.getId(), meritScheme.getId()));

        assertEquals(EligibilityStatus.NOT_ELIGIBLE, app.getEligibilityStatus());
        assertEquals(ApplicationStatus.REJECTED, app.getApplicationStatus());
    }

    @Test
    @DisplayName("TEST 4: Eligible application can proceed to verification")
    void test4_EligibleApplicationCanProceedToVerification() {
        Student student = studentRepository.save(new Student("Alice Brown", "alice@example.com", "9998887773", 80.0, 150000.0, "M.Tech", "DEF College"));
        Application app = applicationService.applyForScholarship(new ApplicationRequest(student.getId(), meritScheme.getId()));

        assertEquals(EligibilityStatus.ELIGIBLE, app.getEligibilityStatus());

        Verification verification = verificationService.createVerification(
                new VerificationRequest(app.getId(), VerificationStatus.PENDING, "Under initial review"));

        assertNotNull(verification.getId());
        assertEquals(VerificationStatus.PENDING, verification.getVerificationStatus());
    }

    @Test
    @DisplayName("TEST 5: Verifier approves application -> Verification status = APPROVED")
    void test5_VerifierApprovesApplication_StatusApproved() {
        Student student = studentRepository.save(new Student("Bob Green", "bob@example.com", "9998887774", 82.0, 120000.0, "B.Com", "PQR College"));
        Application app = applicationService.applyForScholarship(new ApplicationRequest(student.getId(), meritScheme.getId()));

        Verification verification = verificationService.createVerification(
                new VerificationRequest(app.getId(), VerificationStatus.APPROVED, "Verified all documents clean."));

        assertEquals(VerificationStatus.APPROVED, verification.getVerificationStatus());

        Application updatedApp = applicationService.getApplicationById(app.getId());
        assertEquals(ApplicationStatus.APPROVED, updatedApp.getApplicationStatus());
    }

    @Test
    @DisplayName("TEST 6: Verifier rejects application -> Verification status = REJECTED")
    void test6_VerifierRejectsApplication_StatusRejected() {
        Student student = studentRepository.save(new Student("Charlie White", "charlie@example.com", "9998887775", 85.0, 140000.0, "B.Arch", "STU College"));
        Application app = applicationService.applyForScholarship(new ApplicationRequest(student.getId(), meritScheme.getId()));

        Verification verification = verificationService.createVerification(
                new VerificationRequest(app.getId(), VerificationStatus.REJECTED, "Invalid income proof provided."));

        assertEquals(VerificationStatus.REJECTED, verification.getVerificationStatus());

        Application updatedApp = applicationService.getApplicationById(app.getId());
        assertEquals(ApplicationStatus.REJECTED, updatedApp.getApplicationStatus());
    }

    @Test
    @DisplayName("TEST 7: Try to mark disbursement COMPLETED before verification approval -> Reject with error")
    void test7_DisbursementBeforeApproval_ShouldFail() {
        Student student = studentRepository.save(new Student("David Black", "david@example.com", "9998887776", 88.0, 110000.0, "MBBS", "VWX College"));
        Application app = applicationService.applyForScholarship(new ApplicationRequest(student.getId(), meritScheme.getId()));

        // Verification is NOT performed or NOT approved yet
        BusinessRuleException exception = assertThrows(BusinessRuleException.class, () -> {
            applicationService.updateDisbursementStatus(app.getId(), new DisbursementUpdateRequest(DisbursementStatus.COMPLETED));
        });

        assertTrue(exception.getMessage().contains("Disbursement cannot be marked COMPLETED unless verification has been APPROVED"));
    }

    @Test
    @DisplayName("TEST 8: After verification APPROVED, mark disbursement COMPLETED -> Succeeds")
    void test8_DisbursementAfterApproval_ShouldSucceed() {
        Student student = studentRepository.save(new Student("Eve Yellow", "eve@example.com", "9998887777", 89.0, 100000.0, "B.Pharm", "YZA College"));
        Application app = applicationService.applyForScholarship(new ApplicationRequest(student.getId(), meritScheme.getId()));

        // Verifier approves
        verificationService.createVerification(
                new VerificationRequest(app.getId(), VerificationStatus.APPROVED, "Verified and approved."));

        // Now mark disbursement COMPLETED
        Application updatedApp = applicationService.updateDisbursementStatus(app.getId(), new DisbursementUpdateRequest(DisbursementStatus.COMPLETED));

        assertEquals(DisbursementStatus.COMPLETED, updatedApp.getDisbursementStatus());
    }

    @Test
    @DisplayName("TEST 9: Retrieve application status -> Correct summary structure returned")
    void test9_RetrieveApplicationStatus_ReturnsFullSummary() {
        Student student = studentRepository.save(new Student("Frank Blue", "frank@example.com", "9998887778", 92.0, 90000.0, "M.Sc", "ZAB College"));
        Application app = applicationService.applyForScholarship(new ApplicationRequest(student.getId(), meritScheme.getId()));

        verificationService.createVerification(
                new VerificationRequest(app.getId(), VerificationStatus.APPROVED, "Document inspection completed without issues."));

        applicationService.updateDisbursementStatus(app.getId(), new DisbursementUpdateRequest(DisbursementStatus.COMPLETED));

        ApplicationStatusResponse statusResponse = applicationService.getApplicationStatus(app.getId());

        assertEquals(app.getId(), statusResponse.getApplicationId());
        assertEquals("Frank Blue", statusResponse.getStudentName());
        assertEquals("Merit Scholarship", statusResponse.getSchemeName());
        assertEquals(EligibilityStatus.ELIGIBLE, statusResponse.getEligibilityStatus());
        assertEquals(ApplicationStatus.APPROVED, statusResponse.getApplicationStatus());
        assertEquals(VerificationStatus.APPROVED, statusResponse.getVerificationStatus());
        assertEquals("Document inspection completed without issues.", statusResponse.getVerificationRemarks());
        assertEquals(DisbursementStatus.COMPLETED, statusResponse.getDisbursementStatus());
    }
}
