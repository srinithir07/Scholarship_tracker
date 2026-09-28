package com.scholartrack.config;

import com.scholartrack.dto.ApplicationRequest;
import com.scholartrack.dto.DisbursementUpdateRequest;
import com.scholartrack.dto.VerificationRequest;
import com.scholartrack.entity.*;
import com.scholartrack.repository.SchemeRepository;
import com.scholartrack.repository.StudentRepository;
import com.scholartrack.service.ApplicationService;
import com.scholartrack.service.VerificationService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    private final StudentRepository studentRepository;
    private final SchemeRepository schemeRepository;
    private final ApplicationService applicationService;
    private final VerificationService verificationService;

    public DataInitializer(StudentRepository studentRepository,
                           SchemeRepository schemeRepository,
                           ApplicationService applicationService,
                           VerificationService verificationService) {
        this.studentRepository = studentRepository;
        this.schemeRepository = schemeRepository;
        this.applicationService = applicationService;
        this.verificationService = verificationService;
    }

    @Override
    public void run(String... args) throws Exception {
        if (studentRepository.count() > 0) {
            return; // Data already exists
        }

        // 1. Seed 5 Students
        Student s1 = studentRepository.save(new Student("Rahul Sharma", "rahul.sharma@example.com", "9876543210", 88.5, 180000.0, "Computer Science B.Tech", "IIT Delhi"));
        Student s2 = studentRepository.save(new Student("Priya Patel", "priya.patel@example.com", "9876543211", 92.0, 240000.0, "Electronics Engineering", "NIT Trichy"));
        Student s3 = studentRepository.save(new Student("Amit Kumar", "amit.kumar@example.com", "9876543212", 58.0, 150000.0, "B.Sc Physics", "Delhi University"));
        Student s4 = studentRepository.save(new Student("Ananya Roy", "ananya.roy@example.com", "9876543213", 95.0, 600000.0, "Information Technology", "Jadavpur University"));
        Student s5 = studentRepository.save(new Student("Vikram Singh", "vikram.singh@example.com", "9876543214", 76.0, 220000.0, "Mechanical Engineering", "BITS Pilani"));

        // 2. Seed 4 Scholarship Schemes
        Scheme sch1 = schemeRepository.save(new Scheme("Merit Cum Means Scholarship", "Provides financial assistance to deserving students from economically weaker sections.", 250000.0, 75.0, "ACTIVE"));
        Scheme sch2 = schemeRepository.save(new Scheme("National Higher Education Grant", "Government grant for higher education students maintaining passing grades.", 500000.0, 60.0, "ACTIVE"));
        Scheme sch3 = schemeRepository.save(new Scheme("Tech Excellence Scholarship", "Prestige scholarship for top engineering achievers.", 300000.0, 90.0, "ACTIVE"));
        Scheme sch4 = schemeRepository.save(new Scheme("Economically Weaker Section Grant", "Full tuition waiver for low-income background families.", 120000.0, 50.0, "ACTIVE"));

        // 3. Create Sample Applications (Triggers Rule 1 Eligibility Check automatically)

        // Application 1: Rahul -> Merit Cum Means (Eligible: Marks 88.5 >= 75, Income 180k <= 250k)
        Application app1 = applicationService.applyForScholarship(new ApplicationRequest(s1.getId(), sch1.getId()));

        // Application 2: Priya -> Tech Excellence (Eligible: Marks 92.0 >= 90, Income 240k <= 300k)
        Application app2 = applicationService.applyForScholarship(new ApplicationRequest(s2.getId(), sch3.getId()));

        // Application 3: Amit -> Merit Cum Means (Not Eligible: Marks 58.0 < 75)
        Application app3 = applicationService.applyForScholarship(new ApplicationRequest(s3.getId(), sch1.getId()));

        // Application 4: Ananya -> Merit Cum Means (Not Eligible: Income 600k > 250k)
        Application app4 = applicationService.applyForScholarship(new ApplicationRequest(s4.getId(), sch1.getId()));

        // Application 5: Vikram -> National Grant (Eligible: Marks 76.0 >= 60, Income 220k <= 500k)
        Application app5 = applicationService.applyForScholarship(new ApplicationRequest(s5.getId(), sch2.getId()));

        // 4. Verifications & Disbursement Progress

        // App 1: Verifier approves documents
        verificationService.createVerification(new VerificationRequest(app1.getId(), VerificationStatus.APPROVED, "All marksheets and income certificates verified successfully."));
        // App 1: Disbursement completed
        applicationService.updateDisbursementStatus(app1.getId(), new DisbursementUpdateRequest(DisbursementStatus.COMPLETED));

        // App 2: Verifier approves
        verificationService.createVerification(new VerificationRequest(app2.getId(), VerificationStatus.APPROVED, "Academic records verified."));
        // App 2: Disbursement processing
        applicationService.updateDisbursementStatus(app2.getId(), new DisbursementUpdateRequest(DisbursementStatus.PROCESSING));

        // App 5: Verifier rejects due to incomplete documentation
        verificationService.createVerification(new VerificationRequest(app5.getId(), VerificationStatus.REJECTED, "Income certificate expired. Needs re-submission."));
    }
}
