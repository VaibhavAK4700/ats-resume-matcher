package com.ats.service;

import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);
    private final JavaMailSender mailSender;

    @Value("${ats.notification.target-email}")
    private String targetEmail;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendJobMatchesDigest(List<Map<String, Object>> highMatches) {
        try {
            log.info("Attempting to send email digest with {} jobs to {}...", highMatches.size(), targetEmail);

            if (targetEmail == null || targetEmail.isEmpty()) {
                log.warn("Target email is not configured");
                return;
            }

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(targetEmail.trim().strip());
            helper.setSubject("🤖 Daily ATS Job Matches Found!");

            StringBuilder content = new StringBuilder();
            content.append("<h2>Daily Automated AI Job Matches</h2>");
            content.append("<p>Top matching job postings found around your location:</p>");

            for (Map<String, Object> job : highMatches) {
                String title = job == null || job.get("title") == null ? "" : String.valueOf(job.get("title"));
                String company = job == null || job.get("company") == null ? "" : String.valueOf(job.get("company"));
                String score = job == null || job.get("score") == null ? "0" : String.valueOf(job.get("score"));
                String jobUrl = job == null || job.get("job_url") == null ? "" : String.valueOf(job.get("job_url"));

                content.append("<div style='border:1px solid #ddd; padding:15px; margin-bottom:10px; border-radius:5px;'>")
                       .append("<h3 style='margin:0 0 5px 0;'>").append(title).append(" - ").append(company).append("</h3>")
                       .append("<p><strong>Match Score:</strong> ").append(score).append("%</p>")
                       .append("<p><a href='").append(jobUrl).append("' target='_blank'>View Job Posting</a></p>")
                       .append("</div>");
            }

            String emailBody = content.toString();
            helper.setText(emailBody, true);
            mailSender.send(message);

            log.info("✅ Email successfully sent to {}", targetEmail);

        } catch (Exception e) {
            log.error("❌ Failed to send email via SMTP: {}", e.getMessage(), e);
        }
    }
}