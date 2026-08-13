package com.ats.Repository; // (or com.ats.repository if you renamed the folder)

import com.ats.model.AnalysisResult; // 1. Add this import for your model!
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AnalysisResultRepository extends JpaRepository<AnalysisResult, Long> { 
    // 2. Replace 'YourEntityClass' with 'AnalysisResult' above ^
}