package it.iot.application;

import it.iot.application.coap.StartingServer;
import it.iot.application.mqtt.LightHandler;
import it.iot.application.user.UserMenu;
import org.eclipse.californium.elements.exception.ConnectorException;
import org.eclipse.paho.client.mqttv3.MqttException;

import java.io.IOException;
import java.sql.SQLException;

public class MainClass {
    public static void main(String[] args) throws MqttException, ConnectorException, IOException, SQLException, InterruptedException {
        String topicMotion = "motion";
        String brokerMotion = "tcp://127.0.0.1:1883";
        String idmotion = "lightHandler";



        LightHandler motion = new LightHandler(brokerMotion,idmotion,topicMotion);


        StartingServer startingServer = new StartingServer();
        Thread ser = new Thread(startingServer);
        ser.start();

        UserMenu menu = new UserMenu();
        Thread user = new Thread(menu);
        synchronized (user) {
            user.start();
            user.wait(5000);
        }
    }
}