package com.smarthire.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * User Entity — Maps to the 'users' table in MySQL.
 *
 * This table stores authentication data for ALL users (both candidates and admins).
 * It contains:
 *   - email:    Used as the unique login identifier (not a username)
 *   - password: BCrypt-hashed password (NEVER stored in plain text)
 *   - role:     Either CANDIDATE or ADMIN, stored as a string
 *
 * Relationships:
 *   - One User has One Profile (only for CANDIDATE users)
 *   - The Profile is the "child" side, meaning Profile holds the foreign key (user_id)
 *
 * Design Decisions:
 *   - Email as login ID: More natural than usernames, avoids duplicates, useful for notifications
 *   - BCrypt for passwords: One-way hash with built-in salt, industry standard
 *   - Timestamps: created_at and updated_at are auto-managed by Hibernate
 *   - cascade = ALL: When a user is deleted, their profile is also deleted
 *   - orphanRemoval: If you un-link a profile from a user, it gets deleted from DB
 */
@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @Column(nullable = false)
    private String password;

    /**
     * @Enumerated(EnumType.STRING) stores the role as "CANDIDATE" or "ADMIN" text
     * in the database, instead of 0 or 1 (which would be EnumType.ORDINAL).
     * STRING is safer because adding new roles won't break existing data.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Role role;

    /**
     * One-to-One relationship with Profile.
     * - mappedBy = "user": The Profile entity owns the relationship (has the FK column)
     * - cascade = ALL: Any save/update/delete on User cascades to its Profile
     * - orphanRemoval: If profile is set to null, the orphaned Profile row is deleted
     * - fetch = LAZY: Profile is NOT loaded from DB until explicitly accessed (performance)
     */
    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Profile profile;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
