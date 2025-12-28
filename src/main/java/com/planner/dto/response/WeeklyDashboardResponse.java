package com.planner.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WeeklyDashboardResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    private LocalDate startDate;
    private LocalDate endDate;
    private Map<LocalDate, DayData> dailyData;
    private WeeklySummary summary;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DayData implements Serializable {
        private static final long serialVersionUID = 1L;

        private int taskCount;
        private int scheduleCount;
        private int reminderCount;
        private List<TaskResponse> tasks;
        private List<ScheduleResponse> schedules;
        private List<ReminderResponse> reminders;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class WeeklySummary implements Serializable {
        private static final long serialVersionUID = 1L;

        private int totalTasks;
        private int totalSchedules;
        private int totalReminders;
        private int completedTasks;
        private int pendingTasks;
    }
}