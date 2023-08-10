package it.iot.application.DB;

import org.eclipse.californium.elements.exception.ConnectorException;

import java.io.IOException;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;

public class LightData {
    private DB db;
    private Connection connection;
    Map<Integer, Integer> lightCounters = new HashMap<>();

    public LightData() throws ConnectorException, IOException {
        // Inizializza i campi del motore delle risorse
        this.db = new DB();
        this.connection = this.db.connDb();
        System.out.println("Connected to Collector DB, ready to insert Motion measurements in DB|");
        ;
    }


    private void createMotionTable() {
        String sql = "CREATE TABLE motion " +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "idlights INTEGER,"+
                "lights VARCHAR(5), " +
                "lightsDegree INTEGER, " +
                "brights INTEGER, " +
                "lightsOnCount INTEGER, " +
                "lightsOffCount INTEGER," +
                "timestamp CURRENT_TIMESTAMP";
        try {
            PreparedStatement stmt = this.connection.prepareStatement(sql);
            stmt.executeUpdate(sql);
            System.out.println("[!] Motion table created!");

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private boolean tableMotionExists(String table) {
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

    public void insertMotionData(int id, String lights, int lights_degree, String brights) {
        String insert = "INSERT INTO coapmotion (id,counter,lights,lightsDegree,brights) VALUES (?,?,?,?,?)";
        Connection conn = this.connection;
        if (!tableMotionExists("coapmotion")) {
            createMotionTable();
        }
        int count = lightCounters.getOrDefault(id, 0) + 1;
        lightCounters.put(id, count);

        try {
            PreparedStatement stmt = conn.prepareStatement(insert);
            stmt.setInt(1,id);
            stmt.setInt(2,count);
            stmt.setString(3, lights);
            stmt.setInt(4, lights_degree);
            stmt.setString(5, brights);
            stmt.executeUpdate();
        }catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public String getLightStatus(int lightId) {
        Connection conn = this.connection;
        String lightStatus = null;

        try {

            String sql = "SELECT lights FROM coapmotion WHERE id = ?";

            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, lightId);

            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                lightStatus = rs.getString("lights");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return lightStatus;

    }

    public int getCounterForLight(int lightId) {

        int counter = 0;
        try {
            Connection conn = this.connection;
            // Query per ottenere il contatore
            String sql = "SELECT counter FROM coapmotion WHERE id = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, lightId);

            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                // Estrai contatore
                counter = rs.getInt("counter");
            }

        } catch (SQLException e) {
            // Gestisci eccezione
        }

        return counter;

    }
}