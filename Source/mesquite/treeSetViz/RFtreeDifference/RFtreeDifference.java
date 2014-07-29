/*
 * This software is part of the Tree Set Visualization module for Mesquite,
 * written by Jeff Klingner, Fred Clarke, and Denise Edwards.
 *
 * Copyright (c) 2002 by the University of Texas
 *
 * Permission to use, copy, modify, and distribute this software for any
 * purpose without fee under the GNU Public License is hereby granted, 
 * provided that this entire notice  is included in all copies of any software 
 * which is or includes a copy or modification of this software and in all copies
 * of the supporting documentation for such software.
 * THIS SOFTWARE IS BEING PROVIDED "AS IS", WITHOUT ANY EXPRESS OR IMPLIED
 * WARRANTY.  IN PARTICULAR, NEITHER THE AUTHORS NOR THE UNIVERSITY OF TEXAS
 * AT AUSTIN MAKE ANY REPRESENTATION OR WARRANTY OF ANY KIND CONCERNING THE 
 * MERCHANTABILITY OF THIS SOFTWARE OR ITS FITNESS FOR ANY PARTICULAR PURPOSE.
 */
 
 
package mesquite.treeSetViz.RFtreeDifference;
/*~~  */

import java.util.*;
import mesquite.consensusTSV.common.*;
//import mesquite.consensusTSV.common.PSWTree;
import mesquite.lib.*;
import mesquite.lib.duties.*;


/**
 * This is a reimplementation of RFtreeDifference, created by Wayne Maddison.  I wrote
 * it because the existing module was too slow for the the large number of calls required
 * by TreeSetVisualization.  I have tried to make this as fast as I could, sacrificing
 * unnecesary abstractions and coherence of style with the rest of Mesqutie for the sake
 * of space and time efficiency.
 *
 *@author Jeff Klingner, September 2001
*/
public class RFtreeDifference extends DistanceBetween2Trees {
	private static final int INITIAL_HASHMAP_CAPACITY = 500;

	/* Memoization variables */
	private HashMap PSWs;
	private HashMap bipTables;
	
	public String getName() { return "Robinson-Foulds Tree Difference (Rooted) [TSV]"; }
	public String getVersion() { return "1.1"; }
	public String getYearReleased() { return "2002"; }
	public boolean showCitation() {	return true; }
	public String getPackageName() { return "Tree Comparison Package"; }
	public boolean getUserChoosable() { return false; }
	public boolean isPrerelease() { return false; }
	public boolean isSubstantive() { return true; }
	public String getCitation() { return "\n" + getYearReleased() + ". " + getAuthors() + "\n"; }
	public String getAuthors() { return "Jeff Klingner, The University of Texas at Austin"; }
	
	public String getExplanation() {
		return	"Calculates the Robinson-Foulds (Hamming) distance\n" +
				"between two rooted trees.  This is the number of edges\n" + 
				"that is present in exactly one of the two trees.";
	}
	

	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		PSWs = new HashMap(INITIAL_HASHMAP_CAPACITY);
		bipTables = new HashMap(INITIAL_HASHMAP_CAPACITY);
		return true;
	}

	public void employeeQuit(MesquiteModule m){
		iQuit();
	}

	/*.................................................................................................................*/
	public void calculateNumber(Tree tree1, Tree tree2, MesquiteNumber result, MesquiteString resultString) {
		
		int answer;
		if (tree1==null || tree2==null) {   // DRM: added check January 2014
			return;
		}

		/* Start by checking a couple preconditions. */
		if (result==null) {
			System.out.println("You passed an uninitialized result holder to calculateNumber().");
			return;
		}

		if (tree1.getTaxa() != tree2.getTaxa()) {
			System.out.println("RFtreeDifference only works for trees over the same Taxa.");
			return;
		}
	   	clearResultAndLastResult(result);

		/* First, we check to see if we have seen either of the trees before. 
		 * If we haven't we compute a PSW tree and a bipartition table for them 
		 * For the calculation to come, we need a table for tree1 and a PSW for
		 * tree2
		 */
		MarkedBipartitionTable table,table1; // actual table to be used in calculation
		PSWTree tree;			// actual tree to be used in calculation
		 
		PSWTree tempPSW;
		MarkedBipartitionTable tempTable,tempTable1;
		
		/*WPM Oct05  saving of trees in hashmap is disabled because it had two bugs:
		-- if tree seen before but was modified, then it failed to redo the calculations because pointers were the same
		-- all trees seen were saved, not purged (is that right?) leading to huge memory use, especially with simulations
		
		*/
		/* Check to see if we have seen the first tree or not */
		if ( !PSWs.containsKey(tree1)) {
			tempPSW = new PSWTree(tree1);
			tempTable = new MarkedBipartitionTable(tempPSW);
		//WPM Oct05 disabled because this saved ALL TREES EVER SEEN and thus ran roughshod over memory	PSWs.put(tree1, tempPSW);
		//	WPM Oct05 disabled  bipTables.put(tree1, tempTable);
			PSWs.put(tree1, tempPSW);
			bipTables.put(tree1, tempTable);
			table = tempTable;
		//	psw1 = tempPSW;
			
		} else { // we have already seen this tree; retrieve its table from memory
			table = (MarkedBipartitionTable) bipTables.get(tree1);
		}
		
		/* Check to see if we have seen the second tree or not */
		if ( !PSWs.containsKey(tree2)) {
			tempPSW = new PSWTree(tree2);
		
			//WPM Oct05 disabled  PSWs.put(tree2,tempPSW);
			//WPM Oct05 disabled  bipTables.put(tree2, new MarkedBipartitionTable(tempPSW));
			PSWs.put(tree2,tempPSW);
			bipTables.put(tree2, new MarkedBipartitionTable(tempPSW));
			tree = tempPSW;
		} else {
			tree = (PSWTree) PSWs.get(tree2); //
		}


		/* To compute RF difference, we can't compare bipartition tables directly,
		   because the two tables may have been (and probably were) constructed using
		   two different leaf relabeling functions.  Instead we traverse one tree
		   (using it's PSW representation) and check for the bipartitions we find in
		   the other tree's bipartition table.  This is still O(n). */


		LrnwStack stack = new LrnwStack(tree.getN() * 2);
		stack.empty();
		int[] lrnw,poppedLRNW; /* see description below at initialization */
		int[] vw; /* the current vertex,weight pair */
		int w; /* total weight unaccounted for beneath the current vertex */
		int hits = 0; /* number of bipartitions in tree2 found in tree1 */
		int misses = 0; /* number of bipartitions in tree 2 not found in tree 1 */

		//System.out.println("Traversing " + tree2.getName() + " against table for " + tree1.getName());
		for (tree.prepareForEnumeration(), vw = tree.nextVertex();
			 vw != null;
			 vw = tree.nextVertex()) {
			lrnw = new int[4];
			if (vw[1] == 0) { /* leaf vertex */
				lrnw[0] = table.encoding[vw[0]]; /* leftmost is this leaf */
				lrnw[1] = table.encoding[vw[0]]; /* rightmost is this leaf */
				lrnw[2] = 1; /* one leaf in this subtree */
				lrnw[3] = 1; /* subtree's total weight is one */
				stack.push(lrnw);
			} else { /* interior vertex; represents a bipartition */
				lrnw[0] = tree.getN() + 1; /* we want the smallest leaf among this vertex's children
										   with min(). This is bigger than any leaf */
				lrnw[1] = 0; /* likewise, smaller than ever leaf among my children */
				lrnw[2] = 0; /* start with a leaf count of zero */
				lrnw[3] = 1; /* will be total weight; start with one for this vertex */

				w = vw[1]; /* total weight below this vertex */
				while (w > 0) { /* while there are still children unaccounted for */
					poppedLRNW = stack.pop();
					lrnw[0] = Math.min(lrnw[0],poppedLRNW[0]); /* smallest #ed leaf below me */
					lrnw[1] = Math.max(lrnw[1],poppedLRNW[1]); /* largest #ed leaf below me */
					lrnw[2] = lrnw[2] + poppedLRNW[2]; /* total up the leaves below me */
					lrnw[3] = lrnw[3] + poppedLRNW[3]; /* total up the weight below me */
					w = w - poppedLRNW[3];
				}
				stack.push(lrnw);

				/* Now, for the subtree below this interior vertex, we have the total number
				   of leaves (N), and the smallest (L) and largest (R) #ed leaves (according
				   to the encoding used for tree1's bipartition table).  If the leaves below
				   this vertex form a contiguous sequence (i.e. if N=R-L+1) then we have a
				   candidate bipartiton that could be in tree 1.  If this is the case, we
				   check tree1's bipartition talbe for it and keep score accordingly. */
				if	(!(  // two of the clusters we get in the traversal are not bipartitions
						// (lrnw[0]==1 && lrnw[1] == tree.getN())   // cluster of everything but taxon zero - not a bipartition
					  //|| (lrnw[0]==1 && lrnw[1] == tree.getN()-1) // cluster that includes the whole tree - not a bipartition
						(lrnw[0]==0 && lrnw[1] == tree.getN()-1)
						) 
					)
				{
					if (   (lrnw[2] == lrnw[1] - lrnw[0] + 1) /* if (N = R - L + 1) */
						&& table.containsBipartition(lrnw[0],lrnw[1])) {
						hits++;
					//	 System.out.println("Hit: <" + lrnw[0] + "," + lrnw[1] + ">");
					} else {
						misses++;
					//	 System.out.println("Miss: <" + lrnw[0] + "," + lrnw[1] + ">");
					}
				}
			}
		}

	//	System.out.println(hits + " hits, " + misses + " misses, " + table.getNumBipartitions() + " bipartitions in tabled tree");
		answer = misses + table.getNumBipartitions() - hits;
       // System.out.println("RF Difference="+answer);   
        
        
        
		/* Now fill the caller-supplied result holders. */
		result.setValue(answer);
		if (resultString!=null) {
			resultString.setValue("RF tree difference: " + result.toString() );
		}
		saveLastResult(result);
		saveLastResultString(resultString);
		return;
	}


	/** Called to provoke any necessary initialization.  This helps prevent the module's intialization queries to the user from
   	happening at inopportune times (e.g., while a long chart calculation is in mid-progress)*/
	public void initialize(Tree t1, Tree t2) {
	}
}


/**
 * This class implements the postorder sequence with weights representation of trees.  This
 * representation is the starting point for computing Day's "cluster tables"  The main advantage
 * is in being able to look up the next node visited after a given node in a postorder
 * traversal of the tree in constant time.
 *
 *@author Jeff Klinger
 */
class PSWTree {

	int n; /* number of leaves */
	int enumerator; /* used in calls to enumerate vertices */
	int j; /* total number of verticies (leaves+internal) in the tree */
	int[][] vw; /* array of vertex/weight pairs */
	Tree t; /* The Mesquite tree this PSWtree represents */



	public PSWTree(Tree t) {

		this.t = t;
		n = t.getNumTaxa();
		enumerator = 0;
		vw = new int[n*2][2];

		j = 0; /* index for building the array */
		int root=t.getRoot();
		constructionRecursor(root);

		/* insert taxon zero in list at second-to-last entry */
		//vw[j][0] = t.taxonNumberOfNode(firstNode);
	//	vw[j][1] = 0;
		//j++;

		/* insert pseudo-root (node on the branch incident on firstNode) at last entry */
//		vw[j][0] = n+1; //actual value here doesn't matter
	//	if (j>=2)  // otherwise may crash if this tree has fewer taxa than other trees
		//	vw[j][1] = vw[j-2][1]+2; /* This one does. */

		//j++;
	//	barfPSW(); 
	}

	private int constructionRecursor(int v) {
		if (t.nodeIsTerminal(v)) {
			vw[j][0] = t.taxonNumberOfNode(v);
			vw[j][1] = 0;
			j++;
			return 1;
		} else {
			int w = 0;
			int numChildren = 0;
			int root= t.getRoot();
			
			for (int d=t.firstDaughterOfNode(v); d > 0; d=t.nextSisterOfNode(d)) { // recurse, baby! 
				w += constructionRecursor(d);
				numChildren++;
			}
			if (numChildren > 1 ) {
			
				vw[j][0] = j + n + 1; // to ensure that interior labels are > n 
				vw[j][1] = w; 
				j++;
				return w+1;
				
			} else {
				// interior node of degree 2. Treat as if non-existent 
				return w;
			}
		}
		
	}

		public int getN() {
		return n;
	}

	public void prepareForEnumeration() {
		enumerator = 0;
	}

	public int[] nextVertex() {
		if (enumerator < j) {
			return vw[enumerator++];
		} else {
			return null;
		}
	}

	/** Returns the leftmost leaf of the vertex that returned by the last call of nextVertex */
	public int leftmostLeaf() {
		return vw[ (enumerator - 1 - vw[enumerator-1][1]) ][0];
	}



	private void barfPSW() {
		System.out.println("PSW for " + t.getName());
		for (int i=0; i<j; i++) {
			System.out.println(i + ": " + vw[i][0] + "," + vw[i][1] + " label: " + t.getNodeLabel(t.nodeOfTaxonNumber(vw[i][0])));
		}
		System.out.println();
	}

}

class MarkedBipartitionTable 
{
    public int[] encoding; /* function mapping leaf labels to internal labels */
    private int[][] table;  /* the table of bipartitions */
	public BitSet marks; /* a flag for each bipartition */
	private BitSet touches;
	private int tableSize;
    private int numberOfBipartitions; /* in the sense of the Day paper, after pseudo-rooting */
    
    public MarkedBipartitionTable(PSWTree t) {
		tableSize = t.getN() + 1;
		encoding = new int[tableSize];
		table = new int[tableSize][2];
		marks = new BitSet(tableSize);
		touches = new BitSet(tableSize);
		numberOfBipartitions = 0;
		
		/* First, zero out the table */
		for (int i=0; i<table.length; i++) {
			table[i][0] = 0;
			table[i][1] = 0;
			marks.set(i);
			touches.clear(i);
		}
		
		int L,R,leafcode;
		int[] vw;
		t.prepareForEnumeration();
		leafcode = 0;
		R = 0;
		vw = t.nextVertex();
		
		while (vw != null) {
			if (vw[1] == 0) { /* v is a leaf */
			
			encoding[vw[0]] = leafcode;
			R = leafcode;
			leafcode++;
			vw = t.nextVertex();
			} else { /* v is an internal vertex */
			L = encoding[t.leftmostLeaf()];
			vw = t.nextVertex();
			if	(!(  // two of the clusters we get in the traversal are not bipartitions and shouldn't go in the table
				//	 (L==1 && R == t.getN())   // cluster of everything but taxon zero - not a bipartition
				   (L==0 && R == t.getN()-1) // cluster that includes the whole tree - not a bipartition
				  ) 
				)
			{
				
				if (vw == null || vw[1] == 0) {
				//System.out.println("ifffffffff");
				table[R][0] = L;
				table[R][1] = R;
				} else {
				//System.out.println("elseeeeeeee");
				table[L][0] = L;
				table[L][1] = R;
				}
				numberOfBipartitions++;
			}
			}
		}
	//	barfTable(); 
    }
    
    private void barfTable() {
		for (int i = 0; i<table.length; i++) {
			System.out.print("e(" + i + ") = " + encoding[i] + "  ");
			System.out.println("<" + table[i][0] + "," + table[i][1] + ">");
		}
		System.out.print(numberOfBipartitions + " bipartitions\n\n");
    }
    
    public void touchBipartition(int L, int R) {
		if (table[L][0] == L && table[L][1] == R) {
			touches.set(L);
		} else if (table[R][0] == L && table[R][1] == R) {
			touches.set(R);
		}
    }
	
	public boolean containsBipartition(int L, int R) {
		if (table[L][0] == L && table[L][1] == R) {
			return true;
		}
		if (table[R][0] == L && table[R][1] == R) {
			return true;
		}
		return false;
    }
	
	public boolean containsMarkedBipartition(int L, int R) {
		if (table[L][0] == L && table[L][1] == R) {
			return marks.get(L);
		}
		if (table[R][0] == L && table[R][1] == R) {
			return marks.get(R);
		}
		return false;
    }
	
	public void clearUntouchedMarks() {
		for (int i=0; i < tableSize; ++i) {
			if (!touches.get(i)) { // if this bipartition was never checked for
				marks.clear(i);    // remove its mark
			}
			touches.clear(i); // reset this touch for next time around
		}
	}
		
    
	public int getNumBipartitions() {
	    return numberOfBipartitions;
	}
}




