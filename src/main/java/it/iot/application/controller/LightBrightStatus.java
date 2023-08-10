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
        String uri = "coap://" + ip + "/sensor/motion";
        CoapClient coapClient = new CoapClient(uri);
        CoapResponse coapResponse = coapClient.get();
        if (coapResponse != null && coapResponse.isSuccess()) {
            String responseText = coapResponse.getResponseText();
            try {
                JSONParser parser = new JSONParser();
                JSONObject obj = (JSONObject) parser.parse(responseText);
                String lightStatus = (String) obj.get("lights");
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

    public void putLightsOn(String ip, String order) {
        String uri = "coap://" + ip + "/actuator/lights";
        CoapClient coapClient = new CoapClient(uri);
        try {
            CoapResponse lights = coapClient.put(order, MediaTypeRegistry.TEXT_PLAIN);
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
        String uri = "coap://" + ip + "/actuator/lights";
        CoapClient coapClient = new CoapClient(uri);
        try {
            CoapResponse lights = coapClient.put(order, MediaTypeRegistry.TEXT_PLAIN);
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
/*
    public void onLightsObserver (String ip){
        motionApp = new CoapClient("coap://[" + ip + "]/actuator/lights");
        motionApp.observe(
                new CoapHandler() {
                    @Override public void onLoad(CoapResponse response) {
                        try {
                            lights(response);
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                    }
                    @Override public void onError() {
                        logger.severe("[!] LIGHTS observation failed");
                    }
                }
        );
    }

    public void lights (CoapResponse res) throws ParseException {
        try{
            byte[] payload = res.getPayload();
            JSONParser parser = new JSONParser();
            JSONObject data = (JSONObject) parser.parse(String.valueOf(payload));
            String lightStatus = data.getString("lights");
            // Registra evento
            DBremote(lightStatus);
        }catch (ParseException e) {
            // Sostituisce println
            logger.severe("Error parsing payload: " + e.getMessage());
        }
    }
    }

    public String getLightsOnOff(String address) throws ConnectorException, IOException {
        String uri = "coap://" + address + "/sensor/motion";

        CoapClient coapClient = new CoapClient(uri);
        CoapResponse coapResponse = coapClient.get();

        if (coapResponse != null && coapResponse.isSuccess()) {
            String responseText = coapResponse.getResponseText();
            try {
                JSONParser parser = new JSONParser();
                JSONObject obj = (JSONObject) parser.parse(responseText);
                String lightStatus = (String) obj.get("lights");
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


    public void turnLightsOn(String address, String request) throws IOException, ConnectorException {
        String lightStatus = getLightsOnOff(address);
        if (lightStatus.equalsIgnoreCase("ON")) {
            System.out.println("[!] Lights are already ON.");
        } else {
            performPutRequest(address, request);
            System.out.println("[+] Lights turned ON.");
        }
    }

    public void turnLightsOff(String address, String request) throws IOException, ConnectorException {
        String lightStatus = getLightsOnOff(address);
        if (lightStatus.equalsIgnoreCase("OFF")) {
            System.out.println("[!] Lights are already OFF.");
        } else {
            performPutRequest(address, request);
            System.out.println("[+] Lights turned OFF.");
        }
    }

    public void turnIndicatorsOn(String address, String request) throws IOException {
        System.out.println("[+] Turning Indicators ON.");
        performPutRequest(address,request);

    }

    public void turnBrightsOn(String address, String request) throws IOException, ConnectorException {
        Integer lightStatus = getBrightsOnOff(address);
        if (lightStatus == 1) {
            System.out.println("[!] Brights are already ON.");
        } else {
            performPutRequest(address, request);
            System.out.println("[+] Brights turned ON.");
        }
    }

    public void turnHornOff(String address, String request) throws IOException {
        System.out.println("[+] Turning Horn OFF.");
        performPutRequest(address,request);

    }


    public void turnHornOn(String address, String request) throws IOException{
        System.out.println("[+] Turning Horn ON.");
        performPutRequest(address,request);

    }


    public void turnBrightsOff(String address, String request) throws IOException, ConnectorException {
        Integer lightStatus = getBrightsOnOff(address);
        if (lightStatus == 0) {
            System.out.println("[!] Brights are already OFF.");
        } else {
            performPutRequest(address, request);
            System.out.println("[+] Brights turned OFF.");
        }
    }


    private void performPutRequest(String address, String order) throws IOException {
        String coapUrl = "coap://" + address + "/sensor/motion";
        CoapClient client = new CoapClient(coapUrl);
        try {
            Request request = new Request(CoAP.Code.PUT);
            request.getOptions().setAccept(MediaTypeRegistry.APPLICATION_JSON);
            request.setPayload(order.getBytes());
            CoapResponse coapResponse = client.advanced(request);
            if (coapResponse != null) {
                CoAP.ResponseCode responseCode = coapResponse.getCode();
                if (responseCode == CoAP.ResponseCode.CHANGED) {
                    System.out.println("[+] PUT request successful.");
                } else {
                    System.out.println("[!] PUT request failed. Response code: " + responseCode);
                }
            } else {
                System.out.println("[!] No response received for PUT request.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("[!] # Error during PUT request.");
        } finally {
            client.shutdown();
        }
    }

    public int getBrightsOnOff(String address) throws ConnectorException, IOException {
        String uri = "coap://" + address + "/sensor/motion";

        CoapClient coapClient = new CoapClient(uri);
        CoapResponse coapResponse = coapClient.get();

        if (coapResponse != null && coapResponse.isSuccess()) {
            String responseText = coapResponse.getResponseText();
            try {
                JSONParser parser = new JSONParser();
                JSONObject obj = (JSONObject) parser.parse(responseText);
                Integer brightStatus = (Integer) obj.get("brights");
                return brightStatus;
            } catch (ParseException e) {
                e.printStackTrace();
                System.out.println(" [!] # Error during reading JSON Response");
                // In caso di errore, potresti restituire un valore di default o sollevare un'eccezione personalizzata.
                return -1;
            }
        } else {
            System.out.println(" [!] # Error during CoAP request");
            // In caso di errore nella richiesta CoAP, potresti restituire un valore di default o sollevare un'eccezione personalizzata.
            return -1;
        }
    }

   public Boolean getFulminated(String address) throws ConnectorException, IOException {
       String uri = "coap://" + address + "/sensor/light";

       CoapClient coapClient = new CoapClient(uri);
       CoapResponse coapResponse = coapClient.get();

       if (coapResponse != null && coapResponse.isSuccess()) {
           String responseText = coapResponse.getResponseText();
           try {
               JSONParser parser = new JSONParser();
               JSONObject obj = (JSONObject) parser.parse(responseText);
               Boolean fulminated = (Boolean) obj.get("lightFulminated");
               return fulminated;
           } catch (ParseException e) {
               e.printStackTrace();
               System.out.println(" [!] # Error during reading JSON Response");
               // In caso di errore, potresti restituire un valore di default o sollevare un'eccezione personalizzata.
               return false;
           }
       } else {
           System.out.println(" [!] # Error during CoAP request");
           // In caso di errore nella richiesta CoAP, potresti restituire un valore di default o sollevare un'eccezione personalizzata.
           return false;
       }
   }

    public Double getWearLevel(String address) throws ConnectorException, IOException {
        String uri = "coap://" + address + "/sensor/light";

        CoapClient coapClient = new CoapClient(uri);
        CoapResponse coapResponse = coapClient.get();

        if (coapResponse != null && coapResponse.isSuccess()) {
            String responseText = coapResponse.getResponseText();
            try {
                JSONParser parser = new JSONParser();
                JSONObject obj = (JSONObject) parser.parse(responseText);
                Double wearLevel = (Double) obj.get("wearLevel");
                return wearLevel;
            } catch (ParseException e) {
                e.printStackTrace();
                System.out.println(" [!] # Error during reading JSON Response");
                // In caso di errore, potresti restituire un valore di default o sollevare un'eccezione personalizzata.
                return -1.0;
            }
        } else {
            System.out.println(" [!] # Error during CoAP request");
            // In caso di errore nella richiesta CoAP, potresti restituire un valore di default o sollevare un'eccezione personalizzata.
            return -1.0;
        }
    }*/

