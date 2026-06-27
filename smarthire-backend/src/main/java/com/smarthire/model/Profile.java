package com.smarthire.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * Profile Entity — Maps to the 'profiles' table in MySQL.
 *
 * This table stores a candidate's personal and academic information.
 * Only CANDIDATE users have profiles (ADMIN users don't need them).
 *
 * Fields:
 *   - fullName:      Candidate's full name
 *   - phone:         Contact number
 *   - branch:        Academic branch (e.g., "Computer Science", "Electronics")
 *   - percentage:    Academic percentage/CGPA (stored as Double for decimal support)
 *   - skills:        Comma-separated skill list (e.g., "Java, Spring Boot, React")
 *   - isShortlisted: Flag set by ADMIN when candidate is selected
 *
 * Relationships:
 *   - Belongs to one User (via @OneToOne with @JoinColumn)
 *   - Has one Resume (child side, mapped by Profile)
 *   - Has one AIResult (child side, mapped by Profile)
 *
 * Design Decisions:
 *   - Skills as comma-separated String: Simple, queryable, no need for a separate table
 *     for this project's scope. In a larger system, you'd use a many-to-many relationship.
 *   - isShortlisted defaults to false: Candidates start as not-shortlisted
 *   - @JoinColumn on this side: Profile owns the FK (user_id) to Users table
 */
@Entity
@Table(name = "profiles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Profile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Owning side of the User ↔ Profile relationship.
     * This entity holds the foreign key column 'user_id'.
     * - @JoinColumn creates the 'user_id' FK column in the profiles table
     * - nullable = false: Every profile MUST belong to a user
     * - unique = true: One user can have only one profile (enforced at DB level)
     */
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(name = "full_name", nullable = false, length = 100)
    private String fullName;

    @Column(length = 20)
    private String phone;

    @Column(length = 50)
    private String branch;

    @Column
    private Double percentage;

    /**
     * Skills stored as a comma-separated string.
     * Example: "Java, Spring Boot, React, MySQL, Docker"
     * Length 500 allows for many skills to be listed.
     */
    @Column(length = 500)
    private String skills;

    /**
     * Shortlist flag — set to true by ADMIN when this candidate
     * is selected for further consideration. Defaults to false.
     */
    @Column(name = "is_shortlisted", nullable = false)
    @Builder.Default
    private Boolean isShortlisted = false;

    /**
     * One-to-One with Resume.
     * - mappedBy = "profile": Resume entity owns the FK
     * - cascade ALL: Deleting a profile deletes its resume
     * - LAZY: Resume data (especially extracted text) is large, don't load unless needed
     */
    @OneToOne(mappedBy = "profile", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Resume resume;

    /**
     * One-to-One with AIResult.
     * - mappedBy = "profile": AIResult entity owns the FK
     * - cascade ALL: Deleting a profile deletes its AI results
     * - LAZY: AI result data can be large, load on demand
     */
    @OneToOne(mappedBy = "profile", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private AIResult aiResult;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
