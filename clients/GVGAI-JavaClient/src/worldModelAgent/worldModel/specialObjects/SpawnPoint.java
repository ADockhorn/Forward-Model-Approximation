package worldModelAgent.worldModel.specialObjects;

import serialization.Observation;
import worldModelAgent.util;

/**
 * Created by dockhorn on 18.10.2017.
 */
public class SpawnPoint {

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    private int x;
    private int y;

    private boolean active;
    private int lastTick;
    private int estimatedCooldown;

    private Observation elementToSpawn;



    public SpawnPoint(int x, int y, Observation spawnedElement, int tick){
        this.x = x;
        this.y = y;
        this.elementToSpawn = util.copyObservation(spawnedElement);
        this.estimatedCooldown = tick;
    }

    public SpawnPoint(int x, int y, Observation spawnedElement){
        this(x,y, spawnedElement, -1);
    }

    public boolean isActive(){
        return active;
    }

    /**
     *
     * @param obs
     * @param tick
     * @return true if the current observation is explainable by this spawnPoint, otherwise false
     */
    public boolean addSpawnEvent(Observation obs, int tick){
        if (estimatedCooldown == -1){
            this.estimatedCooldown = tick;
        }
        else {
            if (tick - lastTick == estimatedCooldown){
                return isSpawnedObservationCorrect(obs);
            } else {
                this.estimatedCooldown = tick - lastTick;
            }
        }
        return true;
    }

    /**
     * Checks if the spawned Observation can be explained by this spawnPoint
     * @param spawned_obs
     * @return
     */
    private boolean isSpawnedObservationCorrect(Observation spawned_obs){
        if (spawned_obs.getPosition() == elementToSpawn.getPosition() &&
                spawned_obs.getItype() == elementToSpawn.getItype() &&
                spawned_obs.getCategory() == elementToSpawn.getCategory() &&
                spawned_obs.getReference() == elementToSpawn.getReference())
            return true;
        return false;
    }


    private Observation simulate(){
        return null;
    }

}
