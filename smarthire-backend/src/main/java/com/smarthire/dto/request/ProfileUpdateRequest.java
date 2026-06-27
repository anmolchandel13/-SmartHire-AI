package com.smarthire.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for updating a candidate's profile.
 *
 * This defines what the frontend sends to PUT /api/candidate/profile.
 * All fields are optional (no @NotBlank) because the candidate
 * may want to update just one field at a time.
 *
 * Example request body:
 * {
 *     "fullName": "John Doe",
 *     "phone": "+91-9876543210",
 *     "branch": "Computer Science",
 *     "percentage": 85.5,
 *     "skills": "Java, Spring Boot, React, MySQL, Docker"
 * }
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProfileUpdateRequest {

    @Size(min = 2, max = 100, message = "Full name must be between 2 and 100 characters")
    private String fullName;

    @Size(max = 20, message = "Phone number must not exceed 20 characters")
    private String phone;

    @Size(max = 50, message = "Branch must not exceed 50 characters")
    private String branch;

    @Min(value = 0, message = "Percentage cannot be negative")
    @Max(value = 100, message = "Percentage cannot exceed 100")
    private Double percentage;

    @Size(max = 500, message = "Skills must not exceed 500 characters")
    private String skills;
}
