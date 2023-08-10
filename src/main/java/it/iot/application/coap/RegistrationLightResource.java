package it.iot.application.coap;


import it.iot.application.DB.NodeData;
import it.iot.application.controller.LightBrightStatus;
import it.iot.application.controller.PoweringBrights;
import it.iot.application.controller.PoweringLights;
import org.eclipse.californium.core.CoapResource;
import org.eclipse.californium.core.coap.CoAP;
import org.eclipse.californium.core.coap.Response;
import org.eclipse.californium.core.server.resources.CoapExchange;
import org.eclipse.californium.elements.exception.ConnectorException;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.parser.ParseException;

import java.io.IOException;

public class RegistrationLightResource extends CoapResource {
    NodeData node;
    private static LightBrightStatus lightBrightStatus = null;

    static {
        try {
            lightBrightStatus = LightBrightStatus.getInstance();
        } catch (ConnectorException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public RegistrationLightResource(String name) {
        super(name);
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
            int id = (int) json.get("id");

            System.out.println("[!] Insertion node in the configuration table ... ");
            res = "{\"res\":\"ok\"}";
            node.insertNodeData(id, ipv6);
            System.out.println("[!] Finish insertion node");

            Response response = new Response(CoAP.ResponseCode.CONTENT);
            response.setPayload(res);
            exchange.respond(response);
            System.out.println(" >  " + res);

            int lightId = id; // ipotizzo che l'id sia l'id della light
            PoweringLights poweringLights = new PoweringLights(lightId, ipv6);
            Thread threadLights = new Thread(poweringLights);
            threadLights.start();

            // Creazione e avvio thread PoweringBrights
            PoweringBrights poweringBrights = new PoweringBrights(lightId, ipv6);
            Thread threadBrights = new Thread(poweringBrights);
            threadBrights.start();
        } catch (ParseException e) {
            System.out.println("! ERROR during parsing");
        }



    }
}
