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
public class TaskStatsResponse implements Serializable {  // Add this

    private static final long serialVersionUID = 1L;  // Add this

    private long totalTasks;
    private long pendingTasks;
    private long inProgressTasks;
    private long completedTasks;
}