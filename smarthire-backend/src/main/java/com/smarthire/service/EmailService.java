package com.smarthire.service;

import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

/**
 * EmailService — Dispatches HTML-formatted email alerts using JavaMailSender.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    /**
     * Sends a congratulatory email alert to a shortlisted candidate.
     */
    public void sendShortlistEmail(String toEmail, String candidateName, String recommendedRole) {
        log.info("Sending shortlist notification email to: {}", toEmail);

        if (fromEmail == null || "your-email@gmail.com".equalsIgnoreCase(fromEmail.trim()) || fromEmail.trim().isEmpty()) {
            log.info("[MOCK EMAIL MODE] Suppressing SMTP dispatch. Logged notification details locally:");
            log.info("------------------------------------------------------------------");
            log.info("TO: {}", toEmail);
            log.info("SUBJECT: Congratulations! You have been Shortlisted - SmartHire AI");
            log.info("BODY: Hello {}, you are shortlisted for the role: {}!", candidateName, recommendedRole);
            log.info("------------------------------------------------------------------");
            return;
        }

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject("Congratulations! You have been Shortlisted - SmartHire AI");

            String htmlContent = "<div style=\"font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; padding: 20px; color: #333;\">"
                    + "<h2 style=\"color: #4CAF50;\">Congratulations, " + candidateName + "!</h2>"
                    + "<p>We are thrilled to inform you that your profile has been <strong>shortlisted</strong> on the SmartHire AI Recruitment Platform.</p>"
                    + "<p>Based on our AI analysis, you showed exceptional capabilities aligned with the role of <strong>" + recommendedRole + "</strong>.</p>"
                    + "<p>Our HR representative will get in touch with you shortly to coordinate the next stages of the interview process.</p>"
                    + "<br/>"
                    + "<p>Best regards,</p>"
                    + "<p><strong>SmartHire AI Recruitment Team</strong></p>"
                    + "<hr style=\"border: none; border-top: 1px solid #eee; margin-top: 20px;\" />"
                    + "<small style=\"color: #888;\">This is an automated notification. Please do not reply directly to this email.</small>"
                    + "</div>";

            helper.setText(htmlContent, true); // Set to true to enable HTML rendering

            mailSender.send(message);
            log.info("Shortlist notification email successfully dispatched to: {}", toEmail);

        } catch (Exception ex) {
            log.error("Failed to send shortlist notification email to: {}. Error: {}", toEmail, ex.getMessage());
        }
    }
}
