package com.planner.service;
import com.planner.model.Reminder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class NotificationService {

    @Autowired(required = false)
    private JavaMailSender mailSender;

    @Value("${notification.email.enabled:false}")
    private boolean emailEnabled;

    @Value("${notification.email.from}")
    private String fromEmail;

    @Value("${notification.console.enabled:true}")
    private boolean consoleEnabled;

    public void sendReminderNotification(Reminder reminder, String userEmail) {
        log.info("Sending reminder notification for: {}", reminder.getTitle());

        // Console notification (always for demo)
        if (consoleEnabled) {
            sendConsoleNotification(reminder);
        }

        // Email notification (if configured)
        if (emailEnabled && mailSender != null) {
            try {
                sendEmailNotification(reminder, userEmail);
            } catch (Exception e) {
                log.error("Failed to send email notification: {}", e.getMessage());
                // Fallback to console if email fails
                log.warn("Email failed, notification logged to console instead");
            }
        }
    }

    private void sendConsoleNotification(Reminder reminder) {
        log.info("╔════════════════════════════════════════════════════════════╗");
        log.info("║               🔔 REMINDER NOTIFICATION                    ║");
        log.info("╠════════════════════════════════════════════════════════════╣");
        log.info("║ Title: {}", String.format("%-50s", reminder.getTitle()) + "║");
        log.info("║ Description: {}", String.format("%-44s",
                reminder.getDescription() != null ? reminder.getDescription() : "N/A") + "║");
        log.info("║ Time: {}", String.format("%-51s", reminder.getRemindAt()) + "║");
        log.info("╚════════════════════════════════════════════════════════════╝");
    }

    private void sendEmailNotification(Reminder reminder, String toEmail) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(toEmail);
        message.setSubject("🔔 Reminder: " + reminder.getTitle());
        message.setText(buildEmailBody(reminder));

        mailSender.send(message);
        log.info("Email notification sent to: {}", toEmail);
    }

    private String buildEmailBody(Reminder reminder) {
        StringBuilder body = new StringBuilder();
        body.append("Hello,\n\n");
        body.append("This is a reminder notification:\n\n");
        body.append("Title: ").append(reminder.getTitle()).append("\n");
        if (reminder.getDescription() != null) {
            body.append("Description: ").append(reminder.getDescription()).append("\n");
        }
        body.append("Scheduled Time: ").append(reminder.getRemindAt()).append("\n\n");
        body.append("Best regards,\n");
        body.append("Planner Team");
        return body.toString();
    }
}