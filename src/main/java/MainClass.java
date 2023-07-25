import org.eclipse.californium.elements.exception.ConnectorException;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;

import java.io.IOException;


/*
class MainClass {
    public static void main(String[] args) throws ConnectorException, IOException {
        // Configura i parametri del broker MQTT di Motion
        String motionBrokerUrl = "tcp://127.0.0.1:1883";
        String motionClientId = "CoapToMqttClient";
        String motionTopic = "coap/sensor/data";

        // Configura i parametri del broker MQTT di Light
        String lightBrokerUrl = "tcp://127.0.0.1:1883";
        String lightClientId = "LightMqttClient";
        String lightTopic = "light/sensor/data";

        try {
            // Crea un'istanza del client MQTT di Motion e connettiti al broker MQTT di Motion
            MqttClient motionMqttClient = new MqttClient(motionBrokerUrl, motionClientId);
            MqttConnectOptions options = new MqttConnectOptions();
            options.setCleanSession(true);
            motionMqttClient.connect(options);
            System.out.println("Connected to Motion MQTT Broker.");

            // Crea un'istanza del client MQTT di Light e connettiti al broker MQTT di Light
            MqttClient lightMqttClient = new MqttClient(lightBrokerUrl, lightClientId);
            lightMqttClient.connect(options);
            System.out.println("Connected to Light MQTT Broker.");

            // Crea un'istanza di MotionHandler e connettiti al broker MQTT di Motion
            MotionHandler motionHandler = new MotionHandler(motionBrokerUrl, motionClientId, motionTopic, new Motion(motionBrokerUrl,motionTopic,lightMqttClient), lightMqttClient);
            motionHandler.connect();

            // Ora MotionHandler è in ascolto dei messaggi MQTT e invierà il wearLevel al broker MQTT di Light
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }
}
*/


public class MainClass {
    public static void main(String[] args) {
        // Configuro i parametri del broker MQTT di Motion
        String motionBrokerUrl = "tcp://127.0.0.1:1883";
        String motionClientId = "CoapToMqttClient";
        String motionTopic = "coap/sensor/data";

        // Configuro i parametri del broker MQTT di Light
        String lightBrokerUrl = "tcp://127.0.0.1:1883";
        String lightClientId = "LightMqttClient";
        String lightTopic = "light/sensor/data";

        try {
            // Create an instance of the Motion class
            Motion motion = new Motion("sourceAddress", "resource", new MqttClient("mqttBrokerUrl", "motionClient"));

            // Create an instance of the LightStatusHandler class
            LightStatusHandler lightStatusHandler = new LightStatusHandler("lightBrokerUrl", "lightStatusClient", "coap/sensor/data", new LightsStatus("lightSourceAddress", "lightResource", 0.0), null);

            // Create an instance of the MotionHandler class
            MotionHandler motionHandler = new MotionHandler("motionBrokerUrl", "motionClient", "coap/sensor/data", motion, lightStatusHandler);

            // Connect the MotionHandler and LightStatusHandler to their respective MQTT brokers
            motionHandler.connect();
            lightStatusHandler.connect();

            // Simulate motion data update (this could be replaced with actual sensor data)
            byte[] motionPayload = "{ \"lights\": \"T\", \"lightsDegree\": \"2\", \"wearLevel\": \"50\" }".getBytes();
            motion.handleMqttMessage(motionPayload);

            // Simulate wear level update (this could be replaced with actual sensor data)
            double wearLevel = 60.0;
            lightStatusHandler.setWearLevel(wearLevel);
            lightStatusHandler.publishWearLevel(wearLevel);

            // Disconnect the handlers from their MQTT brokers when done
            motionHandler.disconnect();
            lightStatusHandler.disconnect();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}