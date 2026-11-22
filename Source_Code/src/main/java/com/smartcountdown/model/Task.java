package com.smartcountdown.model;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import com.smartcountdown.service.CountdownService;

public class Task {

    private long id;
    private String title;
    private String category;
    private LocalDate deadline;
    private String priority;
    private boolean completed;

    public Task(
            String title,
            String category,
            LocalDate deadline,
            String priority) {

        this.title = title;
        this.category = category;
        this.deadline = deadline;
        this.priority = priority;
        this.completed = false;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getCategory() {
        return category;
    }

    public void setDeadline(LocalDate deadline) {
        this.deadline = deadline;
    }
    public LocalDate getDeadline() {
        return deadline;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public String getPriority() {
        return priority;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    public String getDaysRemaining() {

        if (completed) {
            return "Completed";
        }

        long days = ChronoUnit.DAYS.between(
                LocalDate.now(),
                deadline
        );

        if (days < 0) {
            return Math.abs(days) + " days overdue";
        }

        if (days == 0) {
            return "Due today";
        }

        return days + " days left";
    }

    public String getCountdownText() {
        return CountdownService.calculateCountdown(deadline,completed);
    }
}