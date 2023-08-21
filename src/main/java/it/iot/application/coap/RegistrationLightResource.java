package it.iot.application.coap;


import it.iot.application.DB.NodeData;
import org.eclipse.californium.core.CoapResource;
import org.eclipse.californium.core.coap.CoAP;
import org.eclipse.californium.core.coap.Response;
import org.eclipse.californium.core.server.resources.CoapExchange;
import org.eclipse.californium.elements.exception.ConnectorException;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;

public class RegistrationLightResource extends CoapResource {
    static NodeData node;

    static {
        try {
            node = new NodeData();
            node.createDelete("node");

        } catch (ConnectorException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }


    public RegistrationLightResource() throws SQLException {
        super("registration");
        setObservable(false);

    }
    static int count = 0;
    public void handlePOST(CoapExchange exchange) {
        //System.out.println("Receiving POST");
        String msg = exchange.getRequestText();
        String ipv6 = exchange.getSourceAddress().getHostAddress();

        //System.out.println(" <  " + msg);
        try {
            String res;
            // Obtain information from json message
            JSONObject json = (JSONObject) JSONValue.parseWithException(msg);
            String id_string = json.get("id").toString();
            int id = Integer.parseInt(id_string);
            count= count +1;
            System.out.println("STAMPO ID "+id+ "NUMERO ENTRATE : "+count);
            System.out.println("STAMPO ipv6 "+id+ "NUMERO ENTRATE : "+count);


            //System.out.println("[!] Insertion node in the configuration table ... ");
            res = "{\"res\": \"ok\"}";
            //System.out.println("ID: "+id+" IPV6: "+ipv6);
            //controlla se l'ID nodo esiste già
            //if(node.exists(id)) {
                //aggiorna solo l'IP
              //  node.updateIPv6(id, ipv6);
            //} else {
                //inserisci nuovo nodo
            node.insertNodeData(count, ipv6);
                //}
           // System.out.println("[!] Finish insertion node");

            Response response = new Response(CoAP.ResponseCode.CONTENT);
            response.setPayload(res);
            exchange.respond(response);
            //System.out.println(" >  " + res);

        } catch (Throwable e) {
            e.printStackTrace();
            System.out.println("! ERROR during parsing");
            exchange.respond(CoAP.ResponseCode.NOT_ACCEPTABLE,"Unsuccessful".getBytes(StandardCharsets.UTF_8));
        }



    }
}
