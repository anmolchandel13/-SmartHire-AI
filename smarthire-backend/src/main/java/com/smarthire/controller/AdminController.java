package com.smarthire.controller;

import com.smarthire.dto.response.CandidateSummaryResponse;
import com.smarthire.service.AdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * AdminController — REST API endpoints for HR managers and recruitment administrators.
 *
 * Enforces ADMIN role checks for all requests.
 */
@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Admin / Recruiter", description = "Dashboard candidate list filters, shortlisting actions, and PDF reports")
@SecurityRequirement(name = "bearerAuth")
public class AdminController {

    private final AdminService adminService;

    /**
     * Retrieves the candidate list with dynamic sorting and filtering options.
     */
    @GetMapping("/candidates")
    @Operation(summary = "Get candidate lists with filters",
            description = "Get list of candidate summaries applying filters like branch, score thresholds, or skills, with sorting.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Candidate list loaded successfully"),
            @ApiResponse(responseCode = "403", description = "Access denied: Recruiter permissions required")
    })
    public ResponseEntity<List<CandidateSummaryResponse>> getCandidates(
            @RequestParam(required = false) String branch,
            @RequestParam(required = false) Double minPercentage,
            @RequestParam(required = false) Integer minScore,
            @RequestParam(required = false) String skill,
            @RequestParam(required = false, defaultValue = "createdAt") String sortBy,
            @RequestParam(required = false, defaultValue = "desc") String direction) {

        log.info("GET /api/admin/candidates — Called with filters");
        List<CandidateSummaryResponse> candidates = adminService.getCandidates(
                branch, minPercentage, minScore, skill, sortBy, direction);
        return ResponseEntity.ok(candidates);
    }

    /**
     * Mark a candidate as shortlisted, triggers email.
     */
    @PutMapping("/candidates/{id}/shortlist")
    @Operation(summary = "Shortlist a candidate profile",
            description = "Shortlists candidate with target profile ID and triggers automatic congratulatory email.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Candidate successfully shortlisted"),
            @ApiResponse(responseCode = "404", description = "Candidate profile ID not found")
    })
    public ResponseEntity<CandidateSummaryResponse> shortlistCandidate(@PathVariable Long id) {
        log.info("PUT /api/admin/candidates/{}/shortlist — Called", id);
        CandidateSummaryResponse response = adminService.shortlistCandidate(id);
        return ResponseEntity.ok(response);
    }

    /**
     * Exports shortlisted candidates as a PDF table document attachment.
     */
    @GetMapping("/shortlist/export")
    @Operation(summary = "Export shortlisted candidates to PDF",
            description = "Generates and downloads a PDF file containing candidate summaries currently marked as shortlisted.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Shortlist PDF document created successfully")
    })
    public ResponseEntity<byte[]> exportShortlistPdf() {
        log.info("GET /api/admin/shortlist/export — Called");
        byte[] pdfBytes = adminService.exportShortlistPdf();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", "smarthire_shortlist_report.pdf");
        headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");

        return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
    }
}
