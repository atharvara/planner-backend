package com.planner.seeder;

import com.planner.model.Reminder;
import com.planner.model.Schedule;
import com.planner.model.Task;
import com.planner.model.User;
import com.planner.repository.ReminderRepository;
import com.planner.repository.ScheduleRepository;
import com.planner.repository.TaskRepository;
import com.planner.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Component
@Slf4j
public class DataSeeder implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private ScheduleRepository scheduleRepository;

    @Autowired
    private ReminderRepository reminderRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Value("${app.seeder.enabled:false}")
    private boolean seederEnabled;

    @Value("${app.seeder.clear-existing:false}")
    private boolean clearExisting;

    private final Random random = new Random();

    @Override
    @Transactional
    public void run(String... args) {
        if (!seederEnabled) {
            log.info("⏭️  Data seeder is DISABLED. Enable it in application.yml");
            return;
        }

        log.info("╔════════════════════════════════════════════════════════════╗");
        log.info("║           🌱 DATA SEEDER STARTED                          ║");
        log.info("╚════════════════════════════════════════════════════════════╝");

        try {
            // Check if data already exists
            if (!clearExisting && userRepository.count() > 0) {
                log.info("⏭️  Database already contains data. Skipping seeding.");
                log.info("   Set app.seeder.clear-existing=true to force re-seed.");
                return;
            }

            // Clear existing data if requested
            if (clearExisting) {
                clearExistingData();
            }

            // Seed data
            List<User> users = seedUsers();
            seedTasksForUsers(users);
            seedSchedulesForUsers(users);
            seedRemindersForUsers(users);

            log.info("╔════════════════════════════════════════════════════════════╗");
            log.info("║           ✅ DATA SEEDER COMPLETED SUCCESSFULLY           ║");
            log.info("╠════════════════════════════════════════════════════════════╣");
            log.info("║ Users created: {}", String.format("%-44d", users.size()) + "║");
            log.info("║ Tasks created: {}", String.format("%-44d", taskRepository.count()) + "║");
            log.info("║ Schedules created: {}", String.format("%-40d", scheduleRepository.count()) + "║");
            log.info("║ Reminders created: {}", String.format("%-40d", reminderRepository.count()) + "║");
            log.info("╠════════════════════════════════════════════════════════════╣");
            log.info("║ Test Credentials:                                         ║");
            for (User user : users) {
                log.info("║ 📧 Email: {}", String.format("%-48s", user.getEmail()) + "║");
                log.info("║ 🔑 Password: password123                                  ║");
            }
            log.info("╚════════════════════════════════════════════════════════════╝");

        } catch (Exception e) {
            log.error("❌ Data seeder failed: {}", e.getMessage(), e);
        }
    }

    private void clearExistingData() {
        log.info("🗑️  Clearing existing data...");
        reminderRepository.deleteAll();
        scheduleRepository.deleteAll();
        taskRepository.deleteAll();
        userRepository.deleteAll();
        log.info("✅ Existing data cleared");
    }

    private List<User> seedUsers() {
        log.info("👥 Creating users...");

        List<User> users = new ArrayList<>();

        users.add(User.builder()
                .email("atharva@planner.com")
                .password(passwordEncoder.encode("password123"))
                .fullName("Atharva Rathi")
                .build());

        users.add(User.builder()
                .email("john.doe@planner.com")
                .password(passwordEncoder.encode("password123"))
                .fullName("John Doe")
                .build());

        users.add(User.builder()
                .email("jane.smith@planner.com")
                .password(passwordEncoder.encode("password123"))
                .fullName("Jane Smith")
                .build());

        users = userRepository.saveAll(users);
        log.info("✅ Created {} users", users.size());

        return users;
    }

    private void seedTasksForUsers(List<User> users) {
        log.info("📝 Creating tasks...");

        String[] taskTitles = {
                "Review project documentation",
                "Fix bug in authentication module",
                "Update API documentation",
                "Prepare presentation for client meeting",
                "Code review for PR #123",
                "Write unit tests for new features",
                "Optimize database queries",
                "Deploy to staging environment",
                "Research new technologies",
                "Attend team standup meeting",
                "Update project timeline",
                "Refactor legacy code",
                "Setup CI/CD pipeline",
                "Security audit review",
                "Performance testing"
        };

        String[] descriptions = {
                "High priority task that needs immediate attention",
                "Regular maintenance work",
                "Scheduled for end of week",
                "Part of sprint planning",
                "Follow-up from last meeting",
                "Technical debt cleanup",
                "Customer requested feature",
                "Internal improvement initiative"
        };

        String[] statuses = {"PENDING", "IN_PROGRESS", "COMPLETED"};
        String[] priorities = {"LOW", "MEDIUM", "HIGH"};

        for (User user : users) {
            int taskCount = 10 + random.nextInt(6); // 10-15 tasks per user

            for (int i = 0; i < taskCount; i++) {
                LocalDate dueDate = LocalDate.now().plusDays(random.nextInt(14) - 3); // -3 to +10 days

                Task task = Task.builder()
                        .userId(user.getId())
                        .title(taskTitles[random.nextInt(taskTitles.length)])
                        .description(descriptions[random.nextInt(descriptions.length)])
                        .status(statuses[random.nextInt(statuses.length)])
                        .priority(priorities[random.nextInt(priorities.length)])
                        .dueDate(dueDate)
                        .build();

                taskRepository.save(task);
            }
        }

        log.info("✅ Created {} tasks", taskRepository.count());
    }

    private void seedSchedulesForUsers(List<User> users) {
        log.info("📅 Creating schedules...");

        String[] scheduleTitles = {
                "Team Standup Meeting",
                "Client Presentation",
                "Code Review Session",
                "Sprint Planning",
                "One-on-One with Manager",
                "Department All-Hands",
                "Technical Architecture Discussion",
                "Product Demo",
                "Training Session",
                "Interview - Backend Developer",
                "Lunch with Team",
                "Project Kickoff Meeting"
        };

        String[] locations = {
                "Conference Room A",
                "Zoom Meeting",
                "Office Building 2, Floor 3",
                "Google Meet",
                "Teams Call",
                "Main Office",
                "Client Office",
                "Virtual"
        };

        for (User user : users) {
            int scheduleCount = 5 + random.nextInt(3); // 5-7 schedules per user

            for (int i = 0; i < scheduleCount; i++) {
                LocalDate scheduleDate = LocalDate.now().plusDays(random.nextInt(7)); // Next 7 days
                LocalTime startTime = LocalTime.of(9 + random.nextInt(8), random.nextInt(2) * 30); // 9 AM - 5 PM
                LocalTime endTime = startTime.plusHours(1); // 1-hour meetings

                Schedule schedule = Schedule.builder()
                        .userId(user.getId())
                        .title(scheduleTitles[random.nextInt(scheduleTitles.length)])
                        .description("Scheduled meeting for project coordination")
                        .startTime(LocalDateTime.of(scheduleDate, startTime))
                        .endTime(LocalDateTime.of(scheduleDate, endTime))
                        .location(locations[random.nextInt(locations.length)])
                        .build();

                scheduleRepository.save(schedule);
            }
        }

        log.info("✅ Created {} schedules", scheduleRepository.count());
    }

    private void seedRemindersForUsers(List<User> users) {
        log.info("🔔 Creating reminders...");

        String[] reminderTitles = {
                "Submit timesheet",
                "Review expense reports",
                "Update project status",
                "Prepare for tomorrow's meeting",
                "Follow up with client",
                "Complete performance review",
                "Renew software licenses",
                "Backup project files",
                "Update documentation",
                "Send weekly report"
        };

        for (User user : users) {
            int reminderCount = 3 + random.nextInt(3); // 3-5 reminders per user

            for (int i = 0; i < reminderCount; i++) {
                // Mix of past (for testing "sent" functionality) and future reminders
                int daysOffset = random.nextInt(10) - 2; // -2 to +7 days
                LocalDateTime remindAt = LocalDateTime.now()
                        .plusDays(daysOffset)
                        .withHour(9 + random.nextInt(9))
                        .withMinute(random.nextInt(60))
                        .withSecond(0);

                boolean isSent = remindAt.isBefore(LocalDateTime.now());

                Reminder reminder = Reminder.builder()
                        .userId(user.getId())
                        .title(reminderTitles[random.nextInt(reminderTitles.length)])
                        .description("Don't forget to complete this task on time")
                        .remindAt(remindAt)
                        .isSent(isSent)
                        .build();

                reminderRepository.save(reminder);
            }
        }

        log.info("✅ Created {} reminders", reminderRepository.count());
    }
}