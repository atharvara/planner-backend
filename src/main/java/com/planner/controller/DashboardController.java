package com.planner.controller;

import com.planner.dto.response.DashboardResponse;
import com.planner.dto.response.ProductivityStatsResponse;
import com.planner.dto.response.WeeklyDashboardResponse;
import com.planner.exception.ResourceNotFoundException;
import com.planner.repository.UserRepository;
import com.planner.service.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
@Tag(name = "Dashboard", description = "Dashboard & Analytics APIs")
@SecurityRequirement(name = "bearer-jwt")
public class DashboardController {

    @Autowired
    private DashboardService dashboardService;

    @Autowired
    private UserRepository userRepository;

    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"))
                .getId();
    }

    @GetMapping("/today")
    @Operation(summary = "Get today's dashboard overview",
            description = "Returns tasks, schedules, and reminders for today with summary statistics")
    public ResponseEntity<DashboardResponse> getTodayDashboard() {
        Long userId = getCurrentUserId();
        DashboardResponse response = dashboardService.getTodayDashboard(userId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/week")
    @Operation(summary = "Get weekly dashboard overview",
            description = "Returns next 7 days of tasks, schedules, and reminders grouped by date")
    public ResponseEntity<WeeklyDashboardResponse> getWeeklyDashboard() {
        Long userId = getCurrentUserId();
        WeeklyDashboardResponse response = dashboardService.getWeeklyDashboard(userId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/stats")
    @Operation(summary = "Get productivity statistics",
            description = "Returns comprehensive statistics including completion rates and overall productivity metrics")
    public ResponseEntity<ProductivityStatsResponse> getProductivityStats() {
        Long userId = getCurrentUserId();
        ProductivityStatsResponse response = dashboardService.getProductivityStats(userId);
        return ResponseEntity.ok(response);
    }
}