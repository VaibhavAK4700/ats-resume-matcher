package com.ats.model;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
@Table(
    name = "analysis_results",
    indexes = {
        @Index(name = "idx_analysis_resume", columnList = "resume_id"),
        @Index(name = "idx_analysis_job", columnList = "job_posting_id")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class AnalysisResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "analysis_id")
    @JsonProperty("analysisId")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "resume_id", nullable = false)
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private Resume resume;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "job_posting_id", nullable = false)
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private JobPosting jobPosting;

    @Column(name = "match_score", nullable = false)
    @JsonProperty("matchScore")
    @JsonAlias("match_score")
    private int matchScore;

    @Column(name = "reasoning", columnDefinition = "TEXT")
    @JsonProperty("reasoning")
    @JsonAlias("reasoning")
    private String reasoning;

    @Column(name = "tailored_summary", columnDefinition = "TEXT")
    @JsonProperty("tailoredSummary")
    @JsonAlias("tailored_summary")
    private String tailoredSummary;

    @Column(name = "tailored_bullets", columnDefinition = "TEXT")
    @JsonProperty("tailoredBullets")
    @JsonAlias("tailored_bullets")
    private String tailoredBullets;

    @Column(name = "tailored_resume_text", columnDefinition = "TEXT")
    @JsonProperty("tailoredResumeText")
    @JsonAlias("tailored_resume_text")
    private String tailoredResumeText;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
        name = "analysis_missing_keywords",
        joinColumns = @JoinColumn(name = "analysis_id")
    )
    @Column(name = "keyword", nullable = false)
    @JsonProperty("missingKeywords")
    @JsonAlias("missing_keywords")
    private List<String> missingKeywords = new ArrayList<>();

    @Column(name = "created_at", nullable = false, updatable = false)
    @JsonProperty("createdAt")
    private OffsetDateTime createdAt;

    @JsonProperty("resumeId")
    public Long getResumeId() {
        return resume != null ? resume.getId() : null;
    }

    @JsonProperty("jobPostingId")
    public Long getJobPostingId() {
        return jobPosting != null ? jobPosting.getId() : null;
    }

    @PrePersist
    protected void onCreate() {
        if (this.createdAt == null) {
            this.createdAt = OffsetDateTime.now();
        }
    }

    public boolean isHighMatch() {
        return this.matchScore >= 65;
    }

    public AnalysisResult(Resume resume, JobPosting jobPosting, int matchScore, String reasoning, String tailoredSummary, String tailoredBullets, List<String> missingKeywords) {
        this.resume = resume;
        this.jobPosting = jobPosting;
        this.matchScore = matchScore;
        this.reasoning = reasoning;
        this.tailoredSummary = tailoredSummary;
        this.tailoredBullets = tailoredBullets;
        this.missingKeywords = missingKeywords != null ? missingKeywords : new ArrayList<>();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AnalysisResult that = (AnalysisResult) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}