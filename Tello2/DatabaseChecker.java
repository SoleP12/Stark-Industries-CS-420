package tello;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

public class DatabaseChecker {
    public static void main(String[] args) {
        // Database credentials
        String url = "jdbc:mysql://localhost:3306/drone_database";
        String username = "root";
        String password = "ZiggleFox43#";

        // Connection and query execution
        try (Connection connection = DriverManager.getConnection(url, username, password)) {
            System.out.println("Connected to the database!");

            // Create a statement
            Statement statement = connection.createStatement();
            String query = "SELECT * FROM traffic_data";

            // Execute query and process the result set
            ResultSet resultSet = statement.executeQuery(query);
            while (resultSet.next()) {
                System.out.println("Column1: " + resultSet.getString("location"));
                System.out.println("Column2: " + resultSet.getInt("car_count"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
