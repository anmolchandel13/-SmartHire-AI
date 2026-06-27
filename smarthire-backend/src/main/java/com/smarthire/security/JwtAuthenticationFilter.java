package com.smarthire.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * JwtAuthenticationFilter — Intercepts every HTTP request to check for JWT tokens.
 *
 * This filter runs ONCE per request (extends OncePerRequestFilter) and does:
 *   1. Extract the JWT token from the "Authorization" header
 *   2. Validate the token (signature + expiration)
 *   3. Extract the user's email and roles from the token
 *   4. Set the Spring Security authentication context
 *
 * The flow:
 *   Request → JwtFilter → SecurityContext → Controller
 *
 *   If token is valid:   SecurityContext is set → request proceeds to controller
 *   If token is missing: SecurityContext stays empty → Spring Security handles access
 *   If token is invalid: SecurityContext stays empty → returns 401 Unauthorized
 *
 * The "Authorization" header format:
 *   Authorization: Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWI...
 *   ^^^^^^^^       ^^^^^^ ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
 *   Header name    Prefix  JWT token
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider tokenProvider;
    private final CustomUserDetailsService customUserDetailsService;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        try {
            // Step 1: Extract token from the Authorization header
            String jwt = getJwtFromRequest(request);

            // Step 2: If token exists and is valid, set up authentication
            if (StringUtils.hasText(jwt) && tokenProvider.validateToken(jwt)) {

                // Step 3: Extract user info from the token
                String email = tokenProvider.getEmailFromToken(jwt);
                String roles = tokenProvider.getRolesFromToken(jwt);

                // Step 4: Convert roles string into Spring Security authorities
                List<SimpleGrantedAuthority> authorities = Arrays.stream(roles.split(","))
                        .map(SimpleGrantedAuthority::new)
                        .collect(Collectors.toList());

                // Step 5: Load full user details from database
                UserDetails userDetails = customUserDetailsService.loadUserByUsername(email);

                // Step 6: Create authentication token and set it in the security context
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                userDetails,    // The authenticated user
                                null,           // Credentials (null because we already validated)
                                authorities     // The user's roles/permissions
                        );
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                // Step 7: Tell Spring Security "this user is authenticated"
                SecurityContextHolder.getContext().setAuthentication(authentication);

                log.debug("Authenticated user: {} with roles: {}", email, roles);
            }
        } catch (Exception ex) {
            log.error("Could not set user authentication in security context: {}", ex.getMessage());
        }

        // Step 8: Continue the filter chain (pass request to next filter or controller)
        filterChain.doFilter(request, response);
    }

    /**
     * Extract JWT token from the Authorization header.
     *
     * Expected format: "Bearer eyJhbGciOi..."
     * We strip the "Bearer " prefix (7 characters) to get the raw token.
     *
     * @param request the HTTP request
     * @return the JWT token string, or null if not present
     */
    private String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");

        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }

        return null;
    }
}
