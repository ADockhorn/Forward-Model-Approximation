package gvgai_mcts;

import kbextraction.MultiAbstractionLevelKB;
import kbextraction.agents.GVGAIBot;
import mcts.Board;
import mcts.CallLocation;
import mcts.Move;
import serialization.SerializableStateObservation;
import serialization.Types;
import java.util.*;

/**
 * Created by dockhorn on 12.04.2018.
 */
public class GVGAIBoard implements Board{

    GVGAIState gamestate;
    float timedif;
    ArrayList<Move> moves = new ArrayList<>(Arrays.asList(new GVGAIMove(Types.ACTIONS.ACTION_DOWN),
            new GVGAIMove(Types.ACTIONS.ACTION_UP),
            new GVGAIMove(Types.ACTIONS.ACTION_LEFT),
            new GVGAIMove(Types.ACTIONS.ACTION_RIGHT)
            ,new GVGAIMove(Types.ACTIONS.ACTION_USE)
    ));

    HashMap<GVGAIBot.KnowledgeType, MultiAbstractionLevelKB> metaKB;
    int startingGameTick;


    public GVGAIBoard(SerializableStateObservation sso, HashMap<GVGAIBot.KnowledgeType, MultiAbstractionLevelKB> metaKB){
        this.metaKB = metaKB;
        this.gamestate = new GVGAIState(sso);
        this.startingGameTick = sso.gameTick;
        this.timedif = 0;

        //only use actions allowed in this game
        ArrayList<Types.ACTIONS> actions = sso.getAvailableActions();
        moves = new ArrayList<>();
        for (int i = 0; i < actions.size(); i++){
            moves.add(new GVGAIMove(actions.get(i)));
        }
    }

    public GVGAIBoard(GVGAIBoard copyOf){
        this.metaKB = copyOf.metaKB;
        this.gamestate = new GVGAIState(copyOf.gamestate);
        this.startingGameTick = copyOf.startingGameTick;
        this.timedif = copyOf.timedif + 1;
    }

    @Override
    public Board duplicate() {
        return new GVGAIBoard(this);
    }

    @Override
    public ArrayList<Move> getMoves(CallLocation location) {
        return moves;
    }

    @Override
    public void makeMove(Move m) {
        Set<String> stateWithAction = gamestate.getPerception();
        //stateWithAction.add("c_Action="+((GVGAIMove)m).action.toString());

        switch (((GVGAIMove)m).action){
            case ACTION_DOWN: stateWithAction.add("c_Action5"); break;
            case ACTION_UP: stateWithAction.add("c_Action4"); break;
            case ACTION_LEFT: stateWithAction.add("c_Action3"); break;
            case ACTION_RIGHT: stateWithAction.add("c_Action2"); break;
            case ACTION_USE: stateWithAction.add("c_Action1"); break;
        }

        if (MCTSPARAMETERS.MCTS_DEBUG) {
            System.out.print("c_Action="+((GVGAIMove)m).action.toString() + " " );
            for (String s : stateWithAction) {
                System.out.print(s + " ");
            }
            System.out.println();
        }

        addInference(
            metaKB.get( GVGAIBot.KnowledgeType.SCORE ).reasoning( stateWithAction ),
            metaKB.get( GVGAIBot.KnowledgeType.MOVE_REL ).reasoning( stateWithAction ),
            metaKB.get( GVGAIBot.KnowledgeType.MOVE_ABS ).reasoning( stateWithAction ),
            metaKB.get( GVGAIBot.KnowledgeType.WIN ).reasoning( stateWithAction )
            );

    }

    public void addInference(List<String> scoreInferences, List<String> moveRelInferences,
                             List<String> moveAbsInferences, List<String> winInferences){

        this.gamestate.gameTick += 1;

        //Position Inference
        for (String inference : moveRelInferences) {
            if (inference.contains("Pos")) {
                // Get predicted delta x and y
                String[] xAndYPos = inference.split("___");
                int deltaX = Integer.parseInt(xAndYPos[0].replace("PosX", ""));
                int deltaY = Integer.parseInt(xAndYPos[1].replace("PosY", ""));

                if (MCTSPARAMETERS.FAST_FORWARD_PREDICTION){
                    this.gamestate.avatarPosition[0] += deltaX * this.gamestate.blockSize;
                    this.gamestate.avatarPosition[1] += deltaY * this.gamestate.blockSize;
                } else {
                    this.gamestate.avatarPosition[0] += deltaX;
                    this.gamestate.avatarPosition[1] += deltaY;
                }

                if (MCTSPARAMETERS.MCTS_DEBUG && (deltaX != 0 || deltaY != 0)){
                    System.out.println("HKB predicts position change: " + deltaX + "," + deltaY);
                }
            }
        }

        //Score Inference
        for (String inference : scoreInferences){
            if( inference.contains( "Score" )  )
            {
                // Get predicted score delta
                String[] score = inference.split( "Score" );
                double deltaScore = Double.parseDouble( score[ 1 ].replace( "_", ".") );
                if (MCTSPARAMETERS.MCTS_DEBUG && deltaScore != 0){
                    System.out.println("HKB predicts score change: " + deltaScore);
                }
                this.gamestate.gameScore += deltaScore* Math.pow(MCTSPARAMETERS.DISCOUNT, timedif);
            }
            if (MCTSPARAMETERS.DECREASE_SCORE_IF_LOST && this.gamestate.gameWinner == Types.WINNER.NO_WINNER){
                this.gamestate.gameScore -= 1;
            }
        }

        //Win Inference
        for (String inference : winInferences){
            if( inference.contains( "GameState" ) && this.gamestate.gameWinner == Types.WINNER.NO_WINNER){
                String[] gameState = inference.split( "GameState" );
                int gameStateValue = Integer.parseInt( gameState[ 1 ] );
                switch (gameStateValue){
                    case 0: this.gamestate.gameWinner = Types.WINNER.NO_WINNER;
                    case 1: this.gamestate.gameWinner = Types.WINNER.PLAYER_WINS;
                    case 2: this.gamestate.gameWinner = Types.WINNER.PLAYER_LOSES;
                }

                if (MCTSPARAMETERS.MCTS_DEBUG && gameStateValue != 0){
                    System.out.println("HKB predicts game winner: " + this.gamestate.gameWinner);
                }

            }
        }
    }

    @Override
    public boolean gameOver() {
        return gamestate.isGameOver || this.gamestate.gameTick - startingGameTick  > MCTSPARAMETERS.SIMULATION_DEPTH;
    }

    @Override
    public int getCurrentPlayer() {
        return 0;
    }

    @Override
    public int getQuantityOfPlayers() {
        return 1;
    }

    @Override
    public double[] getScore() {
        return new double[]{gamestate.gameScore};
    }

    @Override
    public double[] getMoveWeights() {
        return null;
    }

    @Override
    public void bPrint() {
        gamestate.print();
    }
}
