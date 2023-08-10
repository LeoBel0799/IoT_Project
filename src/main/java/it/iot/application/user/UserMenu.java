package it.iot.application.user;

import it.iot.application.controller.PoweringBrights;
import it.iot.application.controller.PoweringLights;

import java.util.Scanner;

public class UserMenu implements Runnable {
        final int OPTION_TURN_ON_LIGHT = 2;
        final int OPTION_TURN_ON_BRIGHT = 3;
        private Scanner input;
        String ip = "127.0.0.1:5683";

        public UserMenu() {
            Scanner input = new Scanner(System.in);
        }

        public void run() {
            menu();
        }

        public void menu() {
            boolean shouldExit = false;

            while (!shouldExit){
                System.out.println("+++++++++++++++++++++++++++++++++");
                System.out.println("         Car Controller  (User)  ");
                System.out.println("+++++++++++++++++++++++++++++++++");
                System.out.println("(1) Exit controller (Press 0 to Exit)");
                System.out.println("(2) Turn Lights On");
                System.out.println("(3) Turn Brights On");

                int choice = input.nextInt();
                switch (choice) {

                    case OPTION_TURN_ON_LIGHT:
                        int lightId = askForLightId();
                        PoweringLights lights = new PoweringLights(lightId, ip);
                        Thread thlights = new Thread(lights);
                        try {
                            thlights.join();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        break;


                    case OPTION_TURN_ON_BRIGHT:
                        // brights
                        int brightId = askForLightId();
                        PoweringBrights brights = new PoweringBrights(brightId, ip);
                        Thread thBrights = new Thread(brights);
                        try {
                            thBrights.join();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        break;


                    case 1:
                        shouldExit = true;
                        break;


                    default:
                        System.out.println("Invalid option");
                }
            }
        }



    int askForLightId() {

        int id;

        while(true) {
            System.out.print("Insert light id (1-4): ");
            id = input.nextInt();

            if(isValidLightId(id)) {
                break;
            } else {
                System.out.println("Invalid light id. Please retry.");
            }
        }

        return id;

    }

    boolean isValidLightId(int id) {
        return id >= 1 && id <=4;
    }

}