package worldModelAgent.worldModel;

import serialization.Observation;
import serialization.SerializableStateObservation;
import serialization.Types;
import worldModelAgent.util;
import worldModelAgent.worldModel.objectModels.ConstantModel;
import worldModelAgent.worldModel.objectModels.ObjectModel;
import worldModelAgent.worldModel.specialObjects.SpawnPoint;

import java.util.*;

/**
 * Created by dockhorn on 11.10.2017.
 */
public class WorldModel {

    SerializableStateObservation previous_world;
    int number_of_players;
    double[] world_dimension;

    //store all known hierarchy types and a behavior model for each of them <iType, ObjectModel>
    Set<Integer> observationITypes = new HashSet<>();
    HashMap<Integer, ObjectModel> observationModels;

    //
    HashMap<Integer, Observation> observations = new HashMap<>();
    List<Integer> updated_observations = new LinkedList<>();

    Set<Integer> newIDs;
    Set<Integer> updatedIDs;

    List<SpawnPoint> spawnPoints = new LinkedList<SpawnPoint>();

    public WorldModel(SerializableStateObservation sso){
        this.number_of_players = sso.noOfPlayers;
        this.previous_world = sso;
        this.world_dimension = sso.getWorldDimension();

        this.init(sso);
    }


    public void init(SerializableStateObservation new_world){
        Observation[][][] observationGrid = new_world.getObservationGrid();
        if (observationGrid != null) {
            for (int i = 0; i < observationGrid.length; i++) {
                for (int j = 0; j < observationGrid[i].length; j++) {
                    for (Observation obs : observationGrid[i][j]) {

                        if (observationITypes.add(obs.getItype())){
                            //if observation iType does not exist yet
                            addNewModel(obs);
                        }

                        //if observation ID does already exit, check for updates
                        if (!observations.containsKey(obs.getObsID())){
                            addNewObservation(obs);
                        }
                    }
                }
            }
        }
    }

    public void update(SerializableStateObservation new_world){
        updated_observations.clear();

        //traverse all elements of the observation grid and track changes to the game world
        Observation[][][] observationGrid = new_world.getObservationGrid();
        int[] position;

        if (observationGrid != null) {
            for (int i = 0; i < observationGrid.length; i++) {
                for (int j = 0; j < observationGrid[i].length; j++) {
                    for (Observation obs : observationGrid[i][j]) {

                        if (observationITypes.add(obs.getItype())){
                            //if observation iType does not exist yet
                            addNewModel(obs);
                        }

                        //if observation ID does already exit, check for updates
                        if (observations.containsKey(obs.getObsID())){
                            updateObservation(obs);
                        }
                        else {
                            addNewObservation(obs);
                            position = util.pixelToGridPosition(obs.getPosition(), new_world);

                            SpawnPoint matching_spawnpoint = null;
                            for (SpawnPoint spawn_point : spawnPoints){
                                if (position[0] == spawn_point.getX() && position[1] == spawn_point.getY()){
                                    matching_spawnpoint = spawn_point;
                                    break;
                                }
                            }
                            if (matching_spawnpoint != null){
                                matching_spawnpoint.addSpawnEvent(obs, new_world.gameTick);
                            }
                            else {
                                createNewSpawnPoint(position, obs, new_world.gameTick);
                            }
                        }
                    }
                }
            }
        }

        this.previous_world = new_world;
    }

    private void createNewSpawnPoint(int[] position, Observation obs, int tick) {
        this.spawnPoints.add(new SpawnPoint(position[0], position[1], obs, tick));
    }


    private void addNewModel(Observation obs){
        observationModels.put(obs.getItype(), new ConstantModel());

        switch (obs.getCategory()){
            //the category can be used to restrict the model
                                /*
                                    TODO search from a list of biased observationModels per category
                                    - which model categories exist? 0-player, 4-immovable
                                    - is there a general behavior? e.g. static observationModels, ressource observationModels,
                                 */
        }
    }


    private void updateObservation(Observation obs) {
        //find differences and create updateEvents, which are stored in the List
        Observation estimated_results = observationModels.get(obs.getItype()).simulate(obs);

        this.updated_observations.add(obs.getObsID());
    }


    private void addNewObservation(Observation obs) {
        //does not exist yet add Observation to the HashMap
        //since copy constructor is missing -> copy all attributes

        Observation newObservation = new Observation();
        newObservation.category = obs.getCategory();
        newObservation.position = obs.getPosition();
        newObservation.itype = obs.getItype();
        newObservation.obsID = obs.getObsID();
        newObservation.sqDist = obs.getSqDist();
        newObservation.reference = obs.getReference();
        this.observations.put(obs.getObsID(), newObservation);
    }


    public void simulate(SerializableStateObservation sso, Types.ACTIONS playerAction){
        for (Observation obs : observations.values()){
            observationModels.get(obs.getObsID()).simulate(obs);
        }
        for (SpawnPoint spawnPoint : spawnPoints){

        }
    }



}
