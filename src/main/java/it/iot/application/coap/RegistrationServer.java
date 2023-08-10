package it.iot.application.coap;

import org.eclipse.californium.core.CoapServer;

import java.net.SocketException;

public class RegistrationServer extends CoapServer implements Runnable {
    public void createRegistrationServer() throws SocketException {
        System.out.println("[!] Start registration server ...");
        RegistrationServer server = new RegistrationServer();
        server.add(new RegistrationLightResource("registration"));
        server.start();
    }

    @Override
    public void run() {
        try {
            createRegistrationServer();
        } catch (SocketException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
}