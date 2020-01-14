package playtracewriter;

import serialization.Types;
import serialization.SerializableStateObservation;
import serialization.Vector2d;
import serialization.Observation;

import playtracewriter.TickInfo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;


/**
 * Created by dockhorn on 28.11.2017.
 */
public class TransactionTracker extends PlaytraceTracker {

    boolean delete_on_level_end;
    int lastStoredGameTick = -1;

    TickInfo previousTick = null;
    LinkedList<Transaction> transactions;

    public TransactionTracker(boolean delete_on_level_end)
    {
        this.delete_on_level_end = delete_on_level_end;
        transactions = new LinkedList<>();
    }

    @Override
    public void initNewLevel(SerializableStateObservation sso) {
        previousTick = null;
    }

    @Override
    public void storeTick(SerializableStateObservation sso, Types.ACTIONS action)
    {
        TickInfo current_tick = new TickInfo(sso, action, previousTick);
        if (sso.gameTick != lastStoredGameTick+1){
            // at least one tick was not stored so we should skip the transaction saving
            previousTick = null;
        }

        if (previousTick != null)
        {
            this.transactions.add(new Transaction(previousTick, current_tick));
        }
        previousTick = current_tick;
        lastStoredGameTick = sso.gameTick;

            //saveNpcXML(sso, current_tick);
            //saveObservationCount(sso, current_tick);
            //saveObsDeathAndCreateXML(sso, current_tick);
            //saveGameXML(sso, current_tick);
    }

    @Override
    public void endLevel() {
        if (this.delete_on_level_end){
            transactions.clear();
        }
        previousTick = null;
    }

    @Override
    public TickInfo getLastStoredTick(){
        return this.previousTick;
    }

    @Override
    public Transaction getLatestTransaction(){
        if (this.transactions.isEmpty())
            return null;
        return this.transactions.getLast();
    }

    @Override
    public String getLatestTransactionString(){
        if (this.getLatestTransaction() == null)
            return "NoTransactionAvailable";
        // this method avoids that other classes need to import the class Transaction
        return this.getLatestTransaction().get_transaction_string();
    }


    /*
    private void saveObsDeathAndCreateXML(SerializableStateObservation stateObs, StringBuilder stringBuilder){
        ArrayList<Observation>[][] obs = stateObs.getObservationGrid();

        //receive the actual collisions
        TreeSet<Event> eventtree = stateObs.getEventsHistory();
        if (eventtree.size() > lastsize){
            for(Event event : eventtree){
                if (event.gameStep == stateObs.getGameTick()-1)
                    stringBuilder.append("Collision="+eventtree.last().activeTypeId+"_"+eventtree.last().passiveTypeId + " ");
            }
        }


        if (obsID_to_itype != null){
            //update deaths and spawns
            HashMap<Integer, Integer> new_hash_map = new HashMap<>();

            for (int x=0; x < obs.length; x++){
                for (int y=0; y < obs[x].length ;  y++)
                {
                    for(Observation ob : obs[x][y])
                    {
                        if (!obsID_to_itype.containsKey(ob.obsID)){
                            stringBuilder.append("newITypeEl="+ob.itype + " ");
                        }
                        new_hash_map.put(ob.obsID, ob.itype);
                    }
                }
            }

            for(Map.Entry<Integer, Integer> entry : this.obsID_to_itype.entrySet()) {
                if (!new_hash_map.containsKey(entry.getKey())){
                    stringBuilder.append("delITypeEl="+entry.getValue() + " ");
                }
            }
            this.obsID_to_itype = new_hash_map;
        } else {
            //initialize at first tick
            obsID_to_itype = new HashMap<>();

            for (int x=0; x < obs.length; x++){
                for (int y=0; y < obs[x].length ;  y++)
                {
                    for(Observation ob : obs[x][y])
                    {
                        obsID_to_itype.put(ob.obsID, ob.itype);
                    }
                }
            }
        }
    }
    */

    /*


    private void saveObservationCount(SerializableStateObservation stateObs, StringBuilder stringbuilder){
        HashMap<Integer, Integer> typecounter = new HashMap<>();
        Integer obs_itype_value = null;
        ArrayList<Observation>[][] obs = stateObs.getObservationGrid();

        for (int x=0; x < obs.length; x++){
            for (int y=0; y < obs[x].length ;  y++)
            {
                for(Observation ob : obs[x][y])
                {
                    obs_itype_value = typecounter.get(ob.itype);
                    if (obs_itype_value != null) {
                        typecounter.put(ob.itype, obs_itype_value+1);
                    } else {
                        typecounter.put(ob.itype, 1);
                        // No such key
                    }
                }
            }
        }
        for (Map.Entry<Integer, Integer> entry : typecounter.entrySet()){
            stringbuilder.append("Count_"+entry.getKey().toString() + "=" + entry.getValue().toString() + " ");
        }
    }






    private void saveNpcXML(SerializableStateObservation stateObs, StringBuilder stringBuilder)
    {
        ArrayList<Observation>[] npcs = stateObs.getNPCPositions();

        if(npcs == null) return;

        for(int i = 0; i < npcs.length; i++)
        {
            for(Observation obs : npcs[i])
            {
                //stringBuilder.append("NPC_at="+obs.position.x+"/"+obs.position.y + " ");
            }
        }
    }
    */



}
