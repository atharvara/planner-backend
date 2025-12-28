package com.planner.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    private LocalDate date;
    private DashboardSummary summary;
    private List<TaskResponse> tasks;
    private List<ScheduleResponse> schedules;
    private List<ReminderResponse> reminders;
}