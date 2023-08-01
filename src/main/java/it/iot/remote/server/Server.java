package it.iot.remote.server;

import it.iot.remote.lightsCarManagement.observations.StartingtThreadObservers;
import org.eclipse.californium.core.CoapServer;

import java.net.SocketException;

public class Server extends CoapServer implements Runnable {
    public void registrationServer() throws SocketException {
        System.out.println("[!] Start registration server ...");
        CoapServer server = new CoapServer();
        StartingtThreadObservers res = new StartingtThreadObservers("Resource");
        server.add(res);
        server.start();
        System.out.println("CoAP Server started on port 5683.");

    }

    @Override
    public void run() {
        try {
            Thread.sleep(Long.MAX_VALUE);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            try {
                registrationServer();
            } catch (SocketException e) {
                e.printStackTrace();
            }
            System.out.println("CoAP Server stopped.");
        }
    }
}