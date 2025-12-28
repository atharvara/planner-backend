package com.planner.repository;

import com.planner.model.Schedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ScheduleRepository extends JpaRepository<Schedule, Long> {

    List<Schedule> findByUserId(Long userId);

    @Query("SELECT s FROM Schedule s WHERE s.userId = :userId AND s.startTime BETWEEN :startDate AND :endDate ORDER BY s.startTime")
    List<Schedule> findByUserIdAndStartTimeBetween(
            @Param("userId") Long userId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );

    @Query("SELECT s FROM Schedule s WHERE s.userId = :userId AND DATE(s.startTime) = CURRENT_DATE ORDER BY s.startTime")
    List<Schedule> findTodaySchedules(@Param("userId") Long userId);

    Optional<Schedule> findByIdAndUserId(Long id, Long userId);

    void deleteByIdAndUserId(Long id, Long userId);
}