package worldModelAgent.worldModel.transition;

import serialization.Observation;

/**
 * Created by dockhorn on 17.10.2017.
 */
public class SpawnTransition extends Transition {
    /*
        Spawns a new Object of a certain type, at the same field as this object
        TODO some objects (like spawnpoints) can be hidden in the observation grid
             as long as it cannot be a TypeChangeTransition it must be a SpawnTransition
     */

    private int iType;


    @Override
    public boolean isCorrectTransition(Observation before, Observation after) {
        return false;
    }

}
