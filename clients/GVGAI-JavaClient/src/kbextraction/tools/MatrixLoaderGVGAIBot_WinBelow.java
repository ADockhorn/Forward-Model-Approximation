package kbextraction.tools;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import kbextraction.SparseMatrix4D;


public class MatrixLoaderGVGAIBot_WinBelow 
{
	public static SparseMatrix4D loadMatrix( String filename )
	{
		// Create empty sparse matrix
		SparseMatrix4D matrix = new SparseMatrix4D();
		
		try 
		{
			// Create reader and writer
			BufferedReader reader = new BufferedReader( new FileReader( filename ) );
		    
			// Skip the first line
			String line = reader.readLine();

			// Read the first data line
			line = reader.readLine();
		    
		    // Go through every line
		    while( line != null ) 
		    {
		    	// Split up components
		    	String[] lineComponents = line.split( " " );
		    	String c_Orientation = null;
		    	String c_BelowOfP = null;
		    	String c_Action = null;
		    	String n_GameState = null;
		    	for( String comp : lineComponents )
		    	{
		    		// Skip empty fields
		    		if( comp.equals( "" ) )
		    			continue;
		    		
		    		if( comp.contains( "c_Orientation" ) )
		    		{
		    			c_Orientation = comp.replace( "=", "" );
		    			
		    			// Remove bad symbols
		    			c_Orientation = c_Orientation.replace( ".", "_" );
		    			c_Orientation = c_Orientation.replace( "/", "__" );
		    			c_Orientation = c_Orientation.replace( ",", "__" );
		    		}
		    		if( comp.contains( "c_BelowOfP" ) )
		    		{
		    			c_BelowOfP = comp.replace( "=", "" );
		    		}
		    		if( comp.contains( "n_GameState" ) )
		    		{
		    			n_GameState = comp.replace( "=", "" );

		    			// Map actions to numbers to be compatible with the faster algorithm
		    			n_GameState = n_GameState.replace( "NO_WINNER", "0" );
		    			n_GameState = n_GameState.replace( "PLAYER_WINS", "1" );
		    			n_GameState = n_GameState.replace( "PLAYER_LOSES", "2" );
		    		}
		    		if( comp.contains( "c_Action" ) )
		    		{
		    			c_Action = comp.replace( "=", "" );
		    			
		    			// Map actions to numbers to be compatible with the faster algorithm
		    			c_Action = c_Action.replace( "ACTION_NIL", "0" );
		    			c_Action = c_Action.replace( "ACTION_USE", "1" );
		    			c_Action = c_Action.replace( "ACTION_RIGHT", "2" );
		    			c_Action = c_Action.replace( "ACTION_LEFT", "3" );
		    			c_Action = c_Action.replace( "ACTION_UP", "4" );
		    			c_Action = c_Action.replace( "ACTION_DOWN", "5" );
		    		}
		    	}
		    	
		    	String action = n_GameState;
		    	
		    	// Replace null values
		    	if( c_BelowOfP == null )
		    		c_BelowOfP = "c_BelowOfP999";
		    	
		    	double value = matrix.get( c_BelowOfP, c_Orientation, c_Action, action );
		    	matrix.set( c_BelowOfP, c_Orientation, c_Action, action, value + 1.0 );
		    	
		    	// Read next line
		    	line = reader.readLine();
		    }

		    // Close reader and writer
		    reader.close();
		    
		    return matrix;
		} 
		catch( FileNotFoundException e ) 
		{
			e.printStackTrace();
		} 
		catch( IOException e ) 
		{
			e.printStackTrace();
		}
		
		return null;
	}
}
