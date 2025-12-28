package com.planner.service;

import com.planner.dto.request.ReminderRequest;
import com.planner.dto.response.ReminderResponse;
import com.planner.dto.response.ReminderStatsResponse;
import com.planner.exception.BadRequestException;
import com.planner.exception.ResourceNotFoundException;
import com.planner.model.Reminder;
import com.planner.model.User;
import com.planner.repository.ReminderRepository;
import com.planner.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ReminderService {

    @Autowired
    private ReminderRepository reminderRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private NotificationService notificationService;

    /**
     * Manually trigger notification for a reminder (for testing)
     */
    @Transactional
    public void sendReminderNow(Long userId, Long reminderId) {
        Reminder reminder = reminderRepository.findByIdAndUserId(reminderId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Reminder not found with id: " + reminderId));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Send notification
        notificationService.sendReminderNotification(reminder, user.getEmail());

        // Mark as sent
        reminder.setIsSent(true);
        reminderRepository.save(reminder);

        log.info("Manually sent reminder ID: {} for user: {}", reminderId, userId);
    }

    @Transactional
    @CacheEvict(value = {"user-reminders", "reminder-stats"}, key = "#userId")
    public ReminderResponse createReminder(Long userId, ReminderRequest request) {
        // Validate that remind time is in the future
        if (request.getRemindAt().isBefore(LocalDateTime.now())) {
            throw new BadRequestException("Reminder time must be in the future");
        }

        Reminder reminder = Reminder.builder()
                .userId(userId)
                .title(request.getTitle())
                .description(request.getDescription())
                .remindAt(request.getRemindAt())
                .isSent(false)
                .build();

        Reminder savedReminder = reminderRepository.save(reminder);
        log.info("Created reminder with ID: {} for user: {}", savedReminder.getId(), userId);
        return mapToResponse(savedReminder);
    }

    @Cacheable(value = "user-reminders", key = "#userId")
    public List<ReminderResponse> getAllRemindersByUser(Long userId) {
        log.info("Fetching all reminders for user: {}", userId);
        List<Reminder> reminders = reminderRepository.findByUserId(userId);
        return reminders.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public ReminderResponse getReminderById(Long userId, Long reminderId) {
        Reminder reminder = reminderRepository.findByIdAndUserId(reminderId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Reminder not found with id: " + reminderId));
        return mapToResponse(reminder);
    }

    public List<ReminderResponse> getPendingReminders(Long userId) {
        log.info("Fetching pending reminders for user: {}", userId);
        List<Reminder> reminders = reminderRepository.findByUserIdAndIsSent(userId, false);
        return reminders.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<ReminderResponse> getUpcomingReminders(Long userId) {
        log.info("Fetching upcoming reminders for user: {}", userId);
        LocalDateTime now = LocalDateTime.now();
        List<Reminder> reminders = reminderRepository.findUpcomingReminders(userId, now);
        return reminders.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<ReminderResponse> getTodayReminders(Long userId) {
        log.info("Fetching today's reminders for user: {}", userId);
        List<Reminder> reminders = reminderRepository.findTodayReminders(userId);
        return reminders.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<ReminderResponse> getSentReminders(Long userId) {
        log.info("Fetching sent reminders for user: {}", userId);
        List<Reminder> reminders = reminderRepository.findByUserIdAndIsSent(userId, true);
        return reminders.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    @CacheEvict(value = {"user-reminders", "reminder-stats"}, key = "#userId")
    public ReminderResponse updateReminder(Long userId, Long reminderId, ReminderRequest request) {
        Reminder reminder = reminderRepository.findByIdAndUserId(reminderId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Reminder not found with id: " + reminderId));

        // Validate that remind time is in the future (only if not already sent)
        if (!reminder.getIsSent() && request.getRemindAt().isBefore(LocalDateTime.now())) {
            throw new BadRequestException("Reminder time must be in the future");
        }

        reminder.setTitle(request.getTitle());
        reminder.setDescription(request.getDescription());
        reminder.setRemindAt(request.getRemindAt());

        Reminder updatedReminder = reminderRepository.save(reminder);
        log.info("Updated reminder with ID: {} for user: {}", reminderId, userId);
        return mapToResponse(updatedReminder);
    }

    @Transactional
    @CacheEvict(value = {"user-reminders", "reminder-stats"}, key = "#userId")
    public ReminderResponse markAsSent(Long userId, Long reminderId) {
        Reminder reminder = reminderRepository.findByIdAndUserId(reminderId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Reminder not found with id: " + reminderId));

        reminder.setIsSent(true);
        Reminder updatedReminder = reminderRepository.save(reminder);
        log.info("Marked reminder as sent: {} for user: {}", reminderId, userId);
        return mapToResponse(updatedReminder);
    }

    @Transactional
    @CacheEvict(value = {"user-reminders", "reminder-stats"}, key = "#userId")
    public void deleteReminder(Long userId, Long reminderId) {
        Reminder reminder = reminderRepository.findByIdAndUserId(reminderId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Reminder not found with id: " + reminderId));

        reminderRepository.delete(reminder);
        log.info("Deleted reminder with ID: {} for user: {}", reminderId, userId);
    }

    @Cacheable(value = "reminder-stats", key = "#userId")
    public ReminderStatsResponse getReminderStats(Long userId) {
        long total = reminderRepository.findByUserId(userId).size();
        long pending = reminderRepository.countByUserIdAndIsSent(userId, false);
        long sent = reminderRepository.countByUserIdAndIsSent(userId, true);

        return ReminderStatsResponse.builder()
                .totalReminders(total)
                .pendingReminders(pending)
                .sentReminders(sent)
                .build();
    }

    private ReminderResponse mapToResponse(Reminder reminder) {
        return ReminderResponse.builder()
                .id(reminder.getId())
                .userId(reminder.getUserId())
                .title(reminder.getTitle())
                .description(reminder.getDescription())
                .remindAt(reminder.getRemindAt())
                .isSent(reminder.getIsSent())
                .createdAt(reminder.getCreatedAt())
                .updatedAt(reminder.getUpdatedAt())
                .build();
    }
}