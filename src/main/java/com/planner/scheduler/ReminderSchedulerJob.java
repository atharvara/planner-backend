package com.planner.scheduler;

import com.planner.model.Reminder;
import com.planner.model.User;
import com.planner.repository.ReminderRepository;
import com.planner.repository.UserRepository;
import com.planner.service.NotificationService;
import lombok.extern.slf4j.Slf4j;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@Slf4j
public class ReminderSchedulerJob implements Job {

    @Autowired
    private ReminderRepository reminderRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private NotificationService notificationService;

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        log.info("⏰ Reminder Scheduler Job Started at {}", LocalDateTime.now());

        try {
            LocalDateTime now = LocalDateTime.now();
            List<Reminder> pendingReminders = reminderRepository.findPendingReminders(now);

            log.info("Found {} pending reminders to process", pendingReminders.size());

            for (Reminder reminder : pendingReminders) {
                try {
                    // Get user email
                    User user = userRepository.findById(reminder.getUserId())
                            .orElse(null);

                    if (user != null) {
                        // Send notification
                        notificationService.sendReminderNotification(reminder, user.getEmail());

                        // Mark as sent
                        reminder.setIsSent(true);
                        reminderRepository.save(reminder);

                        log.info("✅ Processed reminder ID: {} for user: {}",
                                reminder.getId(), user.getEmail());
                    } else {
                        log.warn("⚠️ User not found for reminder ID: {}", reminder.getId());
                    }
                } catch (Exception e) {
                    log.error("❌ Error processing reminder ID: {}", reminder.getId(), e);
                }
            }

            log.info("✅ Reminder Scheduler Job Completed. Processed {} reminders",
                    pendingReminders.size());

        } catch (Exception e) {
            log.error("❌ Fatal error in Reminder Scheduler Job", e);
            throw new JobExecutionException(e);
        }
    }
}