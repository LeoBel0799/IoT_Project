package it.iot.application.DB;

import it.iot.application.utils.DB;
import org.eclipse.californium.elements.exception.ConnectorException;

import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ActuatorStatus {
    private DB db;
    Map<Integer, Integer> actuatorCounters = new HashMap<>();

    public ActuatorStatus() throws ConnectorException, IOException {
        System.out.println("[INFO] - Connected to Collector DB, ready to insert Actuator status in DB!");
    }

    private void createActuator() {
        String sql = "CREATE TABLE actuator (" +
                "id INTEGER AUTO_INCREMENT  PRIMARY KEY, " +
                "idActuator INTEGER, " +
                "counter INTEGER,"+
                "light VARCHAR(10), " +
                "bright VARCHAR(10)," +
                "wearLevel DOUBLE, "+
                "fulminated VARCHAR(10),"+
                "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP"+
                ");";
        try {
            Connection conn = db.connDb();
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.executeUpdate(sql);
            System.out.println("[OK] - Actuator table created!");

        } catch (SQLException e) {
            System.err.println("[FAIL] - Error during creating Actuator Table in DB\n");
            e.printStackTrace(System.err);
            e.getMessage();
        }
    }

    public void createDelete(String table) throws SQLException {
        if (!DB.tableExists(table)) {
            createActuator();
        }else{
            DB.dropTable(table);
            createActuator();
        }
    }

    public void insertActuatorData(int idActuator, int counter, String light, String bright, Double wearLevel, Boolean fulminated) throws SQLException {
        String insert = "INSERT INTO actuator (idActuator, counter, light,bright,wearLevel,fulminated) VALUES (?,?,?,?,?,?)";
        System.out.println("[INFO] - Receiving actuator data");

        try {
            Connection conn = db.connDb();
            PreparedStatement stmt = conn.prepareStatement(insert);
            stmt.setInt(1,idActuator);
            stmt.setInt(2,counter);
            stmt.setString(3,light);
            stmt.setString(4,bright);
            stmt.setDouble(5,wearLevel);
            stmt.setBoolean(6,fulminated);
            stmt.executeUpdate();
            System.out.println("[OK] - Actuator Data inserted into DB");

        }catch (SQLException e) {
            System.err.println("[FAIL] - Error during insertion data into Actuator table\n");
            e.printStackTrace(System.err);
            e.getMessage();
        }
    }

    public List<String> selectAllActuatorStatus() {

        List<String> rows = new ArrayList<>();

        try {
            Connection conn = db.connDb();
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM actuator");

            while(rs.next()) {
                String row = " ";
                row += "IdActuator: " + rs.getInt("idActuator");
                row += ", Counter: " + rs.getString("counter");
                row += ", Lights: " + rs.getString("light");
                row += ", Brights: " + rs.getString("bright");
                row += ", Wear Level: " + rs.getDouble("wearLevel");
                row += ", Fulminated: " + rs.getBoolean("fulminated");
                row += ", Time:" + rs.getString("created_at");
                rows.add(row);
            }
        } catch(SQLException e) {
            System.err.println("[FAIL] - Error during reading actuator data from DB\n");
            e.printStackTrace(System.err);
            e.getMessage();
        }

        return rows;

    }

    public boolean getFulminatedFromActuator(int idLight) throws SQLException {
        boolean fulminated = false;
        String select = "SELECT fulminated FROM actuator WHERE idActuator=? ORDER BY created_at DESC LIMIT 1";
        Connection conn = db.connDb();
        PreparedStatement stmt = conn.prepareStatement(select);
        stmt.setInt(1, idLight);

        ResultSet rs = stmt.executeQuery();

        if (rs.next()) {
            fulminated = rs.getBoolean("fulminated");
        }

        return fulminated;

    }


    public String getLightStatusFromActuator(int idLight) throws SQLException {
        String lightStatus = null;
        String select = "SELECT light FROM actuator WHERE idActuator=? ORDER BY created_at DESC LIMIT 1";
            Connection conn = db.connDb();
            PreparedStatement stmt = conn.prepareStatement(select);
            stmt.setInt(1, idLight);

            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                lightStatus = rs.getString("light");
            }

        return lightStatus;

    }
    public String getBrightStatusFromActuator(int idLight) throws SQLException {
        String brightStatus = null;
        String select = "SELECT bright FROM actuator WHERE idActuator=? ORDER BY created_at DESC LIMIT 1";
        Connection conn = db.connDb();
        PreparedStatement stmt = conn.prepareStatement(select);
        stmt.setInt(1, idLight);

        ResultSet rs = stmt.executeQuery();

        if (rs.next()) {
            brightStatus = rs.getString("bright");
        }

        return brightStatus;

    }

    public Double getWearLevelromActuator(int idActuator) throws SQLException {
        Double lightStatus = null;
        String select = "SELECT wearLevel FROM actuator WHERE idActuator=? ORDER BY created_at DESC LIMIT 1";
            Connection conn = db.connDb();
            PreparedStatement stmt = conn.prepareStatement(select);
            stmt.setInt(1, idActuator);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                lightStatus = rs.getDouble("wearLevel");
            }
        return lightStatus;

    }


    public void setFulminatedStatus(int idActuator) throws SQLException {
        String update = "UPDATE actuator SET fulminated = ? WHERE idActuator = ?";
            Connection conn = db.connDb();
            PreparedStatement stmt = conn.prepareStatement(update);
            stmt.setBoolean(1, true);
            stmt.setInt(2, idActuator);
            stmt.executeUpdate();
    }

    public boolean getFulminatedStatus (int idActuator) throws SQLException {
        Boolean stats = null;
        String select = "SELECT fulminated FROM actuator WHERE idActuator=?";
        Connection conn = db.connDb();
        PreparedStatement stmt = conn.prepareStatement(select);
        stmt.setInt(1, idActuator);
        ResultSet rs = stmt.executeQuery();
        if (rs.next()) {
            stats = rs.getBoolean("fulminated");
        }
        return stats;
    }


    public void insertWearAndFulminatedResetted(int idActuator, float wear, boolean fulm, int count) throws SQLException {
        String update = "UPDATE actuator SET wearLevel = ?, fulminated = ?, counter = ? WHERE idActuator = ?";
            Connection conn = db.connDb();
            PreparedStatement stmt = conn.prepareStatement(update);
            stmt.setFloat(1, wear);
            stmt.setBoolean(2, fulm);
            stmt.setInt(3,count);
            stmt.setInt(4, idActuator);
            stmt.executeUpdate();

    }

    public int getCounterForActuator(int idActuator) throws SQLException {

        int counter = 0;
            Connection conn = DB.connDb();
            // Query per ottenere il contatore
            String sql = "SELECT MAX(counter) AS counter FROM actuator WHERE idActuator = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, idActuator);

            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                // Estrai contatore
                counter = rs.getInt("counter");
            }



        return counter;

    }

}
