package com.taskanalyzer;

import java.awt.*;
import java.awt.event.*;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

public class TaskGUI extends Frame implements ActionListener {
    private final TaskManager manager = new TaskManager();
    private final CardLayout cards = new CardLayout();
    private final Panel content = new Panel(cards);

    private final TextField taskField = new TextField(24);
    private final TextField descriptionField = new TextField(24);
    private final Choice priorityChoice = new Choice();
    private final TextField deadlineField = new TextField(16);
    private final TextField idField = new TextField(8);
    private final TextArea taskArea = new TextArea(18, 75);
    private final Label statusLabel = new Label("Ready.");
    private final Label dashboardLabel = new Label();

    private final Button addButton = new Button("Add Task");
    private final Button updateButton = new Button("Update Task");
    private final Button deleteButton = new Button("Delete Task");
    private final Button completeButton = new Button("Complete Task");
    private final Button refreshButton = new Button("Refresh");
    private final Button scheduleButton = new Button("Smart Schedule");
    private final Button reportButton = new Button("Reports");
    private final Button tasksButton = new Button("View Tasks");

    private static final DateTimeFormatter FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public TaskGUI() {
        setTitle("Task Scheduling Analyzer");
        setSize(850, 650);
        setLayout(new BorderLayout());

        Panel top = new Panel(new FlowLayout(FlowLayout.LEFT));
        top.add(new Label("TASK SCHEDULING ANALYZER"));
        top.add(tasksButton);
        top.add(scheduleButton);
        top.add(reportButton);
        top.add(refreshButton);
        add(top, BorderLayout.NORTH);

        buildAddPanel();
        buildTaskPanel();
        buildSchedulePanel();
        buildReportPanel();

        add(content, BorderLayout.CENTER);
        add(statusLabel, BorderLayout.SOUTH);

        refreshDashboard();
        cards.show(content, "ADD");

        refreshButton.addActionListener(e -> refreshDashboard());
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                manager.save();
                dispose();
                System.exit(0);
            }
        });

        setLocationRelativeTo(null);
        setVisible(true);
        startReminderThread();
    }

    private void buildAddPanel() {
        Panel p = new Panel(new GridLayout(7, 2, 8, 8));
        p.add(new Label("Task Name:"));
        p.add(taskField);

        p.add(new Label("Description:"));
        p.add(descriptionField);

        p.add(new Label("Priority:"));
        priorityChoice.add("HIGH");
        priorityChoice.add("MEDIUM");
        priorityChoice.add("LOW");
        p.add(priorityChoice);

        p.add(new Label("Deadline (yyyy-MM-dd HH:mm):"));
        p.add(deadlineField);

        p.add(new Label("Task ID:"));
        p.add(idField);

        p.add(addButton);
        p.add(updateButton);
        p.add(deleteButton);
        p.add(completeButton);

        addButton.addActionListener(this);
        updateButton.addActionListener(this);
        deleteButton.addActionListener(this);
        completeButton.addActionListener(this);

        Panel wrapper = new Panel(new BorderLayout());
        wrapper.add(p, BorderLayout.NORTH);
        wrapper.add(dashboardLabel, BorderLayout.SOUTH);
        content.add(wrapper, "ADD");
    }

    private void buildTaskPanel() {
        Panel p = new Panel(new BorderLayout());
        p.add(taskArea, BorderLayout.CENTER);
        tasksButton.addActionListener(e -> {
            showAllTasks();
            cards.show(content, "TASKS");
        });
        content.add(p, "TASKS");
    }

    private void buildSchedulePanel() {
        Panel p = new Panel(new BorderLayout());
        TextArea scheduleArea = new TextArea();
        Button priority = new Button("Priority Scheduling");
        Button edf = new Button("Earliest Deadline First");
        Button fcfs = new Button("FCFS");

        Panel buttons = new Panel(new FlowLayout());
        buttons.add(priority);
        buttons.add(edf);
        buttons.add(fcfs);
        p.add(buttons, BorderLayout.NORTH);
        p.add(scheduleArea, BorderLayout.CENTER);

        priority.addActionListener(e -> showSchedule(scheduleArea, manager.prioritySchedule(), "Priority Scheduling"));
        edf.addActionListener(e -> showSchedule(scheduleArea, manager.deadlineSchedule(), "Earliest Deadline First"));
        fcfs.addActionListener(e -> showSchedule(scheduleArea, manager.fcfsSchedule(), "First Come First Serve"));

        scheduleButton.addActionListener(e -> {
            cards.show(content, "SCHEDULE");
            showSchedule(scheduleArea, manager.prioritySchedule(), "Smart Priority Schedule");
        });

        content.add(p, "SCHEDULE");
    }

    private void buildReportPanel() {
        Panel p = new Panel(new BorderLayout());
        TextArea reportArea = new TextArea();
        p.add(reportArea, BorderLayout.CENTER);

        reportButton.addActionListener(e -> {
            reportArea.setText(buildReport());
            cards.show(content, "REPORT");
        });

        content.add(p, "REPORT");
    }

    private void refreshDashboard() {
        dashboardLabel.setText(
            "Total: " + manager.allTasks().size()
            + "   Pending: " + manager.countStatus("PENDING")
            + "   Completed: " + manager.countStatus("COMPLETED")
            + "   Overdue: " + manager.overdueCount()
            + "   Completion: " + String.format("%.1f%%", manager.completionRate())
        );
    }

    private void showAllTasks() {
        StringBuilder sb = new StringBuilder();
        sb.append("ID | TASK | PRIORITY | DEADLINE | STATUS\n");
        sb.append("---------------------------------------------------------------\n");

        for (Task t : manager.allTasks()) {
            sb.append(t);
            if (t.isOverdue()) sb.append(" [OVERDUE]");
            sb.append("\n");

            if (!t.getDescription().isBlank()) {
                sb.append("    Description: ").append(t.getDescription()).append("\n");
            }
        }

        taskArea.setText(sb.toString());
    }

    private void showSchedule(TextArea area, List<Task> list, String title) {
        StringBuilder sb = new StringBuilder(title).append("\n");
        sb.append("---------------------------------------------------------------\n");

        int position = 1;
        for (Task t : list) {
            sb.append(position++).append(". ").append(t).append("\n");
        }

        if (list.isEmpty()) sb.append("No pending tasks.");
        area.setText(sb.toString());
    }

    private String buildReport() {
        return "TASK REPORT\n"
            + "==============================\n"
            + "Total tasks      : " + manager.allTasks().size() + "\n"
            + "Pending tasks    : " + manager.countStatus("PENDING") + "\n"
            + "Completed tasks  : " + manager.countStatus("COMPLETED") + "\n"
            + "Overdue tasks    : " + manager.overdueCount() + "\n"
            + "Completion rate  : " + String.format("%.1f%%", manager.completionRate()) + "\n"
            + "Recommended task : " + manager.recommendedTask() + "\n";
    }

    private LocalDateTime parseDeadline() {
        try {
            return LocalDateTime.parse(deadlineField.getText().trim(), FORMAT);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Deadline must be yyyy-MM-dd HH:mm");
        }
    }

    private int parseId() {
        try {
            return Integer.parseInt(idField.getText().trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Enter a valid task ID.");
        }
    }

    private void clearForm() {
        taskField.setText("");
        descriptionField.setText("");
        deadlineField.setText("");
        idField.setText("");
        priorityChoice.select("HIGH");
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        try {
            if (e.getSource() == addButton) {
                String name = taskField.getText().trim();
                if (name.isEmpty()) throw new IllegalArgumentException("Task name is required.");

                manager.addTask(
                    name,
                    descriptionField.getText().trim(),
                    priorityChoice.getSelectedItem(),
                    parseDeadline()
                );

                statusLabel.setText("Task added successfully.");
                clearForm();

            } else if (e.getSource() == updateButton) {
                if (!manager.updateTask(
                        parseId(),
                        taskField.getText().trim(),
                        descriptionField.getText().trim(),
                        priorityChoice.getSelectedItem(),
                        parseDeadline())) {
                    throw new IllegalArgumentException("Task ID not found.");
                }

                statusLabel.setText("Task updated successfully.");

            } else if (e.getSource() == deleteButton) {
                if (!manager.deleteTask(parseId())) {
                    throw new IllegalArgumentException("Task ID not found.");
                }

                statusLabel.setText("Task deleted successfully.");
                clearForm();

            } else if (e.getSource() == completeButton) {
                if (!manager.completeTask(parseId())) {
                    throw new IllegalArgumentException("Task ID not found.");
                }

                statusLabel.setText("Task marked as completed.");
                clearForm();
            }

            refreshDashboard();

        } catch (IllegalArgumentException ex) {
            statusLabel.setText(ex.getMessage());
        }
    }

    private String reminderMessage() {
        for (Task t : manager.allTasks()) {
            if (!"COMPLETED".equals(t.getStatus())) {
                long minutes = Duration.between(LocalDateTime.now(), t.getDeadline()).toMinutes();

                if (minutes >= 0 && minutes <= 60) {
                    return "Reminder: " + t.getName()
                            + " is due within " + minutes + " minute(s).";
                }

                if (t.isOverdue()) {
                    return "Reminder: " + t.getName() + " is overdue.";
                }
            }
        }
        return null;
    }

    private void startReminderThread() {
        Thread reminderThread = new Thread(() -> {
            while (isDisplayable()) {
                try {
                    Thread.sleep(60000);

                    String message = reminderMessage();
                    if (message != null) {
                        EventQueue.invokeLater(() -> statusLabel.setText(message));
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        });

        reminderThread.setName("Task-Reminder-Thread");
        reminderThread.setDaemon(true);
        reminderThread.start();
    }

    public static void main(String[] args) {
        EventQueue.invokeLater(TaskGUI::new);
    }
}
