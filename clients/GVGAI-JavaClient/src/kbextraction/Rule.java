package kbextraction;

import java.util.ArrayList;
import java.util.HashSet;


/**
 * An Object of this class represents a symbolic rule of the form: 
 * premise => conclusion [weight] 
 * where premise is a conjunction p1 ^ ... ^ pn or empty (which means 
 * that the conclusion can be considered a fact in this case). 
 * 
 * @author Daan Apeldoorn
 */
public class Rule
{
	/** The rule's premise. */
	HashSet<String> premise;

	/** The rule's conclusion. */
	String conclusion;

	/** The rule's weight. */
	double weight;
	
	
	/**
	 * Creates a new rule with the given premise, conclusion and weight.
	 * 
	 * @param premise     the set of atoms representing the premise of the rule
	 * @param conclusion  the rule's conclusion
	 * @param weight      the rule's weight 
	 */
	public Rule( HashSet<String> premise, String conclusion, double weight )
	{
		// If no premise is given, create an empty premise (i.e. the rule 
		// will count as a fact)
		if( premise != null )
			this.premise = premise;
		else
			this.premise = new HashSet<String>();
		
		// Store the rule's conclusion and weight 
		this.conclusion = conclusion;
		this.weight = weight;
	}


	/**
	 * Returns the rule's premise.
	 * 
	 * @return  a set of atoms representing the rule's premise (empty set in case the rule is a fact)
	 */
	public HashSet<String> getPremise() 
	{
		return premise;
	}


	/**
	 * Set's the rule's premise.
	 * 
	 * @param premise  a set of atoms representing the rule's new premise
	 */
	public void setPremise( HashSet<String> premise ) 
	{
		this.premise = premise;
	}


	/**
	 * Returns the rule's conclusion.
	 * 
	 * @return  the rule's conclusion
	 */
	public String getConclusion() 
	{
		return conclusion;
	}


	/**
	 * Set's the rule's conclusion.
	 * 
	 * @param conclusion  the rule's new conclusion
	 */
	public void setConclusion( String conclusion ) 
	{
		this.conclusion = conclusion;
	}


	/**
	 * Returns the rule's weight.
	 * 
	 * @return  the rule's weight
	 */
	public double getWeight() 
	{
		return weight;
	}


	/**
	 * Sets the rule's weight.
	 * 
	 * @param weight  the rule's new weight
	 */
	public void setWeight( double weight ) 
	{
		this.weight = weight;
	}


	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((conclusion == null) ? 0 : conclusion.hashCode());
		result = prime * result + ((premise == null) ? 0 : premise.hashCode());
		long temp;
		temp = Double.doubleToLongBits(weight);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		return result;
	}


	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Rule other = (Rule) obj;
		if (conclusion == null) {
			if (other.conclusion != null)
				return false;
		} else if (!conclusion.equals(other.conclusion))
			return false;
		if (premise == null) {
			if (other.premise != null)
				return false;
		} else if (!premise.equals(other.premise))
			return false;
		if (Double.doubleToLongBits(weight) != Double
				.doubleToLongBits(other.weight))
			return false;
		return true;
	}


	@Override
	public String toString() 
	{
		ArrayList<String> premiseAsList = new ArrayList<String>( premise );
		java.util.Collections.sort( premiseAsList );
		String result = "";
		for( String atom : premiseAsList )
			result += atom + " ^ ";
		if( !result.equals( "" ) )
		{
			result = result.substring( 0, result.lastIndexOf( " ^ " ) );
			result += " => ";
		}
		result += conclusion;
		result += " [" + weight + "]"; 
		return result;
	}
}
