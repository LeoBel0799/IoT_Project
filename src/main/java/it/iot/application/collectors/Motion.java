package it.iot.application.collectors;

import it.iot.application.DB.DB;
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

public class Motion {
    private DB db;
    private Connection connection;
    private String address;
    private String resource;
    private String lightId;
    private String lights; //luci accese o spente
    private int lightsDegree; //1 posizione - 2 anabbaglianti
    private String brights; //abbaglianti accesi o spenti
    private int lightsOnCount; // Contatore per il numero di volte in cui le luci sono state accese
    private int lightsOffCount; // Contatore per il numero di volte in cui le luci sono state spente
    private int wearLevel;
    public MqttClient lightMqttClient; // Nuovo campo per il riferimento al client MQTT di Light
    private Handler Logging;
    private String lightsDegreeDescription;
    private LightStatusListener lightStatusListener;


    public Motion(String sourceAddress, String resource) throws ConnectorException, IOException {
        // Inizializza i campi del motore delle risorse
        this.db = new DB();
        this.connection = this.db.connectDbs();
        System.out.println("Connected to Collector DB");
        this.address = sourceAddress;
        this.resource = resource;
        // Avvia l'osservazione per gli aggiornamenti
        this.startObserving();
        System.out.println("Motion resource initialized");
    }

    public void setLightStatusistener(LightStatusListener listener){
        this.lightStatusListener = listener;
    }


    public void handleMqttMessage(byte[] payload) throws ConnectorException, IOException {
        System.out.println("Callback called, resource arrived");
        if (payload != null && payload.length > 0) {
            String payloadStr = new String(payload);
            System.out.println(payloadStr);

            Map<String, String> nodeData = parseJson(payloadStr);
            String id = nodeData.get("id");
            String lights = nodeData.get("lights");
            String lightsDegree = nodeData.get("lightsDegree");
            String wearLevel = nodeData.get("wearLevel");
            System.out.println("Detection value node:");
            System.out.println("id: " + id);
            System.out.println("lights: " + lights);
            System.out.println("lightsDegree: " + lightsDegree);
            System.out.println("wearLevel: " + wearLevel);

            this.lightId = id.split(" ")[0];
            this.lights = lights.split(" ")[0];
            this.lightsDegree = Integer.parseInt(lightsDegree.split(" ")[0]);
            this.brights = String.valueOf(Integer.parseInt(brights.split(" ")[0]));

            // if lights on, execute a query for lights degree
            // Aggiornare i contatori di accensioni e spegnimenti delle luci
            if (this.lights.equals("T")) {
                // Incrementa il contatore di accensioni delle luci
                lightsOnCount++;
            } else {
                // Incrementa il contatore di spegnimenti delle luci
                lightsOffCount++;
            }
            this.executeQuery(wearLevel);
            // Chiamare il metodo di callback per passare i valori aggiornati alla classe it.iot.handlers.MotionHandler
            if (lightStatusListener != null) {
                lightStatusListener.onLightsStatusUpdated(lightsOnCount, lightsOffCount);
            }
            // Invia il valore di "wearLevel" al broker MQTT di Light come messaggio
            String lightTopic = "light/sensor/data";
            String wearLevelMessage = "{ \"wearLevel\": " + wearLevel + " }";
            MqttMessage mqttMessage = new MqttMessage(wearLevelMessage.getBytes());
            try {
                this.lightMqttClient.publish(lightTopic, mqttMessage);
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
            String id = (String) jsonObject.get("id");
            String lights = (String) jsonObject.get("lights");
            String lightsDegree = (String) jsonObject.get("lightsDegree");
            data.put("id", id);
            data.put("lights", lights);
            data.put("lightsDegree", lightsDegree);
        } catch (org.json.simple.parser.ParseException e) {
            e.printStackTrace();
        }
        return data;
    }


    public void executeQuery(String wearLevel) {
        try {
            System.out.println(this.connection);
            String sql = "INSERT INTO `coapmotion` (`id`,`lights`,`lightsDegree`,`brights`,`lightsOnCount`,`lightsOffCount`,`timestamp`,`wearLevel`) VALUES (?,?, ?, ?, ?, ?, ?,?)";
            PreparedStatement preparedStatement = this.connection.prepareStatement(sql);
            preparedStatement.setInt(1, Integer.parseInt(this.lightId));
            preparedStatement.setInt(2, this.lights.equals("T") ? 1 : 0);
            preparedStatement.setInt(3, this.lightsDegree);
            preparedStatement.setString(4, this.brights);
            preparedStatement.setInt(5, this.lightsOnCount);
            preparedStatement.setInt(6, this.lightsOffCount);
            preparedStatement.setString(7, getFormattedTimestamp());
            preparedStatement.setString(8, String.valueOf(wearLevel));
            preparedStatement.executeUpdate();

            // Show data log
            String selectQuery = "SELECT * FROM `coapmotion`";
            Statement stmt = this.connection.createStatement();
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



    private String getFormattedTimestamp() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        LocalDateTime now = LocalDateTime.now();
        return formatter.format(now);
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


    // Metodo getter per lightsOnCount
    public int getLightsOnCount() {
        return lightsOnCount;
    }

    // Metodo getter per lightsOffCount
    public int getLightsOffCount() {
        return lightsOffCount;
    }
}