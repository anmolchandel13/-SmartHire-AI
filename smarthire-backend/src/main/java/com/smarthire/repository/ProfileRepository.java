package com.smarthire.repository;

import com.smarthire.model.Profile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * ProfileRepository — Database access for the 'profiles' table.
 *
 * Extends both:
 *   - JpaRepository: Standard CRUD operations
 *   - JpaSpecificationExecutor: Enables dynamic, complex queries at runtime
 *     (used by Admin dashboard for filtering by branch, skills, score, etc.)
 *
 * Custom methods use Spring Data's naming convention for auto-generated SQL.
 */
@Repository
public interface ProfileRepository extends JpaRepository<Profile, Long>, JpaSpecificationExecutor<Profile> {

    /**
     * Find a profile by its associated user ID.
     * Used when a logged-in candidate wants to view/update their profile.
     * The JWT token gives us the userId, and we use it to find their profile.
     */
    Optional<Profile> findByUserId(Long userId);

    /**
     * Check if a profile already exists for a given user.
     * Prevents candidates from creating duplicate profiles.
     */
    Boolean existsByUserId(Long userId);

    /**
     * Find all profiles in a specific academic branch.
     * Used by Admin to filter candidates.
     * Example: findByBranch("Computer Science")
     */
    List<Profile> findByBranch(String branch);

    /**
     * Find all shortlisted candidates.
     * Used by Admin to view/export/notify shortlisted candidates.
     */
    List<Profile> findByIsShortlistedTrue();

    /**
     * Find profiles where the skills field contains a specific keyword.
     * Uses SQL LIKE '%keyword%' under the hood.
     * Example: findBySkillsContainingIgnoreCase("java")
     */
    List<Profile> findBySkillsContainingIgnoreCase(String skill);
}
