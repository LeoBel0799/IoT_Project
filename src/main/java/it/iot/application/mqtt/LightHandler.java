package it.iot.application.mqtt;

import it.iot.application.DB.LightData;
import org.eclipse.californium.elements.exception.ConnectorException;
import org.eclipse.paho.client.mqttv3.*;
import org.json.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.parser.ParseException;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class LightHandler implements MqttCallback {

    String brokerUrl; // Cambiare l'URL del broker MQTT se necessario
    String clientId; // Un identificativo univoco per il client MQTT
    String topic; // Il topic MQTT a cui inviare il payload
    private MqttClient mqttClient;
    LightData lightData;


    public LightHandler(String brokerUrl, String clientId, String topic) throws MqttException, ConnectorException, IOException {
        this.brokerUrl = brokerUrl;
        this.clientId = clientId;
        this.topic = topic;
        mqttClient = new MqttClient(brokerUrl, clientId);
        mqttClient.setCallback(this);
        mqttClient.connect();
        mqttClient.subscribe(topic);
        System.out.println("Connected to MQTT Broker. Subscribed to topic: " + topic);
        lightData = new LightData();
    }



    @Override
    public void connectionLost(Throwable throwable) {
        System.err.println("Connection to MQTT Broker lost!");
    }


    @Override
    public void messageArrived(String topic, MqttMessage mqttMessage) {
        System.out.println("[!] Receiving Motion message");
        String msg = new String(mqttMessage.getPayload());
        System.out.println(msg);

        JSONObject json;
        try {
            json = (JSONObject) JSONValue.parseWithException(msg);
            int id = (int) json.get("id");
            String lights = (String) json.get("lights");
            int lightsDegree = (int) json.get("lightsDegree");
            String brights = (String) json.get("brights");
            System.out.println("[!] Taking data...");
            lightData.insertMotionData(id,lights,lightsDegree,brights);
        } catch (ParseException e) {
            System.err.println("Error parsing JSON: " + e.getMessage());
            e.printStackTrace(); // Stampa la traccia dell'errore per il debug
        }
        mqttClient.setCallback(this);
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {
    }
}
