package worldModelAgent.worldModel.transition;

import serialization.Observation;

/**
 * Created by dockhorn on 17.10.2017.
 */
public class AttributeTransition extends Transition{


    private double x_change;
    private double y_change;

    public double getX_change() {
        return x_change;
    }

    public double getY_change() {
        return y_change;
    }


    public AttributeTransition(Observation before, Observation after){
        this.x_change = after.position.x - before.position.x;
        this.y_change = after.position.y - before.position.y;
    }


    @Override
    public boolean isCorrectTransition(Observation before, Observation after){
        if (before.position.x + this.x_change != after.position.x) return false;
        if (before.position.y + this.y_change != after.position.y) return false;
        // TODO
        return true;
    }
}
