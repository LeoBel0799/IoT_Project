package it.iot.application;

import it.iot.application.actuators.LightsStatus;
import it.iot.application.actuators.Motion;
import it.iot.application.sensors.LightStatusHandler;
import it.iot.application.sensors.MotionHandler;
import org.eclipse.californium.elements.exception.ConnectorException;
import org.eclipse.paho.client.mqttv3.MqttException;

import java.io.IOException;

public class MainClass {
    public static void main(String[] args) throws MqttException, ConnectorException, IOException {
        String topicLight = "light";
        String brokerLights = "tcp://127.0.0.1:1883";
        String id = "lightHandler";
        String topicMotion = "motion";
        String brokerMotion = "tcp://127.0.0.1:1883";
        String idmotion = "motionHandler";
        LightStatusHandler light = new LightStatusHandler(brokerLights,id,topicLight);
        MotionHandler motion = new MotionHandler(brokerMotion,idmotion,topicMotion);
        Motion m = new Motion();
        LightsStatus l = new LightsStatus();
    }
}