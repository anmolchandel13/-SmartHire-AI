package com.smarthire.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for returning the AI-generated scorecard to the frontend.
 *
 * This is what the candidate sees after clicking "Analyze My Resume."
 * It contains the complete AI assessment of their profile.
 *
 * Example response:
 * {
 *     "score": 78,
 *     "summary": "Strong Java developer with solid project experience...",
 *     "strengths": ["Strong OOP fundamentals", "Multiple relevant projects", ...],
 *     "weaknesses": ["No cloud experience", "Limited testing knowledge", ...],
 *     "recommendedRole": "Junior Backend Developer",
 *     "readinessLevel": "Needs Improvement",
 *     "candidateName": "John Doe",
 *     "generatedAt": "2024-01-15T10:30:00"
 * }
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ScorecardResponse {

    /** Overall profile score from 0 to 100 */
    private Integer score;

    /** 2-3 sentence summary of the candidate's profile */
    private String summary;

    /** List of the candidate's key strengths */
    private String strengths;

    /** List of areas needing improvement */
    private String weaknesses;

    /** Best-fit job title recommended by the AI */
    private String recommendedRole;

    /** Interview readiness: "Ready", "Needs Improvement", or "Not Ready" */
    private String readinessLevel;

    /** Candidate's name (for display on the scorecard) */
    private String candidateName;

    /** When the analysis was performed */
    private LocalDateTime generatedAt;
}
