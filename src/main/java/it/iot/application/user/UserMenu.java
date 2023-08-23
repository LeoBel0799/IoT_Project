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
import java.util.*;

public class UserMenu implements Runnable {

    final int EXIT = 1;
    final int OPTION_TURN_ON_LIGHT = 2;
    final int OPTION_TURN_ON_BRIGHT = 3;
    final int SHOW_MOTION = 4;
    final int SHOW_NODES = 5;
    final int SHOW_ACTUATORS = 6;
    private static Scanner input;
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
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }
    public void menu() throws ConnectorException, IOException, InterruptedException, SQLException {
        boolean shouldExit = false;
        Scanner input = new Scanner(System.in);
        while (!shouldExit){
            System.out.println("+++++++++++++++++++++++++++++++++");
            System.out.println("         Car Controller  (User)  ");
            System.out.println("+++++++++++++++++++++++++++++++++");
            System.out.println("(1) Exit controller");
            System.out.println("(2) Handle Lights");
            System.out.println("(3) Handle Brights");
            System.out.println("(4) View Node records");
            System.out.println("(5) View Actuator records");
            System.out.println("(6) View Light status records");
            System.out.println("Insert a command: ");
            int choice = input.nextInt();

            switch (choice) {
                case OPTION_TURN_ON_LIGHT:
                    /*TODO: QUALSIASI SIA IL COMANDO NELLA PUT. DA COAP ARRIVA SEMPRE OFF.
                            PROBOBABILE NON CORRETTA LETTURA DOVUTO ALLA GET ISTANTANEA DOPO LA PUT
                            NEL LIGHT HANDLER.C STO PROVANDO AD INSERIRE UN DELAY APPOSITO PER RITARDARE LA LETTURA
                            I LED NON SI ACCENDONO NELLA PUT + TESTARE IL FUNZIONAMENTO DI QUANDO SI VA NEL CASO DELLA LUCE FULMINATA
                            QUINDI VUOL DIRE TESTARE LA RES POST HANLDER E SE POI VIENE ATTIVATO IL THREAD PER IL RESET TRAMITE BOTTONE
                    */

                    int lightId = askForLightId();
                    PoweringLights lights = new PoweringLights(lightId, lightData, nodeData, actuatorStatus);
                    lights.setLight();
                    /*
                    SPIEGAZIONE DI COME è STATO IMPLMENTATO OBS, CON RELATIVO CAMBIO AUTOMATICO DEI VALORI SOTTO PRESSIONE DI UN BOTTONE
                    Lato Java:
                    Il codice Java effettua una richiesta GET CoAP al nodo C per ottenere i valori correnti di wearLevel e fulminated. Questi vengono ritornati in una stringa separata da virgola.
                    Java elabora la risposta splittandola e convertendo wearLevel in float e fulminated in boolean.
                    In base ai valori ottenuti, il codice decide se accendere/spegnere la luce e se resettare i valori.
                    Dopo aver inviato il comando di accensione/spegnimento tramite una richiesta PUT CoAP, il codice rilegge lo stato corrente della luce con una nuova richiesta GET.
                    I nuovi valori di stato, wearLevel e fulminated vengono salvati nel database tramite chiamate JDBC.
                    Infine il codice invia i nuovi valori di wearLevel e fulminated come stringa al nodo C tramite una richiesta POST CoAP.
                    Lato C:

                    All'avvio, il nodo C espone una risorsa CoAP osservabile per wearLevel e fulminated.
                    Alla richiesta GET Java, il gestore della risorsa crea la stringa di risposta con i valori correnti e la ritorna.
                    Alla ricezione della richiesta POST Java, il gestore aggiorna le variabili globali wearLevel e fulminated.
                    Alle successive richieste GET, i valori aggiornati verranno letti dalle variabili globali e ritornati a Java.
                    Se viene premuto il pulsante, wearLevel e fulminated vengono resettati e la risorsa notifica Java tramite una osservazione CoAP.
                    In sintesi, Java e C si scambiano i dati su wearLevel e fulminated tramite richieste CoAP. Java decide le azioni da intraprendere e salva lo storico nel DB. C mantiene i valori correnti in memoria e li notifica a Java quando cambiano.
                    */
                    //Per capire i dati se arrivano e come arrivano apriti una finestra con sql dove fai select* su tutte e tre le tabelle ogni tot di secondi cos' vedi gli inserimenti
                    break;


                case OPTION_TURN_ON_BRIGHT:
                    //TODO: TESTARE BRIGHT
                   // brights
                    int brightId = askForLightId();
                    PoweringBrights brights = new PoweringBrights(brightId, actuatorStatus,  nodeData );
                    brights.setBright(brightId);
                    break;

                case SHOW_MOTION:
                    //TODO: TESTARE
                    List<String> motions = lightData.selectAllMotion();
                    for(String row : motions) {
                        System.out.println(row);
                    }
                    break;

                case SHOW_ACTUATORS:
                    //TODO: TESTARE
                    List<String> actuators = actuatorStatus.selectAllActuatorStatus();
                    for (String row: actuators){
                        System.out.println(row);
                    }
                    break;

                case SHOW_NODES:
                    //TODO: TESTARE
                    List<String> nodes = nodeData.selectAllNode();
                    for (String row: nodes){
                        System.out.println(row);
                    }
                    break;

                case EXIT:
                    //VERIFICATO FUNGE
                    System.out.println("BYE");
                    shouldExit = true;
                    System.exit(0);
                    break;


                default:
                    System.out.println("Invalid option");
                    input.nextLine();//svoto buffer
            }
        }
    }




    public static int askForLightId() {

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

    static boolean isValidLightId(int id) {
        return id >= 1 && id <=2;
    }

}