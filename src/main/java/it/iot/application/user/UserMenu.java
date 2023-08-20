package it.iot.application.user;

import it.iot.application.DB.ActuatorStatus;
import it.iot.application.DB.LightData;
import it.iot.application.DB.NodeData;
import it.iot.application.controller.PoweringBrights;
import it.iot.application.controller.PoweringLights;
import org.eclipse.californium.elements.exception.ConnectorException;

import java.io.IOException;
import java.net.SocketException;
import java.sql.SQLException;
import java.util.List;
import java.util.Scanner;
import java.util.Timer;
import java.util.TimerTask;

public class UserMenu implements Runnable {


    final int OPTION_TURN_ON_LIGHT = 2;
    final int OPTION_TURN_ON_BRIGHT = 3;
    final int SHOW_MOTION = 4;
    final int SHOW_NODES = 5;
    final int SHOW_ACTUATORS = 6;
    private Scanner input;
    static LightData lightData;
    static NodeData nodeData;
    static ActuatorStatus actuatorStatus;

    static {
        try {
            lightData = new LightData();
            nodeData = new NodeData();
            actuatorStatus = new ActuatorStatus();
        } catch (ConnectorException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        input = new Scanner(System.in);
        try {
            actuatorStatus.createDelete("actuator");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        try {
            menu();
        } catch (ConnectorException | InterruptedException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void menu() throws ConnectorException, IOException, InterruptedException {
        boolean shouldExit = false;
        Scanner input = new Scanner(System.in);
        while (!shouldExit){
            System.out.println("+++++++++++++++++++++++++++++++++");
            System.out.println("         Car Controller  (User)  ");
            System.out.println("+++++++++++++++++++++++++++++++++");
            System.out.println("(1) Exit controller (Press 0 to Exit)");
            System.out.println("(2) Handle Lights");
            System.out.println("(3) Handle Brights");
            System.out.println("(4) View Node records");
            System.out.println("(5) View Actuator records");
            System.out.println("(6) View Light status records");
            System.out.println("Insert a command: ");
            int choice = input.nextInt();

            switch (choice) {
                case OPTION_TURN_ON_LIGHT:
                    int lightId = askForLightId();
                    PoweringLights lights = new PoweringLights(lightId, lightData, nodeData, actuatorStatus);
                    final Thread thlights = new Thread(lights);
                    thlights.start();

                    // Qua provo a killare e fare restart del thread. i dati sono vecchi (dati sono counter e wearLevel vedi nella
                    // classe PoweringLights) perchè il thread una volta avviato
                    //usa sempre quelli quindi se viene killato ogni tot secondi dovrebbe prendere quello più nuovo. Comunque
                    //il codice sotto non funziona.
                    //TODO: Nel caso in cui non vadano altri tentativi, capire e testare se la classe PoweringLights può essere fattasenza thread (con la chiamata secca dovrebbe prendere sempre il più aggiornato al momento della chiamata.
                    //Per quanto rigurda la PUT, arriva a farla ma fallisce. Il caso di bright l'ho commentato perchè non funzionerebbe,
                    //TODO: bisogna applicare le stesse mofiche fatte al costruttore di Powering Light anche per PoweringBrights.
                    //TODO: capire se dobbiamo avere 2 o 4 attuatori, perchè i sensori sono 4 in mqtt quindi sarebbe ottimale avrebbe 4 anche in coap dato che per ogni attuaotre spengo e accendo una luce, sui sensori fisici non dovrebbe essere un problems perhcè penso se la gestiscano in automatico.
                    //TODO: vedere se i metodi di visualizzazione dei dati nel menu funzionano
                    //TODO: capire e implementare come fare un observer sul counter di ogni sensore così chd ad un tot di counter raggiunti
                            //per ogni sensore azzera il wearLevel e resetta l'attuatore
                    Timer timer = new Timer();
                    timer.schedule(new TimerTask() {
                        @Override
                        public void run() {
                            thlights.interrupt();
                        }
                    }, 5000); // 10 secondi

                    try {
                        thlights.join();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    break;


                case OPTION_TURN_ON_BRIGHT:
/*                    // brights
                    int brightId = askForLightId();
                    PoweringBrights brights = new PoweringBrights(brightId, ip);
                    Thread thBrights = new Thread(brights);
                    thBrights.start();
                    try {
                        thBrights.join();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    break;*/

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
            System.out.print("Insert light id (1-2): ");
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
        return id >= 1 && id <=2;
    }

}