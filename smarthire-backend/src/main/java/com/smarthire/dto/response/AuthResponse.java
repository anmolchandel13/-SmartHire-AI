package com.smarthire.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for authentication responses.
 *
 * This is what the backend returns after a successful login or registration.
 * The frontend stores the token and uses it for all subsequent API calls.
 *
 * Example response:
 * {
 *     "token": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJqb2huQGV4...",
 *     "tokenType": "Bearer",
 *     "email": "john@example.com",
 *     "role": "CANDIDATE",
 *     "message": "Login successful"
 * }
 *
 * The frontend will:
 *   1. Store the token in localStorage
 *   2. Include it in every request header: Authorization: Bearer <token>
 *   3. Use the 'role' to show/hide UI elements (candidate vs admin views)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthResponse {

    private String token;

    @Builder.Default
    private String tokenType = "Bearer";

    private String email;

    private String role;

    private String message;
}
