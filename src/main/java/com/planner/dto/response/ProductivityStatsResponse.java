package com.planner.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductivityStatsResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    private TaskStatsResponse taskStats;
    private ScheduleStatsResponse scheduleStats;
    private ReminderStatsResponse reminderStats;
    private OverallStats overallStats;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ScheduleStatsResponse implements Serializable {
        private static final long serialVersionUID = 1L;

        private long totalSchedules;
        private long todaySchedules;
        private long weekSchedules;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OverallStats implements Serializable {
        private static final long serialVersionUID = 1L;

        private long totalItems;
        private long completedItems;
        private long pendingItems;
        private double completionRate;
    }
}