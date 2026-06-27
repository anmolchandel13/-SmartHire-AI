package com.smarthire.service;

import com.smarthire.dto.request.ProfileUpdateRequest;
import com.smarthire.dto.response.ProfileResponse;
import com.smarthire.model.Profile;
import com.smarthire.model.Resume;
import com.smarthire.model.User;
import com.smarthire.model.AIResult;
import com.smarthire.repository.ProfileRepository;
import com.smarthire.repository.ResumeRepository;
import com.smarthire.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

/**
 * CandidateService — Business logic for candidate profile and resume operations.
 *
 * This service handles:
 *   1. Getting the candidate's profile (with resume/analysis status flags)
 *   2. Updating profile information (name, phone, branch, percentage, skills)
 *   3. Uploading a resume PDF (save file + extract text + store in DB)
 *
 * All methods take the user's email (extracted from JWT token by the controller)
 * and use it to find the correct profile.
 *
 * Key design principle: The controller extracts the email from the JWT token
 * and passes it to the service. The service NEVER accesses the security context
 * directly — this keeps it testable and decoupled.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CandidateService {

    private final UserRepository userRepository;
    private final ProfileRepository profileRepository;
    private final ResumeRepository resumeRepository;
    private final com.smarthire.repository.AIResultRepository aiResultRepository;
    private final FileStorageService fileStorageService;
    private final PdfExtractorService pdfExtractorService;
    private final AIService aiService;

    /**
     * Get the candidate's complete profile with status flags.
     *
     * @param email the logged-in candidate's email (from JWT)
     * @return ProfileResponse with all profile data and status flags
     */
    @Transactional(readOnly = true)
    public ProfileResponse getProfile(String email) {
        log.debug("Fetching profile for user: {}", email);

        User user = findUserByEmail(email);
        Profile profile = findProfileByUser(user);

        return mapToProfileResponse(profile, user);
    }

    /**
     * Update the candidate's profile information.
     *
     * Only non-null fields in the request are updated (partial update pattern).
     * This means the candidate can update just their phone number without
     * having to re-send all other fields.
     *
     * @param email the logged-in candidate's email (from JWT)
     * @param request the fields to update
     * @return updated ProfileResponse
     */
    @Transactional
    public ProfileResponse updateProfile(String email, ProfileUpdateRequest request) {
        log.info("Updating profile for user: {}", email);

        User user = findUserByEmail(email);
        Profile profile = findProfileByUser(user);

        // Update only non-null fields (partial update pattern)
        if (request.getFullName() != null) {
            profile.setFullName(request.getFullName());
        }
        if (request.getPhone() != null) {
            profile.setPhone(request.getPhone());
        }
        if (request.getBranch() != null) {
            profile.setBranch(request.getBranch());
        }
        if (request.getPercentage() != null) {
            profile.setPercentage(request.getPercentage());
        }
        if (request.getSkills() != null) {
            profile.setSkills(request.getSkills());
        }

        profileRepository.save(profile);
        log.info("Profile updated successfully for user: {}", email);

        return mapToProfileResponse(profile, user);
    }

    /**
     * Upload a resume PDF, extract text, and store everything.
     *
     * This method does three things:
     *   1. Saves the PDF file to the server's filesystem
     *   2. Extracts plain text from the PDF using Apache PDFBox
     *   3. Saves the file path + extracted text to the resumes table
     *
     * If the candidate already has a resume, the old one is replaced.
     *
     * @param email the logged-in candidate's email (from JWT)
     * @param file the uploaded PDF file
     * @return ProfileResponse with resumeUploaded = true
     */
    @Transactional
    public ProfileResponse uploadResume(String email, MultipartFile file) {
        log.info("Processing resume upload for user: {}", email);

        User user = findUserByEmail(email);
        Profile profile = findProfileByUser(user);

        // Step 1: Save the PDF file to disk
        String filePath = fileStorageService.storeFile(file);
        log.debug("PDF saved to: {}", filePath);

        // Step 2: Extract text from the PDF
        String extractedText = pdfExtractorService.extractText(file);
        log.debug("Extracted {} characters from PDF", extractedText.length());

        // Step 3: Create or update the Resume entity
        Resume resume = resumeRepository.findByProfileId(profile.getId())
                .map(existingResume -> {
                    // Resume already exists — update it (delete old file first)
                    log.info("Replacing existing resume for user: {}", email);
                    fileStorageService.deleteFile(existingResume.getFilePath());
                    existingResume.setFilePath(filePath);
                    existingResume.setExtractedText(extractedText);
                    existingResume.setOriginalFilename(file.getOriginalFilename());
                    return existingResume;
                })
                .orElseGet(() -> {
                    // No resume yet — create a new one
                    log.info("Creating new resume record for user: {}", email);
                    return Resume.builder()
                            .profile(profile)
                            .filePath(filePath)
                            .extractedText(extractedText)
                            .originalFilename(file.getOriginalFilename())
                            .build();
                });

        resumeRepository.save(resume);
        log.info("Resume uploaded and text extracted successfully for user: {}", email);

        return mapToProfileResponse(profile, user);
    }

    /**
     * Triggers AI analysis on the candidate's uploaded resume.
     *
     * @param email candidate's email
     * @return ScorecardResponse containing AI analysis scorecard
     */
    @Transactional
    public com.smarthire.dto.response.ScorecardResponse analyzeCandidateResume(String email) {
        log.info("Triggering AI analysis of resume for candidate: {}", email);

        User user = findUserByEmail(email);
        Profile profile = findProfileByUser(user);

        Resume resume = resumeRepository.findByProfileId(profile.getId())
                .orElseThrow(() -> new RuntimeException("Please upload your resume before requesting analysis."));

        // Trigger analysis
        AIResult newResult = aiService.analyzeResume(profile, resume.getExtractedText());

        // Check if an AIResult already exists for this profile, update if it does, else save new
        AIResult finalResult = aiResultRepository.findByProfileId(profile.getId())
                .map(existingResult -> {
                    existingResult.setScore(newResult.getScore());
                    existingResult.setSummary(newResult.getSummary());
                    existingResult.setStrengths(newResult.getStrengths());
                    existingResult.setWeaknesses(newResult.getWeaknesses());
                    existingResult.setRecommendedRole(newResult.getRecommendedRole());
                    existingResult.setReadinessLevel(newResult.getReadinessLevel());
                    return existingResult;
                })
                .orElse(newResult);

        aiResultRepository.save(finalResult);
        profile.setAiResult(finalResult);
        profileRepository.save(profile);

        log.info("Successfully completed and saved AI analysis for candidate: {}", email);

        return mapToScorecardResponse(finalResult, profile);
    }

    /**
     * Retrieve the candidate's AI evaluation scorecard.
     *
     * @param email candidate's email
     * @return ScorecardResponse
     */
    @Transactional(readOnly = true)
    public com.smarthire.dto.response.ScorecardResponse getScorecard(String email) {
        log.debug("Retrieving scorecard for candidate: {}", email);

        User user = findUserByEmail(email);
        Profile profile = findProfileByUser(user);

        AIResult aiResult = aiResultRepository.findByProfileId(profile.getId())
                .orElseThrow(() -> new RuntimeException("AI evaluation scorecard has not been generated yet."));

        return mapToScorecardResponse(aiResult, profile);
    }

    private com.smarthire.dto.response.ScorecardResponse mapToScorecardResponse(AIResult result, Profile profile) {
        return com.smarthire.dto.response.ScorecardResponse.builder()
                .score(result.getScore())
                .summary(result.getSummary())
                .strengths(result.getStrengths())
                .weaknesses(result.getWeaknesses())
                .recommendedRole(result.getRecommendedRole())
                .readinessLevel(result.getReadinessLevel())
                .candidateName(profile.getFullName())
                .generatedAt(result.getGeneratedAt())
                .build();
    }

    // ==================== HELPER METHODS ====================

    /**
     * Find a user by email or throw an exception.
     */
    private User findUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + email));
    }

    /**
     * Find a profile by user or throw an exception.
     */
    private Profile findProfileByUser(User user) {
        return profileRepository.findByUserId(user.getId())
                .orElseThrow(() -> new RuntimeException("Profile not found for user: " + user.getEmail()));
    }

    /**
     * Map a Profile entity to a ProfileResponse DTO.
     *
     * This method converts the internal database entity into the external
     * API response shape. It also checks whether a resume has been uploaded
     * and whether AI analysis has been completed — these are computed fields,
     * not stored in the profiles table.
     */
    private ProfileResponse mapToProfileResponse(Profile profile, User user) {
        boolean resumeUploaded = resumeRepository.existsByProfileId(profile.getId());
        boolean analysisCompleted = profile.getAiResult() != null;

        String resumeFilename = null;
        if (resumeUploaded) {
            resumeFilename = resumeRepository.findByProfileId(profile.getId())
                    .map(Resume::getOriginalFilename)
                    .orElse(null);
        }

        return ProfileResponse.builder()
                .id(profile.getId())
                .fullName(profile.getFullName())
                .email(user.getEmail())
                .phone(profile.getPhone())
                .branch(profile.getBranch())
                .percentage(profile.getPercentage())
                .skills(profile.getSkills())
                .isShortlisted(profile.getIsShortlisted())
                .resumeUploaded(resumeUploaded)
                .analysisCompleted(analysisCompleted)
                .resumeFilename(resumeFilename)
                .createdAt(profile.getCreatedAt())
                .build();
    }
}
