/**
 * Security Layer — Authentication and Authorization.
 *
 * This package contains everything related to securing the application:
 *
 *   - SecurityConfig:       Main Spring Security configuration class.
 *                           Defines which endpoints are public vs. protected,
 *                           sets up CORS, CSRF, and session management.
 *
 *   - JwtTokenProvider:     Utility class for creating and validating JWT tokens.
 *                           Signs tokens with our secret key and extracts claims.
 *
 *   - JwtAuthenticationFilter: A filter that intercepts every HTTP request,
 *                              checks for a JWT token in the Authorization header,
 *                              validates it, and sets the security context.
 *
 *   - CustomUserDetailsService: Loads user data from the database for Spring Security.
 *                                Implements UserDetailsService interface.
 *
 *   - JwtAuthEntryPoint:   Handles unauthorized access attempts (returns 401).
 */
package com.smarthire.security;
