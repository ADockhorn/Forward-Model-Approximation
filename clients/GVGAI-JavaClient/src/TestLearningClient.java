import bfs.BFSPARAMETERS;
import gvgai_mcts.MCTSPARAMETERS;
import utils.ClientComm;
import utils.CompetitionParameters;
import utils.ElapsedWallTimer;
import worldModelAgent.PlaytraceAgent;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by dperez on 01/06/2017.
 */
public class TestLearningClient
{
    public static void main(String[] args)
    {
        assert (CompetitionParameters.USE_SOCKETS);
        /** Init params */
        //aliens = 0
        //boulderdash = 11
        //butterflies = 13
        //chase = 18
        //frogs = 39
        //missile command = 55
        //portal = 63
        //sokoban = 75
        //survive zombies = 79
        //zelda = 90
        Integer game = 39;
        int[] games = new int[] {0, 11, 13, 18, 39, 55, 63, 75, 79, 90};
        PrintStream console = System.out;

        System.out.println("evaluate game: " + game.toString());
        for (PlaytraceAgent.ReasoningMode mode : PlaytraceAgent.ReasoningMode.values()) {
            PlaytraceAgent.reasoningMode = mode;
            if (mode == PlaytraceAgent.reasoningMode.BFS){
                for (String[] agent_args : BFSPARAMETERS.get_BFSParameters())

                {
                    try {
                        int expansions = Integer.parseInt(agent_args[0]);
                        boolean fast_forward =  agent_args[1].equals("True");
                        boolean pruning =  agent_args[2].equals("True");

                        BFSPARAMETERS.MAXEXPANSIONS = expansions;
                        BFSPARAMETERS.FAST_FORWARD_PREDICTION = fast_forward;
                        BFSPARAMETERS.PRUNING = pruning;

                        File tempFile = new File(String.format("results/Game_%d_BFS_%d_%b_%b.txt", game, expansions, fast_forward, pruning));
                        boolean exists = tempFile.exists();
                        if (exists)
                            continue;

                        PrintStream o = new PrintStream(new File(String.format("results/Game_%d_BFS_%d_%b_%b.txt", game, expansions, fast_forward, pruning)));
                        System.setOut(o);
                        System.out.println(expansions);
                        System.out.println(fast_forward);
                        System.out.println(pruning);

                        run_test(args, game);
                        o.close();
                        java.awt.Toolkit.getDefaultToolkit().beep();
                        return;
                    } catch (Exception e){
                        System.setOut(console);
                        System.out.println(e.toString());
                    }
                }
            } else {
                for (String[] agent_args : MCTSPARAMETERS.get_MCTSParameters())

                {
                    try {
                        int simulations = Integer.parseInt(agent_args[0]);
                        int simulation_depth = Integer.parseInt(agent_args[1]);
                        boolean fast_forward =  agent_args[2].equals("True");

                        MCTSPARAMETERS.NR_OF_SIMULATIONS = simulations;
                        MCTSPARAMETERS.SIMULATION_DEPTH = simulation_depth;
                        MCTSPARAMETERS.FAST_FORWARD_PREDICTION = fast_forward;

                        File tempFile = new File(String.format("results/Game_%d_MCTS_%d_%s_%b.txt", game, simulations, simulation_depth, fast_forward));
                        boolean exists = tempFile.exists();
                        if (exists)
                            continue;

                        PrintStream o = new PrintStream(new File(String.format("results/Game_%d_MCTS_%d_%s_%b.txt", game, simulations, simulation_depth, fast_forward)));
                        System.setOut(o);
                        System.out.println(simulations);
                        System.out.println(simulation_depth);
                        System.out.println(fast_forward);

                        run_test(args, game);
                        o.close();
                        java.awt.Toolkit.getDefaultToolkit().beep();
                        return;

                    } catch (Exception e){
                        System.setOut(console);
                        System.out.println(e.toString());
                    }
                }
            }
        }
        java.awt.Toolkit.getDefaultToolkit().beep();
    }

    public static void run_test(String[] args, int gameId) {

        String shDir = "./src/utils";
        String serverDir;
        String serverJar = "";
        String gameFile = "";
        String levelFile = "";
        if (CompetitionParameters.OS_WIN) {
            serverDir = "..\\..";
        } else {
            serverDir = "../..";
        }

        String agentName = "worldModelAgent.PlaytraceAgent";         //Agent to play with
        boolean visuals = false;                                    //Game visualization

        /** Get arguments */
        Map<String, List<String>> params = new HashMap<>();
        List<String> options = null;
        for (int i = 0; i < args.length; i++) {
            final String a = args[i];
            if (a.charAt(0) == '-') {
                if (a.length() < 2) {
                    System.err.println("Error at argument " + a);
                    return;
                }
                options = new ArrayList<>();
                params.put(a.substring(1), options);
            } else if (options != null) {
                options.add(a);
            } else {
                System.err.println("Illegal parameter usage");
                return;
            }
        }
        /** Update params */
        if (params.containsKey("gameId")) {
            gameId = Integer.parseInt(params.get("gameId").get(0));
        }
        if (params.containsKey("shDir")) {
            shDir = params.get("shDir").get(0);
        }
        if (params.containsKey("serverDir")) {
            serverDir = params.get("serverDir").get(0);
        }
        if (params.containsKey("agentName")) {
            agentName = params.get("agentName").get(0);
        }
        if (params.containsKey("visuals")) {
            visuals = true;
        }
        if (params.containsKey("serverJar")) {
            serverJar = params.get("serverJar").get(0);
        }
        if (params.containsKey("gameFile")) {
            gameFile = params.get("gameFile").get(0);
        }
        if (params.containsKey("levelFile")) {
            levelFile = params.get("levelFile").get(0);
        }
        ElapsedWallTimer wallClock = new ElapsedWallTimer();

        //Available controllers:
        String scriptFile;
        String[] cmd;
        if (serverJar == "") {
            if (CompetitionParameters.OS_WIN) {
                scriptFile = shDir + "\\runServer_nocompile.bat";
            } else {
                scriptFile = shDir + "/runServer_nocompile.sh";
            }
            if (visuals) {
                cmd = new String[]{scriptFile, gameId + "", serverDir, "true"};
            } else {
                cmd = new String[]{scriptFile, gameId + "", serverDir, "false"};
            }
        } else {
            scriptFile = shDir + "/runServer_compile.sh";
            //            cmd = new String[]{scriptFile, serverJar, gameId + "", serverDir};
            cmd = new String[]{scriptFile, serverJar, gameId + "", serverDir, gameFile, levelFile};
        }

        //Start the server side of the communication.
        try {
            ProcessBuilder builder = new ProcessBuilder(cmd);
            builder.redirectErrorStream(true);
            Process p = builder.start();
            System.out.println("Server process started [OK]");
            System.out.println("Agent name:" + agentName);

            //Start the client side of the communication
            ClientComm ccomm = new ClientComm(agentName);
            ccomm.startComm();
            //Report total time spent.
            int minutes = (int) wallClock.elapsedMinutes();
            int seconds = ((int) wallClock.elapsedSeconds()) % 60;
            System.out.printf("\n \t --> Real execution time: %d minutes, %d seconds of wall time.\n", minutes, seconds);
            p.destroy();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
