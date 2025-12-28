package com.planner.controller;
import com.planner.dto.request.ScheduleRequest;
import com.planner.dto.response.ScheduleResponse;
import com.planner.exception.ResourceNotFoundException;
import com.planner.repository.UserRepository;
import com.planner.service.ScheduleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/schedules")
@Tag(name = "Schedules", description = "Schedule/Calendar Management APIs")
@SecurityRequirement(name = "bearerAuth")
public class ScheduleController {

    @Autowired
    private ScheduleService scheduleService;

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
    @Operation(summary = "Create a new schedule/event")
    public ResponseEntity<ScheduleResponse> createSchedule(@Valid @RequestBody ScheduleRequest request) {
        Long userId = getCurrentUserId();
        ScheduleResponse response = scheduleService.createSchedule(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @Operation(summary = "Get all schedules for current user")
    public ResponseEntity<List<ScheduleResponse>> getAllSchedules() {
        Long userId = getCurrentUserId();
        List<ScheduleResponse> schedules = scheduleService.getAllSchedulesByUser(userId);
        return ResponseEntity.ok(schedules);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get schedule by ID")
    public ResponseEntity<ScheduleResponse> getScheduleById(@PathVariable Long id) {
        Long userId = getCurrentUserId();
        ScheduleResponse response = scheduleService.getScheduleById(userId, id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/today")
    @Operation(summary = "Get today's schedules")
    public ResponseEntity<List<ScheduleResponse>> getSchedulesForToday() {
        Long userId = getCurrentUserId();
        List<ScheduleResponse> schedules = scheduleService.getSchedulesForToday(userId);
        return ResponseEntity.ok(schedules);
    }

    @GetMapping("/week")
    @Operation(summary = "Get this week's schedules")
    public ResponseEntity<List<ScheduleResponse>> getSchedulesForWeek() {
        Long userId = getCurrentUserId();
        List<ScheduleResponse> schedules = scheduleService.getSchedulesForWeek(userId);
        return ResponseEntity.ok(schedules);
    }

    @GetMapping("/date-range")
    @Operation(summary = "Get schedules by date range")
    public ResponseEntity<List<ScheduleResponse>> getSchedulesByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        Long userId = getCurrentUserId();
        List<ScheduleResponse> schedules = scheduleService.getSchedulesByDateRange(userId, startDate, endDate);
        return ResponseEntity.ok(schedules);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update schedule")
    public ResponseEntity<ScheduleResponse> updateSchedule(
            @PathVariable Long id,
            @Valid @RequestBody ScheduleRequest request) {
        Long userId = getCurrentUserId();
        ScheduleResponse response = scheduleService.updateSchedule(userId, id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete schedule")
    public ResponseEntity<Void> deleteSchedule(@PathVariable Long id) {
        Long userId = getCurrentUserId();
        scheduleService.deleteSchedule(userId, id);
        return ResponseEntity.noContent().build();
    }
}