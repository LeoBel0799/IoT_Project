package it.iot.application.DB;

import org.eclipse.californium.elements.exception.ConnectorException;

import java.io.IOException;
import java.sql.*;

public class ActuatorStatus {
    private DB db;
    private Connection connection;

    public ActuatorStatus() throws ConnectorException, IOException {
        // Inizializza i campi del motore delle risorse
        this.db = new DB();
        this.connection = this.db.connDb();
        System.out.println("Connected to Collector DB, ready to insert Actuator status in DB|");
    }

    private void createActuator() {
        String sql = "CREATE TABLE actuator " +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "idActuator VARCHAR(5), " +
                "light VARCHAR(10), " +
                "bright VARCHAR(10)" +
                "wearLevel INTEGER "+
                "fulminated VARCHAR(10)"+
                "timestamp CURRENT_TIMESTAMP";
        try {
            PreparedStatement stmt = this.connection.prepareStatement(sql);
            stmt.executeUpdate(sql);
            System.out.println("[!] Node table created!");

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    private boolean tableActuatorExists(String table) {
        Connection conn = this.connection;
        try {
            DatabaseMetaData dbMetadata = conn.getMetaData();
            ResultSet tables = dbMetadata.getTables(null, null, table, null);

            if (tables.next()) {
                // Tabella esiste
                return true;
            } else {
                // Tabella non esiste
                return false;
            }

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public void insertActuatorData(int idActuator, String light, String bright, Double wearLevel, Boolean fulminated) {
        String insert = "INSERT INTO actuator (idActuator,light,bright,wearLevel,fulminated) VALUES (?,?,?,?,?)";
        Connection conn = this.connection;
        if (!tableActuatorExists("actuator")) {
            createActuator();
        }

        try {
            PreparedStatement stmt = conn.prepareStatement(insert);
            stmt.setInt(1,idActuator);
            stmt.setString(2,light);
            stmt.setString(3,bright);
            stmt.setDouble(4,wearLevel);
            stmt.setBoolean(5,fulminated);
            stmt.executeUpdate();
        }catch (SQLException e) {
            e.printStackTrace();
        }
    }

}
