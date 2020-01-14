package gvgai_mcts;

/**
 * Created by dockhorn on 04.05.2018.
 */
public class MCTSPARAMETERS {
    public static int NR_OF_SIMULATIONS = 40;
    public static int SIMULATION_DEPTH = 5;
    public static boolean FAST_FORWARD_PREDICTION = true;
    public static final boolean DECREASE_SCORE_IF_LOST = false;

    public static final boolean MCTS_DEBUG = false;
    public static final float DISCOUNT = 0.5f;

    public static String[][] get_MCTSParameters(){
        return new String[][] {
                {"10", "5", "True"},
                {"20", "5", "True"},
                //{"50", "5", "True"},
                {"10", "10", "True"},
                {"20", "10", "True"},
                //{"50", "10", "True"},
                {"10", "5", "False"},
                {"20", "5", "False"},
                //{"50", "5", "False"},
                {"10", "10", "False"},
                {"20", "10", "False"},
                //{"50", "10", "False"}
        };
    }

}
