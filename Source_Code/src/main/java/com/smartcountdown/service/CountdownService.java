package com.smartcountdown.service;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public class CountdownService {

    public static String calculateCountdown(
            LocalDate deadline,
            boolean completed) {

        if (completed) {
            return "Completed";
        }

        LocalDateTime currentTime =
                LocalDateTime.now();

        LocalDateTime deadlineTime =
                deadline.atTime(LocalTime.MAX);

        Duration duration =
                Duration.between(currentTime, deadlineTime);

        boolean overdue = duration.isNegative();

        if (overdue) {
            duration = duration.abs();
        }

        long days = duration.toDays();

        long hours = duration.toHoursPart();

        long minutes = duration.toMinutesPart();

        long seconds = duration.toSecondsPart();

        String countdown = String.format(
                "%d days %02d:%02d:%02d",
                days,
                hours,
                minutes,
                seconds
        );

        if (overdue) {
            return countdown + " overdue";
        }

        return countdown + " remaining";
    }
}
