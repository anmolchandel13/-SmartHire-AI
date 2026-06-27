package com.smarthire.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for user login requests.
 *
 * This defines EXACTLY what the frontend must send to POST /api/auth/login.
 *
 * Example valid request body:
 * {
 *     "email": "john@example.com",
 *     "password": "securePass123"
 * }
 *
 * The backend will:
 *   1. Look up the user by email
 *   2. Compare the provided password with the stored BCrypt hash
 *   3. If valid, generate and return a JWT token
 *   4. If invalid, return 401 Unauthorized
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequest {

    @NotBlank(message = "Email is required")
    @Email(message = "Please provide a valid email address")
    private String email;

    @NotBlank(message = "Password is required")
    private String password;
}
