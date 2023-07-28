package it.iot.remote.lightsCarManagement;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

public class PoweringIndicators implements Runnable {
    //Faccio accendere il clacson in maniera temporizzata più mantengo accesi gli abbaglianti
    private final static RemoteCarControllerHandler coapClient = RemoteCarControllerHandler.getInstance();
    String powerFlag;
    String address;

    public PoweringIndicators(String power, String addr) {
        powerFlag = power;
        address = addr;
    }

    @Override
    public void run() {
        try {
            String lightsStatus = coapClient.getLightsOnOff(address);
            Integer brightsStatus = coapClient.getBrightsOnOff(address);

            if (lightsStatus.equals("ON") && brightsStatus == 0) {
                // Imposta il timer per accendere e spegnere le frecce e il clacson
                Timer timer = new Timer();
                timer.schedule(new TimerTask() {
                    private boolean isOn = false;

                    @Override
                    public void run() {
                        try {
                            // Accendi e spegni le frecce
                            String res = "{\"mode\": \"" + (isOn ? "ON" : "OFF") + "\"}";
                            System.out.println("[!] Sending PUT request (" + (isOn ? "ON" : "OFF") + ") to Indicators");
                            coapClient.turnIndicatorsOn(address, res);

                            // Accendi e spegni il clacson
                            System.out.println("[!] Sending PUT request (" + (isOn ? "ON" : "OFF") + ") to Horn");
                            coapClient.turnHornOn(address, res);

                            isOn = !isOn;
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }, 0, 1000); // 1000 millisecondi (1 secondo) - intervallo per l'intermittenza

            } else {
                // Se le luci non sono accese o gli abbaglianti sono accesi, fai suonare il clacson una sola volta per 2 secondi
                try {
                    String res = "{\"mode\": \"ON\"}";
                    System.out.println("[!] Sending PUT request (ON) to Horn");
                    coapClient.turnHornOn(address, res);

                    // Dopo 2 secondi, spegni il clacson
                    Timer hornOffTimer = new Timer();
                    hornOffTimer.schedule(new TimerTask() {
                        @Override
                        public void run() {
                            try {
                                System.out.println("[!] Sending PUT request (OFF) to Horn");
                                coapClient.turnHornOff(address, res);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }, 2000); // 2000 millisecondi (2 secondi) - durata del clacson
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}