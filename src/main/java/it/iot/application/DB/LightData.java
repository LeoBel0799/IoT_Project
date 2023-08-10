package it.iot.application.DB;

import org.eclipse.californium.elements.exception.ConnectorException;

import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LightData {
    DB db = new DB();
    Map<Integer, Integer> lightCounters = new HashMap<>();

    public LightData() throws ConnectorException, IOException {
        // Inizializza i campi del motore delle risorse
        System.out.println("Connected to Collector DB, ready to insert Motion measurements in DB!");
    }


    private void createMotionTable() {
        String sql = "CREATE TABLE motion " +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "idlights INTEGER,"+
                "counter INTEGER," +
                "lights VARCHAR(5), " +
                "lightsDegree INTEGER, " +
                "brights VARCHAR , " +
                "timestamp CURRENT_TIMESTAMP";
        try {
            Connection conn = db.connDb();
            PreparedStatement stmt =conn.prepareStatement(sql);
            stmt.executeUpdate(sql);
            System.out.println("[!] Motion table created!");

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private boolean tableMotionExists(String table) {
        try {
            Connection conn = db.connDb();
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
        String insert = "INSERT INTO motion (id,counter,lights,lightsDegree,brights) VALUES (?,?,?,?,?)";
        if (!tableMotionExists("coapmotion")) {
            createMotionTable();
        }
        System.out.println("[!] Receiving light data");
        int count = lightCounters.getOrDefault(id, 0) + 1;
        lightCounters.put(id, count);

        try {
            Connection conn = db.connDb();
            PreparedStatement stmt = conn.prepareStatement(insert);
            stmt.setInt(1,id);
            stmt.setInt(2,count);
            stmt.setString(3, lights);
            stmt.setInt(4, lights_degree);
            stmt.setString(5, brights);
            System.out.println("[!] Insert light data in DB");
            stmt.executeUpdate();

        }catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public String getLightStatus(int lightId) {
        String lightStatus = null;

        try {
            Connection conn = db.connDb();

            String sql = "SELECT lights FROM motion WHERE id = ?";

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
            Connection conn = db.connDb();
            // Query per ottenere il contatore
            String sql = "SELECT counter FROM motion WHERE id = ?";
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

    public List<String> selectAllMotion() {

        List<String> rows = new ArrayList<>();

        try {
            Connection conn = db.connDb();
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM motion");

            while(rs.next()) {
                String row = " ";
                row += "IdLights: " + rs.getInt("idlights");
                row += ", Counter: " + rs.getString("counter");
                row += ", Lights: " + rs.getString("lights");
                row += ", LightsDegree: " + rs.getInt("lightsDegree");
                row += ", Brights: " + rs.getString("brights");
                rows.add(row);
            }
        } catch(SQLException e) {
            // gestisci eccezione
        }

        return rows;

    }
}