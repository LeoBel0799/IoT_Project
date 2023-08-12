package it.iot.application.DB;

import java.sql.*;

public class DB {
    private static Connection connection;

    public DB() {
    }

    public static Connection connDb() {
        if (connection != null) {
            return connection;
        } else {
            try {
                String url = "jdbc:mysql://localhost:3306/collector?serverTimezone=UTC";
                String user = "root";
                String password = "PASSWORD";

                connection = DriverManager.getConnection(url, user, password);

                return connection;
            } catch (SQLException e) {
                System.err.println("[FAIL] - Error during connection to DB\n");
                e.printStackTrace(System.err);
                e.getMessage();
            }
        }
        return null;
    }

    public static void dropTable(String tableName) throws SQLException {
        try{
            Connection conn = connDb();
            Statement stm = conn.createStatement();
            String sql = "DROP TABLE IF EXISTS " + tableName;
            stm.executeUpdate(sql);
            System.out.println("[OK] - " + tableName + " dropped successfully");
        }catch (SQLException e){
            System.err.println("[FAIL] - Error while dropping " + tableName);
            e.printStackTrace(System.err);
            e.getMessage();
        }
    }

    public static boolean tableExists(String tableName) {
        try {
            Connection conn = connDb();
            DatabaseMetaData dbMetadata = conn.getMetaData();
            ResultSet tables = dbMetadata.getTables(null, null, tableName, null);

            if (tables.next()) {
                // Tabella esiste
                return true;
            } else {
                // Tabella non esiste
                return false;
            }

        } catch (SQLException e) {
            System.err.println("[FAIL] - Error while checking existence of " + tableName);
            e.printStackTrace(System.err);
            e.getMessage();
            return false;
        }
    }
}
