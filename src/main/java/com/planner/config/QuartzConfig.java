package com.planner.config;

import com.planner.scheduler.ReminderSchedulerJob;
import org.quartz.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class QuartzConfig {

    @Bean
    public JobDetail reminderJobDetail() {
        return JobBuilder.newJob(ReminderSchedulerJob.class)
                .withIdentity("reminderJob")
                .withDescription("Check and send pending reminders")
                .storeDurably()
                .build();
    }

    @Bean
    public Trigger reminderJobTrigger() {
        // Run every 1 minute
        SimpleScheduleBuilder scheduleBuilder = SimpleScheduleBuilder
                .simpleSchedule()
                .withIntervalInMinutes(1)
                .repeatForever();

        return TriggerBuilder.newTrigger()
                .forJob(reminderJobDetail())
                .withIdentity("reminderTrigger")
                .withDescription("Trigger to check reminders every minute")
                .withSchedule(scheduleBuilder)
                .build();
    }

    // Alternative: Use Cron expression for more control
    // @Bean
    // public Trigger reminderJobCronTrigger() {
    //     return TriggerBuilder.newTrigger()
    //             .forJob(reminderJobDetail())
    //             .withIdentity("reminderCronTrigger")
    //             .withSchedule(CronScheduleBuilder.cronSchedule("0 * * * * ?")) // Every minute
    //             .build();
    // }
}