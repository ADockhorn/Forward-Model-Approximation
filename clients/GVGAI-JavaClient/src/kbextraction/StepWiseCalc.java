package kbextraction;

import java.util.ArrayList;
import java.util.List;

/**
 * This class provides several tools for stepwise
 * calculation within a certain time range.
 * 
 * @author Daan Apeldoorn
 *
 */
public class StepWiseCalc 
{
	/** Sets the DEBUG MODE. */
	public static boolean DEBUG_MODE = true;
	
	/** The percentage of extra time to compensate inaccuracies when estimating whether another iteration can be performed within the given time range. */
	public static final double ESTIMATION_BUFFER = 0.1; 

	/** The data object on which the stepwise calculation is performed. */
	static Object data = null;
	
	/** The stored indices of nested loops of the stepwise calculation. */
	static int[] indices = null;
	
	/** The number of the previous index stored. */
	static int previousIndexNo = -1;
	
	/** The time when the last index was stored. */
	static long previousIndexTime = -1;
	
	/** The time when the stepwise calculation started. */
	static long startTime = -1;
	
	/** The total time needed so far for the stepwise calculation. */
	static long totalIterationTime = 0;

	/** The number of values used for the total iteration time. */
	static int noOfTotalIterationTimeValues = 0;
	
	/** The list storing the names of already finished (parts of) calculations. */
	static List<String> finishedCalcs = new ArrayList<String>();
	
	/** The name of the current running calculation. */
	static String currentCalc = null;
	
	/** Indicates whether this is the first run inside the calculation identified by running calculation. */
	static boolean isFirstRun = false;
	
	
	
	/**
	 * Tries to start a new stepwise calculation and returns false if there is already one
	 * running on a different data object.
	 * 
	 * @param data     the object where the calculation runs on
	 * @param indices  the indices involved in (nested) iterations needed for the calculation 
	 * @return         whether or not a new stepwise calculation can be started
	 */
	public static boolean begin( String calcName, Object data, int[] indices )
	{
		if( (currentCalc == null) && !finishedCalcs.contains( calcName ) )
		{
			startTime = System.currentTimeMillis();
			
			currentCalc = calcName;
			
			StepWiseCalc.data = data;
			StepWiseCalc.indices = indices;
			
			isFirstRun = true;
			
			if( DEBUG_MODE )
				System.out.println( "Calculation " + calcName + " has been started (indices: " + indices + ")." );
			
			return true;
		}
		else if( calcName.equals( currentCalc ) )
		{
			startTime = System.currentTimeMillis();
			
			isFirstRun = false;
			
			if( DEBUG_MODE )
				System.out.println( "Calculation " + calcName + " will be continued " + "(indices: " + indices + ")." );
			
			return true;
		}
		else
		{
			if( DEBUG_MODE )
				System.out.println( "Another calculation (" + currentCalc + ") is already running; calculation " + calcName + " will be skipped." );
			
			return false;
		}
	}
	
		
	/**
	 * Indicates that a stepwise calculation is finished.
	 * Should be always called at the point in code where the calculation has ended. 
	 * The data object and all indices will be reset. 
	 */
	public static void end()
	{
		if( DEBUG_MODE )
			System.out.println( "Calculation " + currentCalc + " will end now (indices: " + indices + ")." );

		StepWiseCalc.data = null;
		StepWiseCalc.indices = null;

		previousIndexNo = -1;
		previousIndexTime = -1;
		startTime = -1;
		totalIterationTime = 0;
		noOfTotalIterationTimeValues = 0;
		
		// Mark the current calculation as finished
		finishedCalcs.add( currentCalc );
		currentCalc = null;
	}
	
	
	/**
	 * Resets the step wise calculation.
	 * This method should be called after the last call to end() when the calculation is completely finished and ready to be 
	 * restarted in the very first phase.
	 */
	public static void reset()
	{
		finishedCalcs.clear();

		if( DEBUG_MODE )
			System.out.println( "Step wise calculation process has been reset." );
	}
	
	
	
	/**
	 * Can be used to query whether a specific block of a stepwise calculation (e.g. some initialization code)
	 * should be skipped after being executed once. 
	 * 
	 * @return  whether or not it is allowed to execute the block of the calculation identified by currentCalc for a second time when it was executed once before.
	 */
	public static boolean isFirstRun()
	{
		if( DEBUG_MODE )
		{
			if( isFirstRun )
				System.out.println( "This is the first run of calculation " + currentCalc + "; initialization block will be executed." );
			else
				System.out.println( "This is NOT the first run of calculation " + currentCalc + "; initialization block will be skipped." );
		}

		return isFirstRun;
	}
	
	
	/**
	 * Returns the index identified by the given number.
	 * 
	 * @param no  the number of the index to be returned
	 * @return    the requested index value
	 */
	public static int getIndex( int no )
	{
		return( indices[ no ] );
	}
	
	
	/**
	 * Stores the given index identified by the given number.
	 * If the given index is the same than the one before, then the total time needed for
	 * this (most inner) iteration is taken for calculating the average time and later
	 * estimating whether another iteration can be performed.
	 * 
	 * @param no     the number of the index to be stored
	 * @param value  the index value
	 */
	public static void setIndex( int no, int value )
	{
		if( no == previousIndexNo )
		{
			totalIterationTime += System.currentTimeMillis() - previousIndexTime;
			noOfTotalIterationTimeValues++;
		}
		
		indices[ no ] = value;
		previousIndexNo = no;
		previousIndexTime = System.currentTimeMillis();
	}
	
	
	/**
	 * Returns the average iteration time measured for the most inner iteration.
	 * 
	 * @return  the average iteration time measured for the most inner iteration
	 */
	public static double getAvgIterationTime()
	{
		return ((double) totalIterationTime / (double) noOfTotalIterationTimeValues);
	}
	
	
	/**
	 * Returns the elapsed time since the stepwise calculation has been successfully started (or continued). 
	 * 
	 * @return  the elapsed time since the stepwise calculation has been successfully started (or continued)
	 */
	public static long getElapsedTime()
	{
		return System.currentTimeMillis() - startTime;
	}

	
	/**
	 * Estimated (including a certain buffer) whether it probably okay to perform a further step
	 * of the stepwise calculation.
	 * 
	 * @param millisec  the overall available milliseconds before it must be interrupted 
	 * @return          whether or not (according to the estimation) it is okay to perform a further step
	 */
	public static boolean isNextStepOK( int millisec )
	{
		if( DEBUG_MODE )
			System.out.print( "Time left: " + (millisec - getElapsedTime()) + ", " + "Estimated avg. iter. time: " + getAvgIterationTime() + ". " );

		if( (getElapsedTime() + getAvgIterationTime()) >= millisec * (1.0 - ESTIMATION_BUFFER) )
		{
			if( DEBUG_MODE )
				System.out.println( "Calculation will be interupted!" );

			return false;
		}
		else
		{
			if( DEBUG_MODE )
				System.out.println( "Going for another iteration!" );
			
			return true;
		}
	}
}
