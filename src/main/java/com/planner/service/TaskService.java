package com.planner.service;
import com.planner.dto.request.TaskRequest;
import com.planner.dto.response.TaskResponse;
import com.planner.dto.response.TaskStatsResponse;
import com.planner.exception.BadRequestException;
import com.planner.exception.ResourceNotFoundException;
import com.planner.model.Task;
import com.planner.repository.TaskRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class TaskService {

    @Autowired
    private TaskRepository taskRepository;

    @Transactional
    @CacheEvict(value = {"user-tasks", "task-stats"}, key = "#userId")
    public TaskResponse createTask(Long userId, TaskRequest request) {
        Task task = Task.builder()
                .userId(userId)
                .title(request.getTitle())
                .description(request.getDescription())
                .status(request.getStatus() != null ? request.getStatus() : "PENDING")
                .priority(request.getPriority() != null ? request.getPriority() : "MEDIUM")
                .dueDate(request.getDueDate())
                .build();

        Task savedTask = taskRepository.save(task);
        log.info("Created task with ID: {} for user: {}", savedTask.getId(), userId);
        return mapToResponse(savedTask);
    }

    @Cacheable(value = "user-tasks", key = "#userId")
    public List<TaskResponse> getAllTasksByUser(Long userId) {
        log.info("Fetching all tasks for user: {}", userId);
        List<Task> tasks = taskRepository.findByUserId(userId);
        return tasks.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public TaskResponse getTaskById(Long userId, Long taskId) {
        Task task = taskRepository.findByIdAndUserId(taskId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + taskId));
        return mapToResponse(task);
    }

    public List<TaskResponse> getTasksByStatus(Long userId, String status) {
        if (!isValidStatus(status)) {
            throw new BadRequestException("Invalid status: " + status);
        }
        List<Task> tasks = taskRepository.findByUserIdAndStatus(userId, status);
        return tasks.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<TaskResponse> getTasksByPriority(Long userId, String priority) {
        if (!isValidPriority(priority)) {
            throw new BadRequestException("Invalid priority: " + priority);
        }
        List<Task> tasks = taskRepository.findByUserIdAndPriority(userId, priority);
        return tasks.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<TaskResponse> getTasksByDueDate(Long userId, LocalDate dueDate) {
        List<Task> tasks = taskRepository.findByUserIdAndDueDate(userId, dueDate);
        return tasks.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<TaskResponse> getTasksForToday(Long userId) {
        LocalDate today = LocalDate.now();
        List<Task> tasks = taskRepository.findByUserIdAndDueDate(userId, today);
        return tasks.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<TaskResponse> getTasksForWeek(Long userId) {
        LocalDate startDate = LocalDate.now();
        LocalDate endDate = startDate.plusDays(7);
        List<Task> tasks = taskRepository.findByUserIdAndDueDateBetween(userId, startDate, endDate);
        return tasks.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    @CacheEvict(value = {"user-tasks", "task-stats"}, key = "#userId")
    public TaskResponse updateTask(Long userId, Long taskId, TaskRequest request) {
        Task task = taskRepository.findByIdAndUserId(taskId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + taskId));

        task.setTitle(request.getTitle());
        task.setDescription(request.getDescription());

        if (request.getStatus() != null && isValidStatus(request.getStatus())) {
            task.setStatus(request.getStatus());
        }

        if (request.getPriority() != null && isValidPriority(request.getPriority())) {
            task.setPriority(request.getPriority());
        }

        task.setDueDate(request.getDueDate());

        Task updatedTask = taskRepository.save(task);
        log.info("Updated task with ID: {} for user: {}", taskId, userId);
        return mapToResponse(updatedTask);
    }

    @Transactional
    @CacheEvict(value = {"user-tasks", "task-stats"}, key = "#userId")
    public TaskResponse updateTaskStatus(Long userId, Long taskId, String status) {
        if (!isValidStatus(status)) {
            throw new BadRequestException("Invalid status: " + status);
        }

        Task task = taskRepository.findByIdAndUserId(taskId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + taskId));

        task.setStatus(status);
        Task updatedTask = taskRepository.save(task);
        log.info("Updated task status to {} for task ID: {}", status, taskId);
        return mapToResponse(updatedTask);
    }

    @Transactional
    @CacheEvict(value = {"user-tasks", "task-stats"}, key = "#userId")
    public void deleteTask(Long userId, Long taskId) {
        Task task = taskRepository.findByIdAndUserId(taskId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + taskId));

        taskRepository.delete(task);
        log.info("Deleted task with ID: {} for user: {}", taskId, userId);
    }

    @Cacheable(value = "task-stats", key = "#userId")
    public TaskStatsResponse getTaskStats(Long userId) {
        long total = taskRepository.findByUserId(userId).size();
        long pending = taskRepository.countByUserIdAndStatus(userId, "PENDING");
        long inProgress = taskRepository.countByUserIdAndStatus(userId, "IN_PROGRESS");
        long completed = taskRepository.countByUserIdAndStatus(userId, "COMPLETED");

        return TaskStatsResponse.builder()
                .totalTasks(total)
                .pendingTasks(pending)
                .inProgressTasks(inProgress)
                .completedTasks(completed)
                .build();
    }

    private TaskResponse mapToResponse(Task task) {
        return TaskResponse.builder()
                .id(task.getId())
                .userId(task.getUserId())
                .title(task.getTitle())
                .description(task.getDescription())
                .status(task.getStatus())
                .priority(task.getPriority())
                .dueDate(task.getDueDate())
                .createdAt(task.getCreatedAt())
                .updatedAt(task.getUpdatedAt())
                .build();
    }

    private boolean isValidStatus(String status) {
        return status != null && (status.equals("PENDING") || status.equals("IN_PROGRESS") || status.equals("COMPLETED"));
    }

    private boolean isValidPriority(String priority) {
        return priority != null && (priority.equals("LOW") || priority.equals("MEDIUM") || priority.equals("HIGH"));
    }
}