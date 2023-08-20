package it.iot.application.mqtt;

import it.iot.application.DB.LightData;
import org.eclipse.californium.elements.exception.ConnectorException;
import org.eclipse.paho.client.mqttv3.*;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.parser.ParseException;

import java.io.IOException;
import java.sql.SQLException;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;

public class LightHandler implements MqttCallback, IMqttMessageListener, Runnable {

    String brokerUrl; // Cambiare l'URL del broker MQTT se necessario
    String clientId; // Un identificativo univoco per il client MQTT
    String topic; // Il topic MQTT a cui inviare il payload
    private MqttClient mqttClient;
    LightData lightData;
    BlockingDeque<String> queue;



    public LightHandler(String brokerUrl, String clientId, String topic) throws MqttException, ConnectorException, IOException, SQLException {
        this.brokerUrl = brokerUrl;
        this.clientId = clientId;
        this.topic = topic;
        mqttClient = new MqttClient(brokerUrl, clientId);
        this.queue = new LinkedBlockingDeque<>(5000);
        mqttClient.setCallback(this);
        mqttClient.connect();
        mqttClient.subscribe(topic);
        System.out.println("[OK] - Connected to MQTT Broker. Subscribed to topic: " + topic);
        lightData = new LightData();
        lightData.createDelete("motion");
        new Thread(this).start();
    }



    @Override
    public void connectionLost(Throwable throwable) {
        System.out.println("[FAIL] - Connection to MQTT Broker lost!");
        int reconnectTime = 3000;
        while (!mqttClient.isConnected()) {
            try {
                Thread.sleep(reconnectTime);
            } catch (InterruptedException e) {
                System.err.println("[FAIL] - Error during reading waiting connection\n");
                e.printStackTrace(System.err);
                e.getMessage();
            }
            System.out.println("[INFO] - MQTT Reconnecting");
            try {
                mqttClient.connect();
                System.out.println("[OK] - MQTT Reconnetted");

            } catch (MqttException e) {
                System.err.println("[FAIL] - Error in connection to MQTT Broker\n");
                e.printStackTrace(System.err);
                e.getMessage();
            }
            try {
                mqttClient.subscribe(topic);
                System.out.println("[OK] - Connected to MQTT Broker. Subscribed to topic: " + topic);
            } catch (MqttException e) {
                System.err.println("[FAIL] - Error in subsciption to "+ topic +"\n");
                e.printStackTrace(System.err);
                e.getMessage();
            }

        }
    }


    @Override
    public void messageArrived(String topic, MqttMessage mqttMessage) throws InterruptedException {
        //System.out.println("[INFO] - Receiving Light message");
        String msg = new String(mqttMessage.getPayload());
        //System.out.println(msg);
        queue.put(msg);

    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {
    }



    @Override
    public void run() {

        int queueCapacity = queue.remainingCapacity();
        int treshold = (int) (queueCapacity*0.75);

        while(true){
            String msg = null;
            try {
                msg = queue.take();
            } catch (InterruptedException e) {
                System.err.println("[FAIL] - Error during taking message from MQTT queue");
            }

            if (queue.size()>treshold){
                for(int i = 0; i<1750; i++){
                    queue.remove();
                }
            }
            JSONObject json;
            try {
                json = (JSONObject) JSONValue.parseWithException(msg);
                int idlight = ((Number)json.get("idlight")).intValue();
                String lights = (String) json.get("lights");
                int lightsDegree =  ((Number)json.get("lightsDegree")).intValue();
                String brights = (String) json.get("brights");
                //System.out.println("[INFO] - Taking data...");
                lightData.insertMotionData(idlight,lights,lightsDegree,brights);
                Thread.sleep(15000); // 50 millisecond
            } catch (ParseException | InterruptedException | SQLException e) {
                System.err.println("[FAIL] - Error during parsing JSON Message");
                e.printStackTrace(System.err);
                e.getMessage();
            }
        }
    }
}
