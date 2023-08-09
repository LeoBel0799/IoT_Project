package it.iot.application.sensors;

import it.iot.application.actuators.LightsStatus;
import org.eclipse.californium.elements.exception.ConnectorException;
import org.eclipse.paho.client.mqttv3.*;
import org.json.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.parser.JSONParser;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class LightStatusHandler implements MqttCallback{

    String lightBrokerUrl;
    String lightClientId;
    String lightTopic;
    MqttClient mqttClient;
    LightsStatus lightsStatus;
    private double wearLevel;

    public LightStatusHandler(String brokerUrl, String clientId, String topic) throws MqttException {
        this.lightBrokerUrl = brokerUrl;
        this.lightClientId = clientId;
        this.lightTopic = topic;
        mqttClient = new MqttClient(lightBrokerUrl, lightClientId);
        MqttConnectOptions options = new MqttConnectOptions();
        options.setCleanSession(true);
        mqttClient.connect(options);

        // Subscribe to the MQTT topic
        mqttClient.subscribe(lightTopic);
        System.out.println("Connected to MQTT Broker. Subscribed to topic: " + lightTopic);
    }

    public void setWearLevel(double wearLevel) {
        this.wearLevel = wearLevel;
    }


    public void connectionLost(Throwable throwable) {
        System.err.println("Connection to MQTT Broker lost!");
    }


    public void messageArrived(String topic, MqttMessage msg) throws Exception {

        double wearLevel = extractWearLevel(msg); // estrai wearLevel dal payload MQTT

        byte[] payload = createCoapPayload(wearLevel);

        lightsStatus.handleMqttMessage(payload);

    }

    public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {
        System.out.println("Delivery Complete!");
    }


    public void publishWearLevel(double calculatedWearLevel) {
        // Crea il payload JSON per il messaggio MQTT contenente il valore di wearLevel
        byte[] payload = createCoapPayload(calculatedWearLevel);
        MqttMessage mqttMessage = new MqttMessage(payload);
        mqttMessage.setRetained(true);
        String topic = "light";
        // Publish messaggio
        try {
            mqttClient.publish(topic, mqttMessage);
            System.out.println("Pubblicato aggiornamento wearLevel su MQTT!");

        } catch (MqttException e) {
            System.err.println("Errore pubblicazione MQTT: " + e.getMessage());
        }
    }

    private double extractWearLevel(MqttMessage msg) throws Exception {

        String payload = new String(msg.getPayload());
        JSONParser parser = new JSONParser();
        JSONObject json = (JSONObject) parser.parse(payload);


        // Estrai il campo wearLevel
        if(json.has("wearLevel")) {
            return json.getDouble("wearLevel");
        } else {
            // Gestisci errore
            throw new Exception("Campo wearLevel non presente nel payload!");
        }

    }

    private byte[] createCoapPayload(double wearLevel) {
        JSONObject jsonObject = new JSONObject();
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
