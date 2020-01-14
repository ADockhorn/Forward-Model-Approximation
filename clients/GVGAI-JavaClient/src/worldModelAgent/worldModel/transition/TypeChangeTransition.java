package worldModelAgent.worldModel.transition;

import serialization.Observation;

/**
 * Created by dockhorn on 17.10.2017.
 */
public class TypeChangeTransition extends Transition {

    private int oldIType;
    private int newIType;

    public TypeChangeTransition(Observation before, Observation after){
        this.oldIType = before.getItype();
        this.newIType = after.getItype();
    }

    @Override
    public boolean isCorrectTransition(Observation before, Observation after) {
        if (this.oldIType == before.getItype() && this.newIType == after.getItype()) return true;
        return false;
    }
}
