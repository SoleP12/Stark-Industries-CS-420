package tello;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

public class InstanceDatabaseCreation {
    public static void main(String[] args) {
        // H2 JDBC URL for an in-memory database
        String jdbcURL = "jdbc:h2:mem:testdb"; // "mem:testdb" creates an in-memory database
        String username = "sa";
        String password = ""; // H2 default credentials

        try (Connection connection = DriverManager.getConnection(jdbcURL, username, password);
             Statement statement = connection.createStatement()) {

            // Step 1: Create a table (temporary, exists only during runtime)
            String createTableQuery = "CREATE TABLE Users (" +
                                       "id INT AUTO_INCREMENT PRIMARY KEY, " +
                                       "name VARCHAR(100), " +
                                       "email VARCHAR(100))";
            statement.executeUpdate(createTableQuery);
            System.out.println("Table 'Users' created successfully!");

            // Step 2: Insert sample data
            String insertDataQuery = "INSERT INTO Users (name, email) VALUES " +
                                      "('Alice', 'alice@example.com'), " +
                                      "('Bob', 'bob@example.com')";
            statement.executeUpdate(insertDataQuery);
            System.out.println("Sample data inserted into the table.");

            // Step 3: Query the data
            ResultSet resultSet = statement.executeQuery("SELECT * FROM Users");
            while (resultSet.next()) {
                System.out.println("ID: " + resultSet.getInt("id") +
                                   ", Name: " + resultSet.getString("name") +
                                   ", Email: " + resultSet.getString("email"));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
