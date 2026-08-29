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
@Table(name = "resumes", indexes = {
    @Index(name = "idx_base_resume", columnList = "is_base_resume")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class Resume {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonProperty("id")
    private Long id;

    @NotBlank(message = "File name cannot be empty")
    @Column(name = "file_name", nullable = false)
    @JsonProperty("fileName")
    @JsonAlias("file_name")
    private String fileName;

    @Column(name = "file_type")
    @JsonProperty("fileType")
    @JsonAlias("file_type")
    private String fileType;

    @Column(name = "file_size")
    @JsonProperty("fileSize")
    @JsonAlias("file_size")
    private Long fileSize;

    @Column(name = "storage_path")
    @JsonProperty("storagePath")
    @JsonAlias("storage_path")
    private String storagePath;

    @Column(name = "candidate_email")
    @JsonProperty("candidateEmail")
    @JsonAlias("candidate_email")
    private String candidateEmail;

    @Column(name = "is_base_resume", nullable = false)
    @JsonProperty("isBaseResume")
    @JsonAlias("is_base_resume")
    private Boolean isBaseResume = false;

    // Explicit getter for Boolean object wrapper
    public Boolean getIsBaseResume() {
        return this.isBaseResume;
    }

    public void setIsBaseResume(Boolean isBaseResume) {
        this.isBaseResume = isBaseResume;
    }
    @Lob
    @Column(name = "template_data")
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private byte[] templateData;

    @Column(name = "extracted_text", nullable = false, columnDefinition = "TEXT")
    @JsonProperty("extractedText")
    @JsonAlias("extracted_text")
    private String extractedText;

    @Column(name = "created_at", nullable = false, updatable = false)
    @JsonProperty("createdAt")
    private OffsetDateTime createdAt;

    @Column(name = "updated_at")
    @JsonProperty("updatedAt")
    private OffsetDateTime updatedAt;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Resume resume = (Resume) o;
        return Objects.equals(id, resume.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Resume{" +
                "id=" + id +
                ", fileName='" + fileName + '\'' +
                ", isBaseResume=" + isBaseResume +
                ", createdAt=" + createdAt +
                '}';
    }
}