package it.iot.application;

import it.iot.application.sensors.LightStatusHandler;
import it.iot.application.sensors.MotionHandler;
import org.eclipse.paho.client.mqttv3.MqttException;

public class MainClass {
    public static void main(String[] args) {
        String topicLight = "light";
        String brokerLights = "tcp://127.0.0.1:1883";
        String id = "lightHandler";
        String topicMotion = "motion";
        String brokerMotion = "tcp://127.0.0.1:1883";
        String idmotion = "motionHandler";

        LightStatusHandler light = new LightStatusHandler(brokerLights,id,topicLight);
        MotionHandler motion = new MotionHandler(brokerMotion,idmotion,topicMotion);
    }
}