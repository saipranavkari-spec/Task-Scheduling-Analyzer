package com.taskanalyzer;

import java.awt.*;
import java.awt.event.*;

public class TaskGUI extends Frame implements ActionListener {

    private TextField taskField;
    private Choice priorityChoice;
    private Button addButton;
    private Label resultLabel;

    public TaskGUI() {
        setTitle("Task Scheduling Analyzer");
        setSize(450, 300);
        setLayout(new FlowLayout());

        add(new Label("Task Name:"));
        taskField = new TextField(20);
        add(taskField);

        add(new Label("Priority:"));
        priorityChoice = new Choice();
        priorityChoice.add("HIGH");
        priorityChoice.add("MEDIUM");
        priorityChoice.add("LOW");
        add(priorityChoice);

        addButton = new Button("Add Task");
        addButton.addActionListener(this);
        add(addButton);

        resultLabel = new Label("Enter a task and select priority.");
        add(resultLabel);

        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                dispose();
            }
        });

        setVisible(true);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        String task = taskField.getText().trim();
        String priority = priorityChoice.getSelectedItem();

        if (task.isEmpty()) {
            resultLabel.setText("Please enter a task name.");
        } else {
            resultLabel.setText("Added: " + task + " (" + priority + ")");
            taskField.setText("");
        }
    }

    public static void main(String[] args) {
        new TaskGUI();
    }
}
