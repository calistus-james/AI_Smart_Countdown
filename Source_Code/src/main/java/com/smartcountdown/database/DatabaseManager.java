package com.smartcountdown.database;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseManager {

    private static final Path DB_DIRECTORY =
            Path.of(System.getProperty("user.home"),
                    ".aismartcountdown");

    private static final Path DB_PATH =
            DB_DIRECTORY.resolve("smartcountdown.db");

    private static final String URL =
            "jdbc:sqlite:" + DB_PATH;

    public static Connection getConnection()
            throws SQLException {

        return DriverManager.getConnection(URL);
    }

    public static void initializeDatabase()
            throws Exception {

        Files.createDirectories(DB_DIRECTORY);

        String sql = """
                CREATE TABLE IF NOT EXISTS tasks (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    title TEXT NOT NULL,
                    category TEXT NOT NULL,
                    deadline TEXT NOT NULL,
                    priority TEXT NOT NULL,
                    completed INTEGER DEFAULT 0
                )
                """;

        try (Connection connection = getConnection();
             Statement statement = connection.createStatement()) {

            statement.execute(sql);
        }

        System.out.println(
                "Database initialized: " + DB_PATH
        );
    }
}
