package it.iot.remote;

import org.eclipse.californium.core.CoapClient;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class LightRemoteHandler {
    private static LightRemoteHandler instance = null;
    private static CoapClient motionApp = null;
    private static CoapClient lightStatusApp = null;
    private static CoapClient brightsHandler = null;
    private static CoapClient degreeLightsHandler = null;
    private static CoapClient LightPower = null;

    public static LightRemoteHandler getInstance() {
        if (instance == null)
            instance = new LightRemoteHandler();
        return instance;
    }

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

    public int getDegreeLights(String address) throws IOException {
        //Questo metodo dice che fari sono accesi se posizione o abbaglianti
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
                int lightDegreeStatus = (int) obj.get("lightsDegree");
                return lightDegreeStatus;
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
    }

    public String getBrightsOnOff(String address) throws IOException {
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
                String brightStatus = (String) obj.get("brights");
                return brightStatus;
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

    public String getFulminated(String address) throws IOException {
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
                String fulminated = (String) obj.get("lightFulminated");
                return fulminated;
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

    public Double getWearLevel(String address) throws IOException {
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
    }
    //TODO: Controllare le get e scrivere le put (devo andare al cesso)
}
