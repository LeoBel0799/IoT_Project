package it.iot.application;

import it.iot.application.coap.RegistrationServer;
import it.iot.application.mqtt.LightHandler;
import it.iot.application.user.UserMenu;
import org.eclipse.californium.elements.exception.ConnectorException;
import org.eclipse.paho.client.mqttv3.MqttException;

import java.io.IOException;

public class MainClass {
    public static void main(String[] args) throws MqttException, ConnectorException, IOException {
        String topicMotion = "motion";
        String brokerMotion = "tcp://127.0.0.1:1883";
        String idmotion = "motionHandler";
        LightHandler motion = new LightHandler(brokerMotion,idmotion,topicMotion);


       /* RegistrationServer registrationServer = new RegistrationServer();
        Thread registrationServerThread = new Thread(registrationServer);
        registrationServerThread.start();*/

        //UserMenu menu = new UserMenu();
    }
}