package it.iot.remote.lightsCarManagement;

import org.eclipse.californium.core.CoapClient;
import org.eclipse.californium.core.CoapResponse;
import org.eclipse.californium.core.coap.CoAP;
import org.eclipse.californium.core.coap.MediaTypeRegistry;
import org.eclipse.californium.core.coap.Request;
import org.eclipse.californium.elements.exception.ConnectorException;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class RemoteCarControllerHandler {
    private static RemoteCarControllerHandler instance = null;
    private static CoapClient motionApp = null;
    private static CoapClient lightStatusApp = null;
    private static CoapClient brightsHandler = null;
    private static CoapClient degreeLightsHandler = null;
    private static CoapClient LightPower = null;

    public static RemoteCarControllerHandler getInstance() {
        if (instance == null)
            instance = new RemoteCarControllerHandler();
        return instance;
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
/*
    public String getLightsOnOff(String address) throws IOException {
        //Questo metodo restituisce se i fari sono accesi o spenti
        String request = "GET " + "coap://" + address + "/sensor/motion"
                + "Host: " + address + "\r\n"
                + "Connection: close\r\n\r\n";
        System.out.println("[+] GET request to Motion sensor");
        try (Socket socket = new Socket(address, 5683);
             OutputStream os = socket.getOutputStream();
             InputStream is = socket.getInputStream()) {

            os.write(request.getBytes());
            os.flush();

            byte[] buffer = new byte[1024];
            int bytesRead;
            StringBuilder response = new StringBuilder();
            while ((bytesRead = is.read(buffer)) != -1) {
                response.append(new String(buffer, 0, bytesRead));
            }

            String responseString = response.toString();
            try {
                JSONParser parser = new JSONParser();
                JSONObject obj = (JSONObject) parser.parse(responseString);
                String lightStatus = (String) obj.get("lights");
                return lightStatus;
            } catch (ParseException e) {
                e.printStackTrace();
                System.out.println(" [!] # Error during reading JSON Response");
                // In caso di errore, potresti restituire un valore di default o sollevare un'eccezione personalizzata.
                return "UNKNOWN";
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println(" [!] # Error during socket communication");
            // In caso di errore nella comunicazione con il socket, potresti restituire un valore di default o sollevare un'eccezione personalizzata.
            return "UNKNOWN";
        }
    }

*/

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

/*    public int getBrightsOnOff(String address) throws IOException {
        //Questo metodo restituisce se i fari sono accesi o spenti
        String request = "GET " + "coap://" + address + "/sensor/motion"
                + "Host: " + address + "\r\n"
                + "Connection: close\r\n\r\n";
        System.out.println("[+] GET request to Motion sensor");
        try (Socket socket = new Socket(address, 5683);
             OutputStream os = socket.getOutputStream();
             InputStream is = socket.getInputStream()) {

            os.write(request.getBytes());
            os.flush();

            byte[] buffer = new byte[1024];
            int bytesRead;
            StringBuilder response = new StringBuilder();
            while ((bytesRead = is.read(buffer)) != -1) {
                response.append(new String(buffer, 0, bytesRead));
            }

            String responseString = response.toString();
            try {
                JSONParser parser = new JSONParser();
                JSONObject obj = (JSONObject) parser.parse(responseString);
                Integer brightStatus = (Integer) obj.get("brights");
                return brightStatus;
            } catch (ParseException e) {
                e.printStackTrace();
                System.out.println(" [!] # Error during reading JSON Response");
                // In caso di errore, potresti restituire un valore di default o sollevare un'eccezione personalizzata.
                return -1;
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println(" [!] # Error during socket communication");
            // In caso di errore nella comunicazione con il socket, potresti restituire un valore di default o sollevare un'eccezione personalizzata.
            return -1;
        }
    }*/

   /* public Boolean getFulminated(String address) throws IOException {
        //Questo metodo restituisce se i fari sono accesi o spenti
        String request = "GET " + "coap://" + address + "/sensor/light"
                + "Host: " + address + "\r\n"
                + "Connection: close\r\n\r\n";
        System.out.println("[+] GET request to Light sensor");
        try (Socket socket = new Socket(address, 5683);
             OutputStream os = socket.getOutputStream();
             InputStream is = socket.getInputStream()) {

            os.write(request.getBytes());
            os.flush();

            byte[] buffer = new byte[1024];
            int bytesRead;
            StringBuilder response = new StringBuilder();
            while ((bytesRead = is.read(buffer)) != -1) {
                response.append(new String(buffer, 0, bytesRead));
            }

            String responseString = response.toString();
            try {
                JSONParser parser = new JSONParser();
                JSONObject obj = (JSONObject) parser.parse(responseString);
                Boolean fulminated = (Boolean) obj.get("lightFulminated");
                return fulminated;
            } catch (ParseException e) {
                e.printStackTrace();
                System.out.println(" [!] # Error during reading JSON Response");
                // In caso di errore, potresti restituire un valore di default o sollevare un'eccezione personalizzata.
                return false;
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println(" [!] # Error during socket communication");
            // In caso di errore nella comunicazione con il socket, potresti restituire un valore di default o sollevare un'eccezione personalizzata.
            return false;
        }
    }*/


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


   /* public Double getWearLevel(String address) throws IOException {
        //Questo metodo restituisce se i fari sono accesi o spenti
        String request = "GET " + "coap://" + address + "/sensor/light"
                + "Host: " + address + "\r\n"
                + "Connection: close\r\n\r\n";
        System.out.println("[+] GET request to Light sensor");
        try (Socket socket = new Socket(address, 5683);
             OutputStream os = socket.getOutputStream();
             InputStream is = socket.getInputStream()) {

            os.write(request.getBytes());
            os.flush();

            byte[] buffer = new byte[1024];
            int bytesRead;
            StringBuilder response = new StringBuilder();
            while ((bytesRead = is.read(buffer)) != -1) {
                response.append(new String(buffer, 0, bytesRead));
            }

            String responseString = response.toString();
            try {
                JSONParser parser = new JSONParser();
                JSONObject obj = (JSONObject) parser.parse(responseString);
                Double wearLevel = (Double) obj.get("wearLevel");
                return wearLevel;
            } catch (ParseException e) {
                e.printStackTrace();
                System.out.println(" [!] # Error during reading JSON Response");
                // In caso di errore, potresti restituire un valore di default o sollevare un'eccezione personalizzata.
                return -1.0;
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println(" [!] # Error during socket communication");
            // In caso di errore nella comunicazione con il socket, potresti restituire un valore di default o sollevare un'eccezione personalizzata.
            return -1.0;
        }
    }*/


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
    }
}
