package it.iot.application.DB;

import org.eclipse.californium.elements.exception.ConnectorException;

import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ActuatorStatus {
    private DB db;

    public ActuatorStatus() throws ConnectorException, IOException {
        System.out.println("[INFO] - Connected to Collector DB, ready to insert Actuator status in DB|");
    }

    private void createActuator() {
        String sql = "CREATE TABLE actuator (" +
                "id INTEGER AUTO_INCREMENT  PRIMARY KEY, " +
                "idActuator INTEGER, " +
                "light VARCHAR(10), " +
                "bright VARCHAR(10)," +
                "wearLevel INTEGER, "+
                "fulminated VARCHAR(10)"+
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

    public void insertActuatorData(int idActuator, String light, String bright, Double wearLevel, Boolean fulminated) throws SQLException {
        String insert = "INSERT INTO actuator (idActuator,light,bright,wearLevel,fulminated) VALUES (?,?,?,?,?)";
        System.out.println("[INFO] - Receiving actuator data");

        try {
            Connection conn = db.connDb();
            PreparedStatement stmt = conn.prepareStatement(insert);
            stmt.setInt(1,idActuator);
            stmt.setString(2,light);
            stmt.setString(3,bright);
            stmt.setDouble(4,wearLevel);
            stmt.setBoolean(5,fulminated);
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
                row += ", Lights: " + rs.getString("light");
                row += ", Brights: " + rs.getString("bright");
                row += ", Wear Level: " + rs.getInt("wearLevel");
                row += ", Fulminated: " + rs.getString("fulminated");
                rows.add(row);
            }
        } catch(SQLException e) {
            System.err.println("[FAIL] - Error during reading actuator data from DB\n");
            e.printStackTrace(System.err);
            e.getMessage();
        }

        return rows;

    }

}
