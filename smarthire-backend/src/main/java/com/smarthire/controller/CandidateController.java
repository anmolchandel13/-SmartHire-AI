package com.smarthire.controller;

import com.smarthire.dto.request.ProfileUpdateRequest;
import com.smarthire.dto.response.ApiResponse;
import com.smarthire.dto.response.ProfileResponse;
import com.smarthire.service.CandidateService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * CandidateController — REST API endpoints for candidate operations.
 *
 * All endpoints require:
 *   1. A valid JWT token (in the Authorization header)
 *   2. The CANDIDATE role (enforced by SecurityConfig)
 *
 * Endpoints:
 *   GET  /api/candidate/profile           — Get the candidate's profile
 *   PUT  /api/candidate/profile           — Update profile information
 *   POST /api/candidate/resume/upload     — Upload a resume PDF
 *
 * How we get the current user's email:
 *   Spring Security's Authentication object contains the email (set by JwtAuthenticationFilter).
 *   We inject it as a method parameter: Authentication authentication → authentication.getName()
 *   This is cleaner than using SecurityContextHolder directly.
 */
@RestController
@RequestMapping("/api/candidate")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Candidate", description = "Candidate profile and resume management")
@SecurityRequirement(name = "bearerAuth")   // Tells Swagger all endpoints need JWT
public class CandidateController {

    private final CandidateService candidateService;

    /**
     * Get the logged-in candidate's profile.
     *
     * Returns all profile data plus status flags:
     *   - resumeUploaded: whether a resume has been uploaded
     *   - analysisCompleted: whether AI analysis has been done
     */
    @GetMapping("/profile")
    @Operation(summary = "Get candidate profile",
            description = "Returns the logged-in candidate's profile with resume and analysis status")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Profile retrieved successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Not authenticated"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Not a CANDIDATE user")
    })
    public ResponseEntity<ProfileResponse> getProfile(Authentication authentication) {
        String email = authentication.getName();
        log.info("GET /api/candidate/profile — user: {}", email);

        ProfileResponse profile = candidateService.getProfile(email);
        return ResponseEntity.ok(profile);
    }

    /**
     * Update the logged-in candidate's profile.
     *
     * Supports partial updates — only send the fields you want to change.
     * For example, to update just the phone number:
     *   PUT /api/candidate/profile
     *   { "phone": "+91-9876543210" }
     */
    @PutMapping("/profile")
    @Operation(summary = "Update candidate profile",
            description = "Updates the candidate's personal and academic information. Supports partial updates.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Profile updated successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid input data"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Not authenticated")
    })
    public ResponseEntity<ProfileResponse> updateProfile(
            Authentication authentication,
            @Valid @RequestBody ProfileUpdateRequest request) {

        String email = authentication.getName();
        log.info("PUT /api/candidate/profile — user: {}", email);

        ProfileResponse profile = candidateService.updateProfile(email, request);
        return ResponseEntity.ok(profile);
    }

    /**
     * Upload a resume PDF file.
     *
     * This endpoint:
     *   1. Accepts a multipart file upload (the PDF)
     *   2. Saves the PDF to the server's filesystem
     *   3. Extracts plain text from the PDF using Apache PDFBox
     *   4. Stores the file path and extracted text in the database
     *
     * If a resume already exists, it replaces the old one.
     *
     * consumes = MediaType.MULTIPART_FORM_DATA_VALUE tells Spring to expect
     * a file upload (not a JSON body).
     */
    @PostMapping(value = "/resume/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload resume PDF",
            description = "Uploads a PDF resume, extracts text, and stores it for AI analysis")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Resume uploaded and text extracted"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid file (not PDF, empty, or too large)"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Not authenticated")
    })
    public ResponseEntity<ProfileResponse> uploadResume(
            Authentication authentication,
            @RequestParam("file") MultipartFile file) {

        String email = authentication.getName();
        log.info("POST /api/candidate/resume/upload — user: {}, file: {}, size: {} bytes",
                email, file.getOriginalFilename(), file.getSize());

        ProfileResponse profile = candidateService.uploadResume(email, file);
        return ResponseEntity.ok(profile);
    }

    /**
     * Trigger AI analysis of the uploaded resume text.
     */
    @PostMapping("/analyze")
    @Operation(summary = "Analyze uploaded resume",
            description = "Triggers the AI engine to evaluate the candidate's resume and generate their scorecard")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "AI analysis completed successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Resume not uploaded yet"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Not authenticated")
    })
    public ResponseEntity<com.smarthire.dto.response.ScorecardResponse> analyzeResume(Authentication authentication) {
        String email = authentication.getName();
        log.info("POST /api/candidate/analyze — user: {}", email);

        com.smarthire.dto.response.ScorecardResponse scorecard = candidateService.analyzeCandidateResume(email);
        return ResponseEntity.ok(scorecard);
    }

    /**
     * Retrieve the candidate's generated scorecard.
     */
    @GetMapping("/scorecard")
    @Operation(summary = "Get candidate scorecard",
            description = "Retrieves the AI-generated scorecard for the logged-in candidate")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Scorecard retrieved successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Scorecard not generated yet"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Not authenticated")
    })
    public ResponseEntity<com.smarthire.dto.response.ScorecardResponse> getScorecard(Authentication authentication) {
        String email = authentication.getName();
        log.info("GET /api/candidate/scorecard — user: {}", email);

        com.smarthire.dto.response.ScorecardResponse scorecard = candidateService.getScorecard(email);
        return ResponseEntity.ok(scorecard);
    }
}
