package worldModelAgent;

import serialization.Observation;
import serialization.SerializableStateObservation;
import serialization.Types;

import java.util.Arrays;
import java.util.HashMap;

/**
 * Created by dockhorn on 19.10.2017.
 */
public class GameLogger {

    public GameLogger(){

    }

    public void getSurroundingObject(){
        //Todo implement
    }

    public void getGridData(SerializableStateObservation sso){
        //TODO implement
        //Observation[][][] observationgrid = sso.getObservationGrid();

        sso.toString();
    }

    public void logPlayerSurroundings(int[] playerposition){

    }

    public void logPlayerData(SerializableStateObservation sso){

        boolean is_avatar_alive = sso.isAvatarAlive();
        double[] avatar_position = sso.getAvatarPosition();
        double[] avatar_orientation = sso.getAvatarOrientation();

        Types.ACTIONS avatar_last_action = sso.getAvatarLastAction();

        int avatar_type = sso.getAvatarType();

        int current_health = sso.getAvatarHealthPoints();
        int max_health = sso.getAvatarMaxHealthPoints();
        int limit_health = sso.getAvatarLimitHealthPoints();

        HashMap<Integer, Integer> avatarResources = sso.getAvatarResources();

        System.out.println( "Avatar data:" + "\n"
                + ", is_avatar_alive: " + is_avatar_alive + "\n"
                + ", avatar_position: " + Arrays.toString(avatar_position)
                + ", avatar_grid_position:" + Arrays.toString(util.pixelToGridPosition(avatar_position, sso.getBlockSize()))
                + ", avatar_orientation: " +  Arrays.toString(avatar_orientation)
                + ", avatar_last_actopm: " + avatar_last_action + "\n"
                + ", current_health: " + current_health + "\n"
                + ", max_health: " + max_health + "\n"
                + ", limit_health: " + limit_health + "\n"
                + ", avatar_type: " + avatar_type + "\n"
                + ", avatar_resspources: " + avatarResources);
    }

}
