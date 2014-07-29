/*
 * This software is part of the Tree Set Visualization module for Mesquite,
 * written by Jeff Klingner, Fred Clarke, and Denise Edwards.
 *
 * Copyright (c) 2002 by the University of Texas
 *
 * Permission to use, copy, modify, and distribute this software for any
 * purpose without fee is hereby granted under the GNU Lesser General
 * Public License, as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version,
 * provided that this entire notice is included in all copies of any
 * software which are or include a copy or modification of this software
 * and in all copies of the supporting documentation for such software.
 *
 * THIS SOFTWARE IS BEING PROVIDED "AS IS", WITHOUT ANY EXPRESS OR IMPLIED
 * WARRANTY.  IN PARTICULAR, NEITHER THE AUTHORS NOR THE UNIVERSITY OF TEXAS
 * AT AUSTIN MAKE ANY REPRESENTATION OR WARRANTY OF ANY KIND CONCERNING THE
 * MERCHANTABILITY OF THIS SOFTWARE OR ITS FITNESS FOR ANY PARTICULAR PURPOSE.
 * IN NO CASE WILL THESE PARTIES BE LIABLE FOR ANY SPECIAL, INCIDENTAL,
 * CONSEQUENTIAL, OR OTHER DAMAGES THAT MAY RESULT FROM USE OF THIS SOFTWARE.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package mesquite.consensusTSV.MajorityTree;

import mesquite.lib.Tree;



/**
 * <code>MajPSWTree</code> is a post-order-sequence representation of a Mesquite tree.
 * It computes the hashcode for its internal nodes during its creation.
 *
 *@author Frederick Clarke
 *@version $Revision: 1.1.6.1 $
 */
public class MajPSWTree
{
   /** Constant that indicates to use the first Hash function. */
    public static final int FIRST_FUNCTION = 1;

    /** Constant that indicates to use the second Hash Function. */
    public static final int SECOND_FUNCTION = 2;

    /** list of vertex/number of leaves pair. */
    private int [][]vw;

    /** Number of taxon. */
    private int n;

    /** first hash values of internal nodes. */
    private  int []internalH1;

    /** second hash values of internal nodes. */
    private int[]internalH2;

    /** Used to iterate through the list of vertices. */
    private int enumerator;

    /** Mesquite tree. */
    private Tree t;

    /** index used to keep track of insertion point in the array. */
    int index;

    /** Size of the Hashtable. */
    private int tableSize;

    /** first hash function of the taxons. */
    int []h1;

    /** second hash function of the taxons. */
    int []h2;


    /**
     * Creates a <code>MajPSWTree</code> - representation of the tree as a
     * table vw, and stored hash codes for interior nodes. 
     */
    public MajPSWTree( Tree t, int tableSize, int []f, int []s )
    {

	this.t         = t;
	this.tableSize = tableSize;
	index          = 0;
	n              = t.getNumTaxa();
	vw             = new int[tableSize][2];
	h1             = f;
	h2             = s;
	internalH1     = new int[tableSize];
	internalH2     = new int[tableSize];

	enumerator = 0;

	/*
	int firstNode = t.nodeOfTaxonNumber(0);

	constructRecursor(firstNode, t.motherOfNode( firstNode ) );

	vw[index][0] = t.taxonNumberOfNode( firstNode );
	vw[index][1] = 0;
	index++;
	*/
	int root = t.getRoot();
	int numLeaves = constructRecursor(root);
//	System.out.println("number of leaves"+numLeaves);

//	barfPSW();
//	System.err.println();


    }

    /**
     * Recurses through the tree by doing a post-order traversal starting at taxon number 0.
     *@param a The ancestor of the current node
     *@param v the current node
     */
    private int constructRecursor(int v )
    {
	if ( t.nodeIsTerminal(v) ) {

	    vw[index][0] = t.taxonNumberOfNode(v);
	    vw[index][1] = 0;
	    index++;

	    return 1;

	} else {

	    int  nodeLeaves = 0 , numOfChildren = 0, parentHash1, parentHash2;
	    
	    for( int d = t.firstDaughterOfNode(v); d > 0; d = t.nextSisterOfNode(d) ) {
			nodeLeaves += constructRecursor(d );
			numOfChildren++;
		    }
/*
	    for( int d = t.firstDaughterOfNodeUR(a,v); d > 0; d = t.nextSisterOfNodeUR(a,v,d) ) {
		nodeLeaves += constructRecursor(v, d );
		numOfChildren++;
	    }

	    int l = t.firstDaughterOfNodeUR(a,v);
	    int r = t.lastDaughterOfNodeUR(a,v);
*/
/*
	    parentHash1 = getParentHashCode( l, r, FIRST_FUNCTION );
	    parentHash2 = getParentHashCode( l, r, SECOND_FUNCTION );
	    */

	    parentHash1 = getParentHashCodeWPM(v, FIRST_FUNCTION );
	    parentHash2 = getParentHashCodeWPM(v, SECOND_FUNCTION );

	    internalH1[v] = parentHash1;
	    internalH2[v] = parentHash2; 
/*
	    if (numOfChildren > 1) {
		vw[index][0] = v;
		vw[index][1] = nodeLeaves;
		index++;
		return nodeLeaves;

	    } else {
		vw[index][0] = v; // outgroup thing (taxa(0))
		vw[index][1] = ++nodeLeaves;
		index++;
		return nodeLeaves;
	    }
	    */
		vw[index][0] = v;
		vw[index][1] = nodeLeaves;
		index++;

		return nodeLeaves;

	}
    }

    public Tree getT()
    {
	return t;
    }

    /**
     * Gets the first hashcode of the partitcular node.
     *@param n The node
     *@return The first hashcode
     */
    public int getFirstHash( int n )
    {
	return internalH1[n];
    }

    /**
     * Gets the second hashcode of the specified node
     *@param n The node
     *@return The second hashcode
     */
    public int getSecondHash( int n )
    {
	return internalH2[n];
    }

    /**
     * Gets the hashcode of a parent node, this is easy because during the traversal
     * we compute the hashcodes of the children before getting to the parent.
     *
      *@param a  ancestor defining direction
      *@param parent node  //VERSION MODIFIED BY WPM April 06
      *@param function Specifies which hash function to use.
      *@return The Hashcode for the parent
     */
    private int getParentHashCodeWPM(int parent, int function)
    {
	int sumHash = 0, parentHash = 0;
	
	switch( function ) {
	case FIRST_FUNCTION:
		
		 for( int d = t.firstDaughterOfNode(parent); d > 0; d = t.nextSisterOfNode(d) ) {
		
		    if ( t.nodeIsTerminal(d) ) {
				if (h1[t.taxonNumberOfNode(d)] == -1)
				    System.err.println("H1[d] is -1");
				else
				    sumHash += h1[ t.taxonNumberOfNode(d) ];
		    } else {
		    	sumHash += internalH1[d];
		    }
	    }
	    
		//System.out.println("wfd " + t.firstDaughterOfNode(parent) + " hash " + sumHash);

		parentHash = sumHash % tableSize;
	    break;

	case SECOND_FUNCTION:
	    for( int d = t.firstDaughterOfNode(parent); d > 0; d = t.nextSisterOfNode(d) ) {

		    if( t.nodeIsTerminal(d) ) {
				if (h2[t.taxonNumberOfNode(d)] == -1)
				    System.err.println("H2[d] is -1");
				else
					sumHash += h2[ t.taxonNumberOfNode(d) ];
		    } else {
		    	sumHash += internalH2[d];
		    }
	    }
	    

  //System.out.println("wfd2 " + t.firstDaughterOfNode(parent) + " hash " + sumHash);
	    parentHash = sumHash % tableSize;
	    break;
	}
	return parentHash;
    }
			/* 
	    for( int d = t.firstDaughterOfNodeUR(a,parent); d > 0; d = t.nextSisterOfNodeUR(a,parent,d) ) {
	    	

		    if ( t.nodeIsTerminal(d) ) {
				if (h1[t.taxonNumberOfNode(d)] == -1)
				    System.err.println("H1[d] is -1");
				else
				    sumHash += h1[ t.taxonNumberOfNode(d) ];
		    } else {
		    	sumHash += internalH1[d];
		    }
	    }
	    
	
System.out.println("wfd " + t.firstDaughterOfNodeUR(a,parent) + " hash " + sumHash);
	    parentHash = sumHash % tableSize;
	    break;

	case SECOND_FUNCTION:
	    for( int d = t.firstDaughterOfNodeUR(a,parent); d > 0; d = t.nextSisterOfNodeUR(a,parent,d) ) {

		    if( t.nodeIsTerminal(d) ) {
				if (h2[t.taxonNumberOfNode(d)] == -1)
				    System.err.println("H2[d] is -1");
				else
					sumHash += h2[ t.taxonNumberOfNode(d) ];
		    } else {
		    	sumHash += internalH2[d];
		    }
	    }
	    

  System.out.println("wfd2 " + t.firstDaughterOfNodeUR(a,parent) + " hash " + sumHash);
	    parentHash = sumHash % tableSize;
	    break;
	}
	return parentHash;
    }
*/
			
    /**
     * Gets the hashcode of a parent node, this is trivial because during the traversal
     * we compute the hashcodes of the children before getting to the parent.
     *
     *@param left The left child
     *@param right The right child
     *@param function Specifies which hash function to use.
     *@return The Hashcode for the parent
     */
    /*
    private int getParentHashCode(int left, int right, int function)
    {
	int leftHash = 0, rightHash = 0, parentHash = 0;


	switch( function ) {
	case FIRST_FUNCTION:

	    if ( t.nodeIsTerminal(left) ) {
		if (h1[t.taxonNumberOfNode(left)] == -1)
		    System.err.println("H1[left] is -1");
		else
		    leftHash = h1[ t.taxonNumberOfNode(left) ];
	    } else {
		leftHash = internalH1[left];
	    }

	    if ( t.nodeIsTerminal(right) ) {
		if (h1[t.taxonNumberOfNode(right)] == -1)
		    System.err.println("H1[right] is -1");
		else
		    rightHash = h1[ t.taxonNumberOfNode(right) ];
	    } else {
		rightHash = internalH1[right];
	    }

 System.out.println("fd " + left + " hash " + (leftHash + rightHash));
	    parentHash = (leftHash + rightHash) % tableSize;
	    break;

	case SECOND_FUNCTION:

	    if( t.nodeIsTerminal(left) ) {
		if (h2[t.taxonNumberOfNode(left)] == -1)
		    System.err.println("H2[left] is -1");
		else
		    leftHash = h2[ t.taxonNumberOfNode(left) ];
	    } else {
		leftHash = internalH2[left];
	    }

	    if ( t.nodeIsTerminal(right) ) {
		if (h2[t.taxonNumberOfNode(right)] == -1)
		    System.err.println("H2[right] is -1");
		else
		    rightHash = h2[ t.taxonNumberOfNode(right) ];
	    } else {
		rightHash = internalH2[right];
	    }

	    System.out.println("fd2 " + left + " hash " + (leftHash + rightHash));
	    parentHash = (leftHash + rightHash) % tableSize;
	    break;
	}
	return parentHash;
    }
    */

    /**
     * Prepares the <code>MajPSWTree</code> for iteration
     */
    public void prepareForEnumeration()
    {
	enumerator = 0;
    }

    /**
     * Gets the next vertex in the tree.
     *@return The next vertex or null if we are at the end
     */
    public int []nextVertex()
    {

	if( enumerator < index ) {
	    return (vw[enumerator++]);

	} else {
	    return null;
	}
    }


    private void barfPSW() {

	System.out.println("PSW for " + t.getName());

	for (int i=0; i<index; i++) {

	    if (vw[i][1] == 0)
		System.out.println(i + ": " + vw[i][0] + "," + vw[i][1] + " label: " +
			       t.getNodeLabel(vw[i][0]) );

	    else
		System.out.println(i + ": " + vw[i][0] + "," + vw[i][1]);
	}
	System.out.println();
    }


}



















