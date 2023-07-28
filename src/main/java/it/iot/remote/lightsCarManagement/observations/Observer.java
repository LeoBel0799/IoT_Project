package it.iot.remote.lightsCarManagement.observations;
import it.iot.remote.lightsCarManagement.*;
import org.eclipse.californium.core.CoapClient;
import org.eclipse.californium.core.CoapHandler;
import org.eclipse.californium.core.CoapResponse;
import org.json.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.parser.ParseException;

public class Observer {
    private static CoapClient motionApp = null;
    private static CoapClient lightStatusApp = null;
    private static CoapClient brightsHandler = null;
    private static CoapClient degreeLightsHandler = null;
    private static CoapClient LightPower = null;

    public void onLightsObserver (String ip){
        motionApp = new CoapClient("coap://[" + ip + "]/sensor/motion");
        LightPower = new CoapClient("coap://[" + ip + "]/sensor/motion");
        motionApp.observe(
                new CoapHandler() {
                    @Override public void onLoad(CoapResponse response) {
                        lightsValue(response, ip);
                    }
                    @Override public void onError() {
                        System.out.println("[-] LIGHTS observation failed");
                    }
                }
        );
    }
    public void lightsValue (CoapResponse res, String addr){
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
/*TODO: FARE OBS PER ABBAGLIANTI, USURA E LUCE FULMINATA.
   QUELLO CHE SI FA è OSSERVARE CIò CHE AVVIENE SUL SENSORE E AVVIARE IL THREAD RELATIVO AI COMANDI DEL TELECOMANDO
 */
}
