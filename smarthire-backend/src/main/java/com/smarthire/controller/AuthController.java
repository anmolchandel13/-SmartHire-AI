package com.smarthire.controller;

import com.smarthire.dto.request.LoginRequest;
import com.smarthire.dto.request.RegisterRequest;
import com.smarthire.dto.response.AuthResponse;
import com.smarthire.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * AuthController — REST API endpoints for authentication.
 *
 * Endpoints:
 *   POST /api/auth/register  — Create a new CANDIDATE account
 *   POST /api/auth/login     — Authenticate and receive a JWT token
 *
 * Both endpoints are PUBLIC (no JWT token required).
 * This is configured in SecurityConfig.java: .requestMatchers("/api/auth/**").permitAll()
 *
 * @RestController = @Controller + @ResponseBody
 *   Tells Spring this class handles HTTP requests and returns JSON directly
 *   (not HTML templates).
 *
 * @RequestMapping("/api/auth")
 *   Sets the base URL path for all endpoints in this controller.
 *
 * @Valid on the request parameter
 *   Triggers the validation annotations (@NotBlank, @Email, @Size) on the DTO.
 *   If validation fails, Spring returns 400 Bad Request automatically.
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Authentication", description = "User registration and login endpoints")
public class AuthController {

    private final AuthService authService;

    /**
     * Register a new candidate account.
     *
     * Request body: { "email": "...", "password": "...", "fullName": "..." }
     * Response: 201 Created with success message
     *
     * @param request the registration data (validated automatically)
     * @return AuthResponse with registration confirmation
     */
    @PostMapping("/register")
    @Operation(summary = "Register a new candidate",
            description = "Creates a new CANDIDATE user account with a skeleton profile")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Registration successful"),
            @ApiResponse(responseCode = "400", description = "Invalid input data or email already exists")
    })
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        log.info("Received registration request for email: {}", request.getEmail());
        AuthResponse response = authService.register(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    /**
     * Login with email and password, receive JWT token.
     *
     * Request body: { "email": "...", "password": "..." }
     * Response: 200 OK with JWT token, email, role, and message
     *
     * @param request the login credentials (validated automatically)
     * @return AuthResponse with JWT token for subsequent requests
     */
    @PostMapping("/login")
    @Operation(summary = "Login and get JWT token",
            description = "Authenticates user credentials and returns a JWT token for API access")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Login successful, JWT token returned"),
            @ApiResponse(responseCode = "401", description = "Invalid email or password")
    })
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        log.info("Received login request for email: {}", request.getEmail());
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }
}
