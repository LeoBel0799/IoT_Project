package it.iot.remote.lightsCarManagement;

import org.eclipse.californium.elements.exception.ConnectorException;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

public class PoweringHorn implements Runnable {
    //Faccio accendere il clacson in maniera temporizzata più mantengo accesi gli abbaglianti
    private final static RemoteCarControllerHandler coapClient = RemoteCarControllerHandler.getInstance();
    private static final double MAX_WEAR_LEVEL = 5.0;
    String powerFlag;
    String address;

    public PoweringHorn(String power, String addr) {
        powerFlag = power;
        address = addr;
    }


    public void run() {
        try {
            String status;
            status = coapClient.getLightsOnOff(address);
            if ((status.equals("ON") || status.equals("OFF"))) {
                try {
                    // Accendi gli abbaglianti e il clacson
                    String res = "{\"mode\": \"ON\"}";
                    System.out.println("[!] Sending PUT request (ON) to Brights");
                    coapClient.turnBrightsOn(address, res);
                    System.out.println("[!] Sending PUT request (ON) to Horn");
                    coapClient.turnHornOn(address, res);

                    // Imposta il timer per spegnere gli abbaglianti e il clacson dopo un breve periodo (ad esempio 2 secondi)
                    Timer timer = new Timer();
                    timer.schedule(new TimerTask() {
                        @Override
                        public void run() {
                            try {
                                // Spegni gli abbaglianti e il clacson
                                String res = "{\"mode\": \"OFF\"}";
                                System.out.println("[!] Sending PUT request (OFF) to Brights");
                                coapClient.turnBrightsOff(address, res);
                                System.out.println("[!] Sending PUT request (OFF) to Horn");
                                coapClient.turnHornOff(address, res);

                                // Dopo 2 secondi, accendi solo le luci
                                Timer lightsTimer = new Timer();
                                lightsTimer.schedule(new TimerTask() {
                                    @Override
                                    public void run() {
                                        PoweringLights poweringLights = new PoweringLights("ON", address);
                                        poweringLights.run();
                                    }
                                }, 2000); // 2000 millisecondi (2 secondi) - breve periodo

                            } catch (IOException | ConnectorException e) {
                                e.printStackTrace();
                            }
                        }
                    }, 2000); // 2000 millisecondi (2 secondi) - breve periodo

                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                // Gestisci il caso in cui lo stato delle luci non è valido
            }
        } catch (IOException | ConnectorException e) {
            e.printStackTrace();
        }
    }
}


