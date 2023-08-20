package it.iot.application.user;

import it.iot.application.DB.ActuatorStatus;
import it.iot.application.DB.LightData;
import it.iot.application.DB.NodeData;
import it.iot.application.controller.PoweringBrights;
import it.iot.application.controller.PoweringLights;

import java.io.IOException;
import java.net.SocketException;
import java.util.List;
import java.util.Scanner;

public class UserMenu implements Runnable {


    final int OPTION_TURN_ON_LIGHT = 2;
    final int OPTION_TURN_ON_BRIGHT = 3;
    final int SHOW_MOTION = 4;
    final int SHOW_NODES = 5;
    final int SHOW_ACTUATORS = 6;
    private Scanner input;
    String ip = "127.0.0.1:5683";
    LightData lightData;
    NodeData nodeData;
    ActuatorStatus actuatorStatus;



    @Override
    public void run() {
        menu();
    }
    public void menu() {
        boolean shouldExit = false;
        Scanner input = new Scanner(System.in);
        while (!shouldExit){
            System.out.println("+++++++++++++++++++++++++++++++++");
            System.out.println("         Car Controller  (User)  ");
            System.out.println("+++++++++++++++++++++++++++++++++");
            System.out.println("(1) Exit controller (Press 0 to Exit)");
            System.out.println("(2) Turn Lights On");
            System.out.println("(3) Turn Brights On");
            System.out.println("(4) View Node records");
            System.out.println("(5) View Actuator records");
            System.out.println("(6) View Light status records");
            System.out.println("Insert a command: ");
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

                case SHOW_MOTION:
                    List<String> motions = lightData.selectAllMotion();
                    for(String row : motions) {
                        System.out.println(row);
                    }
                    break;

                case SHOW_ACTUATORS:
                    List<String> actuators = actuatorStatus.selectAllActuatorStatus();
                    for (String row: actuators){
                        System.out.println(row);
                    }
                    break;

                case SHOW_NODES:
                    List<String> nodes = nodeData.selectAllNode();
                    for (String row: nodes){
                        System.out.println(row);
                    }
                    break;

                case 1:
                    shouldExit = true;
                    break;


                default:
                    System.out.println("Invalid option");
                    input.nextLine();//svoto buffer
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