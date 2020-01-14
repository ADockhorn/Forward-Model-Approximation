package worldModelAgent.worldModel.objectModels;

import serialization.Observation;

/**
 * Created by dockhorn on 16.10.2017.
 */
public class ConstantModel extends ObjectModel {

    @Override
    public Observation simulate(Observation objForSimulation){
        return objForSimulation;
    }
}
