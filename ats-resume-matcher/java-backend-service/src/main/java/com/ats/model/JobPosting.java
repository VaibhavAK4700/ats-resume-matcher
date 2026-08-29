package com.ats.model;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.time.OffsetDateTime;
import java.util.Objects;

@Entity
@Table(name = "job_postings", indexes = {
    @Index(name = "idx_job_title", columnList = "title"),
    @Index(name = "idx_job_company", columnList = "company_name"),
    @Index(name = "idx_job_url", columnList = "job_url", unique = true)
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class JobPosting {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonProperty("id")
    private Long id;

    @NotBlank(message = "Job title cannot be empty")
    @Column(name = "title", nullable = false)
    @JsonProperty("title")
    private String title;

    @Column(name = "company_name")
    @JsonProperty("companyName")
    @JsonAlias({"company_name", "company"})
    private String companyName;

    @Column(name = "location")
    @JsonProperty("location")
    private String location;

    @Column(name = "job_url", unique = true, length = 1000, nullable = true)
    @JsonProperty("jobUrl")
    @JsonAlias({"job_url", "url", "link"})
    private String jobUrl;

    @Column(name = "source")
    @JsonProperty("source")
    @JsonAlias("site")
    private String source;

    @NotBlank(message = "Job description cannot be empty")
    @Column(name = "description", nullable = false, columnDefinition = "TEXT")
    @JsonProperty("description")
    private String description;

    @Column(name = "created_at", nullable = false, updatable = false)
    @JsonProperty("createdAt")
    private OffsetDateTime createdAt;

    @Column(name = "updated_at")
    @JsonProperty("updatedAt")
    private OffsetDateTime updatedAt;

    // Direct alias getters and setters for backward compatibility with repositories/controllers
    public String getUrl() {
        return this.jobUrl;
    }

    public void setUrl(String url) {
        this.jobUrl = url;
    }

    @PrePersist
    protected void onCreate() {
        if (this.createdAt == null) {
            this.createdAt = OffsetDateTime.now();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = OffsetDateTime.now();
    }

    public String getDisplayCompany() {
        return companyName != null && !companyName.isBlank() ? companyName : "Unknown Company";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        JobPosting that = (JobPosting) o;
        return Objects.equals(id, that.id) || Objects.equals(jobUrl, that.jobUrl);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, jobUrl);
    }

    @Override
    public String toString() {
        return "JobPosting{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", companyName='" + companyName + '\'' +
                ", source='" + source + '\'' +
                ", jobUrl='" + jobUrl + '\'' +
                '}';
    }
}