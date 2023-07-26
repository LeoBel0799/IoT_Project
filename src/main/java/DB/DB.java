package DB;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DB {
    private Connection connection;

    public DB() {
        System.out.println("Instantiating!");
    }

    public Connection connectDbs() {
        if (connection != null) {
            return connection;
        } else {
            try {
                String url = "jdbc:mysql://localhost/collector";
                String user = "root";
                String password = "PASSWORD";

                connection = DriverManager.getConnection(url, user, password);
                return connection;
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public static void main(String[] args) {
        // Usage example
        DB database = new DB();
        Connection connection = database.connectDbs();
        if (connection != null) {
            System.out.println("Connected to the database!");
            // Perform database operations here
        } else {
            System.out.println("Failed to connect to the database.");
        }
    }
}
