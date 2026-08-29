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
            log.info("Attempting to send bilingual email digest with {} jobs to {}...", highMatches.size(), targetEmail);

            if (targetEmail == null || targetEmail.isBlank()) {
                log.warn("Target email address is not configured.");
                return;
            }

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(targetEmail.trim().strip());
            helper.setSubject("🤖 Daily AI Job Matches / Tägliche KI-Stellenangebote (09:00 Update)");

            StringBuilder content = new StringBuilder();
            
            // --- ENGLISH SECTION ---
            content.append("<h2>Good morning, Vaibhav!</h2>");
            content.append("<p>Here are your daily automated job matches based on your profile:</p>");
            
            content.append(buildJobListHtml(highMatches, "Match Score", "View Job Posting →"));

            content.append("<hr style='border: none; border-top: 2px dashed #ccc; margin: 30px 0;'>");

            // --- GERMAN SECTION ---
            content.append("<h2>Guten Morgen Vaibhav!</h2>");
            content.append("<p>Hier sind deine täglichen passenden Stellenangebote basierend auf deinem Profil:</p>");

            content.append(buildJobListHtml(highMatches, "Übereinstimmung", "Stellenangebot ansehen →"));

            content.append("<p style='margin-top: 20px;'><br>Good luck with your applications! / Viel Erfolg bei den Bewerbungen!</p>");

            helper.setText(content.toString(), true);
            mailSender.send(message);

            log.info("✅ Bilingual email successfully sent to {}", targetEmail);

        } catch (Exception e) {
            log.error("❌ Failed to send email via SMTP: {}", e.getMessage(), e);
        }
    }

    private String buildJobListHtml(List<Map<String, Object>> jobs, String scoreLabel, String buttonLabel) {
        StringBuilder sb = new StringBuilder();
        for (Map<String, Object> job : jobs) {
            String title = job == null || job.get("title") == null ? "N/A" : String.valueOf(job.get("title"));
            String company = job == null || job.get("company") == null ? "N/A" : String.valueOf(job.get("company"));
            String score = job == null || job.get("score") == null ? "0" : String.valueOf(job.get("score"));
            
            String jobUrl = "#";
            if (job != null) {
                if (job.get("job_url") != null) {
                    jobUrl = String.valueOf(job.get("job_url"));
                } else if (job.get("url") != null) {
                    jobUrl = String.valueOf(job.get("url"));
                }
            }

            sb.append("<div style='border:1px solid #ddd; padding:15px; margin-bottom:12px; border-radius:6px; background-color: #fcfcfc;'>")
              .append("<h3 style='margin:0 0 8px 0; color: #1a73e8;'>").append(title).append(" - ").append(company).append("</h3>")
              .append("<p style='margin:4px 0;'><strong>").append(scoreLabel).append(":</strong> <span style='color: #2e7d32; font-weight: bold;'>").append(score).append("%</span></p>")
              .append("<p style='margin:8px 0 0 0;'><a href='").append(jobUrl).append("' target='_blank' style='color: #1a73e8; text-decoration: none;'><b>").append(buttonLabel).append("</b></a></p>")
              .append("</div>");
        }
        return sb.toString();
    }
}