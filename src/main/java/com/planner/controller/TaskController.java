package com.planner.controller;
import com.planner.dto.request.TaskRequest;
import com.planner.dto.response.TaskResponse;
import com.planner.dto.response.TaskStatsResponse;
import com.planner.exception.ResourceNotFoundException;
import com.planner.repository.UserRepository;
import com.planner.service.TaskService;
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
@RequestMapping("/api/tasks")
@Tag(name = "Tasks", description = "Task Management APIs")
@SecurityRequirement(name = "bearerAuth")
public class TaskController {

    @Autowired
    private TaskService taskService;
    @Autowired
    private UserRepository userRepository;

    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        // You need to inject UserRepository
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"))
                .getId();
    }

    @PostMapping
    @Operation(summary = "Create a new task")
    public ResponseEntity<TaskResponse> createTask(@Valid @RequestBody TaskRequest request) {
        Long userId = getCurrentUserId();
        TaskResponse response = taskService.createTask(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @Operation(summary = "Get all tasks for current user")
    public ResponseEntity<List<TaskResponse>> getAllTasks() {
        Long userId = getCurrentUserId();
        List<TaskResponse> tasks = taskService.getAllTasksByUser(userId);
        return ResponseEntity.ok(tasks);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get task by ID")
    public ResponseEntity<TaskResponse> getTaskById(@PathVariable Long id) {
        Long userId = getCurrentUserId();
        TaskResponse response = taskService.getTaskById(userId, id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/status/{status}")
    @Operation(summary = "Get tasks by status (PENDING, IN_PROGRESS, COMPLETED)")
    public ResponseEntity<List<TaskResponse>> getTasksByStatus(@PathVariable String status) {
        Long userId = getCurrentUserId();
        List<TaskResponse> tasks = taskService.getTasksByStatus(userId, status.toUpperCase());
        return ResponseEntity.ok(tasks);
    }

    @GetMapping("/priority/{priority}")
    @Operation(summary = "Get tasks by priority (LOW, MEDIUM, HIGH)")
    public ResponseEntity<List<TaskResponse>> getTasksByPriority(@PathVariable String priority) {
        Long userId = getCurrentUserId();
        List<TaskResponse> tasks = taskService.getTasksByPriority(userId, priority.toUpperCase());
        return ResponseEntity.ok(tasks);
    }

    @GetMapping("/due-date")
    @Operation(summary = "Get tasks by due date")
    public ResponseEntity<List<TaskResponse>> getTasksByDueDate(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dueDate) {
        Long userId = getCurrentUserId();
        List<TaskResponse> tasks = taskService.getTasksByDueDate(userId, dueDate);
        return ResponseEntity.ok(tasks);
    }

    @GetMapping("/today")
    @Operation(summary = "Get tasks due today")
    public ResponseEntity<List<TaskResponse>> getTasksForToday() {
        Long userId = getCurrentUserId();
        List<TaskResponse> tasks = taskService.getTasksForToday(userId);
        return ResponseEntity.ok(tasks);
    }

    @GetMapping("/week")
    @Operation(summary = "Get tasks due this week")
    public ResponseEntity<List<TaskResponse>> getTasksForWeek() {
        Long userId = getCurrentUserId();
        List<TaskResponse> tasks = taskService.getTasksForWeek(userId);
        return ResponseEntity.ok(tasks);
    }

    @GetMapping("/stats")
    @Operation(summary = "Get task statistics")
    public ResponseEntity<TaskStatsResponse> getTaskStats() {
        Long userId = getCurrentUserId();
        TaskStatsResponse stats = taskService.getTaskStats(userId);
        return ResponseEntity.ok(stats);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update task")
    public ResponseEntity<TaskResponse> updateTask(
            @PathVariable Long id,
            @Valid @RequestBody TaskRequest request) {
        Long userId = getCurrentUserId();
        TaskResponse response = taskService.updateTask(userId, id, request);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Update task status")
    public ResponseEntity<TaskResponse> updateTaskStatus(
            @PathVariable Long id,
            @RequestParam String status) {
        Long userId = getCurrentUserId();
        TaskResponse response = taskService.updateTaskStatus(userId, id, status.toUpperCase());
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete task")
    public ResponseEntity<Void> deleteTask(@PathVariable Long id) {
        Long userId = getCurrentUserId();
        taskService.deleteTask(userId, id);
        return ResponseEntity.noContent().build();
    }
}