package com.ats.controller;

import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import com.ats.Repository.AnalysisResultRepository;
import com.ats.model.AnalysisResult;

@RestController
@RequestMapping("/api/v1/resume")
@CrossOrigin(origins = "*")
public class ResumeController {

    private final AnalysisResultRepository repository;

    public ResumeController(AnalysisResultRepository repository) {
        this.repository = repository;
    }

    // 1. Resume Match & Score Endpoint
    @PostMapping("/match")
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

            // Calls Python FastAPI /analyze
            ResponseEntity<AnalysisResult> response = restTemplate.postForEntity(
                "http://python-ai-service:8000/analyze", 
                requestEntity, 
                AnalysisResult.class
            );

            AnalysisResult analysisResult = response.getBody();
            if (analysisResult == null) {
                return ResponseEntity.status(502).body("AI service returned no analysis result");
            }

            // Save result into PostgreSQL
            AnalysisResult savedResult = repository.save(analysisResult);

            return ResponseEntity.ok(savedResult);

        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error in match processing: " + e.getMessage());
        }
    }

    // 2. AI Resume Rephrase Endpoint
    @PostMapping("/rephrase")
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

            // Calls Python FastAPI /rephrase
            ResponseEntity<String> response = restTemplate.postForEntity(
                "http://python-ai-service:8000/rephrase", 
                requestEntity, 
                String.class
            );

            return ResponseEntity.ok(response.getBody());

        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error in rephrase processing: " + e.getMessage());
        }
    }
}