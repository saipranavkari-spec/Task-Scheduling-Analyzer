package com.taskanalyzer;

import java.io.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class TaskManager {
    private static final String FILE_NAME = "task_analyzer_data.ser";
    private final List<Task> tasks = new ArrayList<>();
    private int nextId = 1;

    public TaskManager() {
        load();
    }

    public synchronized Task addTask(String name, String description, String priority, LocalDateTime deadline) {
        Task task = new Task(nextId++, name, description, priority, deadline);
        tasks.add(task);
        save();
        return task;
    }

    public synchronized boolean updateTask(int id, String name, String description, String priority, LocalDateTime deadline) {
        Task task = findById(id);
        if (task == null) return false;
        task.setName(name);
        task.setDescription(description);
        task.setPriority(priority);
        task.setDeadline(deadline);
        save();
        return true;
    }

    public synchronized boolean deleteTask(int id) {
        boolean removed = tasks.removeIf(t -> t.getId() == id);
        if (removed) save();
        return removed;
    }

    public synchronized boolean completeTask(int id) {
        Task task = findById(id);
        if (task == null) return false;
        task.setStatus("COMPLETED");
        save();
        return true;
    }

    public synchronized Task findById(int id) {
        return tasks.stream().filter(t -> t.getId() == id).findFirst().orElse(null);
    }

    public synchronized List<Task> allTasks() {
        return new ArrayList<>(tasks);
    }

    public synchronized List<Task> prioritySchedule() {
        return tasks.stream()
                .filter(t -> !"COMPLETED".equals(t.getStatus()))
                .sorted(Comparator.comparingInt(this::priorityValue)
                        .thenComparing(Task::getDeadline))
                .collect(Collectors.toList());
    }

    public synchronized List<Task> deadlineSchedule() {
        return tasks.stream()
                .filter(t -> !"COMPLETED".equals(t.getStatus()))
                .sorted(Comparator.comparing(Task::getDeadline))
                .collect(Collectors.toList());
    }

    public synchronized List<Task> fcfsSchedule() {
        return tasks.stream()
                .filter(t -> !"COMPLETED".equals(t.getStatus()))
                .sorted(Comparator.comparingInt(Task::getId))
                .collect(Collectors.toList());
    }

    public synchronized long countStatus(String status) {
        return tasks.stream().filter(t -> status.equals(t.getStatus())).count();
    }

    public synchronized long overdueCount() {
        return tasks.stream().filter(Task::isOverdue).count();
    }

    public synchronized double completionRate() {
        if (tasks.isEmpty()) return 0.0;
        return (countStatus("COMPLETED") * 100.0) / tasks.size();
    }

    public synchronized String recommendedTask() {
        List<Task> list = prioritySchedule();
        if (list.isEmpty()) return "No pending tasks.";
        Task t = list.get(0);
        return t.getName() + " (" + t.getPriority() + ", due " + t.deadlineText() + ")";
    }

    public synchronized void save() {
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(FILE_NAME))) {
            out.writeObject(new ArrayList<>(tasks));
            out.writeInt(nextId);
        } catch (IOException e) {
            System.err.println("Could not save tasks: " + e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    private void load() {
        File file = new File(FILE_NAME);
        if (!file.exists()) return;
        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(file))) {
            tasks.clear();
            tasks.addAll((ArrayList<Task>) in.readObject());
            nextId = in.readInt();
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Could not load saved tasks: " + e.getMessage());
        }
    }

    private int priorityValue(Task task) {
        return switch (task.getPriority()) {
            case "HIGH" -> 1;
            case "MEDIUM" -> 2;
            default -> 3;
        };
    }
}
