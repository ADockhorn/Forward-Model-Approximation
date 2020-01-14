package bfs;

/**
 * Created by dockhorn on 08.05.2018.
 */
public class BFSPARAMETERS {
    public static int MAXEXPANSIONS = 100;
    public static boolean FAST_FORWARD_PREDICTION = false;
    public static boolean PRUNING = true;
    public static final boolean DECREASE_SCORE_IF_LOST = false;

    public static final boolean BFS_DEBUG = false;

    public static String[][] get_BFSParameters(){
        return new String[][] {
                {"100", "True", "True"},
                {"200", "True", "True"},
                //{"500", "True", "True"},
                {"100", "True", "False"},
                {"200", "True", "False"},
                //{"500", "True", "False"},
                {"100", "False", "True"},
                {"200", "False", "True"},
                //{"500", "False", "True"},
                {"100", "False", "False"},
                {"200", "False", "False"},
                //{"500", "False", "False"},
        };
    }
}