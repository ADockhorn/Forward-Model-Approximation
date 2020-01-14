package kbextraction;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;


/**
 * Sparse implementation of a multi-dimensional matrix based on strings as keys.
 * The implementation is not very efficient and has to be optimized.
 * 
 * @author Daan Apeldoorn
 */
// REPLACE_MATRIX public class SparseMatrix$MDIM$D 
 public class SparseMatrix4D 
{
	/** The underlying hash map for implementing the sparse matrix. */
// REPLACE_HASHMAP	private HashMap<String, HashMap<String, HashMap<String, HashMap<String$COMMA_HASHMAPSTRING$, Double$RIGHT_ANGLEBRACKET$>>>> data;
	private HashMap<String, HashMap<String, HashMap<String, HashMap<String, Double>>>> data;

	/** The predefined x-size of the matrix. */
	private int sizeX; 

	/** The predefined y-size of the matrix. */
	private int sizeY; 

	/** The predefined z-size of the matrix. */
	private int sizeZ; 

	/** The predefined z2-size of the matrix. */
	private int sizeZ2; 
// ADD_SIZEZ2
//
//	/** The predefined z$Z2$-size of the matrix. */
//	private int sizeZ$Z2$; 
// END
	
	/** 
	 * The set of all x-keys currently contained in the matrix. 
	 * The set will contain dummy keys in case the matrix is initialized with -1 in 
	 * the x dimension and not all values of x have been added already. 
	 */
	private HashSet<String> keysX;

	/** 
	 * The set of all y-keys currently contained in the matrix. 
	 * The set will contain dummy keys in case the matrix is initialized with -1 in 
	 * the y dimension and not all values of y have been added already. 
	 */
	private HashSet<String> keysY;

	/** 
	 * The set of all z-keys currently contained in the matrix. 
	 * The set will contain dummy keys in case the matrix is initialized with -1 in 
	 * the z dimension and not all values of z have been added already. 
	 */
	private HashSet<String> keysZ;

	/** 
	 * The set of all z2-keys currently contained in the matrix. 
	 * The set will contain dummy keys in case the matrix is initialized with -1 in 
	 * the z2 dimension and not all values of z2 have been added already. 
	 */
	private HashSet<String> keysZ2;
// ADD_KEYSZ2
//
//	/** 
//	 * The set of all z$Z2$-keys currently contained in the matrix. 
//	 * The set will contain dummy keys in case the matrix is initialized with -1 in 
//	 * the z$Z2$ dimension and not all values of z$Z2$ have been added already. 
//	 */
//	private HashSet<String> keysZ$Z2$;
// END
	
	/** The default value to be returned if a value is requested for keys that are not present in the matrix. The default for this value is 0. */
	private double defaultValue;
	

	
	/**
	 * Creates a sparse matrix with dynamic extends.
	 */
// REPLACE_MATRIX	public SparseMatrix$MDIM$D()
	public SparseMatrix4D()
	{
// REPLACE_CONSTRUCTORCALL		this( -1, -1, -1, -1$COMMA_-1$ );
		this( -1, -1, -1, -1 );
	}


	/**
	 * Creates a sparse matrix with fixed predefined or dynamic extends.
	 * If one of the given sizes equals -1, the respective dimension will be of dynamic extend.
	 * In this case only values that have been added before will be considered 
	 * during iterations over the matrix when the respective getter methods (with parameters) are used.
	 * 
	 * @param sizeX  the predefined x-size of the matrix or -1
	 * @param sizeY  the predefined y-size of the matrix or -1
	 * @param sizeZ  the predefined z-size of the matrix or -1
	 * @param sizeZ2  the predefined z2-size of the matrix or -1
// ADD_PARAMSIZEZ2	 * @param sizeZ$Z2$  the predefined z$Z2$-size of the matrix or -1
	 */
// REPLACE_CONSTRUCTORHEADSIZE	public SparseMatrix$MDIM$D( int sizeX, int sizeY, int sizeZ, int sizeZ2$COMMA_INTSIZEZ2$ )
	public SparseMatrix4D( int sizeX, int sizeY, int sizeZ, int sizeZ2 )
	{
		// Store the size of the dimensions
		this.sizeX = sizeX;
		this.sizeY = sizeY;
		this.sizeZ = sizeZ;
		this.sizeZ2 = sizeZ2;
// ADD_THISSIZEZ2		this.sizeZ$Z2$ = sizeZ$Z2$;
		
		// Create the underlying data structure
// REPLACE_CREATEHASHMAP		data = new HashMap<String, HashMap<String, HashMap<String, HashMap<String$COMMA_HASHMAPSTRING$, Double$RIGHT_ANGLEBRACKET$>>>>();
		data = new HashMap<String, HashMap<String, HashMap<String, HashMap<String, Double>>>>();
		
		// Create the sets of all contained keys
		keysX = new HashSet<String>();
		keysY = new HashSet<String>();
		keysZ = new HashSet<String>();
		keysZ2 = new HashSet<String>();
// ADD_CREATEKEYSZ2		keysZ$Z2$ = new HashSet<String>();
		
		// In case of dynamic extends, fill the key sets with dummy keys
		if( sizeX != -1 )
			createDummyKeys( keysX, 0, sizeX );
		if( sizeY != -1 )
			createDummyKeys( keysY, 1, sizeY );
		if( sizeZ != -1 )
			createDummyKeys( keysZ, 2, sizeZ );
		if( sizeZ2 != -1 )
			createDummyKeys( keysZ2, 3, sizeZ2 );
// ADD_DUMMYKEYS
//		if( sizeZ$Z2$ != -1 )
//			createDummyKeys( keysZ$Z2$, $INT$, sizeZ$Z2$ );
// END
	
		// Set the default value to be returned for keys that are not contained in the matrix
		defaultValue = 0;
	}
	

	/**
	 * Creates a sparse matrix with fixed predefined or dynamic extends and values.
	 * If one of the given key sets is empty or null, the respective dimension will be of dynamic extend.
	 * In this case only values that have been added before will be considered 
	 * during iterations over the matrix when the respective getter methods (with parameters) are used.
	 * 
	 * @param keysX  the predefined x-key set of the matrix or null
	 * @param keysY  the predefined y-key set of the matrix or null
	 * @param keysZ  the predefined z-key set of the matrix or null
	 * @param keysZ2  the predefined z2-key set of the matrix or null
// ADD_PARAMZ2	 * @param keysZ$Z2$  the predefined z$Z2$-key set of the matrix or null
	 */
// REPLACE_CONTRUCTORHEADKEYS	public SparseMatrix$MDIM$D( HashSet<String> keysX, HashSet<String> keysY, HashSet<String> keysZ, HashSet<String> keysZ2$COMMA_HASHSETKEYSZ2$ )
	public SparseMatrix4D( HashSet<String> keysX, HashSet<String> keysY, HashSet<String> keysZ, HashSet<String> keysZ2 )
	{
		// Store/Create the sets of all contained keys
		this.keysX = keysX; 
		if( keysX == null )
			this.keysX = new HashSet<String>();
		this.keysY = keysY; 
		if( keysY == null )
			this.keysY = new HashSet<String>();
		this.keysZ = keysZ; 
		if( keysZ == null )
			this.keysZ = new HashSet<String>();
		this.keysZ2 = keysZ2; 
		if( keysZ2 == null )
			this.keysZ2 = new HashSet<String>();
// ADD_THISKEYSZ2
//		this.keysZ$Z2$ = keysZ$Z2$; 
//		if( keysZ$Z2$ == null )
//			this.keysZ$Z2$ = new HashSet<String>();
// END
		
		// Store the size of the dimensions
		sizeX = this.keysX.size();
		if( sizeX == 0 )
			sizeX = -1;
		sizeY = this.keysY.size();
		if( sizeY == 0 )
			sizeY = -1;
		sizeZ = this.keysZ.size();
		if( sizeZ == 0 )
			sizeZ = -1;
		sizeZ2 = this.keysZ2.size();
		if( sizeZ2 == 0 )
			sizeZ2 = -1;
// ADD_THISSIZEFROMKEYSZ2
//		sizeZ$Z2$ = this.keysZ$Z2$.size();
//		if( sizeZ$Z2$ == 0 )
//			sizeZ$Z2$ = -1;
// END

		// Create the underlying data structure
// REPLACE_CREATEHASHMAP		data = new HashMap<String, HashMap<String, HashMap<String, HashMap<String$COMMA_HASHMAPSTRING$, Double$RIGHT_ANGLEBRACKET$>>>>();
		data = new HashMap<String, HashMap<String, HashMap<String, HashMap<String, Double>>>>();

		// Set the default value to be returned for keys that are not contained in the matrix
		defaultValue = 0;
	}
	
	
	/**
	 * Creates a new copy of the given sparse matrix. 
	 * 
	 * @param sparseMatrix  the matrix to be copied
	 */
// REPLACE_MATRIX	public SparseMatrix$MDIM$D( SparseMatrix$MDIM$D sparseMatrix )
	public SparseMatrix4D( SparseMatrix4D sparseMatrix )
	{
		// Copy the dimension sizes
		this.sizeX = sparseMatrix.sizeX;
		this.sizeY = sparseMatrix.sizeY;
		this.sizeZ = sparseMatrix.sizeZ;
		this.sizeZ2 = sparseMatrix.sizeZ2;
// ADD_THISSIZEZ2		this.sizeZ$Z2$ = sparseMatrix.sizeZ$Z2$;
		
		// Create a new underlying data structure
// REPLACE_CREATEHASHMAP		data = new HashMap<String, HashMap<String, HashMap<String, HashMap<String$COMMA_HASHMAPSTRING$, Double$RIGHT_ANGLEBRACKET$>>>>();
		data = new HashMap<String, HashMap<String, HashMap<String, HashMap<String, Double>>>>();

		// Copy the key sets
		this.keysX = new HashSet<String>();
		for( String x : sparseMatrix.keysX )
			this.keysX.add( x );
		this.keysY = new HashSet<String>();
		for( String y : sparseMatrix.keysY )
			this.keysY.add( y );
		this.keysZ = new HashSet<String>();
		for( String z : sparseMatrix.keysZ )
			this.keysZ.add( z );
		this.keysZ2 = new HashSet<String>();
		for( String z2 : sparseMatrix.keysZ2 )
			this.keysZ2.add( z2 );
// ADD_THISCREATEKEYSZ2
//		this.keysZ$Z2$ = new HashSet<String>();
//		for( String z$Z2$ : sparseMatrix.keysZ$Z2$ )
//			this.keysZ$Z2$.add( z$Z2$ );
// END
		
		// Copy the data structure
		for( String x : sparseMatrix.data.keySet() )
			for( String y : sparseMatrix.data.get( x ).keySet() )
				for( String z : sparseMatrix.data.get( x ).get( y ).keySet() )
					for( String z2 : sparseMatrix.data.get( x ).get( y ).get( z ).keySet() )
// ADD_FORZ2					$INDENT$for( String z$Z2$ : sparseMatrix.data.get( x ).get( y ).get( z )$DOT_GETZ$.keySet() )
// REPLACE_SETZ2						$INDENT$set( x, y, z, z2$COMMA_Z2$, sparseMatrix.data.get( x ).get( y ).get( z ).get( z2 )$DOT_GETZ2$ );
						set( x, y, z, z2, sparseMatrix.data.get( x ).get( y ).get( z ).get( z2 ) );

		// Copy the default value to be returned for keys that are not contained in the matrix
		this.defaultValue = sparseMatrix.defaultValue;
	}
	
	
	/**
	 * Returns the x-size of the matrix.
	 * In case a fixed x-size is set in advance, the fixed x-size is returned. Otherwise, the current x-size is returned.
	 * 
	 * @return  the x-size of the matrix
	 */
	public int getSizeX() 
	{
		return getKeysX().size();
	}


	/**
	 * Returns the y-size of the matrix.
	 * In case a fixed y-size is set in advance, the fixed y-size is returned. Otherwise, the current y-size is returned.
	 * 
	 * @return  the y-size of the matrix
	 */
	public int getSizeY() 
	{
		return getKeysY().size();
	}

	
	/**
	 * Returns the z-size of the matrix.
	 * In case a fixed z-size is set in advance, the fixed z-size is returned. Otherwise, the current z-size is returned.
	 * 
	 * @return  the z-size of the matrix
	 */
	public int getSizeZ() 
	{
		return getKeysZ().size();
	}


	/**
	 * Returns the z2-size of the matrix.
	 * In case a fixed z2-size is set in advance, the fixed z2-size is returned. Otherwise, the current z2-size is returned.
	 * 
	 * @return  the z2-size of the matrix
	 */
	public int getSizeZ2() 
	{
		return getKeysZ2().size();
	}
// ADD_GETSIZEZ2
//
//
//	/**
//	 * Returns the z$Z2$-size of the matrix.
//	 * In case a fixed z$Z2$-size is set in advance, the fixed z$Z2$-size is returned. Otherwise, the current z$Z2$-size is returned.
//	 * 
//	 * @return  the z$Z2$-size of the matrix
//	 */
//	public int getSizeZ$Z2$() 
//	{
//		return getKeysZ$Z2$().size();
//	}
// END

	
	/**
	 * Returns the keys for the x-dimension of the matrix. 
	 * In case a fixed x-size was set in advance, the missing keys are filled up with artificial keys.
	 * 
	 * @return  the keys for the x-dimension of the matrix.
	 */
	public Set<String> getKeysX()
	{
		return keysX;
	}
	

	/**
	 * Returns the keys for the y-dimension of the matrix. 
	 * In case a fixed y-size was set in advance, the missing keys are filled up with artificial keys.
	 * 
	 * @return  the keys for the y-dimension of the matrix.
	 */
	public Set<String> getKeysY()
	{
		return keysY;
	}


	/**
	 * Returns the keys for the y-dimension of the matrix. 
	 * Only the known keys given a fixed value of the x-dimension are returned. 
	 * In case an unknown value of the x-dimension is provided, an empty key set is returned.
	 * This is the method to be used when the matrix has a dynamic extend in the y-dimension. 
	 * 
	 * @param x  value of the x-dimension for which the y-keys should be returned 
	 * @return   the y-keys given the fixed value of the x-dimension or an empty key set in case an unknown x-value was given 
	 */
	public Set<String> getKeysY( String x )
	{
		if( data.containsKey( x ) )
			return data.get( x ).keySet();
		else
			return new HashSet<String>();
	}
	
	
	/**
	 * Returns the keys for the z-dimension of the matrix. 
	 * In case a fixed z-size was set in advance, the missing keys are filled up with artificial keys.
	 * 
	 * @return  the keys for the z-dimension of the matrix.
	 */
	public Set<String> getKeysZ()
	{
		return keysZ;
	}
	

	/**
	 * Returns the keys for the z-dimension of the matrix. 
	 * Only the known keys given fixed values of the x- and y-dimensions are returned. 
	 * In case unknown values of the x- or y-dimension are provided, an empty key set is returned.
	 * This is the method to be used when the matrix has a dynamic extend in the z-dimension. 
	 * 
	 * @param x  value of the x-dimension 
	 * @param y  value of the y-dimension 
	 * @return   the z-keys given the fixed value of the x- and y-dimension or an empty key set in case an unknown x- and/or y-value was given
	 */
	public Set<String> getKeysZ( String x, String y )
	{
		if( data.containsKey( x ) && data.get( x ).containsKey( y ) )
			return data.get( x ).get( y ).keySet();
		else
			return new HashSet<String>();
	}


	/**
	 * Returns the keys for the z2-dimension of the matrix. 
	 * In case a fixed z2-size was set in advance, the missing keys are filled up with artificial keys.
	 * 
	 * @return  the keys for the z2-dimension of the matrix.
	 */
	public Set<String> getKeysZ2()
	{
		return keysZ2;
	}


	/**
	 * Returns the keys for the z2-dimension of the matrix. 
	 * Only the known keys given fixed values of the x, y, z-dimensions are returned. 
	 * In case unknown values of one of the x, y, z-dimension are provided, an empty key set is returned.
	 * This is the method to be used when the matrix has a dynamic extend in the z2-dimension. 
	 * 
	 * @param x  value of the x-dimension 
	 * @param y  value of the y-dimension 
	 * @param z  value of the z-dimension 
	 * @return   the z2-keys given the fixed value of the x, y, z-dimension or an empty key set in case an unknown x, y, z-value was given
	 */
	public Set<String> getKeysZ2( String x, String y, String z )
	{
		if(    data.containsKey( x ) 
		    && data.get( x ).containsKey( y )
		    && data.get( x ).get( y ).containsKey( z )
		  )
			return data.get( x ).get( y ).get( z ).keySet();
		else
			return new HashSet<String>();
	}
// ADD_GETKEYSZ2
//
//
//	/**
//	 * Returns the keys for the z$Z2$-dimension of the matrix. 
//	 * In case a fixed z$Z2$-size was set in advance, the missing keys are filled up with artificial keys.
//	 * 
//	 * @return  the keys for the z$Z2$-dimension of the matrix.
//	 */
//	public Set<String> getKeysZ$Z2$()
//	{
//		return keysZ$Z2$;
//	}
//
//
//	/**
//	 * Returns the keys for the z$Z2$-dimension of the matrix. 
//	 * Only the known keys given fixed values of the x, y, z$COMMA_Z$-dimensions are returned. 
//	 * In case unknown values of one of the x, y, z$COMMA_Z$-dimension are provided, an empty key set is returned.
//	 * This is the method to be used when the matrix has a dynamic extend in the z$Z2$-dimension. 
//	 * 
//	 * @param x  value of the x-dimension 
//	 * @param y  value of the y-dimension 
//	 * @param z  value of the z-dimension 
//	 * @param z$Z$  value of the z$Z$-dimension 
//	 * @return   the z$Z2$-keys given the fixed value of the x, y, z$COMMA_Z$-dimension or an empty key set in case an unknown x, y, z$COMMA_Z$-value was given
//	 */
//	public Set<String> getKeysZ$Z2$( String x, String y, String z$COMMA_STRINGZ$ )
//	{
//		if(    data.containsKey( x ) 
//		    && data.get( x ).containsKey( y )
//		    && data.get( x ).get( y ).containsKey( z )
//		    && data.get( x ).get( y )$DOT_GETZ-1$.containsKey( z$Z$ )
//		  )
//			return data.get( x ).get( y ).get( z )$DOT_GETZ$.keySet();
//		else
//			return new HashSet<String>();
//	}
// END

	
	/**
	 * Returns a value of the matrix identified by the given keys. 
	 * In case one of the given keys is not contained in the respective key set, the current default value is returned.
	 * 
	 * @param x  the x-key of the value in the matrix
	 * @param y  the y-key of the value in the matrix
	 * @param z  the z-key of the value in the matrix
	 * @param z2  the z2-key of the value in the matrix
// ADD_PARAMZ2	 * @param z$Z2$  the z$Z2$-key of the value in the matrix
	 * @return   the value located at the given position; 0 if one of the given keys is not contained in the respective key set 
	 */
// REPLACE_STRINGZ2	public double get( String x, String y, String z, String z2$COMMA_STRINGZ2$ )
	public double get( String x, String y, String z, String z2 )
	{
		if(    data.containsKey( x )
		    && data.get( x ).containsKey( y )
		    && data.get( x ).get( y ).containsKey( z )
		    && data.get( x ).get( y ).get( z ).containsKey( z2 )
// ADD_ANDCONTAINSZ2		    && data.get( x ).get( y ).get( z )$DOT_GETZ$.containsKey( z$Z2$ )
		  )
		{
// REPLACE_GETZ2			return data.get( x ).get( y ).get( z ).get( z2 )$DOT_GETZ2$;
			return data.get( x ).get( y ).get( z ).get( z2 );
		}
		else
		{
			return defaultValue;
		}
	}

	
	/**
	 * Sets a value in the matrix at the position specified by the given keys. 
	 * 
	 * @param x  the x-key of the value in the matrix
	 * @param y  the y-key of the value in the matrix
	 * @param z  the z-key of the value in the matrix
	 * @param z2  the z2-key of the value in the matrix
// ADD_PARAMZ2	 * @param z$Z2$  the z$Z2$-key of the value in the matrix
	 * @param value  the value to be set at the position specified by the given keys
	 */
// REPLACE_STRINGZ2	public void set( String x, String y, String z, String z2$COMMA_STRINGZ2$, double value )
	public void set( String x, String y, String z, String z2, double value )
	{
		if( !data.containsKey( x ) )
		{
// REPLACE_HASHMAP			data.put( x, new HashMap<String, HashMap<String, HashMap<String$COMMA_HASHMAPSTRING$, Double$RIGHT_ANGLEBRACKET$>>>() );
			data.put( x, new HashMap<String, HashMap<String, HashMap<String, Double>>>() );
			addKey( keysX, x );
		}

		if( !data.get( x ).containsKey( y ) )
		{
// REPLACE_HASHMAP			data.get( x ).put( y, new HashMap<String, HashMap<String$COMMA_HASHMAPSTRING$, Double$RIGHT_ANGLEBRACKET$>>() );
			data.get( x ).put( y, new HashMap<String, HashMap<String, Double>>() );
			addKey( keysY, y );
		}

		if( !data.get( x ).get( y ).containsKey( z ) )
		{
// REPLACE_HASHMAP			data.get( x ).get( y ).put( z, new HashMap<String$COMMA_HASHMAPSTRING$, Double$RIGHT_ANGLEBRACKET$>() );
			data.get( x ).get( y ).put( z, new HashMap<String, Double>() );
			addKey( keysZ, z );
		}
// ADD_ADDKEY
//
//		if( !data.get( x ).get( y )$DOT_GETZ-1$.containsKey( z$Z$ ) )
//		{
//// REPLACE_HASHMAP			data.get( x ).get( y )$DOT_GETZ-1$.put( z$Z$, new HashMap<String$VAR_COMMA_HASHMAPSTRING$, Double$VAR_RIGHT_ANGLEBRACKET$>() );
//			data.get( x ).get( y )$DOT_GETZ-1$.put( z$Z$, new HashMap<String, Double>() );
//			addKey( keysZ$Z$, z$Z$ );
//		}
// END

// REPLACE_PUTVALUE		data.get( x ).get( y ).get( z )$DOT_GETZ$.put( z$Z2$, value );
		data.get( x ).get( y ).get( z ).put( z2, value );
// REPLACE_ADDKEY		addKey( keysZ$Z2$, z$Z2$ );
		addKey( keysZ2, z2 );
	}	


	/**
	 * Sets the default value to be returned if a value is requested for keys that are not stored 
	 * in the matrix.
	 * (This can be used, e.g., to adapt the weights of unseen states, e.g. to the lowest local reward
	 * seen so far by an agent, see [Tokic et al.]. E.g.: (1) set the default value to the minimal local reward seen so far, (2) do decision making, (3) reset to 0 as default value.)
	 * 
	 * @param value  the new value to be returned if a value for keys that are not stored yet in the matrix is requested
	 */
	public void setDefaultValue( double value )
	{
		defaultValue = value;
	}
	
	
	/**
	 * Fills the given key set of the given dimension with artificial keys until it has the given.  
	 * 
	 * @param keys   the key set to be filled up
	 * @param dimNo  the no. of the dimension to which the key set belongs
	 * @param size   the predefined size of the corresponding dimension
	 */
	private void createDummyKeys( Set<String> keys, int dimNo, int size )
	{
		for( int i = 0; i < size; i++ )
			keys.add( dimNo + "_" + i );
	}
	
	
	/**
	 * If the given key is a new key to the given key set, a dummy key in the key set will be 
	 * replaced by the given key. 
	 * 
	 * @param keys  the key set in which a dummy key will be replaced by the new key
	 * @param key   the new key to be added
	 */
	private void addKey( Set<String> keys, String key )
	{
		if( !keys.contains( key ) )
		{
			// Remove a dummy key from the set
			for( String dummyKey : keys )
				if( dummyKey.matches( "[0-9]+_[0-9]+" ) )
				{
					keys.remove( dummyKey );
					break;
				}

			// Add the new key
			keys.add( key );
		}
	}
}
