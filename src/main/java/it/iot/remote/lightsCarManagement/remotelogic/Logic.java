package it.iot.remote.lightsCarManagement.remotelogic;

import it.iot.application.DB.DB;
import it.iot.remote.lightsCarManagement.carcontroller.RemoteCarControllerHandler;
import it.iot.remote.lightsCarManagement.observations.Observer;

public class Logic {
    Observer observer;
    RemoteCarControllerHandler coapHandler;
    DB db;

    public Logic(){
        observer = new Observer();
        coapHandler = RemoteCarControllerHandler.getInstance();
        db = new DB();
    }
}
