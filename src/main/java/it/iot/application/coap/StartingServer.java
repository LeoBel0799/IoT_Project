package it.iot.application.coap;

import org.eclipse.californium.core.CoapServer;


public class StartingServer extends CoapServer implements Runnable{

    private static StartingServer server;


    public void createRegistrationServer() {
        try {
            System.out.println("[INFO] - Start registration server ...");
            server = new StartingServer();
            server.add(new ActuatorRegistration());
            server.start();
        } catch (Exception e) {
            System.err.println("[FAIL] - An unexpected error occurred during server startup.");
        }
    }
    @Override
    public void run() {
        createRegistrationServer();
    }
}