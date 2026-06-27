package com.smarthire.service;

import com.smarthire.dto.request.LoginRequest;
import com.smarthire.dto.request.RegisterRequest;
import com.smarthire.dto.response.AuthResponse;
import com.smarthire.model.Profile;
import com.smarthire.model.Role;
import com.smarthire.model.User;
import com.smarthire.repository.UserRepository;
import com.smarthire.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * AuthService — Business logic for user registration and login.
 *
 * Registration Flow:
 *   1. Check if email already exists → throw exception if it does
 *   2. Hash the password with BCrypt
 *   3. Create a User entity with role = CANDIDATE
 *   4. Create a basic Profile entity linked to the User
 *   5. Save to database
 *   6. Return success message (no token — user must login separately)
 *
 * Login Flow:
 *   1. Authenticate email + password using Spring Security's AuthenticationManager
 *   2. If credentials are valid, generate a JWT token
 *   3. Return the token + user details in the response
 *
 * Why register creates a Profile automatically?
 *   - Every candidate needs a profile, so we create a skeleton profile
 *     with just the fullName during registration. The user can update
 *     the rest (phone, branch, percentage, skills) later.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;

    /**
     * Register a new CANDIDATE user.
     *
     * @param request contains email, password, and fullName
     * @return AuthResponse with success message (no token)
     * @throws RuntimeException if email already exists
     */
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        log.info("Processing registration for email: {}", request.getEmail());

        // Check for duplicate email
        if (userRepository.existsByEmail(request.getEmail())) {
            log.warn("Registration failed — email already exists: {}", request.getEmail());
            throw new RuntimeException("An account with this email already exists");
        }

        // Create the User entity
        User user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))  // BCrypt hash
                .role(Role.CANDIDATE)  // All registrations create CANDIDATE users
                .build();

        // Create a skeleton Profile linked to this user
        Profile profile = Profile.builder()
                .user(user)
                .fullName(request.getFullName())
                .isShortlisted(false)
                .build();

        // Link profile to user (bidirectional relationship)
        user.setProfile(profile);

        // Save user (profile is saved automatically via cascade = ALL)
        userRepository.save(user);

        log.info("Successfully registered new user: {} with role: {}", user.getEmail(), user.getRole());

        return AuthResponse.builder()
                .email(user.getEmail())
                .role(user.getRole().name())
                .message("Registration successful! Please login to continue.")
                .build();
    }

    /**
     * Authenticate a user and generate a JWT token.
     *
     * @param request contains email and password
     * @return AuthResponse with JWT token and user details
     * @throws BadCredentialsException if email or password is incorrect
     */
    public AuthResponse login(LoginRequest request) {
        log.info("Processing login for email: {}", request.getEmail());

        try {
            // Authenticate using Spring Security
            // This calls CustomUserDetailsService.loadUserByUsername() internally
            // and compares passwords using BCryptPasswordEncoder
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getEmail(),
                            request.getPassword()
                    )
            );

            // Generate JWT token from the authenticated user
            String token = tokenProvider.generateToken(authentication);

            // Look up the user to get their role
            User user = userRepository.findByEmail(request.getEmail())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            log.info("User logged in successfully: {} with role: {}", user.getEmail(), user.getRole());

            return AuthResponse.builder()
                    .token(token)
                    .tokenType("Bearer")
                    .email(user.getEmail())
                    .role(user.getRole().name())
                    .message("Login successful!")
                    .build();

        } catch (BadCredentialsException ex) {
            log.warn("Login failed for email: {} — bad credentials", request.getEmail());
            throw new BadCredentialsException("Invalid email or password");
        }
    }
}
