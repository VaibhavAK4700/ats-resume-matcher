package com.ats.repository;

import com.ats.model.Resume;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ResumeRepository extends JpaRepository<Resume, Long> {

    /**
     * 1. FETCH MASTER BASE RESUME
     * Retrieves the single active base resume used by the automated scoring pipeline.
     */
    Optional<Resume> findByIsBaseResumeTrue();

    /**
     * Checks if an active base resume exists in the system.
     */
    boolean existsByIsBaseResumeTrue();

    /**
     * 2. CLEAR PREVIOUS BASE RESUMES
     * Deactivates all existing base resumes before setting a new master resume.
     * Called by ResumeController during upload.
     */
    @Modifying
    @Query("UPDATE Resume r SET r.isBaseResume = false WHERE r.isBaseResume = true")
    int clearPreviousBaseResumes();

    /**
     * 3. CANDIDATE LOOKUP
     * Finds resume records associated with a specific email.
     */
    Optional<Resume> findByCandidateEmail(String candidateEmail);

    /**
     * 4. JPQL KEYWORD SEARCH QUERY
     * Searches extracted resume body text using case-insensitive partial keyword match.
     */
    @Query("SELECT r FROM Resume r WHERE LOWER(r.extractedText) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<Resume> searchByExtractedTextKeyword(@Param("keyword") String keyword, Pageable pageable);
}