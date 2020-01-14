package gvgai_mcts;

import mcts.Move;
import serialization.Types;

/**
 * Created by dockhorn on 24.04.2018.
 */
public class GVGAIMove implements Move{
    Types.ACTIONS action;

    GVGAIMove(Types.ACTIONS action){
        this.action = action;
    }

    @Override
    public int compareTo(Move o) {
        return 0;
    }

    public Types.ACTIONS getAction(){return this.action;}
}
