package it.iot.application.controller;

import it.iot.application.DB.ActuatorStatus;
import it.iot.application.DB.LightData;
import org.eclipse.californium.core.CoapClient;
import org.eclipse.californium.core.CoapResponse;
import org.eclipse.californium.core.coap.MediaTypeRegistry;
import org.eclipse.californium.elements.exception.ConnectorException;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.IOException;
import java.sql.SQLException;

public class LightBrightStatus {
    ActuatorStatus actuator  = new ActuatorStatus();
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
        System.out.println("[INFO] - COAP response: " + coapResponse);
        if (coapResponse != null && coapResponse.isSuccess()) {
            String responseText = coapResponse.getResponseText();
            if(responseText == null || responseText.length() == 0) {
                return "UNKNOWN";
            } else {
                return responseText;
            }
        } else {
            System.out.println("[FAIL] - Error during CoAP (GET light) request");
            // In caso di errore nella richiesta CoAP, potresti restituire un valore di default o sollevare un'eccezione personalizzata.
            return "UNKNOWN";
        }
    }

    //questo prende i dati nuovi dal coap e me li porta in Java per poi metterli nel DB
    public String[] getWearAndFulminatedFromActuator(String ip) throws ConnectorException, IOException {
        String uri = "coap://[" + ip + "]/actuator/data";
        CoapClient coapClient = new CoapClient(uri);
        CoapResponse coapResponse = coapClient.get();
        String[] results = new String[3];

        if (coapResponse != null && coapResponse.isSuccess()) {
            String responseText = coapResponse.getResponseText();
            if(responseText == null || responseText.length() == 0) {
                return null;
            } else {
                String[] parts = responseText.split(",");
                // Il primo elemento è wearLevel
                String wearLevelStr = parts[0];
                double wearLevel = Double.parseDouble(wearLevelStr);
                // Il secondo elemento è fulminated
                String fulStr = parts[1];
                boolean fulminated = Boolean.parseBoolean(fulStr);
                // Il terzo elemento è counter
                String counterStr = parts[2];
                int counter = Integer.parseInt(counterStr);
                // Imposta i risultati da ritornare
                results[0] = String.valueOf(wearLevel);
                results[1] = String.valueOf(fulminated);
                results[2] = String.valueOf(counter);
                return results;
            }
        } else {
            System.out.println("[FAIL] - Error during CoAP (GET light) request");
            // In caso di errore nella richiesta CoAP, potresti restituire un valore di default o sollevare un'eccezione personalizzata.
            return null;
        }
    }


    public void putLightsOn(String ip, String order) {
        String uri = "coap://[" + ip + "]/actuator/lights?command=ON";
        CoapClient coapClient = new CoapClient(uri);
        try {
            CoapResponse lights = coapClient.put(order, MediaTypeRegistry.TEXT_PLAIN);
            System.out.println(" Command  " + order);
            if (lights.isSuccess()) {
                System.out.println("[OK] - PUT request succeeded");
            } else {
                System.out.println("[FAIL] - PUT request failed");
            }
            coapClient.shutdown();
        } catch (ConnectorException | IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }

    }

    public void putLightsOff(String ip, String order) {
        String uri = "coap://[" + ip + "]/actuator/lights?command=OFF";
        CoapClient coapClient = new CoapClient(uri);
        try {
            CoapResponse lights = coapClient.put(order, MediaTypeRegistry.TEXT_PLAIN);
            System.out.println("[INFO] - COAP response:" + lights);
            System.out.println("[INFO] - Command  " + order);
            if (lights.isSuccess()) {
                System.out.println("[OK] - PUT request succeeded");
            } else {
                System.out.println("[FAIL] - PUT request failed");
            }
            coapClient.shutdown();
        } catch (ConnectorException | IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }

    }


    public void sendWearLevel(String ip, Double wearLevel, boolean fulminated, int counter) throws ConnectorException, IOException {
        String uri = "coap://[" + ip + "]/actuator/data";
        CoapClient coapClient = new CoapClient(uri);
        // crea payload
        String payload = wearLevel + "," + fulminated + "," + counter;
        System.out.println(payload);
        CoapResponse response = coapClient.post(payload, MediaTypeRegistry.TEXT_PLAIN);
        System.out.println("[INFO] - COAP response: " + response);
        if(response.isSuccess()) {
            System.out.println("[OK] - Data send successfully");
        } else {
            System.out.println("[FAIL]- Something went wrong in sending");
        }

        coapClient.shutdown();

    }




    public String getBrightsOnOff(String ip) throws ConnectorException, IOException {
        String uri = "coap://[" + ip + "]/actuator/brights";
        CoapClient coapClient = new CoapClient(uri);
        CoapResponse coapResponse = coapClient.get();
        System.out.println("[INFO] - COAP response: " + coapResponse);
        if (coapResponse != null && coapResponse.isSuccess()) {
            String responseText = coapResponse.getResponseText();
            if(responseText == null || responseText.length() == 0) {
                return "UNKNOWN";
            } else {
                return responseText;
            }
        } else {
            System.out.println("[FAIL] -Error during CoAP (GET Bright) request");
            return "UNKNOWN";
        }
    }


    public void putBrightsOn(String ip, String order) {
        String uri = "coap://[" + ip + "]/actuator/brights?order=ON";
        CoapClient brightActuator = new CoapClient(uri);

        try {
            CoapResponse response = brightActuator.put(order, MediaTypeRegistry.TEXT_PLAIN);
            System.out.println("[INFO] - COAP response:" + response);

            // Check the response
            if (response.isSuccess()) {
                System.out.println("[OK] PUT request succeeded");
            } else {
                System.out.println("[FAIL] PUT request failed");
            }
            brightActuator.shutdown();
        } catch (ConnectorException | IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }

    }

    public void putBrightsOff(String ip, String order) {
        CoapClient brightActuator = new CoapClient("coap://[" + ip + "]/actuator/brights?order=OFF");
        try {
            CoapResponse response = brightActuator.put(order, MediaTypeRegistry.TEXT_PLAIN);
            System.out.println("[INFO] - COAP response: " + response);

            // Check the response
            if (response.isSuccess()) {
                System.out.println("[OK] - PUT request succeeded");
            } else {
                System.out.println("[FAIL] - PUT request failed");
            }
            brightActuator.shutdown();
        } catch (ConnectorException | IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }

    }

    double calculateWearFromCounter(int counter) {
        return counter*0.40;
    }


}


