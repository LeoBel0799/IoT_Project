package it.iot.remote.lightsCarManagement.carcontroller;

import org.eclipse.californium.elements.exception.ConnectorException;

import java.io.IOException;

public class PoweringLights implements Runnable {

    private final static RemoteCarControllerHandler coapClient = RemoteCarControllerHandler.getInstance();
    private static final double MAX_WEAR_LEVEL = 5.0;
    String powerFlag;
    String address;

    public PoweringLights(String power, String addr) {
        powerFlag = power;
        address = addr;
    }


    @Override
    public void run() {
        String status = null;
        try {
            Double wearLevelreceived = coapClient.getWearLevel(address);
            Boolean fulminated = coapClient.getFulminated(address);
            String res;
            status = coapClient.getLightsOnOff(address);
            powerFlag = status;
            if (status.equals("ON") && wearLevelreceived<MAX_WEAR_LEVEL && !fulminated) {
                try {
                    res = "{\"mode\": \"ON\"}";
                    System.out.println("[!] Sending PUT request (ON) to Lights");
                    coapClient.turnLightsOn(address, res);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                try {
                    res = "{\"mode\": \"OFF\"}";
                    System.out.println("[!] Sending PUT request (OFF) to Lights");
                    coapClient.turnLightsOff(address, res);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } catch (IOException | ConnectorException e) {
            e.printStackTrace();
        }
    }
}