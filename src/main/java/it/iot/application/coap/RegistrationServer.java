package it.iot.application.coap;

import org.eclipse.californium.core.CoapServer;

import java.net.SocketException;
import java.sql.SQLException;


public class RegistrationServer extends CoapServer implements Runnable{

    private static RegistrationServer server;


    public void createRegistrationServer() {
        try {
            System.out.println("[INFO] - Start registration server ...");
            server = new RegistrationServer();
            server.add(new RegistrationLightResource());
            server.start();
            System.out.println("[INFO] - Registration server started successfully.");
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