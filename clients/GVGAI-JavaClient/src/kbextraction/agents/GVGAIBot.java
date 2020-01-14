package kbextraction.agents;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import kbextraction.MultiAbstractionLevelKB;
import kbextraction.Rule;
import worldModelAgent.HKBPARAMETERS;

public class GVGAIBot 
{
	private static Random random = new Random();
	
	public static enum KnowledgeType {MOVE_ABS, MOVE_REL, SCORE, WIN};
	
	public static final String GAMESTATE_NOWINNER = "n_GameState0";
	public static final String GAMESTATE_WIN = "n_GameState1";
	public static final String GAMESTATE_LOSE = "n_GameState2";
	
	public static final String MOVEMENT_NONE = "PosX0___PosY0";

	public static final String ACTION_NONE = "c_Action0";
	public static final String ACTION_USE = "c_Action1";
	public static final String ACTION_RIGHT = "c_Action2";
	public static final String ACTION_LEFT = "c_Action3";
	public static final String ACTION_UP = "c_Action4";
	public static final String ACTION_DOWN = "c_Action5";
	
	public static final ArrayList<String> ACTIONS = new ArrayList<String>();
	static
	{
		ACTIONS.add( ACTION_USE );
		ACTIONS.add( ACTION_RIGHT );
		ACTIONS.add( ACTION_LEFT );
		ACTIONS.add( ACTION_UP );
		ACTIONS.add( ACTION_DOWN );
		ACTIONS.add( ACTION_NONE );
	}
		
	public HashMap<KnowledgeType, MultiAbstractionLevelKB> metaKB;
	
	
	public GVGAIBot( String filename )
	{
		metaKB = createMetaKB( filename );
	}
	

	public GVGAIBot( String filename, boolean loadPredefinedHKB )
	{
		if( loadPredefinedHKB )
		{
			metaKB = loadMetaKB( filename );
		}
		else
		{
			metaKB = createMetaKB( filename );
		}
	}

	
	public static MultiAbstractionLevelKB mergeKBs( MultiAbstractionLevelKB kb1, MultiAbstractionLevelKB kb2 )
	{
		MultiAbstractionLevelKB mergedKB = new MultiAbstractionLevelKB(); 
		for( int i = 0; i < kb1.getNoOfLevels(); i++ )
			for( int j = 0; j < kb1.getLevel( i ).size(); j++ )
				mergedKB.addRule( kb1.getLevel( i ).get( j ) );
		
		for( int i = 0; i < kb2.getNoOfLevels(); i++ )
			for( int j = 0; j < kb2.getLevel( i ).size(); j++ )
			{
				boolean isAlreadyContained = false;
				if( i < mergedKB.getNoOfLevels()  )
				{
					double minWeight = Double.MAX_VALUE;
					for( Rule rule : mergedKB.getLevel( i ) )
						if( rule.getPremise().equals( kb2.getLevel( i ).get( j ).getPremise() ) && rule.getConclusion().equals( kb2.getLevel( i ).get( j ).getConclusion() ) )
						{
							minWeight = Math.min( rule.getWeight() , kb2.getLevel( i ).get( j ).getWeight() );
							rule.setWeight( minWeight );
							
							isAlreadyContained = true;
							break;
						}
				}
				
				if( !isAlreadyContained )
					mergedKB.addRule( kb2.getLevel( i ).get( j ) );
			}
		
		return mergedKB;
	}
	
	
	/**
	 * Revises a given knowledge base by expansion in case the given state and action do not
	 * produce the given effect. 
	 * 
	 * @param state           the state to be checked
	 * @param action          the action to be checked with the state
	 * @param resultingState  the perceived resulting state when performing the given action in the given state
	 */
	public void revision( Set<String> state, String action, Set<String> resultingState )
	{
//		// DEBUG
//		System.out.println( "------------------------------------------" );
//		System.out.println( "-----> state: " + state );
//		System.out.println( "-----> action: " + action );
//		System.out.println( "-----> resultingState: " + resultingState );
//		System.out.println( "------------------------------------------" );
		
		for( KnowledgeType knowledgeType : metaKB.keySet() )
		{
			HashSet<String> stateWithAction = new HashSet<String>( state );
			stateWithAction.add( action );

			if (knowledgeType == KnowledgeType.MOVE_ABS)
				continue;

			List<String> inferences = metaKB.get( knowledgeType ).reasoning( stateWithAction );
			
			boolean containsResultingStateValue = false;
			for( String inference : inferences )
			{
				if( inference.contains( "Pos" ) )
				{
					// Get predicted delta x and y
					String[] xAndYPos = inference.split( "___" );
					int deltaX = Integer.parseInt( xAndYPos[ 0 ].replace( "PosX", "" ) );
					int deltaY = Integer.parseInt( xAndYPos[ 1 ].replace( "PosY", "" ) );
					
					// Calculate new x and y values according to the prediction
					String newPosXValue = null;
					String newPosYValue = null;
					for( String value : state )
					{
						if( value.contains( "PosX" ) )
						{
							String[] xPosSplit = value.split( "PosX" );
							int newXPos = Integer.parseInt( xPosSplit[ 1 ] );
							newXPos += deltaX;
							newPosXValue = xPosSplit[ 0 ] + "PosX" + newXPos;
						}
						else if( value.contains( "PosY" ) )
						{
							String[] yPosSplit = value.split( "PosY" );
							int newYPos = Integer.parseInt( yPosSplit[ 1 ] );
							newYPos += deltaY;
							newPosYValue = yPosSplit[ 0 ] + "PosY" + newYPos;
						}
					}

					// If both new x and y value are contained
					if( resultingState.contains( newPosXValue ) && resultingState.contains( newPosYValue ) )
					{
						containsResultingStateValue = true;
						break;
					}
				}
				else if( inference.contains( "Score" )  )
				{
					// Get predicted score delta
					String[] score = inference.split( "Score" );
					double deltaScore = Double.parseDouble( score[ 1 ].replace( "_", ".") );

					// Calculate new score according to the prediction
					String newScoreValue = null;
					for( String value : state )
					{
						if( value.contains( "Score" ) && !value.contains( "ScoreInc" ) )
						{
							String[] scoreSplit = value.split( "Score" );
							double newScore = Double.parseDouble( scoreSplit[ 1 ].replace( "_", ".") );
							newScore += deltaScore;
							newScoreValue = (scoreSplit[ 0 ] + "Score" + newScore).replace( ".", "_" ).trim();
							break;
						}
					}

//					// DEBUG
//					System.out.println( "-----> newScoreValue: " + newScoreValue );
//					System.out.println( "-----> resultingState: " + resultingState );
					
					// If new score is contained
					if( resultingState.contains( newScoreValue ) )
					{
						containsResultingStateValue = true;
						break;
					}
				}
				else if( inference.contains( "GameState" ) )
				{
					// Get predicted game state
					String[] gameState = inference.split( "GameState" );
					int gameStateValue = Integer.parseInt( gameState[ 1 ] );

					// Calculate new game state according to the prediction
					// (if value is not present, default is that the game is still running and/or there is no winner)
					String newGameStateValue = "c_" + "GameState" + GAMESTATE_NOWINNER.split( "GameState" )[ 1 ];
					for( String value : state )
					{
						if( value.contains( "GameState" ) )
						{
							String[] gameStateSplit = value.split( "GameState" );
							int newGameState = gameStateValue;
							newGameStateValue = gameStateSplit[ 0 ] + "GameState" + newGameState;
							break;
						}
					}

					// If new game state is contained
					if( resultingState.contains( newGameStateValue ) )
					{
						containsResultingStateValue = true;
						break;
					}
				}
			}
			
			if( !containsResultingStateValue && !inferences.isEmpty() )
			{
				String newEffect = null;
				if( inferences.get( 0 ).contains( "Pos" ) )
				{
					// Calculate difference between x and y positions to create the new effect
					int posX = 0;
					int posY = 0;
					for( String value : state )
					{
						if( value.contains( "PosX" ) )
							posX = Integer.parseInt( value.split( "PosX" )[ 1 ] );
						else if( value.contains( "PosY" ) )
							posY = Integer.parseInt( value.split( "PosY" )[ 1 ] );
					}
					int resultingPosX = 0;
					int resultingPosY = 0;
					for( String value : resultingState )
					{
						if( value.contains( "PosX" ) )
							resultingPosX = Integer.parseInt( value.split( "PosX" )[ 1 ] );
						else if( value.contains( "PosY" ) )
							resultingPosY = Integer.parseInt( value.split( "PosY" )[ 1 ] );
					}
					int deltaX = resultingPosX - posX;
					int deltaY = resultingPosY - posY;
					
					newEffect = "PosX" + deltaX + "___" + "PosY" + deltaY;
				}
				else if( inferences.get( 0 ).contains( "Score" ) )
				{
					// Calculate difference between scores to create the new effect
					double score = 0;
					for( String value : state )
					{
						if( value.contains( "Score" ) && !value.contains( "ScoreInc" ) )
						{
							score = Double.parseDouble( value.split( "Score" )[ 1 ].replace( "_", "." ) );
							break;
						}
					}
					double resultingScore = 0;
					for( String value : resultingState )
					{
						if( value.contains( "Score" ) && !value.contains( "ScoreInc" ) )
						{
							resultingScore = Double.parseDouble( value.split( "Score" )[ 1 ].replace( "_", "." ) );
							break;
						}
					}
					double deltaScore = resultingScore - score;
					
					newEffect = ("Score" + deltaScore).replace( ".", "_" );
				}
				else if( inferences.get( 0 ).contains( "GameState" ) )
				{
					// Calculate new game state to create the new effect
					for( String value : resultingState )
					{
						if( value.contains( "GameState" ) )
						{
							newEffect = inferences.get( 0 ).split( "GameState" )[ 0 ] + "GameState" + value.split( "GameState" )[ 1 ];
							break;
						}
					}
				}
				
				// Filter unneeded stuff depending on the knowledge type
				HashSet<String> stateWithActionFiltered = new HashSet<String>();
				for( String token : stateWithAction )
				{
					switch( knowledgeType )
					{
						case MOVE_ABS:
							if( token.contains( "c_PosX" ) || token.contains( "c_PosY" ) || token.contains( "c_Action" ) )
								stateWithActionFiltered.add( token );
						break;
						
						case MOVE_REL:
						case SCORE:
						case WIN:
							if( token.contains( "c_Orientation" ) || token.contains( "c_AboveOfP" ) || token.contains( "c_BelowOfP" ) || token.contains( "c_LeftOfP" ) || token.contains( "c_RightOfP" ) || token.contains( "c_Action" ) )
								stateWithActionFiltered.add( token );
						break;
					}
				}
				stateWithAction = stateWithActionFiltered;
				
				// Remove old rule with the same premise
				// (to avoid inconsistent rules resulting in alternating decisions)
				List<Rule> mostConcreteLevel = metaKB.get( knowledgeType ).getLevel( metaKB.get( knowledgeType ).getNoOfLevels() - 1 );
				for( int i = 0; i < mostConcreteLevel.size(); i++ )
				{
					if( mostConcreteLevel.get( i ).getPremise().equals( stateWithAction ) )
					{
//						// DEBUG
						if (HKBPARAMETERS.HKB_DEBUG){
							System.out.println( "R E M O V E ! (" + knowledgeType + ")" );
						}

						mostConcreteLevel.remove( i );
						break;
					}
				}
				
				// Only add new exceptions after removal if the new effect cannot be inferred from 
				// the knowledge base after removal of the old exception rule with the same premise
				List<String> inferencesAfterRemoval = metaKB.get( knowledgeType ).reasoning( stateWithAction );
				if( (inferencesAfterRemoval.size() != 1) || !inferencesAfterRemoval.contains( newEffect ) )
				{
					Rule rule = new Rule( stateWithAction, newEffect, 1.0 );
					metaKB.get( knowledgeType ).addRule( rule );
				}
				
//				// DEBUG
				if (HKBPARAMETERS.HKB_DEBUG){
					System.out.println( "!!! REVISION !!! (" + knowledgeType + ")" );
				}

//				System.out.println( "Changed KB: " + knowledgeType );
//				System.out.println( metaKB.get( knowledgeType ) );
			}
		}
	}
	
	
	public HashMap<KnowledgeType, MultiAbstractionLevelKB> createMetaKB( String filename )
	{
		// Create meta KB
		HashMap<KnowledgeType, MultiAbstractionLevelKB> metaKB = new HashMap<KnowledgeType, MultiAbstractionLevelKB>();		
		
		// Create meta categories
		metaKB.put( KnowledgeType.MOVE_ABS, new MultiAbstractionLevelKB() );
		metaKB.put( KnowledgeType.MOVE_REL, new MultiAbstractionLevelKB() );
		metaKB.put( KnowledgeType.SCORE, new MultiAbstractionLevelKB() );
		metaKB.put( KnowledgeType.WIN, new MultiAbstractionLevelKB() );

		// Create and fill absolute movement categories
		MultiAbstractionLevelKB kb = MainGVGAIBot.extractKB_AbsoluteMovement( filename );
		metaKB.put( KnowledgeType.MOVE_ABS, kb );

		// Create and fill relative movement categories
		MultiAbstractionLevelKB mergedKB = MainGVGAIBot.extractKB_RelativeMovementAbove( filename );
		kb = MainGVGAIBot.extractKB_RelativeMovementBelow( filename );
		mergedKB = mergeKBs( mergedKB, kb );
		kb = MainGVGAIBot.extractKB_RelativeMovementLeft( filename );
		mergedKB = mergeKBs( mergedKB, kb );
		kb = MainGVGAIBot.extractKB_RelativeMovementRight( filename );
		mergedKB = mergeKBs( mergedKB, kb );
		metaKB.put( KnowledgeType.MOVE_REL, mergedKB );
		
		// Create and fill score categories
		mergedKB = MainGVGAIBot.extractKB_ScoreAbove( filename );
		kb = MainGVGAIBot.extractKB_ScoreBelow( filename );
		mergedKB = mergeKBs( mergedKB, kb );
		kb = MainGVGAIBot.extractKB_ScoreLeft( filename );
		mergedKB = mergeKBs( mergedKB, kb );
		kb = MainGVGAIBot.extractKB_ScoreRight( filename );
		mergedKB = mergeKBs( mergedKB, kb );
		metaKB.put( KnowledgeType.SCORE, mergedKB );

		// Create and fill win categories
		mergedKB = MainGVGAIBot.extractKB_WinAbove( filename );
		kb = MainGVGAIBot.extractKB_WinBelow( filename );
		mergedKB = mergeKBs( mergedKB, kb );
		kb = MainGVGAIBot.extractKB_WinLeft( filename );
		mergedKB = mergeKBs( mergedKB, kb );
		kb = MainGVGAIBot.extractKB_WinRight( filename );
		mergedKB = mergeKBs( mergedKB, kb );
		metaKB.put( KnowledgeType.WIN, mergedKB );

		return metaKB;
	}
	
	
	public HashMap<KnowledgeType, MultiAbstractionLevelKB> loadMetaKB( String filenamePrefix )
	{
		// Create meta KB
		HashMap<KnowledgeType, MultiAbstractionLevelKB> metaKB = new HashMap<KnowledgeType, MultiAbstractionLevelKB>();		
		
		// Create meta categories
		metaKB.put( KnowledgeType.MOVE_ABS, new MultiAbstractionLevelKB() );
		metaKB.put( KnowledgeType.MOVE_REL, new MultiAbstractionLevelKB() );
		metaKB.put( KnowledgeType.SCORE, new MultiAbstractionLevelKB() );
		metaKB.put( KnowledgeType.WIN, new MultiAbstractionLevelKB() );
		
		// Load and fill absolute movement categories
		MultiAbstractionLevelKB kb = new MultiAbstractionLevelKB( filenamePrefix + "MOVE_ABS.txt" );
		metaKB.put( KnowledgeType.MOVE_ABS, kb );

		// Load and fill relative movement categories
		MultiAbstractionLevelKB mergedKB = new MultiAbstractionLevelKB( filenamePrefix + "MOVE_REL.txt" );
		metaKB.put( KnowledgeType.MOVE_REL, mergedKB );

		// Load and fill score categories
		mergedKB = new MultiAbstractionLevelKB( filenamePrefix + "SCORE.txt" );
		metaKB.put( KnowledgeType.SCORE, mergedKB );
		
		// Load and fill win categories
		mergedKB = new MultiAbstractionLevelKB( filenamePrefix + "WIN.txt" );
		metaKB.put( KnowledgeType.WIN, mergedKB );
		
//		// DEBUG
//		System.out.println( "LOADING COMPLETED! Knowledge is: " );
//		printKnowledge();
		
		return metaKB;
	}
	
		
	private String nextAction( HashMap<KnowledgeType, MultiAbstractionLevelKB> metaKB, Set<String> state )
	{
		// Search for action that wins the game
		for( String action : ACTIONS )
		{
			// Extent set with current action
			HashSet<String> stateWithAction = new HashSet<String>( state );
			stateWithAction.add( action );
			
			// OPTIMIST
			List<String> inferences = metaKB.get( KnowledgeType.WIN ).reasoning( stateWithAction );
			if( inferences.contains( GAMESTATE_WIN ) )
				return action;
		}
		
		// Search for action that maximizes score gain
		double maxScore = Double.MIN_VALUE;
		List<String> bestActions = new ArrayList<String>();
		for( String action : ACTIONS )
		{
			// Extent set with current action
			HashSet<String> stateWithAction = new HashSet<String>( state );
			stateWithAction.add( action );
			
			List<String> inferences = metaKB.get( KnowledgeType.SCORE ).reasoning( stateWithAction );

			// OPTIMIST
			double maxInfScore = Double.MIN_VALUE;
			for( String inference : inferences )
			{
				String score = inference.replace( "Score", "" );
				score = score.replace( "_", "." );
				double scoreAsDbl = Double.parseDouble( score );
				if( maxInfScore < scoreAsDbl )
					maxInfScore = scoreAsDbl;
			}
			
			if( maxScore < maxInfScore )
			{
				maxScore = maxInfScore;
				bestActions.clear();
				bestActions.add( action );
			}
			else if( maxScore == maxInfScore )
			{
				bestActions.add( action );
			}
		}
		
//		// DEBUG
//		System.out.println( bestActions );
		
		if( !bestActions.isEmpty() )
			return bestActions.get( random.nextInt( bestActions.size() ) );
		else
		{
			ArrayList<String> allActions = new ArrayList<String>( ACTIONS );
			
//			// Filter movement actions that run into obstacles
//			// (The method does not work for games with orientation change and is therefore not in use, see JavaDoc.)
//			filterMoveActionsIntoObstacles( allActions, state );
			
			return allActions.get( random.nextInt( allActions.size() ) );
		}
	}
	

	public String nextAction( Set<String> state )
	{
		return nextAction( metaKB, state );
	}
	
	
	public void printKnowledge()
	{
		for( KnowledgeType knowledgeType : metaKB.keySet() )
		{
			System.out.println( "------------------------------------------------------------" );
			System.out.println( "knowledgeType: " + knowledgeType );
			System.out.println();
			System.out.println(  metaKB.get( knowledgeType ) );
		}
	}
	
	
	/**
	 * This method should make the random action selection a bit more intelligent:
	 * It filters movement actions that result in no movement.
	 * It does not work currently for games involving the orientation of the avatar.
	 * 
	 * @param actionsToBeFiltered  a list of actions to be filtered accordingly
	 * @param state                the current perceived state
	 */
	private void filterMoveActionsIntoObstacles( List<String> actionsToBeFiltered, Set<String> state )
	{
		// Filter movement actions that run into obstacles
		ArrayList<String> moveActions = new ArrayList<String>( ACTIONS );
		moveActions.remove( ACTION_USE );
		moveActions.remove( ACTION_NONE );
		ArrayList<String> filterActions = new ArrayList<String>();
		for( String action : moveActions )
		{
			// Extent set with current action
			HashSet<String> stateWithAction = new HashSet<String>( state );
			stateWithAction.add( action );
			
			List<String> inferences = metaKB.get( KnowledgeType.MOVE_REL ).reasoning( stateWithAction );
			
			// TODO: SHOULD ALSO CHECK FOR ORIENTATION CHANGE HERE!!!
			if( (inferences.size() == 1) && inferences.contains( MOVEMENT_NONE ) )
				filterActions.add( action );
		}

		actionsToBeFiltered.removeAll( filterActions );
	}
	
	
	public static void main(String[] args) 
	{
		GVGAIBot bot = new GVGAIBot( "continuationset_onegame.csv" );
			
		HashSet<String> state = new HashSet<String>();
		state.add( "c_Orientation0_0__0_0" );
		state.add( "c_PosX16" );
		state.add( "c_PosY4" );
		state.add( "c_AboveOfP0" );
		state.add( "c_BelowOfP3" );
		state.add( "c_RightOfP5" );
		state.add( "c_LeftOfP5" );
		
		long millisec = System.currentTimeMillis();
		String nextAction = bot.nextAction( bot.metaKB, state );
		long millisecDelta = System.currentTimeMillis() - millisec;
		System.out.println( "nextAction: " + nextAction );
		System.out.println( "Needed inference time in millisec: " + millisecDelta );
		
		bot.printKnowledge();
	}
}
