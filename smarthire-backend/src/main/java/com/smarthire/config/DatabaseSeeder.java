package com.smarthire.config;

import com.smarthire.model.Role;
import com.smarthire.model.User;
import com.smarthire.model.Profile;
import com.smarthire.model.AIResult;
import com.smarthire.model.Resume;
import com.smarthire.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * DatabaseSeeder — Automatically seeds demo records and administrator credentials at startup.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DatabaseSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        seedAdminUser();
        seedDemoCandidates();
    }

    private void seedAdminUser() {
        String adminEmail = "admin@smarthire.ai";
        Optional<User> existing = userRepository.findByEmail(adminEmail);
        
        if (existing.isEmpty()) {
            log.info("Database Seeder: Creating default Administrator account...");
            User admin = User.builder()
                    .email(adminEmail)
                    .password(passwordEncoder.encode("adminpass"))
                    .role(Role.ADMIN)
                    .build();
            userRepository.save(admin);
            log.info("Database Seeder: Created admin user: {} with password: 'adminpass'", adminEmail);
        } else {
            log.debug("Database Seeder: Admin account already exists.");
        }
    }

    private void seedDemoCandidates() {
        // Create candidate 1
        String cand1Email = "candidate1@smarthire.ai";
        if (userRepository.findByEmail(cand1Email).isEmpty()) {
            log.info("Database Seeder: Seeding demo Candidate 1...");
            User user = User.builder()
                    .email(cand1Email)
                    .password(passwordEncoder.encode("candidatepass"))
                    .role(Role.CANDIDATE)
                    .build();

            Profile profile = Profile.builder()
                    .user(user)
                    .fullName("Alex Rivera")
                    .phone("+91-9876543201")
                    .branch("Computer Science")
                    .percentage(85.4)
                    .skills("Java, Spring Boot, MySQL, REST APIs, Git")
                    .isShortlisted(false)
                    .build();
            user.setProfile(profile);

            Resume resume = Resume.builder()
                    .profile(profile)
                    .filePath("./uploads/resumes/demo-alex-rivera.pdf")
                    .originalFilename("Alex_Rivera_CV.pdf")
                    .extractedText("Alex Rivera. Software Engineer with 2 years of Java development experience. Built multiple microservices and database applications.")
                    .build();
            profile.setResume(resume);

            AIResult result = AIResult.builder()
                    .profile(profile)
                    .score(88)
                    .summary("Exceptional candidate with solid Java and Spring credentials. Demonstrates strong backend development patterns.")
                    .strengths("- Competent in core Java and OOP principles\n- Experience building Rest APIs\n- Clean database modeling skills")
                    .weaknesses("- Lacks formal testing coverage experience\n- Limited container exposure")
                    .recommendedRole("Junior Backend Developer")
                    .readinessLevel("Ready")
                    .build();
            profile.setAiResult(result);

            userRepository.save(user);
        }

        // Create candidate 2
        String cand2Email = "candidate2@smarthire.ai";
        if (userRepository.findByEmail(cand2Email).isEmpty()) {
            log.info("Database Seeder: Seeding demo Candidate 2...");
            User user = User.builder()
                    .email(cand2Email)
                    .password(passwordEncoder.encode("candidatepass"))
                    .role(Role.CANDIDATE)
                    .build();

            Profile profile = Profile.builder()
                    .user(user)
                    .fullName("Samantha Chen")
                    .phone("+91-9876543202")
                    .branch("Information Technology")
                    .percentage(79.2)
                    .skills("HTML, CSS, JavaScript, React.js, Vite")
                    .isShortlisted(false)
                    .build();
            user.setProfile(profile);

            Resume resume = Resume.builder()
                    .profile(profile)
                    .filePath("./uploads/resumes/demo-samantha-chen.pdf")
                    .originalFilename("Samantha_Chen_CV.pdf")
                    .extractedText("Samantha Chen. Frontend Developer specialized in React.js and user interface layouts. Enthusiastic about creating modern web designs.")
                    .build();
            profile.setResume(resume);

            AIResult result = AIResult.builder()
                    .profile(profile)
                    .score(76)
                    .summary("Promising React developer with design capability. Needs expansion in back-end logic integrations.")
                    .strengths("- Strong React and JavaScript skills\n- Good UI design principles")
                    .weaknesses("- Minimal SQL query competency\n- No Java background")
                    .recommendedRole("Junior Frontend Developer")
                    .readinessLevel("Needs Improvement")
                    .build();
            profile.setAiResult(result);

            userRepository.save(user);
        }
    }
}
