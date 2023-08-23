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

    //questo prende i dati nuovi dal coap e me li porta in Java per poi metterli nel DB
    public String[] getWearAndFulminatedFromActuator(String ip) throws ConnectorException, IOException {
        String uri = "coap://[" + ip + "]/actuator/data";
        CoapClient coapClient = new CoapClient(uri);
        CoapResponse coapResponse = coapClient.get();
        System.out.println("Coap response nella GET Wear: " + coapResponse);
        String[] results = new String[2];

        if (coapResponse != null && coapResponse.isSuccess()) {
            String responseText = coapResponse.getResponseText();
            if(responseText == null || responseText.length() == 0) {
                return null;
            } else {
                String[] parts = responseText.split(",");

                // Il primo elemento è wearLevel
                String wearLevelStr = parts[0];
                wearLevelStr = wearLevelStr.substring(10);
                int wearLevel = Integer.parseInt(wearLevelStr);

                // Il secondo elemento è fulminated
                String fulStr = parts[1].substring(11);
                boolean fulminated = fulStr.equals("true");                // Imposta i risultati da ritornare
                results[0] = String.valueOf(wearLevel);
                results[1] = String.valueOf(fulminated);

                return results;
            }
        } else {
            System.out.println("[FAIL] -Error during CoAP (GET light) request");
            // In caso di errore nella richiesta CoAP, potresti restituire un valore di default o sollevare un'eccezione personalizzata.
            return null;
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


    public void sendWearLevel(String ip, String wearLevel, String fulminated) throws ConnectorException, IOException {
        String uri = "coap://[" + ip + "]/actuator/data";
        CoapClient coapClient = new CoapClient(uri);
        // crea payload
        String payload = wearLevel + "," + fulminated;

        CoapResponse response = coapClient.post(payload, MediaTypeRegistry.TEXT_PLAIN);
        System.out.println("COAP responde per SENDING WEAR LEVEL" + response);
        if(response.isSuccess()) {
            System.out.println("[OK]- Data send successfully");
        } else {
            System.out.println("[FAIL]- Something went wrong in sending");
        }

        coapClient.shutdown();

    }




    public String getBrightsOnOff(String ip) throws ConnectorException, IOException {
        String uri = "coap://[" + ip + "]/actuator/brights";
        CoapClient coapClient = new CoapClient(uri);
        CoapResponse coapResponse = coapClient.get();
        System.out.println("Coap response nella GET Bright: " + coapResponse);
        if (coapResponse != null && coapResponse.isSuccess()) {
            String responseText = coapResponse.getResponseText();
            if(responseText == null || responseText.length() == 0) {
                return "UNKNOWN";
            } else {
                return responseText;
            }
        } else {
            System.out.println("[FAIL] -Error during CoAP (GET Bright) request");
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
        return counter;
    }


    public Double getWearLevel(int lightId) {
        int counter = light.getCounterForLight(lightId);
        double wearLevel = calculateWearFromCounter(counter);
        return wearLevel;
    }
}


