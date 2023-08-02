package it.iot.application.actuators;

import it.iot.application.DB.DB;
import org.eclipse.californium.core.CoapClient;
import org.eclipse.californium.core.CoapHandler;
import org.eclipse.californium.core.CoapResponse;
import org.eclipse.californium.core.coap.CoAP;
import org.eclipse.californium.core.coap.MediaTypeRegistry;
import org.eclipse.californium.core.coap.Request;
import org.eclipse.californium.elements.exception.ConnectorException;
import org.json.JSONObject;
import org.json.simple.JSONValue;

import java.io.IOException;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.logging.Handler;
import java.util.logging.Level;

import static it.iot.remote.database.DBremote.connectDbs;

public class LightsStatus {
    private DB db;
    private Connection connection;
    private static final double MAX_WEAR_LEVEL = 5.0;
    private String address;
    private String resource;
    private String lightId;
    private Boolean lightFulminated; // Stato delle luci, true se sono fulminate, false altrimenti
    private double wearLevel;
    private Handler Logging;

    public LightsStatus(String sourceAddress, String resource) throws ConnectorException, IOException {
        // Inizializza i campi del motore delle risorse
        this.db = new DB();
        this.connection = this.db.connDb();
        System.out.println("Connected to Collector DB");
        // Initialize LightStatus resource fields
        this.address = sourceAddress;
        this.resource = resource;
        // Start observing for updates
        this.startObserving();
        System.out.println("LightStatus resource initialized");
    }



    public double handleMqttMessage(byte[] payload) throws ConnectorException, IOException {
        System.out.println("Callback called, resource arrived");
        if (payload != null && payload.length > 0) {
            String payloadStr = new String(payload);
            JSONObject jsonPayload = (JSONObject) JSONValue.parse(payloadStr);
            String id = (String)jsonPayload.get("id");
            Boolean lightFulminated = (Boolean) jsonPayload.get("lightFulminated");
            String wearLevel = (String)jsonPayload.get("wearLevel");
            System.out.println("Detection value node:");
            System.out.println("id: " + id);
            System.out.println("lightFulminated: " + lightFulminated);
            System.out.println("wearLevel: " + wearLevel);

            this.lightId = id;
            this.lightFulminated = lightFulminated;
            this.wearLevel = Double.parseDouble(wearLevel);

            String wearLevelStr = (String) jsonPayload.get("wearLevel");
            double receivedWearLevel = Double.parseDouble(wearLevelStr);
            this.wearLevel = Math.max(this.wearLevel, receivedWearLevel); // Aggiorna il grado di usura al massimo valore tra quello attuale e quello ricevuto

            // Verifica se le luci sono fulminate in base al grado di usura massimo raggiunto
            if (this.wearLevel >= MAX_WEAR_LEVEL) {
                this.lightFulminated.equals("T");
            } else {
                this.lightFulminated.equals("F");
            }

            // Salva il valore del campo lightFulminated nel database solo quando il grado di usura è al massimo
            if (this.wearLevel >= MAX_WEAR_LEVEL) {
                executeQueryLight(lightFulminated);
            }
        }
        return wearLevel;
    }

    private void createCoapLightStatusTable() {
        String sql = "CREATE TABLE coaplightstatus " +
                " id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                " lightFulminated VARCHAR(10), " +
                " timestamp CURRENT_TIMESTAMP";
        try{
            PreparedStatement stmt = this.connection.prepareStatement(sql);
            stmt.executeUpdate(sql);
            System.out.println("[!] CoapLightStatus table created!");

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private boolean tableCoapLightStatusExists (String table){
        Connection conn = this.connection;
        try(conn) {
            DatabaseMetaData dbMetadata = conn.getMetaData();
            ResultSet tables = dbMetadata.getTables(null, null, table, null);

            if(tables.next()) {
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

    private void createCoapAlarmTable() {
        String sql = "CREATE TABLE coapalarm " +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "wearlevel INTEGER, " +
                "ALARM VARCHAR(5)" +
                "timestamp CURRENT_TIMESTAMP";
        try{
            PreparedStatement stmt = this.connection.prepareStatement(sql);
            stmt.executeUpdate(sql);
            System.out.println("[!] CoapAlarm table created!");

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private boolean tableCoapAlarmExists (String table){
        Connection conn = this.connection;
        try(conn) {
            DatabaseMetaData dbMetadata = conn.getMetaData();
            ResultSet tables = dbMetadata.getTables(null, null, table, null);

            if(tables.next()) {
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


    private void executeQueryLight(Boolean lightFulminated) {
        Connection conn = this.connection;
        if (!tableCoapLightStatusExists("coaplightstatus")){
            createCoapLightStatusTable();
        }
        if (!tableCoapAlarmExists("coapalarm")){
            createCoapAlarmTable();
        }

        String insert = "INSERT INTO coaplightstatus(lightFulimnated) VALUES (?)";
        String insert2 = "INSERT INTO coapalarm (wearLevel, ALARM) VALUES (?, ?)";
        try (conn){
            PreparedStatement stmt = conn.prepareStatement(insert);
            stmt.setBoolean(1, lightFulminated);
            stmt.executeUpdate();

            boolean alarm = (wearLevel >= MAX_WEAR_LEVEL);

            if (wearLevel >= MAX_WEAR_LEVEL){
                PreparedStatement stmt2 = conn.prepareStatement(insert2);
                stmt2.setInt(1, (int) wearLevel);
                stmt2.setBoolean(2, alarm); // true indica che l'allarme è scattato, false altrimenti
                stmt2.executeUpdate();
            }

        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }



    private void startObserving() throws ConnectorException, IOException {
            Logging.setLevel(Level.WARNING);
            CoapClient client = new CoapClient(this.address + "/" + this.resource);
            client.observe(new CoapHandler() {
                @Override
                public void onLoad(CoapResponse response) {
                    try {
                        handleMqttMessage(response.getPayload());
                    } catch (ConnectorException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onError() {
                    System.err.println("Observing Request Failed");
                }
            });
            Request request = new Request(CoAP.Code.GET);
            request.getOptions().setAccept(MediaTypeRegistry.APPLICATION_JSON);
            client.advanced(request);
        }
}