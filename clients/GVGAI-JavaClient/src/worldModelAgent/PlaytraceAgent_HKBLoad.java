package worldModelAgent;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.Set;

import bfs.BreadthFirstSearch;
import gvgai_mcts.GVGAIBoard;
import gvgai_mcts.GVGAIMove;
import gvgai_mcts.MCTSPARAMETERS;
import kbextraction.agents.GVGAIBot;
import mcts.MCTS;
import mcts.Move;
import playtracewriter.PlaytraceTracker;

/**
 * Created by Daniel on 21.05.2017.
 */

import playtracewriter.TransactionTracker;
import serialization.SerializableStateObservation;
import serialization.Types;
import utils.CompetitionParameters;
import utils.ElapsedCpuTimer;

/**
 * This class has been built with a simple design in mind.
 * It is to be used to store player agent information,
 * to be later used by the client to send and receive information
 * to and from the server.
 */


public class PlaytraceAgent_HKBLoad extends utils.AbstractPlayer {

	private enum ReasoningMode {
		BFS, MCTS
	}

	private static final ReasoningMode reasoningMode = ReasoningMode.BFS;
	private float sumPoints = 0;
	private float wins = 0;
	private float sumTicks = 0;
	private int level = 0;
	private int training_levels;
	private int validation_levels;
	private boolean evaluation = false;

	/** The agent wrappers for knowledge extraction */
	private GVGAIBot kbExtractionAgent = null;
	private GVGAIBot kbExtractionAgent1 = null;
	private GVGAIBot kbExtractionAgent2 = null;
	private GVGAIBot kbExtractionAgent3 = null;
	
	/** The transactions collected during training (and beyond) */
	private List<String> transactions = null;
	
	
    PlaytraceTracker playtraceTracker = new TransactionTracker(true);

    public int evaluation_nr = 0;

    
    /**
     * Public method to be called at the start of the communication. No game has been initialized yet.
     * Perform one-time setup here.
     */
    public PlaytraceAgent_HKBLoad(){
        lastSsoType = Types.LEARNING_SSO_TYPE.JSON;
    
        // Create list to store transactions
        transactions = new LinkedList<String>();
		training_levels = 2;
		validation_levels =  5 - training_levels;
		
		// Load predefined HKBs...
		kbExtractionAgent1 = new GVGAIBot( "testgame1_", true );
		kbExtractionAgent2 = new GVGAIBot( "testgame2_", true );
		kbExtractionAgent3 = new GVGAIBot( "testgame3_", true );
		kbExtractionAgent = null;  
    }


    /**
     * Public method to be called at the start of every level of a game.
     * Perform any level-entry initialization here.
     * @param sso Phase Observation of the current game.
     * @param elapsedTimer Timer (1s)
     */
    @Override
    public void init(SerializableStateObservation sso, ElapsedCpuTimer elapsedTimer){
        
		playtraceTracker.initNewLevel(sso);
		
		// Since the agent doesn't know which of the three testgames is played, 
		// rotate the used HKBs...
		if( (kbExtractionAgent == kbExtractionAgent3) || (kbExtractionAgent == null) )
		{
			kbExtractionAgent = kbExtractionAgent1;
			
			// DEBUG
			System.out.println( "Using knowledge of testgame1..." );
		}
		else if( kbExtractionAgent == kbExtractionAgent1 )
		{
			kbExtractionAgent = kbExtractionAgent2;

			// DEBUG
			System.out.println( "Using knowledge of testgame2..." );
		}
		else if( kbExtractionAgent == kbExtractionAgent2 )
		{
			kbExtractionAgent = kbExtractionAgent3;

			// DEBUG
			System.out.println( "Using knowledge of testgame3..." );
		}
		
//        System.out.println("init");
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

        Types.ACTIONS playerAction = null;


        try{
			// If meta knowledge base exists
			if( kbExtractionAgent != null )
			{

				if (elapsedTimer.elapsedMillis() > CompetitionParameters.TOTAL_LEARNING_TIME && HKBPARAMETERS.HKB_DO_REVISION){

					//    		// If the last action did not result in the predicted effect
					//    		// according to the meta knowledge base, do revision
					Set<String> prevState = getPreviousPerception( transactions.get( transactions.size() - 1 ) );
					Set<String> latestState = getLatestPerception( transactions.get( transactions.size() - 1 ) );
					//    		System.out.println( "prevState: " + prevState );
					//    		System.out.println( "latestState: " + latestState );
					String prevAction = getPreviousAction( transactions.get( transactions.size() - 1 ) );
					kbExtractionAgent.revision( prevState, prevAction, latestState );

					/*
					// Choose action based on meta knowledge base (instead of random action)
					Set<String> state = getLatestPerception( transactions.get( transactions.size() - 1 ) );
					//String nextAction = kbExtractionAgent.nextAction( state );
					*/
				}

				switch (reasoningMode){
					case MCTS:
						MCTS mcts = new MCTS();
						GVGAIBoard start_board = new GVGAIBoard(sso, this.kbExtractionAgent.metaKB);
						Move move = mcts.runMCTS_UCT(start_board, MCTSPARAMETERS.NR_OF_SIMULATIONS, false);
						GVGAIMove gvgaiMove = ((GVGAIMove) move);
						playerAction = gvgaiMove.getAction();

						if (MCTSPARAMETERS.MCTS_DEBUG && playerAction!= null){
							System.out.println("DEBUG: Player Action found by MCTS: " + playerAction);
						}
						break;
					case BFS:
						BreadthFirstSearch bfs = new BreadthFirstSearch(sso, this.kbExtractionAgent.metaKB);
						bfs.runBFS();
						playerAction = bfs.getBestMove();
				}

				/*
					// Map inferred action to a corresponding action of the GVGAI framework
					if( nextAction.equals( "c_Action1" ) )
						playerAction = Types.ACTIONS.ACTION_USE;
					else if( nextAction.equals( "c_Action2" ) )
						playerAction = Types.ACTIONS.ACTION_RIGHT;
					else if( nextAction.equals( "c_Action3" ) )
						playerAction = Types.ACTIONS.ACTION_LEFT;
					else if( nextAction.equals( "c_Action4" ) )
						playerAction = Types.ACTIONS.ACTION_UP;
					else if( nextAction.equals( "c_Action5" ) )
						playerAction = Types.ACTIONS.ACTION_DOWN;
					else
						playerAction = Types.ACTIONS.ACTION_NIL;
				*/
			}

			// Else
			else
			{
				// Choose random action (Does this include ACTION_USE and ACTION_ESCAPE?)
	//	        System.out.println("act");
				int index = new Random().nextInt(sso.getAvailableActions().size());
				playerAction = sso.getAvailableActions().get(index);
			}

			playtraceTracker.storeTick(sso, playerAction);
	//	        System.out.println(sso.gameTick);
			String transaction = playtraceTracker.getLatestTransactionString();
	//	        System.out.println( transaction );

			// Only store the transaction if available
			// WHY IS IT SOMETIMES NOT AVAILABLE?
			if( !transaction.equals( "NoTransactionAvailable" ) )
				transactions.add( transaction );


        } catch (Exception e){
            e.printStackTrace();
        }

        if (playerAction == null)
            System.out.println("no playerAction defined");

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
    	
        playtraceTracker.storeTick(sso, null);
//        System.out.println(sso.gameTick);
        String transaction = playtraceTracker.getLatestTransactionString(); 
//        System.out.println( transaction );

        // Only store the transaction if available
        // WHY IS IT SOMETIMES NOT AVAILABLE?
        if( !transaction.equals( "NoTransactionAvailable" ) )
        	transactions.add( transaction );

        
        playtraceTracker.endLevel();



		if (evaluation){
			sumPoints += sso.gameScore;
			sumTicks += sso.gameTick;
			if (sso.gameWinner == Types.WINNER.PLAYER_WINS){
				wins += 1;
			}
			evaluation_nr += 1;

			System.out.println(evaluation_nr + "; " + sso.gameScore + "; Tick: " +
					sso.gameTick + "; Winner:" + sso.gameWinner.name() +
					"; Mode: " + reasoningMode.toString() +
					"; Mean Score: " + sumPoints/evaluation_nr +
					"; Mean Ticks: " + sumTicks/evaluation_nr +
					"; Win Rate: " + wins/evaluation_nr);
		} else {
        	System.out.println("Training run ended with score " + sso.gameScore);
		}
		if (elapsedTimer.elapsedMillis() == 0){
			evaluation = true;
		}
        
        if (!evaluation){
			level = (level + 1) % training_levels;
			System.out.println("return learning level " + level);
			return level;
		} else {
			Integer level = (evaluation_nr % validation_levels) + training_levels;
			return level;
		}
    }


    private Set<String> getLatestPerception( String transaction )
    {
    	String perception = transaction.trim();
    	String[] perceptionTokens = perception.split( " " );
    	
    	Set<String> perceptionSet = new HashSet<String>();
    	boolean hasAboveOfP = false;
    	boolean hasBelowOfP = false;
    	boolean hasLeftOfP = false;
    	boolean hasRightOfP = false;
    	for( String token : perceptionTokens )
    	{
    		if( token.startsWith( "n_" ) && !token.startsWith( "n_Action" ) )
    		{
	    		String modifiedToken = token.replace( "n_" , "c_" );
	    		modifiedToken = modifiedToken.trim();
	    		modifiedToken = modifiedToken.replace( "=", "" );

    			// Map game states to numbers
	    		modifiedToken = modifiedToken.replace( "NO_WINNER", "0" );
	    		modifiedToken = modifiedToken.replace( "PLAYER_WINS", "1" );
	    		modifiedToken = modifiedToken.replace( "PLAYER_LOSES", "2" );
	    		
//	    		// Map actions to numbers
//	    		modifiedToken = modifiedToken.replace( "ACTION_NIL", "0" );
//	    		modifiedToken = modifiedToken.replace( "ACTION_USE", "1" );
//	    		modifiedToken = modifiedToken.replace( "ACTION_RIGHT", "2" );
//	    		modifiedToken = modifiedToken.replace( "ACTION_LEFT", "3" );
//	    		modifiedToken = modifiedToken.replace( "ACTION_UP", "4" );
//	    		modifiedToken = modifiedToken.replace( "ACTION_DOWN", "5" );
	    		
	    		// Replace bad symbols (for orientation)
	    		modifiedToken = modifiedToken.replace( ".", "_" );
	    		modifiedToken = modifiedToken.replace( "/", "__" );  // Depending on the version, orientation may use "/" or "," as separator	
	    		modifiedToken = modifiedToken.replace( ",", "__" );	 // Depending on the version, orientation may use "/" or "," as separator   		
	    		
	    		// Store whether there are information about objects around are present
	    		if( modifiedToken.contains( "c_AboveOfP" ) )
	    			hasAboveOfP = true;
	    		else if( modifiedToken.contains( "c_BelowOfP" ) )
	    			hasBelowOfP = true;
	    		else if( modifiedToken.contains( "c_LeftOfP" ) )
	    			hasLeftOfP = true;
	    		else if( modifiedToken.contains( "c_RightOfP" ) )
	    			hasRightOfP = true;
	    		
	    		perceptionSet.add( modifiedToken );
    		}
    	}

    	// Add missing values as explicit symbols
		if( !hasAboveOfP )
			perceptionSet.add( "c_AboveOfP999" );
		if( !hasBelowOfP )
			perceptionSet.add( "c_BelowOfP999" );
		if( !hasLeftOfP )
			perceptionSet.add( "c_LeftOfP999" );
		if( !hasRightOfP )
			perceptionSet.add( "c_RightOfP999" );
    	
//    	// DEBUG
//    	System.out.println( "Latest perception: " + perceptionSet );
    	
    	return perceptionSet;
    }


    private Set<String> getPreviousPerception( String transaction )
    {
    	String perception = transaction.trim();
    	String[] perceptionTokens = perception.split( " " );
    	
    	Set<String> perceptionSet = new HashSet<String>();
    	boolean hasAboveOfP = false;
    	boolean hasBelowOfP = false;
    	boolean hasLeftOfP = false;
    	boolean hasRightOfP = false;
    	for( String token : perceptionTokens )
    	{
    		if( token.startsWith( "c_" ) && !token.startsWith( "c_Action" ) )
    		{
	    		String modifiedToken = new String( token );
	    		modifiedToken = modifiedToken.trim();
	    		modifiedToken = modifiedToken.replace( "=", "" );

    			// Map game states to numbers
	    		modifiedToken = modifiedToken.replace( "NO_WINNER", "0" );
	    		modifiedToken = modifiedToken.replace( "PLAYER_WINS", "1" );
	    		modifiedToken = modifiedToken.replace( "PLAYER_LOSES", "2" );
	    		
//	    		// Map actions to numbers
//	    		modifiedToken = modifiedToken.replace( "ACTION_NIL", "0" );
//	    		modifiedToken = modifiedToken.replace( "ACTION_USE", "1" );
//	    		modifiedToken = modifiedToken.replace( "ACTION_RIGHT", "2" );
//	    		modifiedToken = modifiedToken.replace( "ACTION_LEFT", "3" );
//	    		modifiedToken = modifiedToken.replace( "ACTION_UP", "4" );
//	    		modifiedToken = modifiedToken.replace( "ACTION_DOWN", "5" );
	    		
	    		// Replace bad symbols (for orientation)
	    		modifiedToken = modifiedToken.replace( ".", "_" );
	    		modifiedToken = modifiedToken.replace( "/", "__" );  // Depending on the version, orientation may use "/" or "," as separator	
	    		modifiedToken = modifiedToken.replace( ",", "__" );	 // Depending on the version, orientation may use "/" or "," as separator   		
	    		
	    		// Store whether there are information about objects around are present
	    		if( modifiedToken.contains( "c_AboveOfP" ) )
	    			hasAboveOfP = true;
	    		else if( modifiedToken.contains( "c_BelowOfP" ) )
	    			hasBelowOfP = true;
	    		else if( modifiedToken.contains( "c_LeftOfP" ) )
	    			hasLeftOfP = true;
	    		else if( modifiedToken.contains( "c_RightOfP" ) )
	    			hasRightOfP = true;
	    		
	    		perceptionSet.add( modifiedToken );
    		}
    	}

    	// Add missing values as explicit symbols
		if( !hasAboveOfP )
			perceptionSet.add( "c_AboveOfP999" );
		if( !hasBelowOfP )
			perceptionSet.add( "c_BelowOfP999" );
		if( !hasLeftOfP )
			perceptionSet.add( "c_LeftOfP999" );
		if( !hasRightOfP )
			perceptionSet.add( "c_RightOfP999" );
    	
//    	// DEBUG
//    	System.out.println( "Latest perception: " + perceptionSet );
    	
    	return perceptionSet;
    }
    
    
    private String getPreviousAction( String transaction )
    {
    	String perception = transaction.trim();
    	String[] perceptionTokens = perception.split( " " );
    	
    	for( String token : perceptionTokens )
    	{
    		if( token.startsWith( "c_Action" ) )
    		{
    			// Map actions to numbers
    			String action = token.replace( "=", "" );
    			action = action.replace( "ACTION_NIL", "0" );
    			action = action.replace( "ACTION_USE", "1" );
    			action = action.replace( "ACTION_RIGHT", "2" );
    			action = action.replace( "ACTION_LEFT", "3" );
    			action = action.replace( "ACTION_UP", "4" );
    			action = action.replace( "ACTION_DOWN", "5" );
    			
    			return action;
    		}
    	}
    	
    	return null;
    }
}
