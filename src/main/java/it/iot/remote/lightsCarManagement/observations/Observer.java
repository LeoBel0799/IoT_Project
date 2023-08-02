package it.iot.remote.lightsCarManagement.observations;
import it.iot.remote.database.DBremoteObservers;
import org.eclipse.californium.core.CoapClient;
import org.eclipse.californium.core.CoapHandler;
import org.eclipse.californium.core.CoapResponse;
import org.json.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.util.logging.Logger;

public class Observer {
    private static CoapClient motionApp = null;
    private static CoapClient lightPower = null;
    private static CoapClient brightsPower = null;
    public DBremoteObservers dBremoteObservers;
    private static final Logger logger = Logger.getLogger(Observer.class.getName());

    public void onLightsObserver (String ip){
        motionApp = new CoapClient("coap://[" + ip + "]/sensor/motion");
        lightPower = new CoapClient("coap://[" + ip + "]/sensor/motion");
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

    public void brightsObserver (String ip){
        motionApp = new CoapClient("coap://[" + ip + "]/sensor/motion");
        brightsPower = new CoapClient("coap://[" + ip + "]/sensor/motion");
        motionApp.observe(
                new CoapHandler() {
                    @Override public void onLoad(CoapResponse response) {
                        try {
                            indicators(response);
                            horn(response);
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }

                    }
                    @Override public void onError() {
                        logger.severe("[!] BRIGHTS observation failed");
                    }
                }
        );

    }

    public void indicators(CoapResponse res) throws ParseException {
        try{
            byte[] payload = res.getPayload();
            JSONParser parser = new JSONParser();
            JSONObject data = (JSONObject) parser.parse(String.valueOf(payload));
            int brights = data.getInt("brights");
            // Registra evento
            dBremoteObservers.insertObserverBright(brights);
        }catch (ParseException e) {
            // Sostituisce println
            logger.severe("Error parsing payload: " + e.getMessage());
        }

    }



    public void horn (CoapResponse res) throws ParseException {
        try{
            byte[] payload = res.getPayload();
            JSONParser parser = new JSONParser();
            JSONObject data = (JSONObject) parser.parse(String.valueOf(payload));
            int brights = data.getInt("brights");
            // Registra evento
            dBremoteObservers.insertObserverBright(brights);
        }catch (ParseException e) {
            // Sostituisce println
            logger.severe("Error parsing payload: " + e.getMessage());
        }

    }


    public void lights (CoapResponse res) throws ParseException {
        try{
            byte[] payload = res.getPayload();
            JSONParser parser = new JSONParser();
            JSONObject data = (JSONObject) parser.parse(String.valueOf(payload));
            String lightStatus = data.getString("lights");
            // Registra evento
            dBremoteObservers.insertObserverLight(lightStatus);
        }catch (ParseException e) {
            // Sostituisce println
            logger.severe("Error parsing payload: " + e.getMessage());
        }
    }

        public static void shutdown(){
        motionApp.shutdown();
        brightsPower.shutdown();
        lightPower.shutdown();
    }
}
