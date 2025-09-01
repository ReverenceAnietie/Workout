package workout;

import java.sql.*;

public class Database {
    private static Connection connection;
    private static final String URL = "jdbc:postgresql://localhost:5432/workout_db";
    private static final String USER = "postgres";
    private static final String PASSWORD = "Admin";

    public static Connection getConnection() {
        try {
            // If no connection OR connection is closed → open a new one
            if (connection == null || connection.isClosed()) {
                Class.forName("org.postgresql.Driver");
                connection = DriverManager.getConnection(URL, USER, PASSWORD);
                connection.setAutoCommit(true);
            }
        } catch (ClassNotFoundException e) {
            System.err.println("PostgreSQL JDBC Driver not found: " + e.getMessage());
            e.printStackTrace();
        } catch (SQLException e) {
            System.err.println("Connection to PostgreSQL failed: " + e.getMessage());
            e.printStackTrace();
        }
        return connection;
    }

    public static void createTables() {
        try (Statement stmt = getConnection().createStatement()) {
            stmt.execute("CREATE TABLE IF NOT EXISTS users (" +
                    "id SERIAL PRIMARY KEY, " +
                    "username VARCHAR(50) UNIQUE NOT NULL, " +
                    "password VARCHAR(255) NOT NULL, " +
                    "name VARCHAR(100), " +
                    "email VARCHAR(100), " +
                    "age INTEGER, " +
                    "profile_image_path VARCHAR(255), " +
                    "preferred_unit VARCHAR(3) DEFAULT 'KG')");

            stmt.execute("CREATE TABLE IF NOT EXISTS workouts (" +
                    "id SERIAL PRIMARY KEY, " +
                    "user_id INTEGER NOT NULL, " +
                    "exercise VARCHAR(100) NOT NULL, " +
                    "sets INTEGER NOT NULL, " +
                    "reps INTEGER NOT NULL, " +
                    "weight REAL NOT NULL, " +
                    "unit VARCHAR(3) NOT NULL, " +
                    "date DATE NOT NULL, " +
                    "FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE)");

            stmt.execute("CREATE TABLE IF NOT EXISTS routines (" +
                    "id SERIAL PRIMARY KEY, " +
                    "user_id INTEGER NOT NULL, " +
                    "name VARCHAR(100) NOT NULL, " +
                    "FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE)");

            stmt.execute("CREATE TABLE IF NOT EXISTS routine_exercises (" +
                    "id SERIAL PRIMARY KEY, " +
                    "routine_id INTEGER NOT NULL, " +
                    "exercise VARCHAR(100) NOT NULL, " +
                    "sets INTEGER NOT NULL, " +
                    "reps INTEGER NOT NULL, " +
                    "FOREIGN KEY (routine_id) REFERENCES routines(id) ON DELETE CASCADE)");

        } catch (SQLException e) {
            System.err.println("Error creating tables: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
