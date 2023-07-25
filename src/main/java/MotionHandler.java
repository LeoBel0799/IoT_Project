import org.eclipse.californium.elements.exception.ConnectorException;
import org.eclipse.paho.client.mqttv3.*;
import org.json.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.parser.ParseException;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class MotionHandler {

    String brokerUrl = "tcp://127.0.0.1:1883"; // Cambiare l'URL del broker MQTT se necessario
    String clientId = "CoapToMqttClient"; // Un identificativo univoco per il client MQTT
    String topic = "coap/sensor/data"; // Il topic MQTT a cui inviare il payload
    private MqttClient mqttClient;
    private Motion motion;
    private LightStatusHandler lightStatusHandler; // Nuovo campo per il riferimento al client MQTT di Light


    public MotionHandler(String brokerUrl, String clientId, String topic, Motion motion, LightStatusHandler lightStatusHandler) {
        this.brokerUrl = brokerUrl;
        this.clientId = clientId;
        this.topic = topic;
        this.motion = motion;
        this.lightStatusHandler = lightStatusHandler; // Imposta il riferimento al client MQTT di Light

    }

    public void connect() {
        try {
            mqttClient = new MqttClient(brokerUrl, clientId);
            MqttConnectOptions options = new MqttConnectOptions();
            options.setCleanSession(true);
            mqttClient.connect(options);

            // Subscribe to the MQTT topic
            mqttClient.subscribe(topic);
            System.out.println("Connected to MQTT Broker. Subscribed to topic: " + topic);

            // Set up message callback
            mqttClient.setCallback(new MqttCallback() {
                @Override
                public void connectionLost(Throwable throwable) {
                    System.err.println("Connection to MQTT Broker lost!");
                }

                @Override
                public void messageArrived(String topic, MqttMessage mqttMessage) {
                    System.out.println("[!] Receiving Motion message");
                    String msg = new String(mqttMessage.getPayload());
                    System.out.println(" ---  " + msg);

                    JSONObject genreJsonObject;
                    try {
                        genreJsonObject = (JSONObject) JSONValue.parseWithException(msg);
                        String lights = (String) genreJsonObject.get("lights");
                        int lightsDegree = Integer.parseInt((String) genreJsonObject.get("lightsDegree"));
                        int numAccensioni = 10; // Numero di accensioni delle luci
                        int numSpegnimenti = 7; // Numero di spegnimenti delle luci
                        double lightIntensity = 50; // Intensità media della luce

                        // Calcolo del wear level
                        double wearLevel = calculateWearLevel(numAccensioni, numSpegnimenti, lightIntensity);
                        handleWearLevel(wearLevel);

                        // Crea il payload CoAP utilizzando il metodo createCoapPayload
                        byte[] coapPayload = createCoapPayload(lights, lightsDegree, (int) wearLevel);

                        // Chiamare il metodo handleMqttMessage della classe Motion
                        motion.handleMqttMessage(coapPayload);
                        lightStatusHandler.setWearLevel(wearLevel);
                    } catch (ParseException | ConnectorException | IOException e) {
                        e.printStackTrace();
                        throw new RuntimeException(e);
                    }
                }

                @Override
                public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {
                }
            });
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    public void handleWearLevel(double wearLevel) {
        // Chiamare il metodo publishWearLevel della classe LightStatusHandler per inviare il valore di wearLevel all'MQTT di LightStatusHandler
        lightStatusHandler.publishWearLevel(wearLevel);
    }

    private double calculateWearLevel(int numAccensioni, int numSpegnimenti, double lightIntensity) {
        // Esempio di calcolo del wear level
        // Implementa il calcolo in base ai dati reali che hai dai sensori
        return (numSpegnimenti / (double) (numAccensioni + numSpegnimenti)) * lightIntensity;
    }

    private byte[] createCoapPayload(String lights, int lightsDegree, int wearLevel) {
        // Combinare i dati "lights", "lightsDegree", "lightsOnCount" e "lightsOffCount" nel formato desiderato per il payload CoAP
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("lights", lights);
        jsonObject.put("lightsDegree", lightsDegree);
        jsonObject.put("wearLevel", wearLevel);

        String payloadStr = jsonObject.toString();
        return payloadStr.getBytes(StandardCharsets.UTF_8);
    }

    public void disconnect() {
        try {
            mqttClient.disconnect();
            System.out.println("Disconnected from MQTT Broker.");
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }


}
