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

    @Value("${python.ai.service.url:http://python-ai-service:8000}")
    private String pythonAiUrl;

    public ResumeController(AnalysisResultRepository repository) {
        this.repository = repository;
    }

    @GetMapping("/")
    public ResponseEntity<String> home() {
        return ResponseEntity.ok("ATS Resume Matcher API is up and running!");
    }

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

            // Map response directly to AnalysisResult class
            ResponseEntity<AnalysisResult> response = restTemplate.postForEntity(
                pythonAiUrl + "/analyze", 
                requestEntity, 
                AnalysisResult.class
            );

            AnalysisResult result = response.getBody();
            if (result != null) {
                // Save to PostgreSQL
                AnalysisResult savedResult = repository.save(result);
                return ResponseEntity.ok(savedResult);
            }

            return ResponseEntity.status(502).body("No result received from AI service");

        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error in match processing: " + e.getMessage());
        }
    }
}