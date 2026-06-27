package com.smarthire.security;

import com.smarthire.model.User;
import com.smarthire.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.Collections;

/**
 * CustomUserDetailsService — Loads user data from MySQL for Spring Security.
 *
 * Spring Security needs a way to look up users during authentication.
 * This service implements UserDetailsService (a Spring Security interface)
 * and provides the loadUserByUsername() method.
 *
 * How it works:
 *   1. User submits email + password to POST /api/auth/login
 *   2. Spring Security calls loadUserByUsername(email)
 *   3. We query the database for a user with that email
 *   4. We return a UserDetails object containing the email, hashed password, and role
 *   5. Spring Security compares the submitted password with the stored hash
 *   6. If they match, authentication succeeds
 *
 * Why "loadUserByUsername" when we use email?
 *   - It's a Spring Security interface method — the name is fixed
 *   - We treat email as the "username" in our system
 *
 * The ROLE_ prefix:
 *   - Spring Security expects roles to have the "ROLE_" prefix internally
 *   - So CANDIDATE becomes ROLE_CANDIDATE, ADMIN becomes ROLE_ADMIN
 *   - When we use @PreAuthorize("hasRole('ADMIN')"), Spring auto-adds the prefix
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    /**
     * Load a user by their email address (which serves as our "username").
     *
     * @param email the email to look up
     * @return UserDetails containing credentials and authorities
     * @throws UsernameNotFoundException if no user found with this email
     */
    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        log.debug("Attempting to load user by email: {}", email);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    log.warn("User not found with email: {}", email);
                    return new UsernameNotFoundException("User not found with email: " + email);
                });

        log.debug("User found: {} with role: {}", user.getEmail(), user.getRole());

        return new org.springframework.security.core.userdetails.User(
                user.getEmail(),
                user.getPassword(),
                getAuthorities(user)
        );
    }

    /**
     * Convert our Role enum into Spring Security's GrantedAuthority format.
     * CANDIDATE → ROLE_CANDIDATE
     * ADMIN → ROLE_ADMIN
     */
    private Collection<? extends GrantedAuthority> getAuthorities(User user) {
        return Collections.singletonList(
                new SimpleGrantedAuthority("ROLE_" + user.getRole().name())
        );
    }
}
