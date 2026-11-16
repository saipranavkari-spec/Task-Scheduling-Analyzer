package com.taskanalyzer;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Task implements Serializable {
    private static final long serialVersionUID = 1L;
    private static final DateTimeFormatter FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private int id;
    private String name;
    private String description;
    private String priority;
    private LocalDateTime deadline;
    private String status;

    public Task(int id, String name, String description, String priority, LocalDateTime deadline) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.priority = priority;
        this.deadline = deadline;
        this.status = "PENDING";
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public String getPriority() { return priority; }
    public LocalDateTime getDeadline() { return deadline; }
    public String getStatus() { return status; }

    public void setName(String name) { this.name = name; }
    public void setDescription(String description) { this.description = description; }
    public void setPriority(String priority) { this.priority = priority; }
    public void setDeadline(LocalDateTime deadline) { this.deadline = deadline; }
    public void setStatus(String status) { this.status = status; }

    public boolean isOverdue() {
        return !"COMPLETED".equals(status) && deadline.isBefore(LocalDateTime.now());
    }

    public String deadlineText() {
        return deadline.format(FORMAT);
    }

    @Override
    public String toString() {
        return id + " | " + name + " | " + priority + " | " + deadlineText() + " | " + status;
    }
}
