package com.smarthire.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.stream.Collectors;

/**
 * JwtTokenProvider — Creates, signs, and validates JWT tokens.
 *
 * A JWT token has three parts separated by dots:
 *   HEADER.PAYLOAD.SIGNATURE
 *
 *   Header:    Contains the algorithm (HS256) and token type (JWT)
 *   Payload:   Contains our custom data (called "claims"):
 *              - sub (subject): the user's email
 *              - roles: the user's role (ROLE_CANDIDATE or ROLE_ADMIN)
 *              - iat (issued at): when the token was created
 *              - exp (expiration): when the token expires
 *   Signature: A cryptographic hash of Header + Payload + Secret Key
 *              This prevents anyone from tampering with the token
 *
 * Example decoded payload:
 * {
 *     "sub": "john@example.com",
 *     "roles": "ROLE_CANDIDATE",
 *     "iat": 1700000000,
 *     "exp": 1700086400
 * }
 *
 * Security:
 *   - The secret key is stored in application.yml (and should be in env vars in production)
 *   - HS256 algorithm requires at least 256-bit key
 *   - Tokens are self-contained — the server doesn't store them
 */
@Component
@Slf4j
public class JwtTokenProvider {

    private final SecretKey secretKey;
    private final long expirationMs;

    /**
     * Constructor reads JWT configuration from application.yml.
     *
     * @Value injects the property value from the YAML file:
     *   app.jwt.secret → the signing key
     *   app.jwt.expiration-ms → token lifetime in milliseconds
     */
    public JwtTokenProvider(
            @Value("${app.jwt.secret}") String jwtSecret,
            @Value("${app.jwt.expiration-ms}") long expirationMs) {
        // Convert the base64-encoded secret string into a cryptographic key
        this.secretKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(
                java.util.Base64.getEncoder().encodeToString(jwtSecret.getBytes())
        ));
        this.expirationMs = expirationMs;
        log.info("JWT Token Provider initialized with expiration: {} ms", expirationMs);
    }

    /**
     * Generate a JWT token after successful authentication.
     *
     * @param authentication the authenticated user's details (from Spring Security)
     * @return a signed JWT token string
     */
    public String generateToken(Authentication authentication) {
        String email = authentication.getName();
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expirationMs);

        // Collect all roles into a comma-separated string
        String roles = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));

        String token = Jwts.builder()
                .subject(email)                    // Who this token belongs to
                .claim("roles", roles)             // What permissions they have
                .issuedAt(now)                     // When the token was created
                .expiration(expiryDate)            // When it expires
                .signWith(secretKey)               // Sign with our secret key
                .compact();                        // Build the final token string

        log.debug("Generated JWT token for user: {}, expires: {}", email, expiryDate);
        return token;
    }

    /**
     * Extract the email (subject) from a JWT token.
     *
     * @param token the JWT token string
     * @return the email address embedded in the token
     */
    public String getEmailFromToken(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();

        return claims.getSubject();
    }

    /**
     * Extract the roles from a JWT token.
     *
     * @param token the JWT token string
     * @return the roles string (e.g., "ROLE_CANDIDATE")
     */
    public String getRolesFromToken(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();

        return claims.get("roles", String.class);
    }

    /**
     * Validate a JWT token — check if it's properly signed and not expired.
     *
     * @param token the JWT token to validate
     * @return true if valid, false otherwise
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (MalformedJwtException ex) {
            log.error("Invalid JWT token: {}", ex.getMessage());
        } catch (ExpiredJwtException ex) {
            log.error("Expired JWT token: {}", ex.getMessage());
        } catch (UnsupportedJwtException ex) {
            log.error("Unsupported JWT token: {}", ex.getMessage());
        } catch (IllegalArgumentException ex) {
            log.error("JWT claims string is empty: {}", ex.getMessage());
        } catch (SecurityException ex) {
            log.error("JWT signature validation failed: {}", ex.getMessage());
        }
        return false;
    }
}
