package com.smarthire.repository;

import com.smarthire.model.Resume;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * ResumeRepository — Database access for the 'resumes' table.
 *
 * Relatively simple repository since resume operations are straightforward:
 *   - Upload: save()
 *   - Retrieve: findByProfileId()
 *   - Check existence: existsByProfileId()
 */
@Repository
public interface ResumeRepository extends JpaRepository<Resume, Long> {

    /**
     * Find a resume by its associated profile ID.
     * Used to retrieve the extracted text for AI analysis
     * and to check if a resume has already been uploaded.
     */
    Optional<Resume> findByProfileId(Long profileId);

    /**
     * Check if a resume already exists for a given profile.
     * If it does, the new upload should replace the existing one.
     */
    Boolean existsByProfileId(Long profileId);
}
