# Task Scheduling Analyzer

Java end-semester project for managing, prioritizing and scheduling tasks.

## Implemented Features

- **Task Management:** add, update, delete and complete tasks
- **Task Details:** name, description, priority and deadline
- **Smart Scheduling:** priority-based recommendation
- **Scheduling Algorithms:** Priority Scheduling, Earliest Deadline First (EDF) and FCFS
- **Reminder System:** background reminder thread for approaching and overdue tasks
- **Dashboard:** total, pending, completed, overdue and completion-rate statistics
- **Reports:** task summary and recommended next task
- **Persistence:** tasks are saved to `task_analyzer_data.ser`
- **Exception Handling:** validation for empty names, task IDs and deadline format
- **OOP:** separate `Task` model and `TaskManager` service
- **AWT GUI:** simple Java AWT desktop interface

## Run

From the project root:

```bash
mvn compile
java -cp target/classes com.taskanalyzer.TaskGUI
```

If Maven is not installed on Windows, use the Maven wrapper:

```cmd
mvnw.cmd compile
java -cp target/classes com.taskanalyzer.TaskGUI
```

## Deadline Format

Use:

`yyyy-MM-dd HH:mm`

Example:

`2026-11-20 18:30`

## Main Project Structure

```
src/main/java/com/taskanalyzer/
├── Task.java
├── TaskManager.java
├── TaskGUI.java
└── TaskAnalyzerApplication.java
```
