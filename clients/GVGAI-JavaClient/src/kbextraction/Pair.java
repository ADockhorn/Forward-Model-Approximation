package kbextraction;


/**
 * A simple lightweight pair class.
 * 
 * @author Daan Apeldoorn
 */
public class Pair<T1,T2>
{
	@Override
	public String toString() {
		return "(" + first + ", " + second + ")";
	}


	/** The pair's first element. */
	T1 first;

	/** The pair's second element. */
	T2 second;
	

	/**
	 * Creates a new pair object. 
	 * 
	 * @param first   the first element of pair
	 * @param second  the second element of pair
	 */
	public Pair( T1 first, T2 second )
	{
		this.first = first;
		this.second = second;
	}
	
	
	/**
	 * Returns the first element of the pair.
	 * 
	 * @return  the first element of the pair 
	 */
	public T1 getFirst()
	{
		return first;
	}


	/**
	 * Returns the second element of the pair.
	 * 
	 * @return  the second element of the pair
	 */
	public T2 getSecond()
	{
		return second;
	}
}
