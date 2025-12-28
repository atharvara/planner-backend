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
public class DashboardSummary implements Serializable {

    private static final long serialVersionUID = 1L;

    private long totalTasks;
    private long pendingTasks;
    private long inProgressTasks;
    private long completedTasks;

    private long totalSchedules;
    private long todaySchedules;
    private long weekSchedules;

    private long totalReminders;
    private long pendingReminders;
    private long sentReminders;
}