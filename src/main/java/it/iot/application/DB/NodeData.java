package it.iot.application.DB;

import org.eclipse.californium.elements.exception.ConnectorException;

import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NodeData  {
        private DB db;
        private Connection connection;
        Map<Integer, Integer> lightCounters = new HashMap<>();

        public NodeData() throws ConnectorException, IOException {
            // Inizializza i campi del motore delle risorse
            this.db = new DB();
            this.connection = this.db.connDb();
            System.out.println("Connected to Collector DB, ready to insert Node attribute in DB|");
            ;
        }


        private void createNodeTable() {
            String sql = "CREATE TABLE node " +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "idlights VARCHAR(5), " +
                    "ipv6 VARCHAR(70), " +
                    "timestamp CURRENT_TIMESTAMP";
            try {
                PreparedStatement stmt = this.connection.prepareStatement(sql);
                stmt.executeUpdate(sql);
                System.out.println("[!] Node table created!");

            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        private boolean tableNodeExists(String table) {
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

        public void insertNodeData(int id, String ipv6) {
            String insert = "INSERT INTO node (idLights,IPv6) VALUES (?,?)";
            Connection conn = this.connection;
            if (!tableNodeExists("node")) {
                createNodeTable();
            }

            try {
                PreparedStatement stmt = conn.prepareStatement(insert);
                stmt.setInt(1,id);
                stmt.setString(2,ipv6);
                stmt.executeUpdate();
            }catch (SQLException e) {
                e.printStackTrace();
            }
        }

    public List<String> selectAllNode() {

        List<String> rows = new ArrayList<>();

        try {
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM node");

            while(rs.next()) {
                String row = " ";
                row += "IdLights: " + rs.getInt("idLights");
                row += ", IPv6: " + rs.getString("ipv6");
                rows.add(row);
            }
        } catch(SQLException e) {
            // gestisci eccezione
        }

        return rows;

    }
}