package worldModelAgent.worldModel.objectModels;

import serialization.Observation;

/**
 * Created by dockhorn on 16.10.2017.
 */
public abstract class ObjectModel {
    public Observation object_observation;
    public boolean immovable;
    public float precision;

    /**
     * Advances the object under Observation by one step
     * Todo: add information the simulation should be based on
     */
    public abstract Observation simulate(Observation objForSimulation);
}
