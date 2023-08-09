package it.iot.application.actuators;

import it.iot.application.DB.DB;
import it.iot.application.sensors.LightStatusHandler;
import it.iot.application.utils.LightStatusListener;
import org.eclipse.californium.core.CoapClient;
import org.eclipse.californium.core.CoapHandler;
import org.eclipse.californium.core.CoapResponse;
import org.eclipse.californium.core.coap.CoAP;
import org.eclipse.californium.core.coap.MediaTypeRegistry;
import org.eclipse.californium.core.coap.Request;
import org.eclipse.californium.elements.exception.ConnectorException;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.json.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.IOException;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Handler;
import java.util.logging.Level;

public class Motion implements LightStatusListener {
    private DB db;
    private Connection connection;
    private String address;
    private String resource;
    private String lights; //luci accese o spente
    private int lightsDegree; //1 posizione - 2 anabbaglianti -3 abbaglianti
   // private int brights; //abbaglianti accesi o spenti - 1 accesi - 0 spenti
    private int lightsOnCount; // Contatore per il numero di volte in cui le luci sono state accese
    private int lightsOffCount; // Contatore per il numero di volte in cui le luci sono state spente
    public MqttClient lightMqttClient; // Nuovo campo per il riferimento al client MQTT di Light
    private Handler Logging;
    private LightStatusListener lightStatusListener;
    LightStatusHandler lightStatusHandler;
    private int brights;


    public Motion(String sourceAddress, String resource) throws ConnectorException, IOException {
        // Inizializza i campi del motore delle risorse
        this.db = new DB();
        this.connection = this.db.connDb();
        System.out.println("Connected to Collector DB");
        this.address = sourceAddress;
        this.resource = resource;
        // Avvia l'osservazione per gli aggiornamenti
        this.startObserving();
        System.out.println("Motion resource initialized");
    }



    public void handleMqttMessage(byte[] payload) throws ConnectorException, IOException {
        System.out.println("Callback called, resource arrived");
        if (payload != null && payload.length > 0) {
            String payloadStr = new String(payload);
            System.out.println(payloadStr);

            Map<String, String> nodeData = parseJson(payloadStr);
            String lights = nodeData.get("lights");
            String lightsDegree = nodeData.get("lightsDegree");
            String brights = nodeData.get("brights");
            System.out.println("Detection value node:");
            System.out.println("lights: " + lights);
            System.out.println("lightsDegree: " + lightsDegree);
            System.out.println("brights: " + brights);

            this.lights = lights.split(" ")[0];
            this.lightsDegree = Integer.parseInt(String.valueOf(lightsDegree));
            this.brights = Integer.parseInt(String.valueOf(Integer.parseInt(brights.split(" ")[0])));

            // if lights on, execute a query for lights degree
            // Aggiornare i contatori di accensioni e spegnimenti delle luci
            if (this.lights.equals("ON")) {
                // Incrementa il contatore di accensioni delle luci
                this.lightsOnCount++;
            } else {
                // Incrementa il contatore di spegnimenti delle luci
                this.lightsOffCount++;
            }
            this.executeQuery();
            // Chiamare il metodo di callback per passare i valori aggiornati alla classe it.iot.handlers.MotionHandler
            if (this.lightStatusListener != null) {
                this.lightStatusListener.onLightsStatusUpdated(this.lightsOnCount, this.lightsOffCount);
            }
            double calculatedWearLevel = calculateWearLevel(this.lightsOnCount, this.lightsOffCount, 20); // 20 è un valore di esempio per l'intensità della luce

            // Invia il valore di "wearLevel" al broker MQTT di Light come messaggio
            String lightTopic = "light";
            String wearLevelMessage = "{ \"wearLevel\": " + calculatedWearLevel + " }";
            MqttMessage mqttMessage = new MqttMessage(wearLevelMessage.getBytes());
            try {
                this.lightMqttClient.publish(lightTopic, mqttMessage);
                lightStatusHandler.publishWearLevel(calculatedWearLevel);
                System.out.println("Sent wearLevel to Light MQTT Broker: " + wearLevelMessage);
            } catch (MqttException e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("Payload empty");
        }
    }


    private Map<String, String> parseJson(String json) {
        Map<String, String> data = new HashMap<>();
        try {
            JSONParser parser = new JSONParser();
            JSONObject jsonObject = (JSONObject) parser.parse(json);
            String lights = (String) jsonObject.get("lights");
            String lightsDegree = (String) jsonObject.get("lightsDegree");
            String brights = (String) jsonObject.get("brights");

            data.put("lights", lights);
            data.put("lightsDegree", lightsDegree);
            data.put("brights", brights);

        } catch (org.json.simple.parser.ParseException e) {
            e.printStackTrace();
        }
        return data;
    }

    // Implementazione del metodo onLightsStatusUpdated dell'interfaccia LightStatusListener
    @Override
    public void onLightsStatusUpdated(int lightsOnCount, int lightsOffCount) {
        System.out.println("Lights On Count: " + lightsOnCount);
        System.out.println("Lights Off Count: " + lightsOffCount);
    }

    private double calculateWearLevel(int numAccensioni, int numSpegnimenti, double lightIntensity) {
        // Implementa il calcolo in base ai dati reali che hai dai sensori
        return (numSpegnimenti / (double) (numAccensioni + numSpegnimenti)) * lightIntensity;
    }

    private void createCoapMotionTable() {
        String sql = "CREATE TABLE coapmotion " +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "lights VARCHAR(5), " +
                "lightsDegree INTEGER, " +
                "brights INTEGER, "+
                "lightsOnCount INTEGER, "+
                "lightsOffCount INTEGER," +
                "timestamp CURRENT_TIMESTAMP";
        try{
            PreparedStatement stmt = this.connection.prepareStatement(sql);
            stmt.executeUpdate(sql);
            System.out.println("[!] Coapmotion table created!");

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private boolean tableCoapMotionExists (String table){
        Connection conn = this.connection;
        try {
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
    public void executeQuery() {
        Connection conn = this.connection;
        if (!tableCoapMotionExists("coapmotion")) {
            createCoapMotionTable();
        }
        String insert = "INSERT INTO coapmotion (lights,lightsDegree,brights,lightsOnCount,lightsOffCount) VALUES (?,?,?,?,?)";
        try {
            PreparedStatement stmt = conn.prepareStatement(insert);
            stmt.setInt(1, this.lights.equals("ON") ? 1 : 0);
            stmt.setInt(2, this.lightsDegree);
            stmt.setString(3, String.valueOf(this.brights));
            stmt.setInt(4, this.lightsOnCount);
            stmt.setInt(5, this.lightsOffCount);
            stmt.executeUpdate();

            // Show data log
            String selectQuery = "SELECT * FROM `coapmotion`";
            ResultSet resultSet = stmt.executeQuery(selectQuery);

            ResultSetMetaData metaData = resultSet.getMetaData();
            int columnCount = metaData.getColumnCount();

            // Print column names
            for (int i = 1; i <= columnCount; i++) {
                System.out.print(metaData.getColumnName(i) + "\t");
            }
            System.out.println();

            // Print rows
            while (resultSet.next()) {
                for (int i = 1; i <= columnCount; i++) {
                    System.out.print(resultSet.getObject(i) + "\t");
                }
                System.out.println();
            }
        } catch (SQLException e) {
            e.printStackTrace();
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