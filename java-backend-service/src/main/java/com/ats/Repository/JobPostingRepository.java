package com.ats.repository;

import com.ats.model.JobPosting;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface JobPostingRepository extends JpaRepository<JobPosting, Long> {

    // 1. URL Deduplication Methods (Primary check for scraper pipeline)
    boolean existsByJobUrl(String jobUrl);

    Optional<JobPosting> findByJobUrl(String jobUrl);

    // Alias query to directly support calls using findByUrl in ResumeController
    default Optional<JobPosting> findByUrl(String url) {
        return findByJobUrl(url);
    }

    // 2. Title & Company Deduplication (Secondary backup check)
    boolean existsByTitleIgnoreCaseAndCompanyNameIgnoreCase(String title, String companyName);

    Optional<JobPosting> findByTitleIgnoreCaseAndCompanyNameIgnoreCase(String title, String companyName);

    // 3. Search jobs by title (Case-insensitive)
    List<JobPosting> findByTitleContainingIgnoreCase(String title);

    // 4. Paginated search by company name
    Page<JobPosting> findByCompanyNameIgnoreCase(String companyName, Pageable pageable);

    // 5. Search jobs by location (Case-insensitive)
    List<JobPosting> findByLocationIgnoreCase(String location);

    // 6. Full-text keyword search across title, company, and description
    @Query("SELECT j FROM JobPosting j WHERE " +
           "LOWER(j.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(j.companyName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(j.description) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<JobPosting> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);

    // 7. Paginated recent job postings sorted by newest
    Page<JobPosting> findAllByOrderByCreatedAtDesc(Pageable pageable);
}