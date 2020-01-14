package kbextraction.agents;

import kbextraction.KBExtractor3D;
import kbextraction.MultiAbstractionLevelKB;
import kbextraction.SparseMatrix4D;
import kbextraction.tools.MatrixLoaderGVGAIBot_AbsoluteMovement;
import kbextraction.tools.MatrixLoaderGVGAIBot_RelativeMovementAbove;
import kbextraction.tools.MatrixLoaderGVGAIBot_RelativeMovementBelow;
import kbextraction.tools.MatrixLoaderGVGAIBot_RelativeMovementLeft;
import kbextraction.tools.MatrixLoaderGVGAIBot_RelativeMovementRight;
import kbextraction.tools.MatrixLoaderGVGAIBot_ScoreAbove;
import kbextraction.tools.MatrixLoaderGVGAIBot_ScoreBelow;
import kbextraction.tools.MatrixLoaderGVGAIBot_ScoreLeft;
import kbextraction.tools.MatrixLoaderGVGAIBot_ScoreRight;
import kbextraction.tools.MatrixLoaderGVGAIBot_WinAbove;
import kbextraction.tools.MatrixLoaderGVGAIBot_WinBelow;
import kbextraction.tools.MatrixLoaderGVGAIBot_WinLeft;
import kbextraction.tools.MatrixLoaderGVGAIBot_WinRight;




/**
 * The main class.
 * 
 * @author Daan Apeldoorn
 */
public class MainGVGAIBot
{
	
//	/** The random generator to be used. */
//	private static Random random = new Random();


	private static void filterRulesWithoutActionInPremise( MultiAbstractionLevelKB kb )
	{
		// Filter out all rules not containing an action in the premise
		for( int i = 1; i < kb.getNoOfLevels(); i++ )
			for( int j = 0; j < kb.getLevel( i ).size(); j++ )
			{
				boolean containsAction = false;
				for( String s : kb.getLevel( i ).get( j ).getPremise() )
				{
					if( s.contains( "Action" ) )
					{
						containsAction = true;
						break;
					}
				}
				
				if( !containsAction )
				{
					kb.getLevel( i ).remove( kb.getLevel( i ).get( j ) );
					j--;
				}
			}
	}
	
	
	public static MultiAbstractionLevelKB extractKB_AbsoluteMovement( String filename )
	{
		SparseMatrix4D matrix4D = MatrixLoaderGVGAIBot_AbsoluteMovement.loadMatrix( filename );

		KBExtractor3D.setEquivRuleSelectionMode( KBExtractor3D.EquivRulesSelectionMode.ALPHABETHICALLY );
		KBExtractor3D.setWeightsConsiderationMode( KBExtractor3D.WeightsConsiderationMode.ALL );
		KBExtractor3D.setFilterMode( KBExtractor3D.FilterMode.UNNEEDED_RULES );
		MultiAbstractionLevelKB kb = KBExtractor3D.extractKB( matrix4D );
		
		filterRulesWithoutActionInPremise( kb );
		
		return kb;
	}
	

	public static MultiAbstractionLevelKB extractKB_RelativeMovementAbove( String filename )
	{
		SparseMatrix4D matrix4D = MatrixLoaderGVGAIBot_RelativeMovementAbove.loadMatrix( filename );

		KBExtractor3D.setEquivRuleSelectionMode( KBExtractor3D.EquivRulesSelectionMode.ALPHABETHICALLY );
		KBExtractor3D.setWeightsConsiderationMode( KBExtractor3D.WeightsConsiderationMode.ALL );
		KBExtractor3D.setFilterMode( KBExtractor3D.FilterMode.UNNEEDED_RULES );
		MultiAbstractionLevelKB kb = KBExtractor3D.extractKB( matrix4D );
		
		filterRulesWithoutActionInPremise( kb );
		
		return kb;
	}
	

	public static MultiAbstractionLevelKB extractKB_RelativeMovementBelow( String filename )
	{
		SparseMatrix4D matrix4D = MatrixLoaderGVGAIBot_RelativeMovementBelow.loadMatrix( filename );

		KBExtractor3D.setEquivRuleSelectionMode( KBExtractor3D.EquivRulesSelectionMode.ALPHABETHICALLY );
		KBExtractor3D.setWeightsConsiderationMode( KBExtractor3D.WeightsConsiderationMode.ALL );
		KBExtractor3D.setFilterMode( KBExtractor3D.FilterMode.UNNEEDED_RULES );
		MultiAbstractionLevelKB kb = KBExtractor3D.extractKB( matrix4D );
		
		filterRulesWithoutActionInPremise( kb );
		
		return kb;
	}
	

	public static MultiAbstractionLevelKB extractKB_RelativeMovementLeft( String filename )
	{
		SparseMatrix4D matrix4D = MatrixLoaderGVGAIBot_RelativeMovementLeft.loadMatrix( filename );

		KBExtractor3D.setEquivRuleSelectionMode( KBExtractor3D.EquivRulesSelectionMode.ALPHABETHICALLY );
		KBExtractor3D.setWeightsConsiderationMode( KBExtractor3D.WeightsConsiderationMode.ALL );
		KBExtractor3D.setFilterMode( KBExtractor3D.FilterMode.UNNEEDED_RULES );
		MultiAbstractionLevelKB kb = KBExtractor3D.extractKB( matrix4D );
		
		filterRulesWithoutActionInPremise( kb );
		
		return kb;
	}
	

	public static MultiAbstractionLevelKB extractKB_RelativeMovementRight( String filename )
	{
		SparseMatrix4D matrix4D = MatrixLoaderGVGAIBot_RelativeMovementRight.loadMatrix( filename );

		KBExtractor3D.setEquivRuleSelectionMode( KBExtractor3D.EquivRulesSelectionMode.ALPHABETHICALLY );
		KBExtractor3D.setWeightsConsiderationMode( KBExtractor3D.WeightsConsiderationMode.ALL );
		KBExtractor3D.setFilterMode( KBExtractor3D.FilterMode.UNNEEDED_RULES );
		MultiAbstractionLevelKB kb = KBExtractor3D.extractKB( matrix4D );
		
		filterRulesWithoutActionInPremise( kb );
		
		return kb;
	}
	

	public static MultiAbstractionLevelKB extractKB_ScoreAbove( String filename )
	{
		SparseMatrix4D matrix4D = MatrixLoaderGVGAIBot_ScoreAbove.loadMatrix( filename );

		KBExtractor3D.setEquivRuleSelectionMode( KBExtractor3D.EquivRulesSelectionMode.ALPHABETHICALLY );
		KBExtractor3D.setWeightsConsiderationMode( KBExtractor3D.WeightsConsiderationMode.ALL );
		KBExtractor3D.setFilterMode( KBExtractor3D.FilterMode.UNNEEDED_RULES );
		MultiAbstractionLevelKB kb = KBExtractor3D.extractKB( matrix4D );
		
		filterRulesWithoutActionInPremise( kb );
		
		return kb;
	}
	

	public static MultiAbstractionLevelKB extractKB_ScoreBelow( String filename )
	{
		SparseMatrix4D matrix4D = MatrixLoaderGVGAIBot_ScoreBelow.loadMatrix( filename );

		KBExtractor3D.setEquivRuleSelectionMode( KBExtractor3D.EquivRulesSelectionMode.ALPHABETHICALLY );
		KBExtractor3D.setWeightsConsiderationMode( KBExtractor3D.WeightsConsiderationMode.ALL );
		KBExtractor3D.setFilterMode( KBExtractor3D.FilterMode.UNNEEDED_RULES );
		MultiAbstractionLevelKB kb = KBExtractor3D.extractKB( matrix4D );
		
		filterRulesWithoutActionInPremise( kb );
		
		return kb;
	}
	

	public static MultiAbstractionLevelKB extractKB_ScoreLeft( String filename )
	{
		SparseMatrix4D matrix4D = MatrixLoaderGVGAIBot_ScoreLeft.loadMatrix( filename );

		KBExtractor3D.setEquivRuleSelectionMode( KBExtractor3D.EquivRulesSelectionMode.ALPHABETHICALLY );
		KBExtractor3D.setWeightsConsiderationMode( KBExtractor3D.WeightsConsiderationMode.ALL );
		KBExtractor3D.setFilterMode( KBExtractor3D.FilterMode.UNNEEDED_RULES );
		MultiAbstractionLevelKB kb = KBExtractor3D.extractKB( matrix4D );
		
		filterRulesWithoutActionInPremise( kb );
		
		return kb;
	}

	
	public static MultiAbstractionLevelKB extractKB_ScoreRight( String filename )
	{
		SparseMatrix4D matrix4D = MatrixLoaderGVGAIBot_ScoreRight.loadMatrix( filename );

		KBExtractor3D.setEquivRuleSelectionMode( KBExtractor3D.EquivRulesSelectionMode.ALPHABETHICALLY );
		KBExtractor3D.setWeightsConsiderationMode( KBExtractor3D.WeightsConsiderationMode.ALL );
		KBExtractor3D.setFilterMode( KBExtractor3D.FilterMode.UNNEEDED_RULES );
		MultiAbstractionLevelKB kb = KBExtractor3D.extractKB( matrix4D );
		
		filterRulesWithoutActionInPremise( kb );
		
		return kb;
	}
	

	public static MultiAbstractionLevelKB extractKB_WinAbove( String filename )
	{
		SparseMatrix4D matrix4D = MatrixLoaderGVGAIBot_WinAbove.loadMatrix( filename );

		KBExtractor3D.setEquivRuleSelectionMode( KBExtractor3D.EquivRulesSelectionMode.ALPHABETHICALLY );
		KBExtractor3D.setWeightsConsiderationMode( KBExtractor3D.WeightsConsiderationMode.ALL );
		KBExtractor3D.setFilterMode( KBExtractor3D.FilterMode.UNNEEDED_RULES );
		MultiAbstractionLevelKB kb = KBExtractor3D.extractKB( matrix4D );
		
		filterRulesWithoutActionInPremise( kb );
		
		return kb;
	}
	

	public static MultiAbstractionLevelKB extractKB_WinBelow( String filename )
	{
		SparseMatrix4D matrix4D = MatrixLoaderGVGAIBot_WinBelow.loadMatrix( filename );

		KBExtractor3D.setEquivRuleSelectionMode( KBExtractor3D.EquivRulesSelectionMode.ALPHABETHICALLY );
		KBExtractor3D.setWeightsConsiderationMode( KBExtractor3D.WeightsConsiderationMode.ALL );
		KBExtractor3D.setFilterMode( KBExtractor3D.FilterMode.UNNEEDED_RULES );
		MultiAbstractionLevelKB kb = KBExtractor3D.extractKB( matrix4D );
		
		filterRulesWithoutActionInPremise( kb );
		
		return kb;
	}
	
	
	public static MultiAbstractionLevelKB extractKB_WinLeft( String filename )
	{
		SparseMatrix4D matrix4D = MatrixLoaderGVGAIBot_WinLeft.loadMatrix( filename );

		KBExtractor3D.setEquivRuleSelectionMode( KBExtractor3D.EquivRulesSelectionMode.ALPHABETHICALLY );
		KBExtractor3D.setWeightsConsiderationMode( KBExtractor3D.WeightsConsiderationMode.ALL );
		KBExtractor3D.setFilterMode( KBExtractor3D.FilterMode.UNNEEDED_RULES );
		MultiAbstractionLevelKB kb = KBExtractor3D.extractKB( matrix4D );
		
		filterRulesWithoutActionInPremise( kb );
		
		return kb;
	}
	
	
	public static MultiAbstractionLevelKB extractKB_WinRight( String filename )
	{
		SparseMatrix4D matrix4D = MatrixLoaderGVGAIBot_WinRight.loadMatrix( filename );

		KBExtractor3D.setEquivRuleSelectionMode( KBExtractor3D.EquivRulesSelectionMode.ALPHABETHICALLY );
		KBExtractor3D.setWeightsConsiderationMode( KBExtractor3D.WeightsConsiderationMode.ALL );
		KBExtractor3D.setFilterMode( KBExtractor3D.FilterMode.UNNEEDED_RULES );
		MultiAbstractionLevelKB kb = KBExtractor3D.extractKB( matrix4D );
		
		filterRulesWithoutActionInPremise( kb );
		
		return kb;
	}
	
	
	
	/**
	 * The entry point.
	 * 
	 * @param args  arguments passed to the application (not used)
	 */
	public static void main( String[] args )
	{
		MultiAbstractionLevelKB kb = extractKB_AbsoluteMovement( "continuationset_onegame.csv" );
////		MultiAbstractionLevelKB kb2 = extractKB_RelativeMovement( "continuationset_onegame.csv" );
//		MultiAbstractionLevelKB kb3 = extractKB_RelativeMovementAbove( "continuationset_onegame.csv" );
//		MultiAbstractionLevelKB kb4 = extractKB_RelativeMovementBelow( "continuationset_onegame.csv" );
//		MultiAbstractionLevelKB kb5 = extractKB_RelativeMovementLeft( "continuationset_onegame.csv" );
//		MultiAbstractionLevelKB kb6 = extractKB_RelativeMovementRight( "continuationset_onegame.csv" );
////		MultiAbstractionLevelKB kb7 = extractKB_Score( "continuationset_onegame.csv" );
//		MultiAbstractionLevelKB kb8 = extractKB_ScoreAbove( "continuationset_onegame.csv" );
//		MultiAbstractionLevelKB kb9 = extractKB_ScoreBelow( "continuationset_onegame.csv" );
//		MultiAbstractionLevelKB kb10 = extractKB_ScoreLeft( "continuationset_onegame.csv" );
//		MultiAbstractionLevelKB kb11 = extractKB_ScoreRight( "continuationset_onegame.csv" );
//		MultiAbstractionLevelKB kb12 = extractKB_WinAbove( "continuationset_onegame.csv" );
//		MultiAbstractionLevelKB kb13 = extractKB_WinBelow( "continuationset_onegame.csv" );
//		MultiAbstractionLevelKB kb14 = extractKB_WinLeft( "continuationset_onegame.csv" );
//		MultiAbstractionLevelKB kb15 = extractKB_WinRight( "continuationset_onegame.csv" );
						
//		// Create sequence
//		List<Pair<List<String>, String>> stateActionSeq = new LinkedList<Pair<List<String>, String>>();
//		for( String key1 : matrix8D.getKeysX() )
//			for( String key2 : matrix8D.getKeysY( key1 ) )
//				for( String key3 : matrix8D.getKeysZ( key1, key2 ) )
//					for( String key4 : matrix8D.getKeysZ2( key1, key2, key3 ) )
//						for( String key5 : matrix8D.getKeysZ3( key1, key2, key3, key4 ) )
//							for( String key6 : matrix8D.getKeysZ4( key1, key2, key3, key4, key5 ) )
//								for( String key7 : matrix8D.getKeysZ5( key1, key2, key3, key4, key5, key6 ) )
//								{
//									List<String> premise = new LinkedList<String>();
//									premise.add( key1 );
//									premise.add( key2 );
//									premise.add( key3 );
//									premise.add( key4 );
//									premise.add( key5 );
//									premise.add( key6 );
//									premise.add( key7 );
//									
//									double maxValue = Double.NEGATIVE_INFINITY;
//									for( String key8 : matrix8D.getKeysZ6( key1, key2, key3, key4, key5, key6, key7 ) )
//										if( matrix8D.get( key1, key2, key3, key4, key5, key6, key7, key8 ) > maxValue )
//											maxValue = matrix8D.get( key1, key2, key3, key4, key5, key6, key7, key8 );
//									List<String> maxKeys = new LinkedList<String>();
//									for( String key8 : matrix8D.getKeysZ6( key1, key2, key3, key4, key5, key6, key7 ) )
//										if( matrix8D.get( key1, key2, key3, key4, key5, key6, key7, key8 ) == maxValue )
//											maxKeys.add( key8 );
//									String maxKey = maxKeys.get( random.nextInt( maxKeys.size() ) );
//									
//									Pair<List<String>, String> pair = new Pair<List<String>, String>( premise, maxKey );
//									
//									stateActionSeq.add( pair );
//								}
//										
//		MultiAbstractionLevelKB kb = AprioriMem.extractKB( stateActionSeq, 0 );
//				
//		

		System.out.println( kb );
		System.out.println();
		System.out.println( "RULE COUNTS:" );
		for( int i = 0; i < kb.getNoOfLevels(); i++ )
			System.out.println( kb.getLevel( i ).size() );
		System.out.println( "------------------------------------------------------------" );
	} 
}
