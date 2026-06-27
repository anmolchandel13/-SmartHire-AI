package com.smarthire.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO representing a candidate's summary on the Admin Dashboard.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CandidateSummaryResponse {

    private Long profileId;
    private String fullName;
    private String email;
    private String phone;
    private String branch;
    private Double percentage;
    private String skills;
    private Boolean isShortlisted;
    
    // AI Scorecard summaries
    private Integer aiScore;
    private String recommendedRole;
    private String readinessLevel;
    
    private Boolean resumeUploaded;
    private LocalDateTime createdAt;
}
