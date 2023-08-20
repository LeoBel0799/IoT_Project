package it.iot.application.coap;


import it.iot.application.DB.NodeData;
import org.eclipse.californium.core.CoapResource;
import org.eclipse.californium.core.coap.CoAP;
import org.eclipse.californium.core.coap.Response;
import org.eclipse.californium.core.server.resources.CoapExchange;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import java.nio.charset.StandardCharsets;

public class RegistrationLightResource extends CoapResource {
    NodeData node;

    public RegistrationLightResource() {
        super("registration");
        setObservable(false);

    }

    public void handlePOST(CoapExchange exchange) {
        System.out.println("Receiving POST");
        String msg = exchange.getRequestText();
        String ipv6 = exchange.getSourceAddress().getHostAddress();

        System.out.println(" <  " + msg);
        try {
            String res;
            // Obtain information from json message
            JSONObject json = (JSONObject) JSONValue.parseWithException(msg);
            String id_string = json.get("id").toString();
            int id = Integer.parseInt(id_string);

            System.out.println("[!] Insertion node in the configuration table ... ");
            res = "{\"res\":\"ok\"}";
            System.out.println("ID: "+id+" IPV6: "+ipv6);
            node.insertNodeData(id, ipv6);
            System.out.println("[!] Finish insertion node");

            Response response = new Response(CoAP.ResponseCode.CONTENT);
            response.setPayload(res);
            exchange.respond(response);
            System.out.println(" >  " + res);

        } catch (Throwable e) {
            e.printStackTrace();
            System.out.println("! ERROR during parsing");
            exchange.respond(CoAP.ResponseCode.NOT_ACCEPTABLE,"Unsuccessful".getBytes(StandardCharsets.UTF_8));
        }



    }
}
