package bfs;

import gvgai_mcts.GVGAIState;
import serialization.SerializableStateObservation;
import serialization.Types;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by dockhorn on 08.05.2018.
 */
public class BFSNode {

    public GVGAIState gamestate;
    public BFSNode parent = null;
    public Types.ACTIONS action = null;
    public ArrayList<BFSNode> children;


    public BFSNode(SerializableStateObservation sso){
        this.gamestate = new GVGAIState(sso);
    }

    public BFSNode(BFSNode parent){
        this.parent = parent;
        this.gamestate = new GVGAIState(parent.gamestate);

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

                if (BFSPARAMETERS.FAST_FORWARD_PREDICTION){
                    this.gamestate.avatarPosition[0] += deltaX * this.gamestate.blockSize;
                    this.gamestate.avatarPosition[1] += deltaY * this.gamestate.blockSize;
                } else {
                    this.gamestate.avatarPosition[0] += deltaX;
                    this.gamestate.avatarPosition[1] += deltaY;
                }

                if (BFSPARAMETERS.BFS_DEBUG && (deltaX != 0 || deltaY != 0)){
                    System.out.println("HKB predicts position change: " + deltaX + "," + deltaY);
                }
            }
        }



        //Win Inference
        for (String inference : winInferences){
            if( inference.contains( "GameState" ) && this.gamestate.gameWinner == Types.WINNER.NO_WINNER){
                String[] gameState = inference.split( "GameState" );
                int gameStateValue = Integer.parseInt( gameState[ 1 ] );
                switch (gameStateValue){
                    case 0: this.gamestate.gameWinner = Types.WINNER.NO_WINNER; break;
                    case 1: this.gamestate.gameWinner = Types.WINNER.PLAYER_WINS; break;
                    case 2: this.gamestate.gameWinner = Types.WINNER.PLAYER_LOSES; break;
                    default : this.gamestate.gameWinner = Types.WINNER.NO_WINNER; break;
                }

                if (BFSPARAMETERS.BFS_DEBUG && gameStateValue != 0){
                    System.out.println("HKB predicts game winner: " + this.gamestate.gameWinner);
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
                if (BFSPARAMETERS.BFS_DEBUG  && deltaScore != 0){
                    System.out.println("HKB predicts score change: " + deltaScore);
                }
                this.gamestate.gameScore += deltaScore;
            }
            if (BFSPARAMETERS.DECREASE_SCORE_IF_LOST && this.gamestate.gameWinner != Types.WINNER.NO_WINNER){
                this.gamestate.gameScore -= 1;
            }
        }
    }



}
