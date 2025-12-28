package com.planner.service;


import com.planner.dto.request.ScheduleRequest;
import com.planner.dto.response.ScheduleResponse;
import com.planner.exception.BadRequestException;
import com.planner.exception.ResourceNotFoundException;
import com.planner.model.Schedule;
import com.planner.repository.ScheduleRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ScheduleService {

    @Autowired
    private ScheduleRepository scheduleRepository;

    @Transactional
    @CacheEvict(value = "user-schedules", key = "#userId")
    public ScheduleResponse createSchedule(Long userId, ScheduleRequest request) {
        // Validate that end time is after start time
        if (request.getEndTime().isBefore(request.getStartTime())) {
            throw new BadRequestException("End time must be after start time");
        }

        Schedule schedule = Schedule.builder()
                .userId(userId)
                .title(request.getTitle())
                .description(request.getDescription())
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .location(request.getLocation())
                .build();

        Schedule savedSchedule = scheduleRepository.save(schedule);
        log.info("Created schedule with ID: {} for user: {}", savedSchedule.getId(), userId);
        return mapToResponse(savedSchedule);
    }

    @Cacheable(value = "user-schedules", key = "#userId")
    public List<ScheduleResponse> getAllSchedulesByUser(Long userId) {
        log.info("Fetching all schedules for user: {}", userId);
        List<Schedule> schedules = scheduleRepository.findByUserId(userId);
        return schedules.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public ScheduleResponse getScheduleById(Long userId, Long scheduleId) {
        Schedule schedule = scheduleRepository.findByIdAndUserId(scheduleId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Schedule not found with id: " + scheduleId));
        return mapToResponse(schedule);
    }

    public List<ScheduleResponse> getSchedulesForToday(Long userId) {
        log.info("Fetching today's schedules for user: {}", userId);
        List<Schedule> schedules = scheduleRepository.findTodaySchedules(userId);
        return schedules.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<ScheduleResponse> getSchedulesForWeek(Long userId) {
        log.info("Fetching week's schedules for user: {}", userId);
        LocalDateTime startOfWeek = LocalDateTime.now().with(LocalTime.MIN);
        LocalDateTime endOfWeek = startOfWeek.plusDays(7);

        List<Schedule> schedules = scheduleRepository.findByUserIdAndStartTimeBetween(
                userId, startOfWeek, endOfWeek
        );
        return schedules.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<ScheduleResponse> getSchedulesByDateRange(Long userId, LocalDate startDate, LocalDate endDate) {
        log.info("Fetching schedules between {} and {} for user: {}", startDate, endDate, userId);

        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(LocalTime.MAX);

        List<Schedule> schedules = scheduleRepository.findByUserIdAndStartTimeBetween(
                userId, startDateTime, endDateTime
        );
        return schedules.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    @CacheEvict(value = "user-schedules", key = "#userId")
    public ScheduleResponse updateSchedule(Long userId, Long scheduleId, ScheduleRequest request) {
        Schedule schedule = scheduleRepository.findByIdAndUserId(scheduleId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Schedule not found with id: " + scheduleId));

        // Validate that end time is after start time
        if (request.getEndTime().isBefore(request.getStartTime())) {
            throw new BadRequestException("End time must be after start time");
        }

        schedule.setTitle(request.getTitle());
        schedule.setDescription(request.getDescription());
        schedule.setStartTime(request.getStartTime());
        schedule.setEndTime(request.getEndTime());
        schedule.setLocation(request.getLocation());

        Schedule updatedSchedule = scheduleRepository.save(schedule);
        log.info("Updated schedule with ID: {} for user: {}", scheduleId, userId);
        return mapToResponse(updatedSchedule);
    }

    @Transactional
    @CacheEvict(value = "user-schedules", key = "#userId")
    public void deleteSchedule(Long userId, Long scheduleId) {
        Schedule schedule = scheduleRepository.findByIdAndUserId(scheduleId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Schedule not found with id: " + scheduleId));

        scheduleRepository.delete(schedule);
        log.info("Deleted schedule with ID: {} for user: {}", scheduleId, userId);
    }

    private ScheduleResponse mapToResponse(Schedule schedule) {
        return ScheduleResponse.builder()
                .id(schedule.getId())
                .userId(schedule.getUserId())
                .title(schedule.getTitle())
                .description(schedule.getDescription())
                .startTime(schedule.getStartTime())
                .endTime(schedule.getEndTime())
                .location(schedule.getLocation())
                .createdAt(schedule.getCreatedAt())
                .updatedAt(schedule.getUpdatedAt())
                .build();
    }
}