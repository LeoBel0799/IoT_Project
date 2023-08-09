package it.iot.application.sensors;

import it.iot.application.utils.LightStatusListener;
import it.iot.application.actuators.Motion;
import org.eclipse.californium.elements.exception.ConnectorException;
import org.eclipse.paho.client.mqttv3.*;
import org.json.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.parser.ParseException;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class MotionHandler implements MqttCallback {

    String brokerUrl; // Cambiare l'URL del broker MQTT se necessario
    String clientId; // Un identificativo univoco per il client MQTT
    String topic; // Il topic MQTT a cui inviare il payload
    private MqttClient mqttClient;
    Motion motion;


    public MotionHandler(String brokerUrl, String clientId, String topic) throws MqttException {
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



    private byte[] createCoapPayload(String lights, int lightsDegree, int brights) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("lights", lights);
        jsonObject.put("lightsDegree", lightsDegree);
        jsonObject.put("brights", brights);

        String payloadStr = jsonObject.toString();
        return payloadStr.getBytes(StandardCharsets.UTF_8);
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
            String lights = (String) genreJsonObject.get("lights");
            int lightsDegree = Integer.parseInt((String) genreJsonObject.get("lightsDegree"));
            int brights = Integer.parseInt((String) genreJsonObject.get("brights"));

            // Crea il payload CoAP utilizzando il metodo createCoapPayload
            byte[] coapPayload = createCoapPayload(lights, lightsDegree,brights);

            // Chiamare il metodo handleMqttMessage della classe it.iot.collectors.Motion
            motion.handleMqttMessage(coapPayload);

        } catch (ParseException | ConnectorException | IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {
    }
}
