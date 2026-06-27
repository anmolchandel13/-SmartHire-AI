package com.smarthire.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for user registration requests.
 *
 * This defines EXACTLY what the frontend must send to POST /api/auth/register.
 * The validation annotations ensure the data is clean BEFORE it reaches our service:
 *
 *   @NotBlank → field cannot be null, empty, or whitespace-only
 *   @Email    → must be a valid email format (contains @, domain, etc.)
 *   @Size     → enforces minimum and maximum length
 *
 * If validation fails, Spring automatically returns a 400 Bad Request
 * with details about which fields are invalid.
 *
 * Example valid request body:
 * {
 *     "email": "john@example.com",
 *     "password": "securePass123",
 *     "fullName": "John Doe"
 * }
 *
 * Note: The 'role' is NOT in this DTO. All registrations create CANDIDATE users.
 * ADMIN accounts are created manually or via a seeded database entry.
 * This prevents anyone from registering themselves as an admin.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequest {

    @NotBlank(message = "Email is required")
    @Email(message = "Please provide a valid email address")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 6, max = 100, message = "Password must be between 6 and 100 characters")
    private String password;

    @NotBlank(message = "Full name is required")
    @Size(min = 2, max = 100, message = "Full name must be between 2 and 100 characters")
    private String fullName;
}
