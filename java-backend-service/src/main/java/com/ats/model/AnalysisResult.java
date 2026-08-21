package com.ats.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import java.util.List;

@Entity
@Table(name = "analysis_results")
@JsonIgnoreProperties(ignoreUnknown = true)
public class AnalysisResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonProperty("match_score")
    private Double matchScore;

    @ElementCollection
    @JsonProperty("missing_keywords")
    private List<String> missingKeywords;

    // Required default constructor for Jackson & JPA
    public AnalysisResult() {}

    public AnalysisResult(Double matchScore, List<String> missingKeywords) {
        this.matchScore = matchScore;
        this.missingKeywords = missingKeywords;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Double getMatchScore() { return matchScore; }
    public void setMatchScore(Double matchScore) { this.matchScore = matchScore; }

    public List<String> getMissingKeywords() { return missingKeywords; }
    public void setMissingKeywords(List<String> missingKeywords) { this.missingKeywords = missingKeywords; }
}