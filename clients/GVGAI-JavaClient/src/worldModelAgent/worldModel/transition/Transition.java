package worldModelAgent.worldModel.transition;

import serialization.Observation;

/**
 * Created by dockhorn on 17.10.2017.
 */
public abstract class Transition {

    public abstract boolean isCorrectTransition(Observation before, Observation after);
}
