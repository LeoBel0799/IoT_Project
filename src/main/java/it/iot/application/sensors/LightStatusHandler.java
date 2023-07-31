package it.iot.application.sensors;

import it.iot.application.actuators.LightsStatus;
import org.eclipse.californium.elements.exception.ConnectorException;
import org.eclipse.paho.client.mqttv3.*;
import org.json.JSONObject;
import org.json.simple.JSONValue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class LightStatusHandler {

    String lightBrokerUrl;
    String lightClientId;
    String lightTopic;
    MqttClient mqttClient;
    LightsStatus lightsStatus;
    private double wearLevel;

    public LightStatusHandler(String brokerUrl, String clientId, String topic) {
        this.lightBrokerUrl = brokerUrl;
        this.lightClientId = clientId;
        this.lightTopic = topic;
    }

    public void setWearLevel(double wearLevel) {
        this.wearLevel = wearLevel;
    }

    public void connect() {
        try {
            mqttClient = new MqttClient(lightBrokerUrl, lightClientId);
            MqttConnectOptions options = new MqttConnectOptions();
            options.setCleanSession(true);
            mqttClient.connect(options);

            // Subscribe to the MQTT topic
            mqttClient.subscribe(lightTopic);
            System.out.println("Connected to MQTT Broker. Subscribed to topic: " + lightTopic);

            // Set up message callback
            mqttClient.setCallback(new MqttCallback() {
                @Override
                public void connectionLost(Throwable throwable) {
                    System.err.println("Connection to MQTT Broker lost!");
                }

                @Override
                public void messageArrived(String topic, MqttMessage mqttMessage){
                    System.out.println("[!] Receiving Alert");
                    String msg = new String(mqttMessage.getPayload());
                    System.out.println(" ---  " + msg);

                    JSONObject genreJsonObject;
                    try {
                        genreJsonObject = (JSONObject) JSONValue.parseWithException(msg);
                        String id = (String) genreJsonObject.get("id");
                        String lightFulminated = (String) genreJsonObject.get("lightFulminated");
                        Integer wearLevel = (Integer) genreJsonObject.get("wearLevel");
                        // Crea il payload CoAP utilizzando il metodo createCoapPayload
                        byte[] coapPayload = createCoapPayload(id, lightFulminated, wearLevel);
                        System.out.println("[!] Insert Alert data in DB");
                        lightsStatus.handleMqttMessage(coapPayload);

                    }catch (org.json.simple.parser.ParseException e){
                        e.printStackTrace();
                        throw new RuntimeException(e);
                    } catch (ConnectorException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {
                      System.out.println("Delivery Complete!");
                }

            });
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    public void publishWearLevel(double wearLevel) {
        // Crea il payload JSON per il messaggio MQTT contenente il valore di wearLevel
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("wearLevel", String.valueOf(wearLevel));

        // Converti l'oggetto JSON in una stringa e pubblica il messaggio MQTT
        String payload = jsonObject.toString();
        MqttMessage mqttMessage = new MqttMessage(payload.getBytes());
        try {
            mqttClient.publish(lightTopic, mqttMessage);
            System.out.println("Sent wearLevel to LightStatusHandler MQTT Broker: " + payload);
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    private byte[] createCoapPayload(String id, String lightsDegree, int wearLevel) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("id", id);
        jsonObject.put("lightFulminated", lightsDegree);
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
