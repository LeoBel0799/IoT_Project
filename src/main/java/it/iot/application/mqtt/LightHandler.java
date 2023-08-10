package it.iot.application.mqtt;

import it.iot.application.DB.LightData;
import org.eclipse.paho.client.mqttv3.*;
import org.json.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.parser.ParseException;

import java.nio.charset.StandardCharsets;

public class LightHandler implements MqttCallback {

    String brokerUrl; // Cambiare l'URL del broker MQTT se necessario
    String clientId; // Un identificativo univoco per il client MQTT
    String topic; // Il topic MQTT a cui inviare il payload
    private MqttClient mqttClient;
    LightData lightData;


    public LightHandler(String brokerUrl, String clientId, String topic) throws MqttException {
        this.brokerUrl = brokerUrl;
        this.clientId = clientId;
        this.topic = topic;
        mqttClient = new MqttClient(brokerUrl, clientId);
        MqttConnectOptions options = new MqttConnectOptions();
        options.setCleanSession(true);
        mqttClient.connect(options);
        mqttClient.subscribe(topic);
        System.out.println("Connected to MQTT Broker. Subscribed to topic: " + topic);

    }



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
            int id = genreJsonObject.getInt("id");
            String lights = (String) genreJsonObject.get("lights");
            int lightsDegree = Integer.parseInt((String) genreJsonObject.get("lightsDegree"));
            String brights = (String) genreJsonObject.get("brights");
            lightData.insertMotionData(id,lights,lightsDegree,brights);
        } catch (ParseException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {
    }
}
