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
                "brights VARCHAR(5),"+
                "bootstrapped VARCHAR(5),"+
                "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP"+
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

    public void insertMotionData(int idlight, String lights, int lights_degree, String brights, Boolean bootstrapped) throws SQLException {
        String insert = "INSERT INTO motion (idlight,counter,lights,lightsDegree,brights,bootstrapped) VALUES (?,?,?,?,?,?)";

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
            stmt.setBoolean(6,bootstrapped);
            stmt.executeUpdate();
            //System.out.println("[OK] - Light Data record inserted into DB");

        }catch (SQLException e) {
            System.err.println("[FAIL] - Error during insertion data record into Motion table\n");
            e.printStackTrace(System.err);
            e.getMessage();
        }
    }

    public String getLightStatus(int lightId) throws SQLException {
        String lightStatus = null;
        Connection conn = DB.connDb();
        String sql = "SELECT lights FROM motion WHERE idlight=? ORDER BY created_at DESC LIMIT 1";
        PreparedStatement stmt = conn.prepareStatement(sql);
        stmt.setInt(1, lightId);
        ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                lightStatus = rs.getString("lights");
            }
        return lightStatus;
    }


    public void setBootstrappedStatus(int idlight, boolean bootstrapped) throws SQLException {
        Connection conn = DB.connDb();
        String update = "UPDATE motion SET bootstrapped = ? WHERE idlight = ?";
        PreparedStatement stmt = conn.prepareStatement(update);
        stmt.setBoolean(1, bootstrapped);
        stmt.setInt(2, idlight);
        stmt.executeUpdate();
        System.out.println("[OK] - Bootstrapped status updated in Motion table");

    }


    public Boolean getBootstrappedStatus(int idlight) throws SQLException {
        String select = "SELECT bootstrapped FROM motion WHERE idlight = ? AND bootstrapped = true ORDER BY counter DESC LIMIT 1";
        Connection conn = DB.connDb();
        PreparedStatement stmt = conn.prepareStatement(select);
        stmt.setInt(1, idlight);

        try (ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                return true; // La luce con l'ID specificato ha il flag bootstrapped settato a true
            }
        }

        return false; // La luce con l'ID specificato non ha il flag bootstrapped settato a true
    }


    public boolean lightExists(int lightId) throws SQLException {
        boolean exists = false;
        Connection conn = DB.connDb();
        String sql = "SELECT COUNT(*) AS count FROM motion WHERE idlight = ?";
        PreparedStatement stmt = conn.prepareStatement(sql);
        stmt.setInt(1, lightId);
        ResultSet rs = stmt.executeQuery();

        if(rs.next()) {
            int count = rs.getInt("count");
            if(count > 0) {
                    exists = true;
            }
        }
        return exists;
    }

    public int getCounterForLight(int lightId) throws SQLException {
        int counter = 0;
        Connection conn = DB.connDb();
        // Query per ottenere il contatore
        String sql = "SELECT MAX(counter) AS counter FROM motion WHERE idlight = ?";
        PreparedStatement stmt = conn.prepareStatement(sql);
        stmt.setInt(1, lightId);
        ResultSet rs = stmt.executeQuery();
        if (rs.next()) {
            // Estrai contatore
            counter = rs.getInt("counter");
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
                row += "IdLights: " + rs.getInt("idlight");
                row += ", Counter: " + rs.getString("counter");
                row += ", Lights: " + rs.getString("lights");
                row += ", LightsDegree: " + rs.getInt("lightsDegree");
                row += ", Brights: " + rs.getString("brights");
                row += ", Bootstrepped: " + rs.getString("bootstrapped");
                row += ", Time:" + rs.getString("created_at");
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