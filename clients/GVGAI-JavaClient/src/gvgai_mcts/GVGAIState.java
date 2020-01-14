package gvgai_mcts;

import serialization.Observation;
import serialization.SerializableStateObservation;
import serialization.Types;

import java.util.*;

/**
 * Created by dockhorn on 24.04.2018.
 */
public class GVGAIState {

    /**
     * Game Phase
     */
    public float gameScore;
    public int gameTick;
    public Types.WINNER gameWinner;
    public boolean isGameOver;
    public double[] worldDimension;
    public int blockSize;

    /**
     * Avatar Phase
     */
    public double[] avatarOrientation;
    public double[] avatarPosition;

    /**
     * Observations of the world.
     */
    public Observation[][][] observationGrid;

    public GVGAIState(GVGAIState state) {
        gameScore = state.gameScore;
        gameTick = state.gameTick;
        gameWinner = state.gameWinner;
        worldDimension = state.worldDimension;
        blockSize = state.blockSize;

        avatarOrientation = state.avatarOrientation.clone();
        avatarPosition = state.avatarPosition.clone();
        //avatarLastAction;
        observationGrid = state.observationGrid.clone();
    }

    public GVGAIState(SerializableStateObservation sso) {
        gameScore = sso.gameScore;
        gameTick = sso.gameTick;
        gameWinner = sso.gameWinner;
        worldDimension = sso.worldDimension.clone();
        blockSize = sso.blockSize;

        avatarOrientation = sso.avatarOrientation.clone();
        avatarPosition = sso.avatarPosition.clone();
        //avatarLastAction;
        observationGrid = sso.observationGrid.clone();
    }

    public void print(){
        System.out.println("GVGAI-State");
        for (String perception : getPerception()){
            System.out.println(perception);
        }
    }

    public Set<String> getPerception(){

        Set<String> perceptionSet = new HashSet<>();
        perceptionSet.add("c_Tick"+this.gameTick);

        int x = (int) (avatarPosition[0]);
        int y = (int) (avatarPosition[1]);
        perceptionSet.add("c_PosX"+x);
        perceptionSet.add("c_PosY"+y);

        String orientation = "c_Orientation" + orientationToString(avatarOrientation);
        orientation = orientation.replace( ".", "_" );
        orientation = orientation.replace( "/", "__" );  // Depending on the version, orientation may use "/" or "," as separator
        orientation = orientation.replace( ",", "__" );
        perceptionSet.add(orientation);

        perceptionSet.add("c_Score" + String.valueOf(gameScore));
        String winner = "c_GameState"+this.gameWinner.toString();
        winner = winner.replace( "NO_WINNER", "0" );
        winner = winner.replace( "PLAYER_WINS", "1" );
        winner = winner.replace( "PLAYER_LOSES", "2" );
        perceptionSet.add(winner);

        this.saveSurroundingObservations(perceptionSet);

        return perceptionSet;
    }


    public static String orientationToString(double[] orientation){
        if (orientation[0] == 0 && orientation[1] == 1)
            return "Up";
        if (orientation[0] == 0 && orientation[1] == -1)
            return "Down";
        if (orientation[0] == 1 && orientation[1] == 0)
            return "Right";
        if (orientation[0] == -1 && orientation[1] == 0)
            return "Left";

        return "" + orientation[0] + "__" + orientation[1];
    }

    private void saveSurroundingObservations(Set<String> perceptionSet)
    {
        double[] pos = this.avatarPosition;
        int x = (int) (pos[0]/this.blockSize);
        int y = (int) (pos[1]/this.blockSize);


        Observation[][][] obs = this.observationGrid;

        if (x < 0 || x >= obs.length || y < 0 || y >= obs[0].length)
            return;

        //UP
        if(y - 1 >= 0)
        {
            for(Observation ob : obs[x][y-1])
            {
                perceptionSet.add("c_AboveOfP" + ob.itype);
            }
        } else perceptionSet.add( "c_AboveOfP999" );


        //UPLeft
        if(y - 1 >= 0 && x -1 >= 0 )
        {
            for(Observation ob : obs[x-1][y-1])
            {
                perceptionSet.add("c_AboveLeftOfP" + ob.itype);
            }
        }
        //UPRight
        if(y - 1 >= 0 && x +1 < obs.length )
        {
            for(Observation ob : obs[x+1][y-1])
            {
                perceptionSet.add("c_AboveRightOfP" + ob.itype);
            }
        }

        //Down
        if(y + 1 < obs[x].length)
        {
            for(Observation ob : obs[x][y+1])
            {
                perceptionSet.add("c_BelowOfP" + ob.itype);
            }
        } else perceptionSet.add( "c_BelowOfP999" );

        //BelowLeft
        if(y+1 < obs[x].length && x-1 >= 0 )
        {
            for(Observation ob : obs[x-1][y+1])
            {
                perceptionSet.add("c_BelowLeftOfP" + ob.itype);
            }
        }

        //BelowRight
        if(y+1 < obs[x].length && x+1 < obs.length )
        {
            for(Observation ob : obs[x+1][y+1])
            {
                perceptionSet.add("c_BelowRightOfP" + ob.itype);
            }
        }

        //Left
        if(x - 1 >= 0)
        {
            for(Observation ob : obs[x-1][y])
            {
                perceptionSet.add("c_LeftOfP" + ob.itype);
            }
        } else perceptionSet.add( "c_LeftOfP999" );

        //Right
        if(x + 1 < obs.length)
        {
            for(Observation ob : obs[x+1][y])
            {
                perceptionSet.add("c_RightOfP" + ob.itype);
            }
        } perceptionSet.add( "c_RightOfP999" );

        //Same
        for(Observation ob : obs[x][y])
        {
            perceptionSet.add("c_SameAsP" + ob.itype);
        }

    }

}
