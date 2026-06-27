package com.smarthire.repository;

import com.smarthire.model.AIResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * AIResultRepository — Database access for the 'ai_results' table.
 *
 * Simple repository for accessing AI-generated scorecards.
 * Each profile has at most one AI result (one-to-one relationship).
 */
@Repository
public interface AIResultRepository extends JpaRepository<AIResult, Long> {

    /**
     * Find the AI result (scorecard) for a specific profile.
     * Used when a candidate wants to view their AI analysis results.
     */
    Optional<AIResult> findByProfileId(Long profileId);

    /**
     * Check if an AI result already exists for a given profile.
     * If it does, re-running analysis should update the existing result
     * rather than creating a duplicate.
     */
    Boolean existsByProfileId(Long profileId);
}
