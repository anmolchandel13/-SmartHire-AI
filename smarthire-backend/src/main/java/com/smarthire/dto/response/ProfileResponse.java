package com.smarthire.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for returning a candidate's profile information.
 *
 * This shapes what the frontend receives when requesting profile data.
 * Notice we include resumeUploaded and analysisCompleted flags so
 * the frontend can show/hide UI elements accordingly.
 *
 * Example response:
 * {
 *     "id": 1,
 *     "fullName": "John Doe",
 *     "email": "john@example.com",
 *     "phone": "+91-9876543210",
 *     "branch": "Computer Science",
 *     "percentage": 85.5,
 *     "skills": "Java, Spring Boot, React",
 *     "isShortlisted": false,
 *     "resumeUploaded": true,
 *     "analysisCompleted": false,
 *     "createdAt": "2024-01-15T10:30:00"
 * }
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProfileResponse {

    private Long id;
    private String fullName;
    private String email;
    private String phone;
    private String branch;
    private Double percentage;
    private String skills;
    private Boolean isShortlisted;

    /** Whether a resume PDF has been uploaded */
    private Boolean resumeUploaded;

    /** Whether AI analysis has been completed */
    private Boolean analysisCompleted;

    /** Original filename of the uploaded resume */
    private String resumeFilename;

    private LocalDateTime createdAt;
}
