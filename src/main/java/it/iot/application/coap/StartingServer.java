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
            //System.out.println("[INFO] - Registration server started successfully.");
        } catch (Exception e) {
            System.err.println("[ERROR] - An unexpected error occurred during server startup.");
            e.printStackTrace();
        }
    }
    @Override
    public void run() {
        createRegistrationServer();
    }
}