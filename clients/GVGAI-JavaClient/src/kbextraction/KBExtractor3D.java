package kbextraction;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;


/**
 * This class implements the knowledge extraction algorithm on a 2-dimensional state space.
 * 
 * @author Daan Apeldoorn
 */
// REPLACE_EXTRACTOR public class KBExtractor$EDIM$D 
 public class KBExtractor3D 
{
	/**
	 * The modes for selecting which rule on the same abstraction level having the same weight will remain in the knowledge base:
	 * ALPHABETHICALLY:  keeps only the rule whose conclusion is on the top in ascending alphabetical order 
	 * RANDOM:           randomly selects one of the equivalent rules (makes the knowledge base creation non-deterministic but can be useful e.g. to avoid statistical distortions!); NOTE THAT THIS MODE WAS A BIT BUGGY BEFORE, SINCE IT COULD OCCUR THAT A RULE WAS SELECTED RANDOMLY ACCORDING TO ITS CONCLUSION AT ONE ABSTRACTION LEVEL AND NOT THE SAME RANDOM DECISION WAS MADE ON ANOTHER LEVEL (WHICH COULD LEAD TO STRANGE EXTRACTION RESULTS, BUT SHOULD BE FIXED NOW THROUGH THE CREATION OF A SPECIAL COMPARATOR AT THE BEGINNING OF THE KNOWLEDGE EXTRACTION)
	 */
	public static enum EquivRulesSelectionMode {ALPHABETHICALLY, RANDOM};
	
	/**
	 * The modes for selecting which weights will be considered during knowledge extraction:
	 * ALL:                         all weights will be considered (requires an agent to know the extends and/or values of the state-action space in advance) 
	 * LEARNED:                     only the weights that where learned (i.e. which where updated at least once) will be considered
	 * LEARNED_KNOWN_ACTION_SPACE:  only the weights that where learned (i.e. which where updated at least once) will be considered except for the z-dimension (actions) where all values will be considered (requires the agent to know the extend and/or values of the action space)
	 */
	public static enum WeightsConsiderationMode {ALL, LEARNED, LEARNED_KNOWN_ACTION_SPACE};
	
	/**
	 * The modes for filtering the knowledge base after extraction:
	 * NONE:            no filter will be applied 
	 * UNUSED_RULES:    unused rules (i.e. rules that never fire given the state space of the weight matrix) will be removed from the knowledge base 
	 * UNNEEDED_RULES:  unused rules will be removed from the knowledge base and in addition those firing rules will be removed for which other rules on the same abstraction level exist, which would do the same job in case the the firing rules were not present (so-called "co-rules", see below) 
	 */
	public static enum FilterMode {NONE, UNUSED_RULES, UNNEEDED_RULES};

	/** Determines the mode for selecting rules on the same abstraction level having the same weight. */
	private static EquivRulesSelectionMode equivRuleSelectionMode = EquivRulesSelectionMode.ALPHABETHICALLY;
	
	/** Determines the mode for which weights are considered by the knowledge extraction. */
	private static WeightsConsiderationMode weightsCosiderationMode = WeightsConsiderationMode.ALL;

	/** Determines the filter after knowledge extraction. */
	private static FilterMode filterMode = FilterMode.NONE;

	/** Defines all characters to be allowed in rules (i.e., characters that may be used for sensor values and conclusions). */
	private static final Character[] RULES_ALLOWED_CHARS = new Character[]{ 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '_' };

	/** The comparator to sort the conclusions of equivalent rules in a random but fixed order (the latter is important to avoid different random decisions on different levels which can lead to wrong/less compact extracted knowledge bases). */
	private static ArbitraryFixedOrderComparator<String> equivRulesRandomComparator;
	
	
	/**
	 * Sets the mode for selecting which rule on the same abstraction level having the same weight will remain in the knowledge base.
	 * 
	 * @param mode  the new mode to be set
	 */
	public static void setEquivRuleSelectionMode( EquivRulesSelectionMode mode )
	{
		equivRuleSelectionMode = mode;
	}
	

	/**
	 * Sets the mode for selecting which weights will be considered during knowledge extraction.
	 * 
	 * @param mode  the new mode to be set
	 */
	public static void setWeightsConsiderationMode( WeightsConsiderationMode mode )
	{
		weightsCosiderationMode = mode;
	}
	
	
	/**
	 * Sets the mode for filtering the knowledge base after extraction.
	 * 
	 * @param mode  the new mode to be set
	 */
	public static void setFilterMode( FilterMode mode )
	{
		filterMode = mode;
	}

	
	/**
	 * A comparator used to compare strings (e.g. of equivalent conclusions) for their alphabetical order
	 * against a given arbitrary but fixed alphabet. If a complete alphabet sorted as usual will be passed
	 * to the comparator, sorting should be the same than simply using java.util.Collections.sort without
	 * additional comparator argument.
	 * (This comparator is used here for sorting and selecting a conclusion among equivalent conclusions).
	 * 
	 * @author Daan Apeldoorn
	 *
	 * @param <T>  The type of the objects to be compared (must be String or derived types)
	 */
	private static class ArbitraryFixedOrderComparator<T> implements Comparator<T>
	{
		/** The alphabet based on which the comparison is done. */
		private List<Character> alphabet;
		
		/** 
		 * Creates the comparator.
		 * 
		 * @param alphabet  the alphabet based on which the comparison is done
		 */
		public ArbitraryFixedOrderComparator( List<Character> alphabet )
		{
			this.alphabet = alphabet;
		}
		
		@Override
		public int compare( T o1, T o2 ) 
		{
			// Convert given objects to strings
			String s1 = (String) o1;
			String s2 = (String) o2;

			// Iterate over the shorter string and immediately return the comparison result if
			// one was lower/higher according to the given alphabet 
			for( int i = 0; i < Math.min( s1.length(), s2.length() ); i++ )
			{
				if( alphabet.indexOf( s1.charAt( i ) ) < alphabet.indexOf( s2.charAt( i ) ) )
					return -1;
				else if( alphabet.indexOf( s1.charAt( i ) ) > alphabet.indexOf( s2.charAt( i ) ) )
					return 1;
			}
			
			// If reached to here, favor the shorter string
			if( s1.length() < s2.length() )
				return -1;
			else if( s1.length() > s2.length() )
				return 1;
			
			// ...else strings are identical
			return 0;
		}
		
	}
	
	
	/**
	 * Implements the selection in case of equivalent conclusions (by rules on the same level of abstraction with the same premises and weights) 
	 * depending on the currently set selection mode.
	 * The method gets a list of equivalent conclusions stemming from the same premise and having the same weight.
	 * The method returns one of the conclusions which will determine the rule to be kept in the knowledge base.  
	 * 
	 * @param equivConclusions  the equivalent conclusions from which a conclusions has to be selected to identify a rule to be kept 
	 * @return                  
	 */
	private static String equivConclusionSelection( List<String> equivConclusions )
	{
		switch( equivRuleSelectionMode )
		{
			case ALPHABETHICALLY:

				// Sort the conclusions alphabetically and return the top conclusions
				java.util.Collections.sort( equivConclusions );
				return equivConclusions.get( 0 );
			
			case RANDOM:

				// Sort the conclusions according to a fixed random alphabet and return the top conclusion
				java.util.Collections.sort( equivConclusions, equivRulesRandomComparator );
				return equivConclusions.get( 0 );
		}
		
		return null;
	}
		
	
	/**
	 * Returns the keys of the y-dimension of the sparse weight matrix.
	 * Depending on the mode, all keys are returned or only the weights 
	 * given a fixed key for the x-dimension are returned.
	 * 
	 * @param matrix  the weight matrix 
	 * @param x       the given key for the x-dimension
	 * @return        the key set for the y-dimension
	 */
// REPLACE_MATRIX	static private Set<String> getKeysY( SparseMatrix$MDIM$D matrix, String x )
	static private Set<String> getKeysY( SparseMatrix4D matrix, String x )
	{
		switch( weightsCosiderationMode )
		{
			case ALL:
				return matrix.getKeysY();
			
			case LEARNED: 
			case LEARNED_KNOWN_ACTION_SPACE:
				return matrix.getKeysY( x );
		}
		
		return null;
	}
	

	/**
	 * Returns the keys of the z-dimension of the sparse weight matrix.
	 * Depending on the mode, all keys are returned or only the weights 
	 * given a fixed key for the x- and the y-dimension are returned.
	 * 
	 * @param matrix  the weight matrix 
	 * @param x       the given key for the x-dimension
	 * @param y       the given key for the y-dimension
	 * @return        the key set for the z-dimension
	 */
// REPLACE_MATRIX	private static Set<String> getKeysZ( SparseMatrix$MDIM$D matrix, String x, String y )
	private static Set<String> getKeysZ( SparseMatrix4D matrix, String x, String y )
	{
		switch( weightsCosiderationMode )
		{
			case ALL:
				return matrix.getKeysZ();
			
			case LEARNED:
			case LEARNED_KNOWN_ACTION_SPACE:
				return matrix.getKeysZ( x, y );
		}
		
		return null;
	}	


	/**
	 * Returns the keys of the z2-dimension of the sparse weight matrix.
	 * Depending on the mode, all keys are returned or only the weights 
	 * given a fixed key for the x, y, z-dimension are returned.
	 * 
	 * @param matrix  the weight matrix 
	 * @param x       the given key for the x-dimension
	 * @param y       the given key for the y-dimension
	 * @param z       the given key for the z-dimension
	 * @return        the key set for the z2-dimension
	 */
// REPLACE_MATRIX	private static Set<String> getKeysZ2( SparseMatrix$MDIM$D matrix, String x, String y, String z )
	private static Set<String> getKeysZ2( SparseMatrix4D matrix, String x, String y, String z )
	{
		switch( weightsCosiderationMode )
		{
			case ALL:
// REMOVE_ONCE_LINE
			case LEARNED_KNOWN_ACTION_SPACE:
				return matrix.getKeysZ2();
			
			case LEARNED:
// ADD_ONCE_CASELABEL			case LEARNED_KNOWN_ACTION_SPACE:
				return matrix.getKeysZ2( x, y, z );
		}
		
		return null;
	}
// ADD_GETKEYS
//
//
//	/**
//	 * Returns the keys of the z$Z2$-dimension of the sparse weight matrix.
//	 * Depending on the mode, all keys are returned or only the weights 
//	 * given a fixed key for the x, y, z$COMMA_Z$-dimension are returned.
//	 * 
//	 * @param matrix  the weight matrix 
//	 * @param x       the given key for the x-dimension
//	 * @param y       the given key for the y-dimension
//	 * @param z       the given key for the z-dimension
//	 * @param z$Z$       the given key for the z$Z$-dimension
//	 * @return        the key set for the z$Z2$-dimension
//	 */
//// REPLACE_MATRIX	private static Set<String> getKeysZ$Z2$( SparseMatrix$VAR_MDIM$D matrix, String x, String y, String z$PARAM_Z$ )
//	private static Set<String> getKeysZ$Z2$( SparseMatrix$MDIM$D matrix, String x, String y, String z$PARAM_Z$ )
//	{
//		switch( weightsCosiderationMode )
//		{
//			case ALL:
//// REMOVE_ONCE_LINE
//			case LEARNED_KNOWN_ACTION_SPACE:
//				return matrix.getKeysZ$Z2$();
//			
//			case LEARNED:
//// ADD_ONCE_CASELABEL			case LEARNED_KNOWN_ACTION_SPACE:
//				return matrix.getKeysZ$Z2$( x, y, z$COMMA_Z$ );
//		}
//		
//		return null;
//	}
// END
	
	
	/**
	 * Performs a filter to eliminate additional rules from the knowledge base.
	 * 
	 * @param kb            the knowledge base to be filtered
	 * @param weightMatrix  the corresponding weight matrix from which the knowledge base was extracted 
	 */
// REPLACE_MATRIX	private static void filter( MultiAbstractionLevelKB kb, SparseMatrix$MDIM$D weightMatrix )
	private static void filter( MultiAbstractionLevelKB kb, SparseMatrix4D weightMatrix )
	{
		switch( filterMode )
		{
			case UNUSED_RULES:
				removeUnusedRules( kb, weightMatrix );
				return;
			
			case UNNEEDED_RULES:
				removeUnusedRules( kb, weightMatrix );
				removeUnneededRules( kb, weightMatrix );
				return;
			
			case NONE:
				return;
		}
	}
	
	
	/**
	 * Converts a string array to a an ArrayList of strings.
	 * This serves as a short cut to create keys for hash maps (which is often used here). 
	 * 
	 * @param components  the string array to be converted
	 * @return            an ArrayList containing the strings of the given string array
	 */
	private static ArrayList<String> key( String[] components )
	{
		return new ArrayList<String>( Arrays.asList( components ) );
	}
	
	
	/**
	 * Updates the weight sum and the weight count of a given subset, which is needed 
	 * to calculate average weights for the rule aggregation.
	 * The order of the values provided in the subset is of importance: {x, y} and {y, x}
	 * will result in two different weight sums/counts, thus the same subset should always be
	 * provided with the same order of variables.
	 *  
	 * @param weightSums    the weight sums of which the one for the given subset will be updated 
	 * @param weightCounts  the weight counts of which the one for the given subset will be updated
	 * @param subset        the subset for which the weight sum and count will be updated
	 * @param weight        the weight of the rule belonging to the subset which will be used to update the weight sum
	 */
	private static void updateWeightSumAndCount( HashMap<ArrayList<String>,Double> weightSums, HashMap<ArrayList<String>,Integer> weightCounts, String[] subset, double weight )
	{
		if( !weightSums.containsKey( key( subset ) ) )
		{
			weightSums.put( key( subset ), 0.0 );
			weightCounts.put( key( subset ), 0 );
		}
		weightSums.put( key( subset ), weightSums.get( key( subset ) ) + weight );
		weightCounts.put( key( subset ), weightCounts.get( key( subset ) ) + 1 );
	}
	

	/**
	 * Create the power set of the given set of integers.
	 * 
	 * @param set  the set of which the power set is created
	 * @return     the power set created from the given set 
	 */
	private static List<List<Integer>> powerSet( Integer[] set )
	{
		List<List<Integer>> result = new LinkedList<List<Integer>>();
		return powerSet( Arrays.asList( set ), result );
	}
	
	
	/**
	 * Create the power set of the given set of integers recursively.
	 * 
	 * @param set     the set of which the power set is created
	 * @param result  the power set created from the given set used to collect the subsets during recursion
	 * @return        the power set created from the given set
	 */
	private static List<List<Integer>> powerSet( List<Integer> set, List<List<Integer>> result )
	{
		// If the given set is not empty
		if( set.size() > 0 )
		{
			// Get the first element
			Integer firstElement = set.get( 0 );
			
			// Calculate the power set for the remaining set (without the first element)
			List<List<Integer>> powerSet = new ArrayList<List<Integer>>( powerSet( set.subList( 1, set.size() ), result ) ); 
			
			// Add all sets of the power set of the remaining set together with refrained the first element
			// to the result set
			for( List<Integer> currentSet : powerSet )
			{
				List<Integer> newSet = new ArrayList<Integer>();
				newSet.add( firstElement );
				newSet.addAll( currentSet );
				result.add( newSet );
			}
		}
		
		// ...else (if the given set is empty), add the empty set to the result set
		else
			result.add( new ArrayList<Integer>() );
		
		return result;
	}
	
	
	/**
	 * Normalizes the weights of the last dimension of the given weight matrix such that the 
	 * the weight of the best action(s) is 1. 
	 * 
	 * @param weightMatrix  the matrix to be normalized
	 * @return              the normalized matrix
	 */
// REPLACE_MATRIX	public static SparseMatrix$MDIM$D normalize( SparseMatrix$MDIM$D weightMatrix )
	public static SparseMatrix4D normalize( SparseMatrix4D weightMatrix )
	{
		// Create a copy of the matrix to work on
// REPLACE_MATRIX		SparseMatrix$MDIM$D normalizedMatrix = new SparseMatrix$MDIM$D( weightMatrix ); 
		SparseMatrix4D normalizedMatrix = new SparseMatrix4D( weightMatrix ); 
		
		// Iterate over the first two dimensions
		for( String x : normalizedMatrix.getKeysX() )
			for( String y : getKeysY( normalizedMatrix, x ) )
				for( String z : getKeysZ( normalizedMatrix, x, y ) )
// ADD_FORZ				$INDENT$for( String z$Z$ : getKeysZ$Z$( normalizedMatrix, x, y$COMMA_Z-1$ ) )
			{
				// Determine the minimal and the maximal value over the third dimension
				double minValue = Double.POSITIVE_INFINITY;
				double maxValue = Double.NEGATIVE_INFINITY;
// REPLACE_GETKEYS				for( String z$Z2$ : getKeysZ$Z2$( normalizedMatrix, x, y, z$COMMA_Z$ ) )
				for( String z2 : getKeysZ2( normalizedMatrix, x, y, z ) )
				{
// REPLACE_Z2					double value = normalizedMatrix.get( x, y, z, z2$COMMA_Z2$ ); 
					double value = normalizedMatrix.get( x, y, z, z2 ); 
					if( value < minValue )
						minValue = value;
					if( value > maxValue )
						maxValue = value;
				}
				
				// If the minimal value is less than 0, shift all values by the minimal value to positive				
				if( minValue < 0 )
				{
// REPLACE_FORZ2					for( String z$Z2$ : getKeysZ$Z2$( normalizedMatrix, x, y, z$COMMA_Z$ ) )
					for( String z2 : getKeysZ2( normalizedMatrix, x, y, z ) )
// REPLACE_Z2						normalizedMatrix.set( x, y, z, z2$COMMA_Z2$, normalizedMatrix.get( x, y, z, z2$COMMA_Z2$ ) + Math.abs( minValue ) );
						normalizedMatrix.set( x, y, z, z2, normalizedMatrix.get( x, y, z, z2 ) + Math.abs( minValue ) );
				
					// Shift the maximum value as well
					maxValue += Math.abs( minValue );
				}
				
				// Divide every value of the third dimension by the maximum values 
				// (if the maximal value is 0, set all values of the third dimension to 1)
// REPLACE_FORZ2				for( String z$Z2$ : getKeysZ$Z2$( normalizedMatrix, x, y, z$COMMA_Z$ ) )
				for( String z2 : getKeysZ2( normalizedMatrix, x, y, z ) )
					if( maxValue != 0 )
// REPLACE_Z2						normalizedMatrix.set( x, y, z, z2$COMMA_Z2$, normalizedMatrix.get( x, y, z, z2$COMMA_Z2$ ) / maxValue );
						normalizedMatrix.set( x, y, z, z2, normalizedMatrix.get( x, y, z, z2 ) / maxValue );
					else
// REPLACE_Z2						normalizedMatrix.set( x, y, z, z2$COMMA_Z2$, 1.0 );
						normalizedMatrix.set( x, y, z, z2, 1.0 );
			}
		
		return normalizedMatrix;
	}
	
	
	/**
	 * Creates the complete multi-abstraction-level knowledge base based on the given weight matrix.
	 * 
	 * @param weightMatrix  the weight matrix to be normalized
	 * @return              the complete multi-abstraction-level knowledge base
	 */
// REPLACE_MATRIX	private static MultiAbstractionLevelKB createInitialKB( SparseMatrix$MDIM$D weightMatrix )
	private static MultiAbstractionLevelKB createInitialKB( SparseMatrix4D weightMatrix )
	{
		// Sums of weights for every (partial) premise-conclusion combination
		HashMap<ArrayList<String>,Double> weightSums = new HashMap<ArrayList<String>,Double>();

		// Counts for every (partial) premise-conclusion combination
		HashMap<ArrayList<String>,Integer> weightCounts = new HashMap<ArrayList<String>,Integer>();
		
		// Create the empty multi-abstraction-level knowledge base
		MultiAbstractionLevelKB multiAbstractionLevelKB = new MultiAbstractionLevelKB(); 
		
		// Iterate over all dimensions of the weight matrix
// REPLACE_POWERSET		List<List<Integer>> powerSetIndices = powerSet( new Integer[]{ 0, 1, 2$COMMA_INT$ } );
		List<List<Integer>> powerSetIndices = powerSet( new Integer[]{ 0, 1, 2 } );
		for( String x : weightMatrix.getKeysX() )
			for( String y : getKeysY( weightMatrix, x ) )
				for( String z : getKeysZ( weightMatrix, x, y ) )
// REPLACE_FORZ					for( String z2 : getKeysZ2( weightMatrix, x, y, z ) )
					for( String a : getKeysZ2( weightMatrix, x, y, z ) )
// ADD_FORZ2
//// REPLACE_FORZ					$INDENT$for( String z$Z2$ : getKeysZ$Z2$( weightMatrix, x, y, z$COMMA_Z$ ) )
//					$INDENT$for( String a : getKeysZ$Z2$( weightMatrix, x, y, z$COMMA_Z$ ) )
// END
				{
					// Add/aggregate rules based on the subsets of all dimensions 
// REPLACE_Z					String[] allPartStates = new String[]{ x, y, z$COMMA_Z$ };
					String[] allPartStates = new String[]{ x, y, z };
// REPLACE_Z					double weight = weightMatrix.get( x, y, z$COMMA_Z$, a );
					double weight = weightMatrix.get( x, y, z, a );
					for( List<Integer> indexSubset : powerSetIndices )
					{
						// If this is the subset containing all partial states, 
						// add corresponding elementary rule for the current action
						if( indexSubset.size() == allPartStates.length )
						{
							HashSet<String> premise = new HashSet<String>();
							for( String partState : allPartStates )
								premise.add( partState );
							Rule rule = new Rule( premise, a, weight );
							multiAbstractionLevelKB.addRule( rule );
						}
						
						// ...else (if it is one of the other subsets), do calculation 
						// for aggregated more general rules
						else
						{
							// Create the subset based on the indices power set calculated before
							String[] subset = new String[ indexSubset.size() + 1 ];
							int i = 0;
							for( Integer index : indexSubset )
							{
								subset[ i ] = allPartStates[ index ];
								i++;
							}
							
							// Add the current action as last dimension
							subset[ subset.length - 1 ] = a;
							
							// Update the corresponding weight sum and count for the subset
							updateWeightSumAndCount( weightSums, weightCounts, subset, weight );
						}
					}
				}
		
		// Add aggregated more general rules
		for( ArrayList<String> key : weightSums.keySet() )
		{
			HashSet<String> premise = new HashSet<String>();
			for( int i = 0; i < key.size() - 1; i++ )
				premise.add( key.get( i ) );
			double weight = weightSums.get( key ) / weightCounts.get( key );
			Rule rule = new Rule( premise, key.get( key.size() - 1 ), weight );
			multiAbstractionLevelKB.addRule( rule );
		}
		
		return multiAbstractionLevelKB;
	}
	
	
	/**
	 * Removes all rules from the knowledge base which have a lower weight than other rules 
	 * on the same abstraction level having the same premise.
	 * 
	 * @param kb  the knowledge base from which the rules are removed
	 */
	private static void removeWorseRules( MultiAbstractionLevelKB kb )
	{
		// Iterate over all abstraction levels of the knowledge base
		for( int i = 0; i < kb.getNoOfLevels(); i++ )
		{
			// Calculate the maximal weights for all premises
			HashMap<HashSet<String>,Double> maxWeights = new HashMap<HashSet<String>,Double>();
			for( Rule rule : kb.getLevel( i ) )
			{
				if( (!maxWeights.containsKey( rule.getPremise() ) ) || (maxWeights.get( rule.getPremise() ) < rule.getWeight()) )
					maxWeights.put( rule.getPremise(), rule.getWeight() );
			}
			
			// Collect all conclusions per premise having a weight equal to the corresponding maximum weight
			HashMap<HashSet<String>,LinkedList<String>> maxConclusions = new HashMap<HashSet<String>,LinkedList<String>>(); 
			for( Rule rule : kb.getLevel( i ) )
			{
				if( rule.getWeight() == maxWeights.get( rule.getPremise() ) )
				{
					if( !maxConclusions.containsKey( rule.getPremise() ) )
						maxConclusions.put( rule.getPremise(), new LinkedList<String>() );
					maxConclusions.get( rule.getPremise() ).add( rule.getConclusion() );
				}
			}

			// Select one conclusion in every list of maximum conclusions
			// (The selection can be done alphabetically - which is done here at the moment, randomly or according to any other selection criterion)
			HashMap<HashSet<String>,String> selectedConclusions = new HashMap<HashSet<String>,String>(); 
			for( HashSet<String> premise : maxConclusions.keySet() )
				selectedConclusions.put( premise, equivConclusionSelection( maxConclusions.get( premise ) ) );
			
			// Remove all rules per premise except the one with the selected conclusion
			for( int j = 0; j < kb.getLevel( i ).size(); j++ )
			{
				if( !kb.getLevel( i ).get( j ).getConclusion().equals( selectedConclusions.get( kb.getLevel( i ).get( j ).getPremise() ) ) )
				{
					kb.getLevel( i ).remove( j );
					j--;
				}
			}
		}
	}
	

	/**
	 * Removes all rules from the knowledge base for which more general rules exist (i.e. rules whose 
	 * premise is a subset of the rules to be removed and which lead to the same conclusion).
	 * 
	 * @param kb  the knowledge base from which the rules are removed
	 */
	private static void removeTooSpecificRules( MultiAbstractionLevelKB kb )
	{
		// For every rule on the i-th abstraction level, iterate over all rules of all lower abstraction levels  
		for( int i = 0; i < kb.getNoOfLevels() - 1; i++ )
		{
			for( int j = i + 1; j < kb.getNoOfLevels(); j++ )
			{
				for( Rule genRule : kb.getLevel( i ) )
				{
					for( int k = 0; k < kb.getLevel( j ).size(); k++ )
					{
						// Get the more specific rule from the lower abstraction level
						Rule specRule = kb.getLevel( j ).get( k );

						
						//
						// Determine whether the rule to be removed is a needed exception to a rule of the next general level
						// (needed exception means that there is no other rule on the next general level with the same conclusion
						// than the rule to which the rule to be removed is an exception that has a higher weight and therefore
						// would lead to the same conclusion in the exceptional case)
						//
						
						// Determine the rules of the previous level to which the rule to be removed is an exception
						// and the those rules having the same conclusion than the rule to be removed and whose premise
						// is a subset of the rule to be removed
						LinkedList<Rule> genRulesPrevLevelWithExcept = new LinkedList<Rule>(); 
						LinkedList<Rule> genRulesPrevLevelSameConcl = new LinkedList<Rule>(); 
						for( Rule genRulePrevLevel : kb.getLevel( j - 1 ) )
						{	
							// If the rule on the next general level has a different conclusion than the more specific rule
							// and the premise of the rule on the next general level is a subset of the more specific rule's premise (or empty)
							// and the more specific rule has a higher weight than the rule on the next general level (this check is not necessary if removal of worse more specific rules is done before!),
							// than the more specific rule is an exception to the rule on the next general level
							if( !genRulePrevLevel.getConclusion().equals( specRule.getConclusion() ) && specRule.getPremise().containsAll( genRulePrevLevel.getPremise() ) && (specRule.getWeight() > genRulePrevLevel.getWeight()) )
								genRulesPrevLevelWithExcept.add( genRulePrevLevel );                                                                                       // This check is not necessary if removal of worse more specific rules is done before! 
							
							// ...else if the rule on the next general level has the same conclusion instead (no matter what weight),
							// the rule could potentially make an exception superfluous
							else if( genRulePrevLevel.getConclusion().equals( specRule.getConclusion() ) && specRule.getPremise().containsAll( genRulePrevLevel.getPremise() ) )
								genRulesPrevLevelSameConcl.add( genRulePrevLevel );                                                                                                                                          
						}
						
						// Determine the maximum weight of the rules on the next general level having an exception
						// and of the rules having the same conclusion than the more specific rule
						double maxWeightWithExcept = Double.NEGATIVE_INFINITY;
						for( Rule genRulePrevLevelWithExcept : genRulesPrevLevelWithExcept )
							if( maxWeightWithExcept < genRulePrevLevelWithExcept.getWeight() )
								maxWeightWithExcept = genRulePrevLevelWithExcept.getWeight();
						double maxWeightSameConcl = Double.NEGATIVE_INFINITY;
						for( Rule genRulePrevLevelSameConcl : genRulesPrevLevelSameConcl )
							if( maxWeightSameConcl < genRulePrevLevelSameConcl.getWeight() )
								maxWeightSameConcl = genRulePrevLevelSameConcl.getWeight();
						
						// If there are any rules on the next higher level to which the more specific rule is an exception
						// and there aren't any rules having the same conclusion than the potential more specific exception rule
						// or the highest weight among the rules having the same conclusion than the potential more specific
						// exception rule is not bigger than the highest weight among all rules to which the more specific rule 
						// is an exception, then the potential more specific exception rule is a needed exception
						boolean isNeededException = false;
						if( !genRulesPrevLevelWithExcept.isEmpty() && (genRulesPrevLevelSameConcl.isEmpty() || (maxWeightSameConcl <= maxWeightWithExcept)) )
							isNeededException = true;
						
						// If the rule is not an exception to a more general rule on the next general level 
						// and the premise of the more general rule is a subset of the more specific rule's premise (or empty) and has the same conclusion, 
						// remove the more specific one 
						if( !isNeededException && genRule.getConclusion().equals( specRule.getConclusion() ) && specRule.getPremise().containsAll( genRule.getPremise() ) )
						{
							kb.getLevel( j ).remove( k );
							k--;
						}
					}
				}
			}
		}
	}
	

	/**
	 * Removes all rules from the knowledge base for which more general rules exist having an equal or
	 * higher weight (i.e. rules with a higher weight whose premise is a subset of the rules to be removed).
	 * 
	 * @param kb  the knowledge base from which the rules are removed
	 */
	public static void removeWorseMoreSpecificRules( MultiAbstractionLevelKB kb )
	{
		// For every rule on the i-th abstraction level, iterate over all rules of all lower abstraction levels  
		for( int i = 0; i < kb.getNoOfLevels() - 1; i++ )
		{
			for( int j = i + 1; j < kb.getNoOfLevels(); j++ )
			{
				for( Rule genRule : kb.getLevel( i ) )
				{
					for( int k = 0; k < kb.getLevel( j ).size(); k++ )
					{
						// If the premise of the more general rule is a subset of the more specific rule (or empty) and has a higher weight, remove the more specific one 
						Rule specRule = kb.getLevel( j ).get( k );
						if( (genRule.getWeight() >= specRule.getWeight()) && specRule.getPremise().containsAll( genRule.getPremise() ) )
						{
							kb.getLevel( j ).remove( k );
							k--;
						}
					}
				}
			}
		}
	}
	
	
	/**
	 * Removes all rules from the knowledge base that never fire according to the reasoning algorithm
	 * given all states of the given weight matrix. 
	 * Usually calling this function makes only sense if only the optimal policy from starting state
	 * to the terminal state is contained the given matrix (since otherwise the algorithm will be 
	 * computational expensive since it has to iterate over all states).
	 * 
	 * @param kb            the knowledge base from which the unused rules will be removed
	 * @param weightMatrix  the matrix to retrieve the states from
	 */
// REPLACE_MATRIX	private static void removeUnusedRules( MultiAbstractionLevelKB kb, SparseMatrix$MDIM$D weightMatrix )
	private static void removeUnusedRules( MultiAbstractionLevelKB kb, SparseMatrix4D weightMatrix )
	{
		// Iterate over every known state of the given weight matrix and 
		// collect all firing rules 
		Set<Rule> firingRules = new HashSet<Rule>();
		for( String x : weightMatrix.getKeysX() )
			for( String y : getKeysY( weightMatrix, x ) )
				for( String z : getKeysZ( weightMatrix, x, y ) )
// ADD_FORZ				$INDENT$for( String z$Z$ : getKeysZ$Z$( weightMatrix, x, y$COMMA_Z-1$ ) )
			{
				Set<String> premise = new HashSet<String>();
				premise.add( x );
				premise.add( y );
				premise.add( z );
// ADD_Z				premise.add( z$Z$ );
				kb.reasoning( premise, firingRules );
			}
		
		// Iterate over every rule in the knowledge base and delete it if 
		// it did not fire 
		for( int i = 0; i < kb.getNoOfLevels() - 1; i++ )
			for( int j = 0; j < kb.getLevel( i ).size(); j++ )
				if( !firingRules.contains( kb.getLevel( i ).get( j ) ) )
				{
					kb.getLevel( i ).remove( j );
					j--;
				}
	}
	
	
	/**
	 * Removes all firing rules which fire always in the same state with so-called "co-rules" 
	 * (which are rules	with the same conclusion on the same abstraction level as the firing rule 
	 * that do not fire at the same time with a firing rule but would fire instead if the firing 
	 * rule was not present).
	 * 
	 * @param kb            the knowledge base from which the unneeded rules will be removed
	 * @param weightMatrix  the matrix to retrieve the states from
	 */
// REPLACE_MATRIX	private static void removeUnneededRules( MultiAbstractionLevelKB kb, SparseMatrix$MDIM$D weightMatrix )
	private static void removeUnneededRules( MultiAbstractionLevelKB kb, SparseMatrix4D weightMatrix )
	{
		// Iterate over every known state of the given weight matrix and store for every firing 
		// rules whether it fires together with so-called co-rules (see definition above):
		// Rules that never fire without corresponding co-rules are unneeded.
		HashMap<Rule,Boolean> firingRules = new HashMap<Rule,Boolean>();
		for( String x : weightMatrix.getKeysX() )
			for( String y : getKeysY( weightMatrix, x ) )
				for( String z : getKeysZ( weightMatrix, x, y ) )
// ADD_FORZ				$INDENT$for( String z$Z$ : getKeysZ$Z$( weightMatrix, x, y$COMMA_Z-1$ ) )
			{
				// Create premise from current state from the weight matrix
				Set<String> premise = new HashSet<String>();
				premise.add( x );
				premise.add( y );
				premise.add( z );
// ADD_Z				premise.add( z$Z$ );
				
				// Get the firing rules given the premise
				HashSet<Rule> currentFiringRules = new HashSet<Rule>();
				kb.reasoning( premise, currentFiringRules );
				
				// Iterate over every firing rule
				for( Rule currentFiringRule : currentFiringRules )
				{
					// If the firing rule is not yet contained in the set of firing rules,
					// add it but mark it as potentially being removed (false)
					if( !firingRules.containsKey( currentFiringRule ) )
						firingRules.put( currentFiringRule, false );
					
					// Create a set of all co-rules
					HashSet<Rule> coRules = new HashSet<Rule>(); 
					for( Rule rule : kb.getLevel( currentFiringRule.getPremise().size() ) )
						if(    (rule != currentFiringRule) && premise.containsAll( rule.getPremise() ) 
							&& rule.getConclusion().equals( currentFiringRule.getConclusion() ) && (!currentFiringRules.contains( rule )) )
						{
							coRules.add( rule );
						}
					
					// If there are no co-rules, then mark the firing rule as okay
					if( coRules.isEmpty() )
						firingRules.put( currentFiringRule, true );
				}
			}
		
		// Remove all firing rule from the knowledge base, that never fired without a co-rule
		for( Rule rule : firingRules.keySet() )
			if( firingRules.get( rule ) == false )
				kb.getLevel( rule.getPremise().size() ).remove( rule );
	}
	
	
	/**
	 * Extracts a knowledge base from the given weight matrix.
	 * 
	 * @param weightMatrix  the matrix from which the knowledge base is extracted
	 * @return              the extracted knowledge base
	 */
// REPLACE_MATRIX	public static MultiAbstractionLevelKB extractKB( SparseMatrix$MDIM$D weightMatrix )
	public static MultiAbstractionLevelKB extractKB( SparseMatrix4D weightMatrix )
	{
		// Create the random comparator for sorting the conclusions of equivalent rules in random but fixed order
		// (Note that the list is copied since lists created by Arrays.asList write through to the array and we don't want the array of allowed rule characters to be modified.)
		List<Character> randomAlphabet = new ArrayList<Character>( Arrays.asList( RULES_ALLOWED_CHARS ) );
		java.util.Collections.shuffle( randomAlphabet );
		equivRulesRandomComparator = new ArbitraryFixedOrderComparator<String>( randomAlphabet );
				
// REPLACE_MATRIX		SparseMatrix$MDIM$D normalizedMatrix = normalize( weightMatrix ); 
		SparseMatrix4D normalizedMatrix = normalize( weightMatrix ); 

		MultiAbstractionLevelKB kb = createInitialKB( normalizedMatrix );
		
		removeWorseRules( kb );

		removeWorseMoreSpecificRules( kb );

		// Must be done as the last step to avoid exception rules remaining in the KB 
		// for more general rules that will be removed later (therefore swapped with previous step)
		removeTooSpecificRules( kb );

		// Apply (optional) filters
		filter( kb, normalizedMatrix );
		
		return kb;
	}


	/**
	 * Extracts a knowledge base from the given state-action sequence also considering
	 * the weight matrix (in case the state-action sequence contains multiple times the 
	 * same state with different actions).
	 * Using this method, all possible actions should be known and passed to the function 
	 * (for mode LEARNED_KNOWN_ACTION_SPACE) or the knowledge extraction should be called
	 * in mode ALL (or ALL_ACTION_SPACE if this mode will exist in future versions).  
	 * 
	 * @param stateActionSeq   the state-action sequence from which the knowledge base is extracted
	 * @param weightMatrix     the matrix containing the weights that will be considered in case states are passed multiple times with different actions in the given state-action sequence
	 * @param possibleActions  all possible actions
	 * @return                 the extracted knowledge base
	 */
// REPLACE_MATRIX	public static MultiAbstractionLevelKB extractKB( List<Pair<List<String>,String>> stateActionSeq, SparseMatrix$MDIM$D weightMatrix, HashSet<String> possibleActions )
	public static MultiAbstractionLevelKB extractKB( List<Pair<List<String>,String>> stateActionSeq, SparseMatrix4D weightMatrix, HashSet<String> possibleActions )
	{
		// Create a new matrix storing the given state action sequence only
// REPLACE_CREATEMATRIX		SparseMatrix$MDIM$D bestStatActSeqMatrix = new SparseMatrix$MDIM$D( null, null, null$COMMA_NULL$, possibleActions );
		SparseMatrix4D bestStatActSeqMatrix = new SparseMatrix4D( null, null, null, possibleActions );

		// Normalize the weight matrix
// REPLACE_MATRIX		SparseMatrix$MDIM$D normalizedMatrix = normalize( weightMatrix );
		SparseMatrix4D normalizedMatrix = normalize( weightMatrix );

		// Add the best state-action sequence to the matrix using the learned weights from the current (normalized) weight matrix
		// (the weights will be normalized to 1 in most cases - except if the same state is traversed multiple times by the best state-action sequence)
		for( Pair<List<String>,String> stateAction : stateActionSeq )
// REPLACE_BESTSTATEACTSEQMATRIX			bestStatActSeqMatrix.set( stateAction.getFirst().get( 0 ), stateAction.getFirst().get( 1 ), stateAction.getFirst().get( 2 )$COMMA_PARTSTATE$, stateAction.getSecond(), /*1.0*/ normalizedMatrix.get( stateAction.getFirst().get( 0 ), stateAction.getFirst().get( 1 ), stateAction.getFirst().get( 2 )$COMMA_PARTSTATE$, stateAction.getSecond() ) );
			bestStatActSeqMatrix.set( stateAction.getFirst().get( 0 ), stateAction.getFirst().get( 1 ), stateAction.getFirst().get( 2 ), stateAction.getSecond(), /*1.0*/ normalizedMatrix.get( stateAction.getFirst().get( 0 ), stateAction.getFirst().get( 1 ), stateAction.getFirst().get( 2 ), stateAction.getSecond() ) );
				
		// Apply the knowledge extraction algorithm to the new matrix only containing the best state-action sequence
		return extractKB( bestStatActSeqMatrix );
	}
}
