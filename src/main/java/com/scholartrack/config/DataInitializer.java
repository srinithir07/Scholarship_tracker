package com.scholartrack.config;

import com.scholartrack.entity.Application;
import com.scholartrack.entity.ApplicationStatus;
import com.scholartrack.entity.Scheme;
import com.scholartrack.entity.SchemeStatus;
import com.scholartrack.entity.Student;
import com.scholartrack.entity.Verification;
import com.scholartrack.entity.VerificationStatus;
import com.scholartrack.repository.ApplicationRepository;
import com.scholartrack.repository.SchemeRepository;
import com.scholartrack.repository.StudentRepository;
import com.scholartrack.repository.VerificationRepository;
import com.scholartrack.service.EligibilityService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Seeds demo data once. Skipped when students or schemes already exist,
 * so restarting the application never creates duplicates.
 */
@Component
@ConditionalOnProperty(name = "scholartrack.seed.enabled", havingValue = "true", matchIfMissing = true)
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final StudentRepository studentRepository;
    private final SchemeRepository schemeRepository;
    private final ApplicationRepository applicationRepository;
    private final VerificationRepository verificationRepository;
    private final EligibilityService eligibilityService;

    public DataInitializer(StudentRepository studentRepository,
                           SchemeRepository schemeRepository,
                           ApplicationRepository applicationRepository,
                           VerificationRepository verificationRepository,
                           EligibilityService eligibilityService) {
        this.studentRepository = studentRepository;
        this.schemeRepository = schemeRepository;
        this.applicationRepository = applicationRepository;
        this.verificationRepository = verificationRepository;
        this.eligibilityService = eligibilityService;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (studentRepository.count() > 0 || schemeRepository.count() > 0) {
            log.info("Sample data skipped: students or schemes already exist.");
            return;
        }

        Student aarav = student("Aarav Krishnan", "aarav.krishnan@example.com", "9876543210", "150000", "88.50", "B.Tech AI & ML", 3);
        Student priya = student("Priya Sharma", "priya.sharma@example.com", "9876501234", "240000", "92.00", "B.E. Computer Science", 2);
        Student karthik = student("Karthik Rajan", "karthik.rajan@example.com", "9876512345", "620000", "81.00", "B.E. Mechanical Engineering", 4);
        Student meena = student("Meena Lakshmi", "meena.lakshmi@example.com", "9876523456", "90000", "54.00", "B.Sc Data Science", 1);
        Student rahul = student("Rahul Verma", "rahul.verma@example.com", "9876534567", "180000", "76.00", "B.Tech Information Technology", 3);

        Scheme merit = scheme("Merit Excellence Scholarship",
                "Rewards high-scoring students from families with an annual income up to 5,00,000.", "500000", "80.00");
        Scheme needBased = scheme("Need-Based Education Grant",
                "Financial support for students from low-income families who maintain good academic standing.", "200000", "60.00");
        Scheme stem = scheme("STEM Achievers Scholarship",
                "Encourages students in engineering and science programmes with strong academic records.", "350000", "70.00");
        Scheme firstGen = scheme("First-Generation Learner Award",
                "Supports first-generation college students from economically weaker families.", "120000", "50.00");

        // 1. Eligible -> approved -> DISBURSED
        Application a1 = application(aarav, merit, 30);
        underReview(a1);
        decide(a1, VerificationStatus.APPROVED, "Dr. S. Ramesh Kumar",
                "All documents verified. Income certificate and mark sheet are authentic.");
        a1.setApplicationStatus(ApplicationStatus.DISBURSED);
        applicationRepository.save(a1);

        // 2. Eligible, freshly SUBMITTED (ready to be moved to review)
        application(aarav, needBased, 2);

        // 3. Eligible -> approved -> DISBURSEMENT_PENDING
        Application a3 = application(priya, merit, 12);
        underReview(a3);
        decide(a3, VerificationStatus.APPROVED, "Prof. Anitha Devi",
                "Documents verified. Approved for disbursement.");

        // 4. Eligible -> UNDER_REVIEW (verification pending)
        Application a4 = application(priya, stem, 5);
        underReview(a4);

        // 5. Ineligible: annual income above the limit
        application(karthik, needBased, 8);

        // 6. Ineligible: marks below the minimum
        application(meena, stem, 6);

        // 7. Eligible -> verification REJECTED
        Application a7 = application(rahul, stem, 20);
        underReview(a7);
        decide(a7, VerificationStatus.REJECTED, "Dr. S. Ramesh Kumar",
                "Income certificate is not attested by a competent authority.");

        // 8. Ineligible: marks below the minimum
        application(rahul, merit, 3);

        log.info("Sample data loaded: {} students, {} schemes, {} applications.",
                studentRepository.count(), schemeRepository.count(), applicationRepository.count());
    }

    private Student student(String name, String email, String phone, String income, String marks,
                            String course, int year) {
        Student s = new Student();
        s.setName(name);
        s.setEmail(email);
        s.setPhone(phone);
        s.setAnnualIncome(new BigDecimal(income));
        s.setMarks(new BigDecimal(marks));
        s.setCourse(course);
        s.setYear(year);
        return studentRepository.save(s);
    }

    private Scheme scheme(String name, String description, String incomeLimit, String minimumMarks) {
        Scheme s = new Scheme();
        s.setName(name);
        s.setDescription(description);
        s.setIncomeLimit(new BigDecimal(incomeLimit));
        s.setMinimumMarks(new BigDecimal(minimumMarks));
        s.setStatus(SchemeStatus.ACTIVE);
        return schemeRepository.save(s);
    }

    private Application application(Student student, Scheme scheme, int daysAgo) {
        EligibilityService.Result result = eligibilityService.evaluate(student, scheme);
        Application a = new Application();
        a.setStudent(student);
        a.setScheme(scheme);
        a.setApplicationDate(LocalDate.now().minusDays(daysAgo));
        a.setEligibilityStatus(result.status());
        a.setEligibilityRemarks(result.remarks());
        a.setApplicationStatus(ApplicationStatus.SUBMITTED);
        return applicationRepository.save(a);
    }

    private void underReview(Application application) {
        application.setApplicationStatus(ApplicationStatus.UNDER_REVIEW);
        Verification v = new Verification();
        v.setApplication(application);
        v.setStatus(VerificationStatus.PENDING);
        application.setVerification(verificationRepository.save(v));
        applicationRepository.save(application);
    }

    private void decide(Application application, VerificationStatus status, String verifiedBy, String remarks) {
        Verification v = application.getVerification();
        v.setStatus(status);
        v.setVerifiedBy(verifiedBy);
        v.setRemarks(remarks);
        v.setVerificationDate(LocalDateTime.now().minusDays(1));
        verificationRepository.save(v);
        application.setApplicationStatus(status == VerificationStatus.APPROVED
                ? ApplicationStatus.DISBURSEMENT_PENDING
                : ApplicationStatus.REJECTED);
        applicationRepository.save(application);
    }
}
