package com.ats.scheduler;

import com.ats.service.EmailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.*;

@Component
@EnableScheduling
public class JobAutomationScheduler {

    private static final Logger log = LoggerFactory.getLogger(JobAutomationScheduler.class);

    private final RestTemplate restTemplate;
    private final EmailService emailService;

    @Value("${ai.service.url}")
    private String aiServiceUrl;

    // Hardcoded profile summary/resume text as fallback if resume isn't pulled from DB
    private final String defaultResumeText = "Vaibhav Vaibhav. Student B.Sc. Applied Artificial Intelligence at TH Rosenheim. " +
            "Grade 1.0 in Einführung in die Künstliche Intelligenz. Skills: Python, PyTorch, LLMs, NLP, Java, SQL, " +
            "Docker, Git, Analysis, Lineare Algebra. Projects: ATS Resume Matcher, NLP web applications.";

    public JobAutomationScheduler(RestTemplate restTemplate, EmailService emailService) {
        this.restTemplate = restTemplate;
        this.emailService = emailService;
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> parseJobsResponse(Object responseBody) {
        return (List<Map<String, Object>>) responseBody;
    }

    // Inside com.ats.scheduler.JobAutomationScheduler

public void runPipelineWithParams(String query, String location, int radius, int maxResults) {
    log.info("Fetching up to {} jobs for Query: '{}', Location: '{}', Radius: {} miles", 
            maxResults, query, location, radius);

    try {
        // Normalize base URL
        String baseUrl = aiServiceUrl.trim();
        if (baseUrl.endsWith("/")) {
            baseUrl = baseUrl.substring(0, baseUrl.length() - 1);
        }
        if (baseUrl.endsWith("/analyze")) {
            baseUrl = baseUrl.replace("/analyze", "");
        }

        // Construct target endpoint
        String url = String.format("%s/api/ai/scrape-jobs?query=%s&location=%s&distance=%d&max_results=%d",
                baseUrl,
                query,
                location,
                radius,
                maxResults
        );

        log.info("Requesting jobs from AI Microservice: {}", url);

        List<Map<String, Object>> scrapedJobs = restTemplate.getForObject(url, List.class);

        if (scrapedJobs == null || scrapedJobs.isEmpty()) {
            log.info("No jobs found for location '{}' within {} miles.", location, radius);
            return;
        }

        log.info("Retrieved {} total job postings. Evaluating AI match scores...", scrapedJobs.size());

        List<Map<String, Object>> topMatches = processAndScoreJobs(scrapedJobs);

        if (!topMatches.isEmpty()) {
            emailService.sendJobMatchesDigest(topMatches);
            log.info("Sent email digest containing {} high-scoring matches.", topMatches.size());
        } else {
            log.info("No scraped jobs met the target match score threshold.");
        }
    } catch (Exception e) {
        log.error("Failed executing automated job search: {}", e.getMessage(), e);
    }
}

    // Triggers daily at 09:00 AM CEST
    @Scheduled(cron = "0 0 9 * * *")
    public void executeDailyJobPipeline() {
        log.info("Starting daily automated job scraping & evaluation pipeline...");
        runPipelineWithParams("Künstliche Intelligenz", "Rosenheim, Germany", 50, 50);
    }

    /**
     * Sends each job to the Python /analyze endpoint and returns matches scoring >= threshold.
     */
    private List<Map<String, Object>> processAndScoreJobs(List<Map<String, Object>> jobs) {
        List<Map<String, Object>> topMatches = new ArrayList<>();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        for (Map<String, Object> job : jobs) {
            try {
                String title = (String) job.getOrDefault("title", "");
                String company = (String) job.getOrDefault("company", "");
                String description = (String) job.getOrDefault("description", "");

                // Combine title & company if description is sparse
                String jobText = (description == null || description.isBlank()) ? title + " " + company : description;

                Map<String, String> requestPayload = Map.of(
                        "resumeText", defaultResumeText,
                        "jobDescription", jobText
                );

                HttpEntity<Map<String, String>> entity = new HttpEntity<>(requestPayload, headers);
                
                ResponseEntity<Map> response = restTemplate.postForEntity(
                        aiServiceUrl + "/analyze", 
                        entity, 
                        Map.class
                );

                if (response.getBody() != null && response.getBody().containsKey("matchScore")) {
                    double score = ((Number) response.getBody().get("matchScore")).doubleValue();
                    job.put("score", score);

                    log.debug("Evaluated '{}' at {} - Match Score: {}%", title, company, score);

                    // Threshold filter (e.g., 20% or higher for TF-IDF raw scores)
                    if (score >= 0.0) {
                        topMatches.add(job);
                    }
                }
            } catch (Exception e) {
                log.warn("Failed to evaluate job match for '{}': {}", job.get("title"), e.getMessage());
            }
        }

        // Sort results descending by score
        topMatches.sort((a, b) -> Double.compare(
                ((Number) b.get("score")).doubleValue(),
                ((Number) a.get("score")).doubleValue()
        ));

        return topMatches;
    }
}