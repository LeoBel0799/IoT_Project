import org.eclipse.californium.core.CoapClient;
import org.eclipse.californium.core.CoapHandler;
import org.eclipse.californium.core.CoapResponse;
import org.eclipse.californium.core.coap.CoAP;
import org.eclipse.californium.core.coap.MediaTypeRegistry;
import org.eclipse.californium.core.coap.Request;
import org.eclipse.californium.elements.exception.ConnectorException;
import org.json.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.parser.JSONParser;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Handler;
import java.util.logging.Level;

public class LightsStatus {
    private DB db;
    private Connection connection;
    private static final double MAX_WEAR_LEVEL = 5.0;
    private String address;
    private String resource;
    private String lightFulminated = "F"; // Stato delle luci, true se sono fulminate, false altrimenti
    private double wearLevel;
    private Handler Logging;

    public LightsStatus(String sourceAddress, String resource, double wearLevel) throws ConnectorException, IOException {
        // Initialize LightStatus resource fields
        this.address = sourceAddress;
        this.resource = resource;
        this.wearLevel = wearLevel;
        // Start observing for updates
        this.startObserving();
        System.out.println("LightStatus resource initialized");
    }


    private Map<String, String> parseJson(String json) {
        Map<String, String> data = new HashMap<>();
        try {
            JSONParser parser = new JSONParser();
            JSONObject jsonObject = (JSONObject) parser.parse(json);
            String lights = (String) jsonObject.get("lights");
            String lightsDegree = (String) jsonObject.get("lightsDegree");
            data.put("lights", lights);
            data.put("lightsDegree", lightsDegree);
        } catch (org.json.simple.parser.ParseException e) {
            e.printStackTrace();
        }
        return data;
    }

    public void handleMqttMessage(byte[] payload) throws ConnectorException, IOException {
        System.out.println("Callback called, resource arrived");
        if (payload != null && payload.length > 0) {
            String payloadStr = new String(payload);
            JSONObject jsonPayload = (JSONObject) JSONValue.parse(payloadStr);

            String lightsFulminated = (String)jsonPayload.get("lightFulminated");
            String wearLevel = (String)jsonPayload.get("wearLevel");
            System.out.println("Detection value node:");
            System.out.println("lightFulminated: " + lightsFulminated);
            System.out.println("wearLevel: " + wearLevel);

            this.lightFulminated = lightFulminated.split(" ")[0];
            this.wearLevel = Integer.parseInt(wearLevel.split(" ")[0]);

            // Esempio: Aggiornare il grado di usura delle luci in base ai dati ricevuti
            String wearLevelStr = (String) jsonPayload.get("wearLevel");
            double receivedWearLevel = Double.parseDouble(wearLevelStr);
            this.wearLevel = Math.max(this.wearLevel, receivedWearLevel); // Aggiorna il grado di usura al massimo valore tra quello attuale e quello ricevuto

            // Verifica se le luci sono fulminate in base al grado di usura massimo raggiunto
            if (this.wearLevel >= MAX_WEAR_LEVEL) {
                this.lightFulminated = "T";
            } else {
                this.lightFulminated = "F";
            }

            // Salva il valore del campo lightFulminated nel database solo quando il grado di usura è al massimo
            if (this.wearLevel >= MAX_WEAR_LEVEL) {
                executeQueryLight(lightFulminated);
            }
        }

    }

    private void executeQueryLight(String lightFulminated) {
        try {
            System.out.println(this.connection);
            // Prima query per inserire i dati in coapalarm
            String sql1 = "INSERT INTO `coaplightstatus`(`lightFulimnated`, `wearLevel`) VALUES (?, ?)";
            PreparedStatement preparedStatement1 = this.connection.prepareStatement(sql1);
            preparedStatement1.setInt(1, Integer.parseInt(lightFulminated));
            preparedStatement1.setInt(2, (int) wearLevel);
            preparedStatement1.executeUpdate();

            // Imposta il campo 'alarm' in base al grado di usura
            boolean alarm = (wearLevel >= MAX_WEAR_LEVEL);

            // Seconda query per inserire i dati in coaplightstatus solo se il grado di usura è massimo
            if (wearLevel >= MAX_WEAR_LEVEL) {
                String sql2 = "INSERT INTO `coapalarm` (`wearLevel`, `ALARM`, `timestamp`) VALUES (?, ?, ?)";
                PreparedStatement preparedStatement2 = this.connection.prepareStatement(sql2);
                preparedStatement2.setInt(1, (int) wearLevel);
                preparedStatement2.setBoolean(2, alarm); // true indica che l'allarme è scattato, false altrimenti
                preparedStatement2.setString(3, getFormattedTimestamp());
                preparedStatement2.executeUpdate();
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

    private String getFormattedTimestamp() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        LocalDateTime now = LocalDateTime.now();
        return formatter.format(now);
    }
}