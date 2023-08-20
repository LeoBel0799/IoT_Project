package it.iot.application.DB;

import org.eclipse.californium.elements.exception.ConnectorException;

import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NodeData  {

        public NodeData() throws ConnectorException, IOException {
            System.out.println("[INFO] - Connected to Collector DB, ready to insert Node attribute in DB");
        }


        private void createNodeTable() {
            String sql = "CREATE TABLE node (" +
                    "id INTEGER AUTO_INCREMENT  PRIMARY KEY, " +
                    "idlight INTEGER, " +
                    "ipv6 VARCHAR(70)" +
                    ");";
            try {
                Connection conn = DB.connDb();
                PreparedStatement stmt = conn.prepareStatement(sql);
                stmt.executeUpdate(sql);
                System.out.println("[OK] - Node table created!");

            } catch (SQLException e) {
                System.err.println("[FAIL] - Error during creating Node Table in DB\n");
                e.printStackTrace(System.err);
                e.getMessage();
            }
        }


        public void createDelete(String table) throws SQLException {
            if (!DB.tableExists(table)) {
                createNodeTable();
            }else{
                DB.dropTable(table);
                createNodeTable();
            }
        }
        public void insertNodeData(int id, String ipv6) throws SQLException {
            String insert = "INSERT INTO node (idlight,IPv6) VALUES (?,?)";
            //System.out.println(" [INFO] - Receiving node data");


            try {
                Connection conn = DB.connDb();
                PreparedStatement stmt = conn.prepareStatement(insert);
                stmt.setInt(1,id);
                stmt.setString(2,ipv6);
                stmt.executeUpdate();
                //System.out.println("[OK] - Node data inserted into DB");
            }catch (SQLException e) {
                System.err.println("[FAIL] - Error during insertion data into Node table\n");
                e.printStackTrace(System.err);
                e.getMessage();
            }
        }

    public List<String> selectAllNode() {
        List<String> rows = new ArrayList<>();

        try {
            Connection conn = DB.connDb();

            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM node");

            while(rs.next()) {
                String row = " ";
                row += "idLights: " + rs.getInt("idlight");
                row += ", IPv6: " + rs.getString("ipv6");
                rows.add(row);
            }
        } catch(SQLException e) {
            System.err.println("[FAIL] - Error during reading data from Node table\n");
            e.printStackTrace(System.err);
            e.getMessage();
        }

        return rows;

    }


}