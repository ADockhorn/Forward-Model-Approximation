package worldModelAgent;

import serialization.Observation;
import serialization.SerializableStateObservation;
import serialization.Vector2d;

/**
 * Created by dockhorn on 16.10.2017.
 */
public class util {

    public static int[] pixelToGridPosition(double[] position, SerializableStateObservation sso)
    {
        int[] grid_position = new int[2];
        grid_position[0] = (int) (position[0] / sso.getBlockSize());
        grid_position[1] = (int) (position[1] / sso.getBlockSize());
        return grid_position;
    }

    public static int[] pixelToGridPosition(Vector2d position, SerializableStateObservation sso)
    {
        int[] grid_position = new int[2];
        grid_position[0] = (int) (position.x / sso.getBlockSize());
        grid_position[1] = (int) (position.y / sso.getBlockSize());
        return grid_position;
    }

    public static int[] pixelToGridPosition(double[] position, int gridsize)
    {
        int[] grid_position = new int[2];
        grid_position[0] = (int) (position[0] / gridsize);
        grid_position[1] = (int) (position[1] / gridsize);
        return grid_position;
    }

    public static int[] pixelToGridPosition(Vector2d position, int gridsize)
    {
        int[] grid_position = new int[2];
        grid_position[0] = (int) (position.x / gridsize);
        grid_position[1] = (int) (position.y / gridsize);
        return grid_position;
    }


    /**
     *
     * @param observation Observation to be copied
     * @return copy of the Observation with obsID set to -1
     */
    public static Observation copyObservation(Observation observation){
        Observation new_observation = new Observation();
        new_observation.position = observation.position;
        new_observation.itype = observation.itype;
        new_observation.reference = observation.reference;
        new_observation.category = observation.category;
        new_observation.sqDist = observation.sqDist;
        new_observation.obsID = -1;
        return new_observation;
    }
}
