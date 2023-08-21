package it.iot.application.DB;

import org.eclipse.californium.elements.exception.ConnectorException;

import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LightData {
    Map<Integer, Integer> lightCounters = new HashMap<>();

    public LightData() throws ConnectorException, IOException {
        // Inizializza i campi del motore delle risorse
        System.out.println("[INFO] - Connected to Collector DB, ready to insert Motion measurements in DB!");
    }


    private void createMotionTable() {
        String sql = "CREATE TABLE motion (" +
                "id INTEGER AUTO_INCREMENT PRIMARY KEY,"+
                "idlight INTEGER,"+
                "counter INTEGER," +
                "lights VARCHAR(5), " +
                "lightsDegree INTEGER, " +
                "brights VARCHAR(5)"+
                ");";
        try {
            Connection conn = DB.connDb();
            PreparedStatement stmt =conn.prepareStatement(sql);
            stmt.executeUpdate(sql);
            System.out.println("[OK] - Motion table created!");

        } catch (SQLException e) {
            System.err.println("[FAIL] - Error during creating Motion Table in DB\n");
            e.printStackTrace(System.err);
            e.getMessage();
        }
    }

    public void createDelete(String table) throws SQLException {
        if (!DB.tableExists(table)) {
            createMotionTable();
        }else{
            DB.dropTable(table);
            createMotionTable();
        }
    }

    public void insertMotionData(int idlight, String lights, int lights_degree, String brights) throws SQLException {
        String insert = "INSERT INTO motion (idlight,counter,lights,lightsDegree,brights) VALUES (?,?,?,?,?)";

        //System.out.println("[INFO] - Inserting  Light record in DB");
        int count = lightCounters.getOrDefault(idlight, 0) + 1;
        lightCounters.put(idlight, count);

        try {
            Connection conn = DB.connDb();
            PreparedStatement stmt = conn.prepareStatement(insert);
            stmt.setInt(1,idlight);
            stmt.setInt(2,count);
            stmt.setString(3, lights);
            stmt.setInt(4, lights_degree);
            stmt.setString(5, brights);
            stmt.executeUpdate();
            //System.out.println("[OK] - Light Data record inserted into DB");

        }catch (SQLException e) {
            System.err.println("[FAIL] - Error during insertion data record into Motion table\n");
            e.printStackTrace(System.err);
            e.getMessage();
        }
    }

    public String getLightStatus(int lightId) {
        String lightStatus = null;

        try {
            Connection conn = DB.connDb();

            String sql = "SELECT lights FROM motion WHERE id = ?";

            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, lightId);

            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                lightStatus = rs.getString("lights");
            }

        } catch (SQLException e) {
            System.err.println("[FAIL] - Error during reading Light status data from DB");
            e.printStackTrace(System.err);
            e.getMessage();
        }

        return lightStatus;

    }

    public int getCounterForLight(int lightId) {

        int counter = 0;
        try {
            Connection conn = DB.connDb();
            // Query per ottenere il contatore
            String sql = "SELECT MAX(counter) AS max_counter FROM motion WHERE id = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, lightId);

            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                // Estrai contatore
                counter = rs.getInt("counter");
            }

        } catch (SQLException e) {
            System.err.println("[FAIL] - Error during Counter reading field from DB");
            e.printStackTrace(System.err);
            e.getMessage();
        }

        return counter;

    }

    public List<String> selectAllMotion() {

        List<String> rows = new ArrayList<>();

        try {
            Connection conn = DB.connDb();
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
            System.err.println("[FAIL] - Error during Light data reading from DB");
            e.printStackTrace(System.err);
            e.getMessage();
        }

        return rows;

    }
}