package it.iot.remote.lightsManagement;

import java.io.IOException;

public class PoweringLights implements Runnable {

    private final static LightRemoteHandler coapClient = LightRemoteHandler.getInstance();
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
            String res;
            status = coapClient.getLightsOnOff(address);
            if (status.equals("ON") && wearLevelreceived<MAX_WEAR_LEVEL) {
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
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}