package it.iot.remote.lightsCarManagement.observations;
import it.iot.remote.lightsCarManagement.carcontroller.PoweringHorn;
import it.iot.remote.lightsCarManagement.carcontroller.PoweringIndicators;
import it.iot.remote.lightsCarManagement.carcontroller.PoweringLights;
import org.eclipse.californium.core.CoapClient;
import org.eclipse.californium.core.CoapHandler;
import org.eclipse.californium.core.CoapResponse;
import org.json.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.parser.ParseException;

public class Observer {
    private static CoapClient motionApp = null;
    private static CoapClient lightPower = null;
    private static CoapClient brightsPower = null;

    public void onLightsObserver (String ip){
        motionApp = new CoapClient("coap://[" + ip + "]/sensor/motion");
        lightPower = new CoapClient("coap://[" + ip + "]/sensor/motion");
        motionApp.observe(
                new CoapHandler() {
                    @Override public void onLoad(CoapResponse response) {
                        lights(response, ip);
                    }
                    @Override public void onError() {
                        System.out.println("[-] LIGHTS observation failed");
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
                        indicators(response, ip);
                        horn(response,ip);
                    }
                    @Override public void onError() {
                        System.out.println("[-] BRIGHTS observation failed");
                    }
                }
        );

    }

    public void indicators (CoapResponse res, String addr){
        String resp = new String(res.getPayload());
        JSONObject obj;
        try {
            obj = (JSONObject) JSONValue.parseWithException(resp);
            String brights = (String) obj.get("brights");
            PoweringIndicators l = new PoweringIndicators(brights,addr);
            Thread threadForIndicators = new Thread(l);
            threadForIndicators.start();
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    public void horn (CoapResponse res, String addr){
        String resp = new String(res.getPayload());
        JSONObject obj;
        try {
            obj = (JSONObject) JSONValue.parseWithException(resp);
            String brights = (String) obj.get("brights");
            PoweringHorn l = new PoweringHorn(brights,addr);
            Thread threadForIndicators = new Thread(l);
            threadForIndicators.start();
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }


    public void lights (CoapResponse res, String addr){
        String resp = new String(res.getPayload());
        JSONObject obj;
        try {
            obj = (JSONObject) JSONValue.parseWithException(resp);
            String lights = (String) obj.get("lights");
            PoweringLights l = new PoweringLights(lights,addr);
            Thread threadForLight = new Thread(l);
            threadForLight.start();
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    public static void shutdown(){
        motionApp.shutdown();
        brightsPower.shutdown();
        lightPower.shutdown();
    }
}
