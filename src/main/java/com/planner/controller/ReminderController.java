package com.planner.controller;

import com.planner.dto.request.ReminderRequest;
import com.planner.dto.response.ReminderResponse;
import com.planner.dto.response.ReminderStatsResponse;
import com.planner.exception.ResourceNotFoundException;
import com.planner.repository.UserRepository;
import com.planner.service.ReminderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reminders")
@Tag(name = "Reminders", description = "Reminder Management APIs")
@SecurityRequirement(name = "bearerAuth")
public class ReminderController {

    @Autowired
    private ReminderService reminderService;

    @Autowired
    private UserRepository userRepository;

    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"))
                .getId();
    }

    @PostMapping
    @Operation(summary = "Create a new reminder")
    public ResponseEntity<ReminderResponse> createReminder(@Valid @RequestBody ReminderRequest request) {
        Long userId = getCurrentUserId();
        ReminderResponse response = reminderService.createReminder(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @Operation(summary = "Get all reminders for current user")
    public ResponseEntity<List<ReminderResponse>> getAllReminders() {
        Long userId = getCurrentUserId();
        List<ReminderResponse> reminders = reminderService.getAllRemindersByUser(userId);
        return ResponseEntity.ok(reminders);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get reminder by ID")
    public ResponseEntity<ReminderResponse> getReminderById(@PathVariable Long id) {
        Long userId = getCurrentUserId();
        ReminderResponse response = reminderService.getReminderById(userId, id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/pending")
    @Operation(summary = "Get all pending reminders")
    public ResponseEntity<List<ReminderResponse>> getPendingReminders() {
        Long userId = getCurrentUserId();
        List<ReminderResponse> reminders = reminderService.getPendingReminders(userId);
        return ResponseEntity.ok(reminders);
    }

    @GetMapping("/upcoming")
    @Operation(summary = "Get upcoming reminders (future, not sent)")
    public ResponseEntity<List<ReminderResponse>> getUpcomingReminders() {
        Long userId = getCurrentUserId();
        List<ReminderResponse> reminders = reminderService.getUpcomingReminders(userId);
        return ResponseEntity.ok(reminders);
    }

    @GetMapping("/today")
    @Operation(summary = "Get today's reminders")
    public ResponseEntity<List<ReminderResponse>> getTodayReminders() {
        Long userId = getCurrentUserId();
        List<ReminderResponse> reminders = reminderService.getTodayReminders(userId);
        return ResponseEntity.ok(reminders);
    }

    @GetMapping("/sent")
    @Operation(summary = "Get sent reminders")
    public ResponseEntity<List<ReminderResponse>> getSentReminders() {
        Long userId = getCurrentUserId();
        List<ReminderResponse> reminders = reminderService.getSentReminders(userId);
        return ResponseEntity.ok(reminders);
    }

    @GetMapping("/stats")
    @Operation(summary = "Get reminder statistics")
    public ResponseEntity<ReminderStatsResponse> getReminderStats() {
        Long userId = getCurrentUserId();
        ReminderStatsResponse stats = reminderService.getReminderStats(userId);
        return ResponseEntity.ok(stats);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update reminder")
    public ResponseEntity<ReminderResponse> updateReminder(
            @PathVariable Long id,
            @Valid @RequestBody ReminderRequest request) {
        Long userId = getCurrentUserId();
        ReminderResponse response = reminderService.updateReminder(userId, id, request);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/mark-sent")
    @Operation(summary = "Mark reminder as sent")
    public ResponseEntity<ReminderResponse> markReminderAsSent(@PathVariable Long id) {
        Long userId = getCurrentUserId();
        ReminderResponse response = reminderService.markAsSent(userId, id);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete reminder")
    public ResponseEntity<Void> deleteReminder(@PathVariable Long id) {
        Long userId = getCurrentUserId();
        reminderService.deleteReminder(userId, id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/send-now")
    @Operation(summary = "Manually trigger reminder notification (for testing)")
    public ResponseEntity<String> sendReminderNow(@PathVariable Long id) {
        Long userId = getCurrentUserId();
        reminderService.sendReminderNow(userId, id);
        return ResponseEntity.ok("Reminder notification sent successfully");
    }
}