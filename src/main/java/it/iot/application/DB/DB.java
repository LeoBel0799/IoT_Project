package it.iot.application.DB;

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
}
