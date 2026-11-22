package com.smartcountdown.database;

import com.smartcountdown.model.Task;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class TaskRepository {

    // Insert a task
    public void insert(Task task) throws SQLException {

        String sql = """
                INSERT INTO tasks
                (title, category, deadline, priority, completed)
                VALUES (?, ?, ?, ?, ?)
                """;

        try (Connection connection =
                     DatabaseManager.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setString(1, task.getTitle());
            statement.setString(2, task.getCategory());
            statement.setString(3,
                    task.getDeadline().toString());
            statement.setString(4, task.getPriority());
            statement.setInt(5,
                    task.isCompleted() ? 1 : 0);

            statement.executeUpdate();

            try (Statement idStatement =
                         connection.createStatement();
                 ResultSet result =
                         idStatement.executeQuery(
                                 "SELECT last_insert_rowid()")) {

                if (result.next()) {
                    task.setId(result.getLong(1));
                }
            }
        }
    }

    // Retrieve all tasks
    public List<Task> findAll() throws SQLException {

        List<Task> taskList = new ArrayList<>();

        String sql = """
                SELECT * FROM tasks
                ORDER BY deadline ASC
                """;

        try (Connection connection =
                     DatabaseManager.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql);
             ResultSet result = statement.executeQuery()) {

            while (result.next()) {

                Task task = new Task(
                        result.getString("title"),
                        result.getString("category"),
                        LocalDate.parse(
                                result.getString("deadline")),
                        result.getString("priority")
                );

                task.setId(result.getLong("id"));

                task.setCompleted(
                        result.getInt("completed") == 1
                );

                taskList.add(task);
            }
        }

        return taskList;
    }

    // Delete a task using its database ID
    public void deleteById(long id) throws SQLException {

        String sql = "DELETE FROM tasks WHERE id = ?";

        try (Connection connection =
                     DatabaseManager.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setLong(1, id);
            statement.executeUpdate();
        }
    }

    public void update(Task task) throws SQLException {
        String sql = """
                UPDATE tasks
                SET title = ?,
                 category = ?,
                 deadline = ?,
                 priority  = ?,
                 completed = ?
                WHERE id = ?
                """;
        try (Connection connection = DatabaseManager.getConnection();
                    PreparedStatement statement = connection.prepareStatement(sql)) {
                            statement.setString(1,task.getTitle());
                            statement.setString(2,task.getCategory());
                            statement.setString(3,task.getDeadline().toString());
                            statement.setString(4,task.getPriority());
                            statement.setInt(5,task.isCompleted() ? 1 : 0);
                            statement.setLong(6,task.getId());
                            statement.executeUpdate();
        }
    }
}