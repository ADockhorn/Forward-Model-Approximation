package worldModelAgent.worldModel.playtrace;

import serialization.SerializableStateObservation;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by dockhorn on 12.10.2017.
 */
public class Playtrace {

    private List<SerializableStateObservation> stateObservations = new LinkedList<SerializableStateObservation>();

    public Playtrace(){

    }

    public void addPlaytrace(SerializableStateObservation sso){
        this.stateObservations.add(sso);
    }

    public void storeToFile(String filename){

    }

    public void storeToFile(File file){

    }
}
