package worldModelAgent;

/**
 * Created by Daniel on 21.05.2017.
 */

import playtracewriter.PlaytraceTracker;
import playtracewriter.TransactionTracker;
import serialization.SerializableStateObservation;
import serialization.Types;
import utils.ElapsedCpuTimer;

import java.util.*;
import worldModelAgent.worldModel.WorldModel;

/**
 * This class has been built with a simple design in mind.
 * It is to be used to store player agent information,
 * to be later used by the client to send and receive information
 * to and from the server.
 */
public class Agent extends utils.AbstractPlayer {

    PlaytraceTracker tracker;
    /**
     * Public method to be called at the start of the communication. No game has been initialized yet.
     * Perform one-time setup here.
     */
    public Agent(){
        lastSsoType = Types.LEARNING_SSO_TYPE.JSON;
    }


    /**
     * Public method to be called at the start of every level of a game.
     * Perform any level-entry initialization here.
     * @param sso Phase Observation of the current game.
     * @param elapsedTimer Timer (1s)
     */
    @Override
    public void init(SerializableStateObservation sso, ElapsedCpuTimer elapsedTimer){
        tracker = new TransactionTracker(true);
        tracker.initNewLevel(sso);
        System.out.println("init");
    }

    /**
     * Method used to determine the next move to be performed by the agent.
     * This method can be used to identify the current state of the game and all
     * relevant details, then to choose the desired course of action.
     *
     * @param sso Observation of the current state of the game to be used in deciding
     *            the next action to be taken by the agent.
     * @param elapsedTimer Timer (40ms)
     * @return The action to be performed by the agent.
     */
    @Override
    public Types.ACTIONS act(SerializableStateObservation sso, ElapsedCpuTimer elapsedTimer){
        System.out.println("act");
        int index = new Random().nextInt(sso.getAvailableActions().size());
        Types.ACTIONS playerAction = sso.getAvailableActions().get(index);
        tracker.storeTick(sso, playerAction);
        System.out.println(tracker.getLatestTransactionString());

        return playerAction;
        //The current level can be exited during training using ACTION_ESCAPE
    }



    /**
     * Method used to perform actions in case of a game end.
     * This is the last thing called when a level is played (the game is already in a terminal state).
     * Use this for actions such as teardown or process data.
     *
     * @param sso The current state observation of the game.
     * @param elapsedTimer Timer (up to CompetitionParameters.TOTAL_LEARNING_TIME
     * or CompetitionParameters.EXTRA_LEARNING_TIME if current global time is beyond TOTAL_LEARNING_TIME)
     * @return The next level of the current game to be played.
     * The level is bound in the range of [0,2]. If the input is any different, then the level
     * chosen will be ignored, and the game will play a sampleRandom one instead.
     */
    @Override
    public int result(SerializableStateObservation sso, ElapsedCpuTimer elapsedTimer){

        tracker.storeTick(sso, null);
        System.out.println(tracker.getLatestTransactionString());

        tracker.endLevel();

        Random r = new Random();
        Integer level = r.nextInt(3);
        return level;
    }

}
