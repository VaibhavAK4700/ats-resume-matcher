package com.ats.service;

import com.ats.exception.BaseResumeNotFoundException;
import com.ats.exception.FileProcessingException;
import com.ats.exception.ResourceNotFoundException;
import com.ats.model.AnalysisResult;
import com.ats.model.JobPosting;
import com.ats.model.Resume;
import com.ats.repository.AnalysisResultRepository;
import com.ats.repository.JobPostingRepository;
import com.ats.repository.ResumeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class AutomatedAtsService {

    private final ResumeRepository resumeRepository;
    private final JobPostingRepository jobPostingRepository;
    private final AnalysisResultRepository analysisResultRepository;
    private final NotificationService notificationService;
    private final PdfGeneratorService pdfGeneratorService;
    private final PdfParserService pdfParserService;
    private final RestTemplate restTemplate;

    @Value("${ai.service.url:http://localhost:8000}")
    private String aiServiceUrl;

    /**
     * Scores a job posting against a base resume, persists the analysis, and dispatches an email alert
     * if the match score meets or exceeds the threshold.
     */
    @Transactional
    public Map<String, Object> processAndNotifyIfMatched(Long baseResumeId, JobPosting jobPosting, int matchThreshold) {
        Long nonNullBaseResumeId = Objects.requireNonNull(baseResumeId, "Base resume ID must not be null");
        JobPosting nonNullJobPosting = Objects.requireNonNull(jobPosting, "Job posting must not be null");
        Long nonNullJobPostingId = Objects.requireNonNull(nonNullJobPosting.getId(), "Job posting ID must not be null");

        Map<String, Object> analysisResultMap = analyzeSavedResumeAndJob(nonNullBaseResumeId, nonNullJobPostingId);
        int score = ((Number) analysisResultMap.getOrDefault("matchScore", 0)).intValue();

        log.info("Evaluated Job: '{}' at '{}' | Match Score: {}%", 
                jobPosting.getTitle(), jobPosting.getDisplayCompany(), score);

        if (score >= matchThreshold) {
            log.info("Score {}% meets or exceeds {}% threshold! Triggering email notification...", score, matchThreshold);
            
            try {
                String tailoredText = (String) analysisResultMap.getOrDefault("tailoredResumeText", "");
                byte[] pdfBytes = pdfGeneratorService.generatePdf(tailoredText);
                notificationService.sendTailoredResumeEmail(null, jobPosting.getTitle(), pdfBytes);
            } catch (Exception e) {
                log.error("Failed to generate PDF or send email notification: {}", e.getMessage(), e);
            }
        } else {
            log.info("Score {}% below threshold ({}%). Notification skipped.", score, matchThreshold);
        }

        return analysisResultMap;
    }

    /**
     * Analyzes match quality between a base resume and job posting, persisting the resulting AnalysisResult to SQL.
     */
    @Transactional
    public Map<String, Object> analyzeSavedResumeAndJob(Long baseResumeId, Long jobId) {
        Long nonNullBaseResumeId = Objects.requireNonNull(baseResumeId, "Base resume ID must not be null");
        Long nonNullJobId = Objects.requireNonNull(jobId, "Job ID must not be null");

        Resume baseResume = resumeRepository.findById(nonNullBaseResumeId)
                .orElseThrow(() -> new BaseResumeNotFoundException("Base Resume not found with ID: " + nonNullBaseResumeId));

        JobPosting jobPosting = jobPostingRepository.findById(nonNullJobId)
                .orElseThrow(() -> new ResourceNotFoundException("Job Posting not found with ID: " + nonNullJobId));

        Map<String, Object> aiResponse = calculateMatchScoreAndTailor(baseResume.getExtractedText(), jobPosting.getDescription());

        int matchScore = ((Number) aiResponse.getOrDefault("matchScore", 0)).intValue();
        String reasoning = (String) aiResponse.getOrDefault("reasoning", "No detailed reasoning provided.");
        String tailoredText = (String) aiResponse.getOrDefault("tailoredResumeText", baseResume.getExtractedText());
        String summary = (String) aiResponse.getOrDefault("tailoredSummary", "");
        String bullets = (String) aiResponse.getOrDefault("tailoredBullets", "");

        AnalysisResult analysisResult = new AnalysisResult(
                baseResume,
                jobPosting,
                matchScore,
                reasoning,
                summary,
                bullets,
                Collections.emptyList()
        );
        analysisResult.setTailoredResumeText(tailoredText);
        
        AnalysisResult savedResult = analysisResultRepository.save(analysisResult);

        Map<String, Object> response = new HashMap<>();
        response.put("analysisId", savedResult.getId());
        response.put("matchScore", matchScore);
        response.put("reasoning", reasoning);
        response.put("tailoredResumeText", tailoredText);
        response.put("jobId", jobId);
        response.put("resumeId", baseResumeId);

        return response;
    }

    /**
     * Generates a tailored PDF document from an uploaded file or the active master base resume.
     */
    @Transactional(readOnly = true)
    public byte[] processAndTailorResumePdf(MultipartFile file, Long jobId, String userEmail) {
        Long nonNullJobId = Objects.requireNonNull(jobId, "Job ID must not be null");

        JobPosting jobPosting = jobPostingRepository.findById(nonNullJobId)
                .orElseThrow(() -> new ResourceNotFoundException("Job Posting not found with ID: " + nonNullJobId));

        String rawText;
        if (file != null && !file.isEmpty()) {
            rawText = pdfParserService.extractText(file);
        } else {
            Resume baseResume = resumeRepository.findByIsBaseResumeTrue()
                    .orElseThrow(() -> new BaseResumeNotFoundException("No active Base Resume found for tailoring."));
            rawText = baseResume.getExtractedText();
        }

        Map<String, Object> aiResponse = calculateMatchScoreAndTailor(rawText, jobPosting.getDescription());
        String tailoredContent = (String) aiResponse.getOrDefault("tailoredResumeText", rawText);
        
        return pdfGeneratorService.generatePdf(tailoredContent);
    }

    /**
     * Connects to external Python AI microservice to analyze ATS metrics and return tailored content.
     */
    private Map<String, Object> calculateMatchScoreAndTailor(String resumeText, String jobDescription) {
        if (resumeText == null || jobDescription == null) {
            throw new FileProcessingException("Resume text and job description must not be null for scoring.");
        }

        try {
            Map<String, String> requestPayload = Map.of(
                    "resume_text", resumeText,
                    "job_description", jobDescription
            );

            HttpHeaders headers = new HttpHeaders();
            headers.set("Content-Type", "application/json");
            HttpEntity<Map<String, String>> request = new HttpEntity<>(requestPayload, headers);

            HttpMethod httpMethod = Objects.requireNonNull(HttpMethod.POST, "HTTP method must not be null");

            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                    aiServiceUrl + "/analyze",
                    httpMethod,
                    request,
                    new ParameterizedTypeReference<Map<String, Object>>() {}
            );

            return response.getBody() != null ? response.getBody() : Collections.emptyMap();
        } catch (Exception e) {
            log.error("Error communicating with Python AI microservice: {}", e.getMessage(), e);
            
            // Fallback response if microservice is unreachable
            Map<String, Object> fallback = new HashMap<>();
            fallback.put("matchScore", 50);
            fallback.put("reasoning", "AI microservice unreachable. Default score assigned.");
            fallback.put("tailoredResumeText", resumeText);
            return fallback;
        }
    }
}