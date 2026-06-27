package com.smarthire.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * AIResult Entity — Maps to the 'ai_results' table in MySQL.
 *
 * This is the AI-generated scorecard that contains the complete assessment
 * of a candidate's resume. The data comes from the Gemini/OpenAI API response
 * and is parsed and stored here.
 *
 * Fields:
 *   - score:           Numerical score from 0-100 (overall profile strength)
 *   - summary:         2-3 sentence overview of the candidate
 *   - strengths:       Detailed list of what the candidate excels at
 *   - weaknesses:      Areas where the candidate needs improvement
 *   - recommendedRole: The best-fit job title based on resume analysis
 *   - readinessLevel:  Interview readiness — "Ready", "Needs Improvement", or "Not Ready"
 *
 * Relationships:
 *   - Belongs to one Profile (via @OneToOne with @JoinColumn)
 *
 * Design Decisions:
 *   - score as Integer (not Double): Whole numbers are cleaner for scoring (78, not 78.3)
 *   - strengths/weaknesses as @Lob LONGTEXT: These can be multi-paragraph detailed analyses
 *   - summary as VARCHAR(1000): Summaries are brief, no need for LONGTEXT
 *   - readinessLevel as String (not enum): The AI might return variations, keeping it
 *     flexible avoids parsing errors. We validate on the service layer instead.
 */
@Entity
@Table(name = "ai_results")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AIResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Owning side of the Profile ↔ AIResult relationship.
     * The 'profile_id' FK column is created in the ai_results table.
     */
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "profile_id", nullable = false, unique = true)
    private Profile profile;

    /**
     * Overall score from 0 to 100.
     * Higher = stronger candidate profile.
     * This is the primary metric used for sorting/filtering on the admin dashboard.
     */
    @Column(nullable = false)
    private Integer score;

    /**
     * Brief 2-3 sentence summary of the candidate's profile.
     * Example: "Strong Java developer with 3 years of project experience.
     *           Demonstrates solid OOP understanding but lacks cloud exposure."
     */
    @Column(length = 1000)
    private String summary;

    /**
     * Detailed analysis of the candidate's strengths.
     * LONGTEXT because the AI may generate paragraph-length responses.
     * Example: "- Strong fundamentals in Java and Spring Boot
     *           - Multiple relevant projects including a REST API
     *           - Clear communication and documentation skills"
     */
    @Lob
    @Column(columnDefinition = "LONGTEXT")
    private String strengths;

    /**
     * Detailed analysis of areas where the candidate needs improvement.
     * LONGTEXT for the same reason as strengths.
     * Example: "- No experience with cloud platforms (AWS, Azure, GCP)
     *           - Limited understanding of testing methodologies
     *           - No contributions to open-source projects"
     */
    @Lob
    @Column(columnDefinition = "LONGTEXT")
    private String weaknesses;

    /**
     * The most suitable job role recommended by the AI.
     * Example: "Junior Backend Developer", "Full-Stack Engineer", "Data Analyst"
     */
    @Column(name = "recommended_role", length = 100)
    private String recommendedRole;

    /**
     * Interview readiness assessment.
     * Expected values: "Ready", "Needs Improvement", "Not Ready"
     * Stored as String for flexibility in AI responses.
     */
    @Column(name = "readiness_level", length = 50)
    private String readinessLevel;

    @CreationTimestamp
    @Column(name = "generated_at", nullable = false, updatable = false)
    private LocalDateTime generatedAt;
}
