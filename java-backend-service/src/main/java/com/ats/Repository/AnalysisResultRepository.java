package com.ats.repository;

import com.ats.model.AnalysisResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface AnalysisResultRepository extends JpaRepository<AnalysisResult, Long> {

    @Query("SELECT a FROM AnalysisResult a WHERE a.resume.id = :resumeId AND a.jobPosting.id = :jobPostingId")
    Optional<AnalysisResult> findByResumeIdAndJobPostingId(
        @Param("resumeId") Long resumeId, 
        @Param("jobPostingId") Long jobPostingId
    );
}