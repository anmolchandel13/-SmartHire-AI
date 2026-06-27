package com.smarthire.service;

import com.smarthire.dto.response.CandidateSummaryResponse;
import com.smarthire.model.AIResult;
import com.smarthire.model.Profile;
import com.smarthire.model.User;
import com.smarthire.repository.ProfileRepository;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * AdminService — Handles all recruiter/admin dashboard operations.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AdminService {

    private final ProfileRepository profileRepository;
    private final EmailService emailService;

    /**
     * Retrieves all candidates applying optional filter parameters.
     */
    @Transactional(readOnly = true)
    public List<CandidateSummaryResponse> getCandidates(
            String branch, Double minPercentage, Integer minScore, String skill, String sortBy, String direction) {

        log.info("Fetching candidate list with filters -> branch: {}, minPercentage: {}, minScore: {}, skill: {}, sortBy: {}",
                branch, minPercentage, minScore, skill, sortBy);

        // Define sort order
        Sort sort = Sort.unsorted();
        if (sortBy != null && !sortBy.isBlank()) {
            Sort.Direction dir = "desc".equalsIgnoreCase(direction) ? Sort.Direction.DESC : Sort.Direction.ASC;
            if ("score".equalsIgnoreCase(sortBy)) {
                sort = Sort.by(dir, "aiResult.score");
            } else if ("percentage".equalsIgnoreCase(sortBy)) {
                sort = Sort.by(dir, "percentage");
            } else {
                sort = Sort.by(dir, sortBy);
            }
        }

        // Construct dynamic query specifications
        Specification<Profile> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (branch != null && !branch.isBlank()) {
                predicates.add(cb.equal(cb.lower(root.get("branch")), branch.toLowerCase().trim()));
            }
            if (minPercentage != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("percentage"), minPercentage));
            }
            if (skill != null && !skill.isBlank()) {
                predicates.add(cb.like(cb.lower(root.get("skills")), "%" + skill.toLowerCase().trim() + "%"));
            }
            if (minScore != null) {
                Join<Profile, AIResult> aiJoin = root.join("aiResult");
                predicates.add(cb.greaterThanOrEqualTo(aiJoin.get("score"), minScore));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        List<Profile> profiles = profileRepository.findAll(spec, sort);

        return profiles.stream()
                .map(this::mapToSummaryResponse)
                .collect(Collectors.toList());
    }

    /**
     * Shortlist a candidate profile and dispatch an email alert asynchronously.
     */
    @Transactional
    public CandidateSummaryResponse shortlistCandidate(Long id) {
        log.info("Shortlisting profile ID: {}", id);

        Profile profile = profileRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Candidate profile not found with ID: " + id));

        profile.setIsShortlisted(true);
        Profile savedProfile = profileRepository.save(profile);

        // Fetch details to trigger email
        User user = profile.getUser();
        String email = user.getEmail();
        String recommendedRole = (profile.getAiResult() != null) 
                ? profile.getAiResult().getRecommendedRole() 
                : "Software Developer";

        // Asynchronously send email notification
        emailService.sendShortlistEmail(email, profile.getFullName(), recommendedRole);

        return mapToSummaryResponse(savedProfile);
    }

    /**
     * Generates a PDF report containing all currently shortlisted candidates.
     */
    @Transactional(readOnly = true)
    public byte[] exportShortlistPdf() {
        log.info("Generating Shortlisted Candidates PDF Report");

        List<Profile> shortlisted = profileRepository.findByIsShortlistedTrue();

        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage();
            document.addPage(page);

            try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
                
                // Write Header
                contentStream.beginText();
                contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 20);
                contentStream.newLineAtOffset(50, 750);
                contentStream.showText("SmartHire AI - Recruiter Report");
                contentStream.endText();

                contentStream.beginText();
                contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 12);
                contentStream.newLineAtOffset(50, 725);
                contentStream.showText("Shortlisted Candidates List - Generated at: " + LocalDateTime.now().toString());
                contentStream.endText();

                // Table Layout Settings
                int yOffset = 680;
                int rowHeight = 25;
                
                // Table Headers
                contentStream.beginText();
                contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 10);
                contentStream.newLineAtOffset(50, yOffset);
                contentStream.showText("Name");
                contentStream.newLineAtOffset(150, 0);
                contentStream.showText("Branch");
                contentStream.newLineAtOffset(120, 0);
                contentStream.showText("Percentage");
                contentStream.newLineAtOffset(80, 0);
                contentStream.showText("AI Score");
                contentStream.newLineAtOffset(80, 0);
                contentStream.showText("Recommended Role");
                contentStream.endText();
                
                // Horizontal divider line
                contentStream.setLineWidth(1f);
                contentStream.moveTo(50, yOffset - 5);
                contentStream.lineTo(550, yOffset - 5);
                contentStream.stroke();
                
                yOffset -= 20;

                // Write Candidate Rows
                for (Profile p : shortlisted) {
                    if (yOffset < 50) {
                        break; // Simple overflow check
                    }
                    
                    String name = p.getFullName() != null ? p.getFullName() : "N/A";
                    String branch = p.getBranch() != null ? p.getBranch() : "N/A";
                    String percentage = p.getPercentage() != null ? p.getPercentage().toString() + "%" : "N/A";
                    String aiScore = p.getAiResult() != null ? p.getAiResult().getScore().toString() : "N/A";
                    String role = p.getAiResult() != null ? p.getAiResult().getRecommendedRole() : "N/A";

                    contentStream.beginText();
                    contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 9);
                    contentStream.newLineAtOffset(50, yOffset);
                    contentStream.showText(name);
                    
                    contentStream.newLineAtOffset(150, 0);
                    contentStream.showText(branch);
                    
                    contentStream.newLineAtOffset(120, 0);
                    contentStream.showText(percentage);
                    
                    contentStream.newLineAtOffset(80, 0);
                    contentStream.showText(aiScore);
                    
                    contentStream.newLineAtOffset(80, 0);
                    contentStream.showText(role);
                    
                    contentStream.endText();

                    yOffset -= rowHeight;
                }
            }

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            document.save(baos);
            return baos.toByteArray();

        } catch (IOException ex) {
            log.error("Failed to generate Shortlist PDF report: {}", ex.getMessage());
            throw new RuntimeException("Failed to generate PDF: " + ex.getMessage(), ex);
        }
    }

    private CandidateSummaryResponse mapToSummaryResponse(Profile profile) {
        User user = profile.getUser();
        AIResult result = profile.getAiResult();

        return CandidateSummaryResponse.builder()
                .profileId(profile.getId())
                .fullName(profile.getFullName())
                .email(user.getEmail())
                .phone(profile.getPhone())
                .branch(profile.getBranch())
                .percentage(profile.getPercentage())
                .skills(profile.getSkills())
                .isShortlisted(profile.getIsShortlisted())
                .aiScore(result != null ? result.getScore() : null)
                .recommendedRole(result != null ? result.getRecommendedRole() : null)
                .readinessLevel(result != null ? result.getReadinessLevel() : null)
                .resumeUploaded(profile.getResume() != null)
                .createdAt(profile.getCreatedAt())
                .build();
    }
}
