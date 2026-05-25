package database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {
    // Налаштування підключення
    private static final String URL = "jdbc:mysql://localhost:3306/course-project-db";
    private static final String USER = "root";
    private static final String PASSWORD = "";

    // Метод для створення з'єднання
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}
