package com.planner.service;

import com.planner.dto.response.*;
import com.planner.repository.ReminderRepository;
import com.planner.repository.ScheduleRepository;
import com.planner.repository.TaskRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
public class DashboardService {

    @Autowired
    private TaskService taskService;

    @Autowired
    private ScheduleService scheduleService;

    @Autowired
    private ReminderService reminderService;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private ScheduleRepository scheduleRepository;

    @Autowired
    private ReminderRepository reminderRepository;

    @Cacheable(value = "dashboard-today", key = "#userId")
    public DashboardResponse getTodayDashboard(Long userId) {
        log.info("Fetching today's dashboard for user: {}", userId);

        LocalDate today = LocalDate.now();

        // Get today's data
        List<TaskResponse> todayTasks = taskService.getTasksForToday(userId);
        List<ScheduleResponse> todaySchedules = scheduleService.getSchedulesForToday(userId);
        List<ReminderResponse> todayReminders = reminderService.getTodayReminders(userId);

        // Build summary
        DashboardSummary summary = buildSummary(userId);

        return DashboardResponse.builder()
                .date(today)
                .summary(summary)
                .tasks(todayTasks)
                .schedules(todaySchedules)
                .reminders(todayReminders)
                .build();
    }

    @Cacheable(value = "dashboard-week", key = "#userId")
    public WeeklyDashboardResponse getWeeklyDashboard(Long userId) {
        log.info("Fetching weekly dashboard for user: {}", userId);

        LocalDate today = LocalDate.now();
        LocalDate endDate = today.plusDays(7);

        // Get week's data
        List<TaskResponse> weekTasks = taskService.getTasksForWeek(userId);
        List<ScheduleResponse> weekSchedules = scheduleService.getSchedulesForWeek(userId);
        List<ReminderResponse> weekReminders = reminderService.getUpcomingReminders(userId);

        // Group by date
        Map<LocalDate, WeeklyDashboardResponse.DayData> dailyData = new LinkedHashMap<>();

        for (int i = 0; i < 7; i++) {
            LocalDate date = today.plusDays(i);

            List<TaskResponse> dayTasks = weekTasks.stream()
                    .filter(t -> t.getDueDate() != null && t.getDueDate().equals(date))
                    .collect(Collectors.toList());

            List<ScheduleResponse> daySchedules = weekSchedules.stream()
                    .filter(s -> s.getStartTime().toLocalDate().equals(date))
                    .collect(Collectors.toList());

            List<ReminderResponse> dayReminders = weekReminders.stream()
                    .filter(r -> r.getRemindAt().toLocalDate().equals(date))
                    .collect(Collectors.toList());

            dailyData.put(date, WeeklyDashboardResponse.DayData.builder()
                    .taskCount(dayTasks.size())
                    .scheduleCount(daySchedules.size())
                    .reminderCount(dayReminders.size())
                    .tasks(dayTasks)
                    .schedules(daySchedules)
                    .reminders(dayReminders)
                    .build());
        }

        // Build weekly summary
        long completedTasks = weekTasks.stream()
                .filter(t -> "COMPLETED".equals(t.getStatus()))
                .count();

        long pendingTasks = weekTasks.stream()
                .filter(t -> !"COMPLETED".equals(t.getStatus()))
                .count();

        WeeklyDashboardResponse.WeeklySummary summary = WeeklyDashboardResponse.WeeklySummary.builder()
                .totalTasks(weekTasks.size())
                .totalSchedules(weekSchedules.size())
                .totalReminders(weekReminders.size())
                .completedTasks((int) completedTasks)
                .pendingTasks((int) pendingTasks)
                .build();

        return WeeklyDashboardResponse.builder()
                .startDate(today)
                .endDate(endDate)
                .dailyData(dailyData)
                .summary(summary)
                .build();
    }

    @Cacheable(value = "dashboard-stats", key = "#userId")
    public ProductivityStatsResponse getProductivityStats(Long userId) {
        log.info("Fetching productivity stats for user: {}", userId);

        // Get all stats
        TaskStatsResponse taskStats = taskService.getTaskStats(userId);
        ReminderStatsResponse reminderStats = reminderService.getReminderStats(userId);

        // Schedule stats
        long totalSchedules = scheduleRepository.findByUserId(userId).size();
        long todaySchedules = scheduleRepository.findTodaySchedules(userId).size();

        LocalDateTime startOfWeek = LocalDateTime.now().with(LocalTime.MIN);
        LocalDateTime endOfWeek = startOfWeek.plusDays(7);
        long weekSchedules = scheduleRepository.findByUserIdAndStartTimeBetween(
                userId, startOfWeek, endOfWeek).size();

        ProductivityStatsResponse.ScheduleStatsResponse scheduleStats =
                ProductivityStatsResponse.ScheduleStatsResponse.builder()
                        .totalSchedules(totalSchedules)
                        .todaySchedules(todaySchedules)
                        .weekSchedules(weekSchedules)
                        .build();

        // Overall stats
        long totalItems = taskStats.getTotalTasks() + totalSchedules + reminderStats.getTotalReminders();
        long completedItems = taskStats.getCompletedTasks() + reminderStats.getSentReminders();
        long pendingItems = totalItems - completedItems;
        double completionRate = totalItems > 0 ? (completedItems * 100.0) / totalItems : 0.0;

        ProductivityStatsResponse.OverallStats overallStats =
                ProductivityStatsResponse.OverallStats.builder()
                        .totalItems(totalItems)
                        .completedItems(completedItems)
                        .pendingItems(pendingItems)
                        .completionRate(Math.round(completionRate * 100.0) / 100.0)
                        .build();

        return ProductivityStatsResponse.builder()
                .taskStats(taskStats)
                .scheduleStats(scheduleStats)
                .reminderStats(reminderStats)
                .overallStats(overallStats)
                .build();
    }

    private DashboardSummary buildSummary(Long userId) {
        // Task stats
        long totalTasks = taskRepository.findByUserId(userId).size();
        long pendingTasks = taskRepository.countByUserIdAndStatus(userId, "PENDING");
        long inProgressTasks = taskRepository.countByUserIdAndStatus(userId, "IN_PROGRESS");
        long completedTasks = taskRepository.countByUserIdAndStatus(userId, "COMPLETED");

        // Schedule stats
        long totalSchedules = scheduleRepository.findByUserId(userId).size();
        long todaySchedules = scheduleRepository.findTodaySchedules(userId).size();

        LocalDateTime startOfWeek = LocalDateTime.now().with(LocalTime.MIN);
        LocalDateTime endOfWeek = startOfWeek.plusDays(7);
        long weekSchedules = scheduleRepository.findByUserIdAndStartTimeBetween(
                userId, startOfWeek, endOfWeek).size();

        // Reminder stats
        long totalReminders = reminderRepository.findByUserId(userId).size();
        long pendingReminders = reminderRepository.countByUserIdAndIsSent(userId, false);
        long sentReminders = reminderRepository.countByUserIdAndIsSent(userId, true);

        return DashboardSummary.builder()
                .totalTasks(totalTasks)
                .pendingTasks(pendingTasks)
                .inProgressTasks(inProgressTasks)
                .completedTasks(completedTasks)
                .totalSchedules(totalSchedules)
                .todaySchedules(todaySchedules)
                .weekSchedules(weekSchedules)
                .totalReminders(totalReminders)
                .pendingReminders(pendingReminders)
                .sentReminders(sentReminders)
                .build();
    }
}