package it.iot.application.controller;

import it.iot.application.DB.LightData;
import org.eclipse.californium.core.CoapClient;
import org.eclipse.californium.core.CoapResponse;
import org.eclipse.californium.core.coap.MediaTypeRegistry;
import org.eclipse.californium.elements.exception.ConnectorException;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.IOException;

public class LightBrightStatus {
    private static CoapClient motionApp = null;
    LightData light = new LightData();
    private static LightBrightStatus instance = null;

    public LightBrightStatus() throws ConnectorException, IOException {
    }

    public static LightBrightStatus getInstance() throws ConnectorException, IOException {
        if (instance == null)
            instance = new LightBrightStatus();
        return instance;
    }


    public String getLightsOnOff(String ip) throws ConnectorException, IOException {
        String uri = "coap://[" + ip + "]/actuator/lights";
        CoapClient coapClient = new CoapClient(uri);
        CoapResponse coapResponse = coapClient.get();
        System.out.println("Coap response nella GET Light: " + coapResponse);
        if (coapResponse != null && coapResponse.isSuccess()) {
            String responseText = coapResponse.getResponseText();
            if(responseText == null || responseText.length() == 0) {
                return "UNKNOWN";
            } else {
                return responseText;
            }
        } else {
            System.out.println("[FAIL] -Error during CoAP (GET light) request");
            // In caso di errore nella richiesta CoAP, potresti restituire un valore di default o sollevare un'eccezione personalizzata.
            return "UNKNOWN";
        }
    }

    public void putLightsOn(String ip, String order) {
        String uri = "coap://[" + ip + "]/actuator/lights";
        CoapClient coapClient = new CoapClient(uri);
        try {
            CoapResponse lights = coapClient.put(order, MediaTypeRegistry.TEXT_PLAIN);
            System.out.println("Coap Response: " + lights);
            System.out.println(" >  " + order);
            if (lights.isSuccess()) {
                System.out.println("[+] PUT request succeeded");
            } else {
                System.out.println("[-] PUT request failed");
            }
            coapClient.shutdown();
        } catch (ConnectorException | IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }

    }

    public void putLightsOff(String ip, String order) {
        String uri = "coap://[" + ip + "]/actuator/lights";
        CoapClient coapClient = new CoapClient(uri);
        try {
            CoapResponse lights = coapClient.put(order, MediaTypeRegistry.TEXT_PLAIN);
            System.out.println("Coap Response: " + lights);
            System.out.println(" >  " + order);
            if (lights.isSuccess()) {
                System.out.println("[+] PUT request succeeded");
            } else {
                System.out.println("[-] PUT request failed");
            }
            coapClient.shutdown();
        } catch (ConnectorException | IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }

    }

    public String getBrightsOnOff(String ip) throws ConnectorException, IOException {
        String uri = "coap://" + ip + "/sensor/motion";
        CoapClient coapClient = new CoapClient(uri);
        CoapResponse coapResponse = coapClient.get();
        if (coapResponse != null && coapResponse.isSuccess()) {
            String responseText = coapResponse.getResponseText();
            try {
                JSONParser parser = new JSONParser();
                JSONObject obj = (JSONObject) parser.parse(responseText);
                String lightStatus = (String) obj.get("brights");
                return lightStatus;
            } catch (ParseException e) {
                e.printStackTrace();
                System.out.println(" [!] # Error during reading JSON Response");
                // In caso di errore, potresti restituire un valore di default o sollevare un'eccezione personalizzata.
                return "UNKNOWN";
            }
        } else {
            System.out.println(" [!] # Error during CoAP request");
            // In caso di errore nella richiesta CoAP, potresti restituire un valore di default o sollevare un'eccezione personalizzata.
            return "UNKNOWN";
        }
    }


    public void putBrightsOn(String ip, String order) {
        CoapClient brightActuator = new CoapClient("coap://[" + ip + "]/actuator/brights");
        try {
            CoapResponse response = brightActuator.put(order, MediaTypeRegistry.TEXT_PLAIN);
            System.out.println(" +  " + order);

            // Check the response
            if (response.isSuccess()) {
                System.out.println("[+] PUT request succeeded");
            } else {
                System.out.println("[-] PUT request failed");
            }
        } catch (ConnectorException | IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }

    }

    public void putBrightsOff(String ip, String order) {
        CoapClient brightActuator = new CoapClient("coap://[" + ip + "]/actuator/brights");
        try {
            CoapResponse response = brightActuator.put(order, MediaTypeRegistry.TEXT_PLAIN);
            System.out.println(" +  " + order);

            // Check the response
            if (response.isSuccess()) {
                System.out.println("[+] PUT request succeeded");
            } else {
                System.out.println("[-] PUT request failed");
            }
        } catch (ConnectorException | IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }

    }

    double calculateWearFromCounter(int counter) {
        return counter * 0.5; //esempio formula
    }


    public Double getWearLevel(int lightId) {
        int counter = light.getCounterForLight(lightId);
        double wearLevel = calculateWearFromCounter(counter);
        return wearLevel;
    }
}


