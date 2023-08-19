package it.iot.application.coap;

import org.eclipse.californium.core.CoapServer;


public class RegistrationServer extends CoapServer {

    private static RegistrationServer server = null;


    public void createRegistrationServer(){
        System.out.println("[!] Start registration server ...");
        server = new RegistrationServer();
        server.add(new RegistrationLightResource());
        server.start();
    }

}