package kbextraction;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;


/**
 * The class implements a multi-abstraction-level knowledge base. 
 * 
 * @author Daan Apeldoorn
 */
public class MultiAbstractionLevelKB 
{
	/**	The list of rule lists where rule lists with lower indices contain more general rules. */
	LinkedList<LinkedList<Rule>> ruleLists;
	

	/**
	 * Creates the knowledge base.
	 */
	public MultiAbstractionLevelKB()
	{
		ruleLists = new LinkedList<LinkedList<Rule>>();
	}
	

	/**
	 * Creates a copy of the given knowledge base.
	 * 
	 * @param kb  the knowledge base to be copied
	 */
	public MultiAbstractionLevelKB( MultiAbstractionLevelKB kb )
	{
		this( kb, false );
	}


	/**
	 * Creates a copy of the given knowledge base. Drops all weight information (by setting every weight to 1.0) if dropWeights is true.
	 * 
	 * @param kb           the knowledge base to be copied
	 * @param dropWeights  if true, all weight information is dropped (by setting every weight to 1.0)
	 */
	public MultiAbstractionLevelKB( MultiAbstractionLevelKB kb, boolean dropWeights )
	{
		this();
		
		for( int i = 0; i < kb.getNoOfLevels(); i++ )
		{
			for( Rule rule : kb.getLevel( i ) )
			{
				Rule newRule = new Rule( new HashSet<String>( rule.getPremise() ), rule.getConclusion(), (dropWeights ? 1.0 : rule.getWeight()) );
				addRule( newRule );
			}
		}
	}
	
	
	/**
	 * Creates the knowledge base by reading the file determined by the given path and filename.
	 * 
	 * @param filename  the path and filename of the file to be read
	 */
	public MultiAbstractionLevelKB( String filename )
	{
		// Do basic initialization
		this();
		
		try 
		{
			// Create reader
			BufferedReader reader = new BufferedReader( new FileReader( filename ) );
		    			
			// Read the first line
			String line = reader.readLine();
		    
		    // Go through every line
		    while( line != null ) 
		    {		    	
		    	// Skip empty lines and comments (comment is "%")
		    	if( !line.equals( "" ) && !line.startsWith( "%" ) )
		    	{
			    	// Split up rules (separated by ",")
			    	String[] rules = line.split( "," );
			    	for( String rule : rules )
			    	{
			    		// Split premise and conclusion (separated by implication "=>")
			    		String[] premiseConclusion = rule.split( "=>" );
			    		
			    		// If there is no premise, just take the conclusion...
			    		HashSet<String> premise = new HashSet<String>();
			    		String[] values = null;
			    		String conclusionWithOrWithoutWeight = null;
			    		if( premiseConclusion.length == 1 )
			    			conclusionWithOrWithoutWeight = premiseConclusion[ 0 ].trim();
			    		
			    		// ...else split conjunction of the premise in the (sensor) values/partial states
			    		// and get the conclusion
			    		else
			    		{
			    			values = premiseConclusion[ 0 ].split( "\\^" );
				    		for( String value : values )
				    			premise.add( value.trim() );
			    			conclusionWithOrWithoutWeight = premiseConclusion[ 1 ].trim();
			    		}			    		

			    		// If a weight was annotated to the rule, extract it
			    		String[] conclusionWeight = conclusionWithOrWithoutWeight.split( "\\[" ); 
			    		String conclusion = conclusionWeight[ 0 ].trim(); 
			    		String weight = "1.0"; 
			    		if( conclusionWeight.length > 1 )
			    			weight = conclusionWeight[ 1 ].split( "\\]" )[ 0 ].trim();
			    		
			    		// Create and add the complete rule
			    		Rule newRule = new Rule( premise, conclusion, Double.parseDouble( weight ) );
			    		this.addRule( newRule );
			    	}
		    	}
		    	
		    	// Read next line
		    	line = reader.readLine();
		    }

		    // Close reader
		    reader.close();
		} 
		catch( FileNotFoundException e ) 
		{
			e.printStackTrace();
		} 
		catch( IOException e ) 
		{
			e.printStackTrace();
		}
	}
	
	
	/**
	 * Writes the knowledge base to the given path and filename.
	 * 
	 * @param filename  the path and filename of the file to be written to
	 */
	public void writeToFile( String filename )
	{
		try 
		{
			// Create reader
			BufferedWriter writer = new BufferedWriter( new FileWriter( filename ) );
		    
			// Write KB to file
			writer.append( toString() );
			
		    // Close writer
			writer.close();
		} 
		catch( FileNotFoundException e ) 
		{
			e.printStackTrace();
		} 
		catch( IOException e ) 
		{
			e.printStackTrace();
		}
	}
	
	
	/**
	 * Adds a rule to the knowledge base.
	 * The rule is automatically added to the right level of abstraction (depending on the rule's premise)
	 * and a the missing abstractions level are created if necessary.
	 * 
	 * @param rule  the rule to be added to the knowledge base
	 */
	public void addRule( Rule rule )
	{
		while( ruleLists.size() < rule.getPremise().size() + 1 )
			ruleLists.add( new LinkedList<Rule>() );
		ruleLists.get( rule.getPremise().size() ).add( rule );
	}
		
	
	/**
	 * Returns the number of abstraction levels in the knowledge base.
	 * 
	 * @return  the number of abstraction levels
	 */
	public int getNoOfLevels()
	{
		return ruleLists.size();
	}
	
	
	/**
	 * Returns the rule list of the i-th abstraction level of the knowledge base.
	 * 
	 * @param i  the index of the abstraction level to be returned
	 * @return   the abstraction level
	 */
	public LinkedList<Rule> getLevel( int i )
	{
		return ruleLists.get( i );
	}

	
	/**
	 * Performs the reasoning algorithm by returning a list of conclusions that fit best to the given
	 * knowledge. 
	 * Usually one conclusion is returned but in case more than one firing rules have the same weight
	 * all of their conclusions are returned.  
	 * 
	 * @param knowledge  the knowledge to be put into the knowledge base for reasoning
	 * @return           the reasoned conclusions
	 * @see              #reasoning(Set, Set)
	 */
	public LinkedList<String> reasoning( Set<String> knowledge )
	{
		return reasoning( knowledge, null );
	}
	

	/**
	 * Performs the reasoning algorithm by returning a list of conclusions that fit best to the given
	 * knowledge. 
	 * Usually one conclusion is returned but in case more than one firing rules have the same weight
	 * all of their conclusions are returned.  
	 * If a rule set is given as second parameter, the firing rules are added to this set. This may
	 * be used to trace which rules where responsible for providing the given conclusion(s). 
	 * 
	 * @param knowledge    the knowledge to be put into the knowledge base for reasoning
	 * @param firingRules  the rule set to which the firing rules leading to the conclusion(s) will be added
	 * @return             the reasoned conclusions (may be empty in case no conclusions could be returned; e.g. if the knowledge base is empty or has no rule on the top level)
	 */
	public LinkedList<String> reasoning( Set<String> knowledge, Set<Rule> firingRules )
	{
		LinkedList<Rule> potentialFiringRules = new LinkedList<Rule>(); 
		int i = getNoOfLevels() - 1;
		while( potentialFiringRules.isEmpty() && (i >= 0) )
		{
			for( Rule rule : getLevel( i ) )
				if( knowledge.containsAll( rule.getPremise() ) )
					potentialFiringRules.add( rule );
			i--;
		}
		
		// Keep only the firing rules with the highest weight
		double maxWeight = Double.NEGATIVE_INFINITY;
		for( Rule rule : potentialFiringRules )
			if( maxWeight < rule.getWeight() )
				maxWeight = rule.getWeight();
		LinkedList<String> conclusions = new LinkedList<String>(); 
		for( int j = 0; j < potentialFiringRules.size(); j++ )
			if( potentialFiringRules.get( j ).getWeight() == maxWeight )
			{
				conclusions.add( potentialFiringRules.get( j ).getConclusion() );
				
				// Store the premises of the firing rules
				if( firingRules != null )
					firingRules.add( potentialFiringRules.get( j ) );
			}
		
		return conclusions;
	}
	
		
	/**
	 * Compares the knowledge base to the given knowledge base on a semantic level (i.e., the degree of equal conclusions given all combinations of the given sensor values).
	 * The measure is symmetrical. 
	 * 
	 * @param kb      the knowledge base to which this knowledge base will be compared
	 * @param states  the states based on which the semantical comparison is done
	 * @return        the similarity of the to knowledge bases between 0 and 1
	 */
	public double compareSemantical( MultiAbstractionLevelKB kb, Set<Set<String>> states )
	{
		int equalConcusionsCount = 0;
		for( Set<String> state : states )
		{
			if( this.reasoning( state ).equals( kb.reasoning( state ) ) )
				equalConcusionsCount++;
		}
		
		return ((double) equalConcusionsCount) / ((double) states.size());
	}
	
			
	/**
	 * Compares the knowledge base to the given knowledge base by counting how many rules are different. 
	 * The measure is asymmetrical. 
	 * 
	 * @param kb  the knowledge base to which this knowledge base will be compared
	 * @return    the ratio how many rules of this knowledge base are contained in the given knowledge base
	 */
	private double compareSyntacticalAsymmetric( MultiAbstractionLevelKB kb )
	{
		// Remove weight information
		MultiAbstractionLevelKB kb1 = new MultiAbstractionLevelKB( this, true );
		MultiAbstractionLevelKB kb2 = new MultiAbstractionLevelKB( kb, true );
		
		int rulesCount = 0;
		int equalRulesCount = 0;
		for( int i = 0; i < kb1.getNoOfLevels(); i++ )
		{
			for( Rule rule : kb1.ruleLists.get( i ) )
			{
				rulesCount++;
				if( i < kb2.getNoOfLevels() )
				{
					for( Rule rule2 : kb2.getLevel( i ) )
					{
						if( rule.equals( rule2 ) )
						{
							equalRulesCount++;
							break;
						}
					}
				}
			}
		}
		
		return ((double) equalRulesCount) / ((double) rulesCount);
	}
	

	/**
	 * Makes a syntactical comparison to the knowledge base based on different rules on the different levels of abstraction. 
	 * The measure is symmetrical. 
	 * 
	 * @param kb  the knowledge base to which this knowledge base will be compared
	 * @return    the similarity of the to knowledge bases between 0 and 1
	 */
	public double compareSyntactical( MultiAbstractionLevelKB kb )
	{
		return (compareSyntacticalAsymmetric( kb ) + kb.compareSyntacticalAsymmetric( this )) / 2;
	}	
	
	
	@Override
	public String toString() 
	{
		String result = new String();
		for( LinkedList<Rule> list : ruleLists )
		{	
			for( Rule rule : list )
				result += rule.toString() + "\n";
			result += "\n";
		}
			
		return result;
	}
}
