package com.ats.controller;

import com.ats.model.JobPosting;
import com.ats.model.Resume;
import com.ats.repository.JobPostingRepository;
import com.ats.repository.ResumeRepository;
import com.ats.service.AutomatedAtsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ResumeController {

    private final AutomatedAtsService automatedAtsService;
    private final JobPostingRepository jobPostingRepository;
    private final ResumeRepository resumeRepository;

    /**
     * Endpoint 1: Save Job Posting
     * POST /api/jobs
     */
    @PostMapping("/jobs")
    public ResponseEntity<JobPosting> createJobPosting(@RequestBody JobPosting jobPosting) {
        log.info("Received request to create job posting: {}", jobPosting.getTitle());
        JobPosting savedJob = jobPostingRepository.save(jobPosting);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedJob);
    }

    /**
     * Endpoint 2: Save Text Resume
     * POST /api/resumes
     */
    @PostMapping("/resumes")
    public ResponseEntity<Resume> createResume(@RequestBody Resume resume) {
        log.info("Received request to save resume label: {}", resume.getFileName());
        if (resume.getIsBaseResume() == null) {
            resume.setIsBaseResume(true);
        }
        Resume savedResume = resumeRepository.save(resume);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedResume);
    }

    /**
     * Endpoint 3: Direct PDF Upload, Automated Tailoring, Notification & PDF Stream
     * POST /api/automated/generate-tailored-resume
     */
    @PostMapping(value = "/automated/generate-tailored-resume", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<byte[]> generateTailoredResumePdf(
            @RequestPart(value = "file", required = false) MultipartFile file,
            @RequestParam("jobId") Long jobId,
            @RequestParam(value = "userEmail", required = false) String userEmail) {

        log.info("Processing PDF tailoring request for jobId: {}, userEmail: {}", jobId, userEmail);
        
        byte[] pdfBytes = automatedAtsService.processAndTailorResumePdf(file, jobId, userEmail);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", "tailored_resume_job_" + jobId + ".pdf");
        headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");

        return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
    }

    /**
     * Endpoint 4: Run Standard Text Match Analysis
     * POST /api/automated/analyze?resumeId=1&jobId=1
     */
    @PostMapping("/automated/analyze")
    public ResponseEntity<Map<String, Object>> analyzeSavedResumeAndJob(
            @RequestParam("resumeId") Long resumeId,
            @RequestParam("jobId") Long jobId) {

        log.info("Running text match analysis between Resume ID: {} and Job ID: {}", resumeId, jobId);
        Map<String, Object> analysisResult = automatedAtsService.analyzeSavedResumeAndJob(resumeId, jobId);
        return ResponseEntity.ok(analysisResult);
    }
}