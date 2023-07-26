package handlers;

import collectors.LightsStatus;
import org.eclipse.californium.elements.exception.ConnectorException;
import org.eclipse.paho.client.mqttv3.*;
import org.json.JSONObject;
import org.json.simple.JSONValue;

import java.io.IOException;

public class LightStatusHandler {

    String lightBrokerUrl = "tcp://127.0.0.1:1883";
    String lightClientId = "LightMqttClient";
    String lightTopic = "light/sensor/data";
    MqttClient mqttClient;
    LightsStatus lightsStatus;
    private double wearLevel;

    public LightStatusHandler(String brokerUrl, String clientId, String topic, LightsStatus lightsStatus, MotionHandler motionHandler) {
        this.lightBrokerUrl = brokerUrl;
        this.lightClientId = clientId;
        this.lightTopic = topic;
        this.lightsStatus = lightsStatus;

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
                        String lightFulminated = (String) genreJsonObject.get("lightFulminated");
                        byte[] payload = lightFulminated.getBytes(); // Converti la stringa in un array di byte
                        System.out.println("[!] Insert Alert data in DB.DB");
                        lightsStatus.handleMqttMessage(payload);

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

                }

            });
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    public void publishWearLevel(double wearLevel) {
        // Crea il payload JSON per il messaggio MQTT contenente il valore di wearLevel
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("wearLevel", wearLevel);

        // Converti l'oggetto JSON in una stringa e pubblica il messaggio MQTT
        String payload = jsonObject.toString();
        MqttMessage mqttMessage = new MqttMessage(payload.getBytes());
        try {
            mqttClient.publish(lightTopic, mqttMessage);
            System.out.println("Sent wearLevel to handlers.LightStatusHandler MQTT Broker: " + payload);
        } catch (MqttException e) {
            e.printStackTrace();
        }
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
