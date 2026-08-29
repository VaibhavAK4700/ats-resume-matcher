package com.ats.service;

import com.ats.exception.ServiceCommunicationException;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Slf4j
@Service
public class NotificationService {

    private final JavaMailSender mailSender;

    @Value("${ats.notification.target-email:notification.personal.mail@gmail.com}")
    private String defaultTargetEmail;

    @Value("${spring.mail.username:notification.personal.mail@gmail.com}")
    private String fromEmail;

    public NotificationService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    /**
     * Asynchronously dispatches an email with the tailored PDF resume attached.
     * Uses defaultTargetEmail if candidate email is not specified.
     */
    @Async("taskExecutor")
    public void sendTailoredResumeEmail(String recipientEmail, String jobTitle, byte[] pdfAttachmentBytes) {
        String targetAddress = (recipientEmail != null && !recipientEmail.isBlank())
                ? recipientEmail
                : defaultTargetEmail;
        if (targetAddress == null || targetAddress.isBlank()) {
            throw new IllegalStateException("No recipient email is configured for notifications");
        }

        String title = (jobTitle != null && !jobTitle.isBlank()) ? jobTitle : "ATS Application";

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            String senderAddress = (fromEmail != null && !fromEmail.isBlank()) ? fromEmail : defaultTargetEmail;
            if (senderAddress == null || senderAddress.isBlank()) {
                throw new IllegalStateException("No sender email is configured for notifications");
            }

            helper.setFrom(senderAddress);
            helper.setTo(targetAddress);
            helper.setSubject("AI Tailored Resume Match: " + title);

            String bodyHtml = String.format(
                "Hello,%n%n" +
                "Great news! An automated market match was processed for the position: %s.%n%n" +
                "Attached to this email is your AI-tailored resume PDF optimized for this specific job description.%n%n" +
                "Best regards,%n" +
                "ATS Automated Matching Service",
                title
            );
            helper.setText(Objects.requireNonNull(bodyHtml));

            if (pdfAttachmentBytes != null && pdfAttachmentBytes.length > 0) {
                String safeFilename = title.replaceAll("[^a-zA-Z0-9-_]", "_") + "_Tailored_Resume.pdf";
                helper.addAttachment(safeFilename, new ByteArrayResource(pdfAttachmentBytes));
            } else {
                log.warn("No PDF attachment bytes provided for email notification to {}", targetAddress);
            }

            mailSender.send(message);
            log.info("Tailored resume email notification sent successfully to {}", targetAddress);

        } catch (MessagingException e) {
            log.error("Failed to format or dispatch tailored resume email to {}: {}", targetAddress, e.getMessage(), e);
            throw new ServiceCommunicationException("Failed to send email notification to " + targetAddress, e);
        } catch (Exception e) {
            log.error("Unexpected error during mail dispatch to {}: {}", targetAddress, e.getMessage(), e);
        }
    }
}