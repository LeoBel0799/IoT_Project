package it.iot.remote.lightsCarManagement.observations;

import it.iot.remote.database.DBremote;
import it.iot.remote.lightsCarManagement.carcontroller.RemoteCarControllerHandler;
import org.eclipse.californium.core.CoapResource;
import org.eclipse.californium.core.coap.CoAP;
import org.eclipse.californium.core.coap.Response;
import org.eclipse.californium.core.server.resources.CoapExchange;
import org.json.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.parser.ParseException;


public class StartingtThreadObservers extends CoapResource {
    private final static RemoteCarControllerHandler handler = RemoteCarControllerHandler.getInstance();
    Observer obs;

    public StartingtThreadObservers(String res){
        super(res);
    }


    public void startingThreads(CoapExchange exchange) throws ParseException {
        String msg = exchange.getRequestText();
        String ip = exchange.getSourceAddress().getHostAddress();

        try{
            JSONObject obj = (JSONObject) JSONValue.parseWithException(msg);
            int lightid = (int) obj.get("lightid");
            if (lightid != 0){
                String res = "{\"status\": \"ok\"}";
                DBremote.insertInfo(lightid,ip);
                System.out.println("[!] Info inserted");
                Response response = new Response(CoAP.ResponseCode.CONTENT);
                exchange.respond(response);
                response.setPayload(res);
                System.out.println(" >  "+ res );
                try {
                    Thread.sleep(1500);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                obs.brightsObserver(ip);
                try {
                    Thread.sleep(1500);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                obs.onLightsObserver(ip);
            }
        } catch (ParseException e) {
            e.printStackTrace();
            System.out.println("[-] error during message parsing");
        }
}
}
