package com.smarthire.repository;

import com.smarthire.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * UserRepository — Database access for the 'users' table.
 *
 * By extending JpaRepository<User, Long>, we automatically get:
 *   - save(user)          → INSERT or UPDATE
 *   - findById(id)        → SELECT by primary key
 *   - findAll()           → SELECT all users
 *   - deleteById(id)      → DELETE by primary key
 *   - count()             → COUNT all rows
 *   - existsById(id)      → Check if a row exists
 *
 * The custom methods below follow Spring Data's naming convention.
 * Spring reads the method name and auto-generates the SQL query:
 *   - findByEmail(email)  → SELECT * FROM users WHERE email = ?
 *   - existsByEmail(email) → SELECT COUNT(*) > 0 FROM users WHERE email = ?
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Find a user by their email address.
     * Used during login to look up the user's credentials.
     *
     * @param email the email to search for
     * @return Optional<User> — empty if no user found, present if found
     */
    Optional<User> findByEmail(String email);

    /**
     * Check if a user with the given email already exists.
     * Used during registration to prevent duplicate accounts.
     *
     * @param email the email to check
     * @return true if a user with this email exists, false otherwise
     */
    Boolean existsByEmail(String email);
}
