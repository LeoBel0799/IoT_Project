package it.iot.remote.user;

import it.iot.remote.lightsCarManagement.carcontroller.PoweringHorn;
import it.iot.remote.lightsCarManagement.carcontroller.PoweringIndicators;
import it.iot.remote.lightsCarManagement.carcontroller.PoweringLights;
import it.iot.remote.lightsCarManagement.observations.Observer;
import org.eclipse.californium.core.CoapResource;
import org.eclipse.californium.core.server.resources.CoapExchange;

import java.util.Scanner;

public class UserMenu implements Runnable {

        private Scanner input;
        private Scanner optionInput; // Scanner per le scelte ON/OFF
        String ip = "127.0.0.1:5683";
        Observer observer = new Observer();

        public UserMenu() {
            input = new Scanner(System.in);
            optionInput = new Scanner(System.in);
        }

        public void run() {
            menu();
        }

        public void menu() {
            int choice;
            do {
                System.out.println("+++++++++++++++++++++++++++++++++");
                System.out.println("         Car Controller  (User)  ");
                System.out.println("+++++++++++++++++++++++++++++++++");
                System.out.println("(1) Exit controller (Press 0 to Exit)");
                System.out.println("(2) Turn Lights On");
                System.out.println("(3) Turn Indicators On");
                System.out.println("(4) Turn Horn On");

                choice = input.nextInt();
                switch (choice) {

                    case 1:
                        // lights
                        System.out.print("Turn ON or turn OFF lights (ON/OFF)? ");
                        String lightChoice = optionInput.next();
                        PoweringLights lights = new PoweringLights(lightChoice, ip);
                        Thread threadLuci = new Thread(lights);
                        threadLuci.start();
                        try {
                            threadLuci.join();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        observer.onLightsObserver(ip);
                        break;

                    case 2:
                        // indicators
                        System.out.print("Turn ON indicators (1)? ");
                        Integer indicator = optionInput.nextInt();
                        PoweringIndicators indicators = new PoweringIndicators(indicator, ip);
                        Thread threadFrecce = new Thread(indicators);
                        threadFrecce.start();

                        try {
                            threadFrecce.join();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        observer.brightsObserver(ip);
                        break;

                    case 3:
                        // horn
                        System.out.print("Turn ON or turn OFF Horn (ON/OFF)? ");
                        String horn = optionInput.next();
                        PoweringHorn clacson = new PoweringHorn(horn, ip);
                        Thread threadClacson = new Thread(clacson);
                        threadClacson.start();

                        try {
                            threadClacson.join();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        observer.brightsObserver(ip);
                        break;
                }
            } while (choice != 0);

            // Chiudi gli Scanner alla fine del programma
            input.close();
            optionInput.close();
        }
    }