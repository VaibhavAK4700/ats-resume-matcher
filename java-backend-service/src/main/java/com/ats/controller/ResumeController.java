package com.ats.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import com.ats.Repository.AnalysisResultRepository;
import com.ats.model.AnalysisResult;

@RestController
@CrossOrigin(origins = "*")
public class ResumeController {

    private final AnalysisResultRepository repository;

    // Reads python.ai.service.url from application.properties, defaulting to Docker service name
    @Value("${python.ai.service.url:http://python-ai-service:8000}")
    private String pythonAiUrl;

    public ResumeController(AnalysisResultRepository repository) {
        this.repository = repository;
    }

    // Prevents 404 Whitelabel Error when opening root URL
    @GetMapping("/")
    public ResponseEntity<String> home() {
        return ResponseEntity.ok("ATS Resume Matcher API is up and running!");
    }

    // 1. Resume Match & Score Endpoint
    @PostMapping("/api/v1/resume/match")
    public ResponseEntity<?> matchResume(
            @RequestParam("resume") MultipartFile file,
            @RequestParam("jobDescription") String jobDescription) {
        try {
            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);

            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("file", file.getResource());
            body.add("job_text", jobDescription);

            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

            ResponseEntity<AnalysisResult> response = restTemplate.postForEntity(
                pythonAiUrl + "/analyze", 
                requestEntity, 
                AnalysisResult.class
            );

            AnalysisResult analysisResult = response.getBody();
            if (analysisResult == null) {
                return ResponseEntity.status(502).body("AI service returned no analysis result");
            }

            AnalysisResult savedResult = repository.save(analysisResult);
            return ResponseEntity.ok(savedResult);

        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error in match processing: " + e.getMessage());
        }
    }

    // 2. AI Resume Rephrase Endpoint
    @PostMapping("/api/v1/resume/rephrase")
    public ResponseEntity<String> rephraseResume(
            @RequestParam("resume") MultipartFile file,
            @RequestParam("userSkills") String userSkills) {
        try {
            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);

            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("file", file.getResource());
            body.add("user_skills", userSkills);

            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

            ResponseEntity<String> response = restTemplate.postForEntity(
                pythonAiUrl + "/rephrase", 
                requestEntity, 
                String.class
            );

            return ResponseEntity.ok(response.getBody());

        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error in rephrase processing: " + e.getMessage());
        }
    }
}