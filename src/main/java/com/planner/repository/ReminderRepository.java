package com.planner.repository;
import com.planner.model.Reminder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ReminderRepository extends JpaRepository<Reminder, Long> {

    List<Reminder> findByUserId(Long userId);

    List<Reminder> findByUserIdAndIsSent(Long userId, Boolean isSent);

    @Query("SELECT r FROM Reminder r WHERE r.userId = :userId AND r.remindAt >= :now AND r.isSent = false ORDER BY r.remindAt")
    List<Reminder> findUpcomingReminders(@Param("userId") Long userId, @Param("now") LocalDateTime now);

    @Query("SELECT r FROM Reminder r WHERE r.userId = :userId AND DATE(r.remindAt) = CURRENT_DATE AND r.isSent = false ORDER BY r.remindAt")
    List<Reminder> findTodayReminders(@Param("userId") Long userId);

    @Query("SELECT r FROM Reminder r WHERE r.remindAt <= :now AND r.isSent = false")
    List<Reminder> findPendingReminders(@Param("now") LocalDateTime now);

    Optional<Reminder> findByIdAndUserId(Long id, Long userId);

    void deleteByIdAndUserId(Long id, Long userId);

    long countByUserIdAndIsSent(Long userId, Boolean isSent);
}