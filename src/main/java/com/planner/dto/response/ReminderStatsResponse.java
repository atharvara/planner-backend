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
public class ReminderStatsResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    private long totalReminders;
    private long pendingReminders;
    private long sentReminders;
}