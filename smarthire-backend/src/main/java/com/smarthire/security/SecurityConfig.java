package com.smarthire.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

/**
 * SecurityConfig — The master security configuration for SmartHire AI.
 *
 * This class configures:
 *   1. CORS (Cross-Origin Resource Sharing) — lets React (port 3000) talk to Spring (port 8080)
 *   2. CSRF protection — disabled because we use JWT (stateless, no cookies)
 *   3. Session management — STATELESS (no server-side sessions, JWT only)
 *   4. URL-based authorization rules — which endpoints require which roles
 *   5. JWT filter integration — our custom filter runs before Spring's default filter
 *   6. Password encoder — BCrypt for hashing passwords
 *   7. Authentication provider — connects UserDetailsService + PasswordEncoder
 *
 * URL Authorization Rules:
 *   PUBLIC (no token needed):
 *     - POST /api/auth/**           (register, login)
 *     - GET  /swagger-ui/**         (API documentation)
 *     - GET  /api-docs/**           (OpenAPI spec)
 *
 *   CANDIDATE ONLY:
 *     - /api/candidate/**           (profile, resume, analysis)
 *
 *   ADMIN ONLY:
 *     - /api/admin/**               (dashboard, shortlist, export, notify)
 *
 *   ALL AUTHENTICATED:
 *     - Everything else requires a valid token (any role)
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity   // Enables @PreAuthorize on controller methods
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final JwtAuthEntryPoint jwtAuthEntryPoint;
    private final CustomUserDetailsService customUserDetailsService;

    /**
     * Main security filter chain — configures all HTTP security rules.
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // Enable CORS with our custom configuration
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                // Disable CSRF — not needed for stateless JWT authentication
                // CSRF protection is for cookie-based sessions, which we don't use
                .csrf(csrf -> csrf.disable())

                // Handle unauthorized access attempts with our custom entry point
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint(jwtAuthEntryPoint)
                )

                // Stateless session — no session cookies, every request must include JWT
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                // URL-based authorization rules
                .authorizeHttpRequests(auth -> auth
                        // Public endpoints — no authentication required
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers("/swagger-ui/**", "/swagger-ui.html").permitAll()
                        .requestMatchers("/api-docs/**", "/v3/api-docs/**").permitAll()
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        // Role-based access control
                        .requestMatchers("/api/candidate/**").hasRole("CANDIDATE")
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")

                        // Everything else requires authentication (any role)
                        .anyRequest().authenticated()
                )

                // Set up the authentication provider
                .authenticationProvider(authenticationProvider())

                // Add our JWT filter BEFORE Spring's default username/password filter
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * CORS Configuration — Allow React frontend to communicate with the backend.
     *
     * Without CORS, the browser blocks requests from http://localhost:3000 (React)
     * to http://localhost:8080 (Spring) because they're different "origins."
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // Allow requests from the React dev server and common deployment URLs
        configuration.setAllowedOrigins(Arrays.asList(
                "http://localhost:3000",        // React dev server
                "http://localhost:5173",        // Vite dev server
                "http://localhost:8080"         // Same origin (for Swagger UI)
        ));

        // Allow all standard HTTP methods
        configuration.setAllowedMethods(Arrays.asList(
                "GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"
        ));

        // Allow all headers (including Authorization for JWT)
        configuration.setAllowedHeaders(List.of("*"));

        // Allow credentials (cookies, authorization headers)
        configuration.setAllowCredentials(true);

        // How long the browser should cache CORS preflight responses (1 hour)
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    /**
     * BCrypt Password Encoder — Hashes passwords before storing in the database.
     *
     * BCrypt is a one-way hashing algorithm that:
     *   - Automatically generates a random salt for each password
     *   - Is computationally expensive (slow on purpose) to prevent brute-force attacks
     *   - Is the industry standard for password hashing
     *
     * Example: "securePass123" → "$2a$10$N9qo8uLOickgx2ZMRZoMy..."
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Authentication Provider — Connects UserDetailsService + PasswordEncoder.
     *
     * When a user tries to login:
     *   1. DaoAuthenticationProvider calls customUserDetailsService.loadUserByUsername()
     *   2. It gets the stored BCrypt hash from the database
     *   3. It uses BCryptPasswordEncoder to compare submitted password with the hash
     *   4. If they match → authentication success
     *   5. If they don't → authentication failure
     */
    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(customUserDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    /**
     * Authentication Manager — Used by AuthService to authenticate login requests.
     *
     * We extract it from the AuthenticationConfiguration so we can inject it
     * into our AuthService and call authenticationManager.authenticate() manually.
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }
}
