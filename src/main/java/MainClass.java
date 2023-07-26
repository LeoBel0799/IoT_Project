import it.iot.application.collectors.LightsStatus;
import it.iot.application.collectors.Motion;
import it.iot.application.handlers.LightStatusHandler;
import it.iot.application.handlers.MotionHandler;
import org.eclipse.paho.client.mqttv3.MqttClient;


/*
class MainClass {
    public static void main(String[] args) throws ConnectorException, IOException {
        // Configura i parametri del broker MQTT di it.iot.collectors.Motion
        String motionBrokerUrl = "tcp://127.0.0.1:1883";
        String motionClientId = "CoapToMqttClient";
        String motionTopic = "coap/sensor/data";

        // Configura i parametri del broker MQTT di Light
        String lightBrokerUrl = "tcp://127.0.0.1:1883";
        String lightClientId = "LightMqttClient";
        String lightTopic = "light/sensor/data";

        try {
            // Crea un'istanza del client MQTT di it.iot.collectors.Motion e connettiti al broker MQTT di it.iot.collectors.Motion
            MqttClient motionMqttClient = new MqttClient(motionBrokerUrl, motionClientId);
            MqttConnectOptions options = new MqttConnectOptions();
            options.setCleanSession(true);
            motionMqttClient.connect(options);
            System.out.println("Connected to it.iot.collectors.Motion MQTT Broker.");

            // Crea un'istanza del client MQTT di Light e connettiti al broker MQTT di Light
            MqttClient lightMqttClient = new MqttClient(lightBrokerUrl, lightClientId);
            lightMqttClient.connect(options);
            System.out.println("Connected to Light MQTT Broker.");

            // Crea un'istanza di it.iot.handlers.MotionHandler e connettiti al broker MQTT di it.iot.collectors.Motion
            it.iot.handlers.MotionHandler motionHandler = new it.iot.handlers.MotionHandler(motionBrokerUrl, motionClientId, motionTopic, new it.iot.collectors.Motion(motionBrokerUrl,motionTopic,lightMqttClient), lightMqttClient);
            motionHandler.connect();

            // Ora it.iot.handlers.MotionHandler è in ascolto dei messaggi MQTT e invierà il wearLevel al broker MQTT di Light
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }
}
*/


public class MainClass {
    public static void main(String[] args) {
        // Configuro i parametri del broker MQTT di it.iot.collectors.Motion
        String motionBrokerUrl = "tcp://127.0.0.1:1883";
        String motionClientId = "CoapToMqttClient";
        String motionTopic = "coap/sensor/data";

        // Configuro i parametri del broker MQTT di Light
        String lightBrokerUrl = "tcp://127.0.0.1:1883";
        String lightClientId = "LightMqttClient";
        String lightTopic = "light/sensor/data";

        try {
            // Create an instance of the it.iot.collectors.Motion class
            Motion motion = new Motion("sourceAddress", "resource", new MqttClient("mqttBrokerUrl", "motionClient"));

            // Create an instance of the it.iot.handlers.LightStatusHandler class
            LightStatusHandler lightStatusHandler = new LightStatusHandler("lightBrokerUrl", "lightStatusClient", "coap/sensor/data", new LightsStatus("lightSourceAddress", "lightResource", 0.0), null);

            // Create an instance of the it.iot.handlers.MotionHandler class
            MotionHandler motionHandler = new MotionHandler("motionBrokerUrl", "motionClient", "coap/sensor/data", motion, lightStatusHandler);

            // Connect the it.iot.handlers.MotionHandler and it.iot.handlers.LightStatusHandler to their respective MQTT brokers
            motionHandler.connect();
            lightStatusHandler.connect();

            // Simulate motion data update (this could be replaced with actual sensor data)
            byte[] motionPayload = "{ \"lights\": \"T\", \"lightsDegree\": \"2\", \"wearLevel\": \"50\" }".getBytes();
            motion.handleMqttMessage(motionPayload);

            // Simulate wear level update (this could be replaced with actual sensor data)
            double wearLevel = 60.0;
            lightStatusHandler.setWearLevel(wearLevel);
            lightStatusHandler.publishWearLevel(wearLevel);

            // Disconnect the it.iot.handlers from their MQTT brokers when done
            motionHandler.disconnect();
            lightStatusHandler.disconnect();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}