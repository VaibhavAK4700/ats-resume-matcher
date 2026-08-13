package com.ats.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;

@Entity
@Table(name = "analysis_results")
public class AnalysisResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonProperty("match_score")
    private Double matchScore;

    @JsonProperty("missing_keywords")
    private String missingKeywords;

    public AnalysisResult() {}

    public Long getId() { 
        return id; 
    }
    
    public void setId(Long id) { 
        this.id = id; 
    }

    public Double getMatchScore() { 
        return matchScore; 
    }
    
    public void setMatchScore(Double matchScore) { 
        this.matchScore = matchScore; 
    }

    public String getMissingKeywords() { 
        return missingKeywords; 
    }
    
    public void setMissingKeywords(String missingKeywords) { 
        this.missingKeywords = missingKeywords; 
    }
}