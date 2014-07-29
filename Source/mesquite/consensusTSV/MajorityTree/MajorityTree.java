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

import java.awt.*;

import mesquite.lib.*;
import mesquite.lib.duties.*;
import java.util.*;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 * A Majority tree is a tree that contains all the edges that appear in more
 * than half the number of input trees.
 *
 *@author Frederick Clarke
 *@version $Revision: 1.1.6.1 $
 * modified by Paul Ivanov
 */
public class MajorityTree extends Consenser
{
    /** Initial capacity of PSWs trees. */
    final static int INITIAL_CAPACITY = 2500;

    /** Map of trees with their corresponding PSWTree. */
    //private HashMap rememberedPSWs;
    // Nina - 7/06 eliminated storing PSWs between passes. 

    /** Representation of our random Hashtable. */
    private RandomHashtable table;

    /** The number of taxa. */
    private int numLeaves;

    /** The number of trees selected. */
    private int numTrees;

    public double majorityPercentage;

    /** Mesquite tree object. */
    Tree t;

    /** Post order traversal representation of a tree. */
    MajPSWTree tempPSW;

    /** first hash function. */
    private int []h1;

    /** Second hash function. */
    private int []h2;

    /** BitSet use to choose unique random numbers. */
    private BitSet bitSet;

    /** List of nodes that belong to the Majority tree. */
    Vector majorityNodes;

    /** List of selected trees.  WPM april06: this was not used and so was deleted
    TreeVector list;*/

    /** Mapping of leaves and their parents. */
    HashMap parentOfLeaf;

    /** Represents the parent of a node. */
    Bipartition parent;

    /** The majority tree. */
    MesquiteTree tempTree;

    /** A mapping of internal to external nodes. */
    Hashtable encoding;

	/** Majority submenu */
	MesquiteSubmenuSpec majorityMenu;

	/** Item in submenu currently selected */
	MesquiteString itemName;
	
	
	/** Menu item names */
	ListableVector v = new ListableVector();
	public double Slider_val;
	JSlider slider;
	final int MAX=100;
	final int MIN=50;
	final int INIT=50;
	
	private Consenser consensusTask; // module computing the consensus trees
	/**	WPM Oct05: it would be helpful to biologists if the trees contained an indication of the frequency 
	 *  with which each branch appears among the input trees.
	 *  
	 * 	The frequencies can be attached to the branches and later accessed, as follows.
	 * 	First, insert the following line to define a NameReference (dont' ask why these exist...), up here 
	 *  above startJob:

		static final NameReference frequencyNameRef = NameReference.getNameReference("branchFrequency");
	
		Then, once you have the consensus tree (in insertAndRearrange???) you can do a call like this:
		
	 	tree.setAssociatedDouble(frequencyNameRef, node, frequencyValue);
	 	
	 	to attach to the node the double "frequencyValue" representing the percentage 
	 	of input trees with that branch.  I couldn't figure out the association betweeen the bipartitions and the
	 	branches of the consensus tree, otherwise I would have done this for you...
 	 */
   /**
     * Mesquite's constructor
     */
    public boolean startJob( String arguments, Object condition, 
			     boolean hiredbyName )
    {
    	
	System.out.println("argument in MajorityTree startJob " + arguments);
    //WPM Oct05:  UI for this module's settings moved into this module
	/*itemName = new MesquiteString("> 50 % (default)");
	v = new ListableVector();
	v.addElement(new MesquiteString(itemName.getValue()), false);
	v.addElement(new MesquiteString("> 55 %"), false);
	v.addElement(new MesquiteString("> 60 %"), false);
	v.addElement(new MesquiteString("> 65 %"), false);
	v.addElement(new MesquiteString("> 70 %"), false);
	v.addElement(new MesquiteString("> 75 %"), false);
	v.addElement(new MesquiteString("> 80 %"), false);
	v.addElement(new MesquiteString("> 85 %"), false);
	v.addElement(new MesquiteString("> 90 %"), false);
	v.addElement(new MesquiteString("> 95 %"), false);
*/
	//majorityMenu = addSubmenu(null, "Set Majority Percentage", makeCommand("setMajorityPercentage", (Commandable)this), v);
	addMenuItem( "Set Majority Percentage", makeCommand("setMajorityPercentage", (Commandable)this));
	//majorityMenu.setList(v);
	//majorityMenu.setSelected(itemName);
	///WPM Oct05 BUG?: does this have the same problem as RFDifference, i.e. (1) memory overflows if many trees because ALL remembered; (2) uses wrong tree data if tree changed 
	// Nina - 7/20/06 - do not store PSWs between passes  eliminage rembmeredPSWs
	//rememberedPSWs = new HashMap( INITIAL_CAPACITY );
	majorityPercentage = 0.50;
	
	return true;
    }

 
    //WPM Oct05:  handling of this module's settings moved into this module
	public Object doCommand(String commandName, String arguments, CommandChecker checker) {
		//--- W. Maddison
		if(checker.compare(this.getClass(), "Sets the percentage majority desired by the user", null, commandName, "setMajorityPercentage")) {
			//int i = MesquiteInteger.fromString(arguments);
			//majorityPercentage = i * .05 + .50;
			//itemName.setValue((MesquiteString)v.elementAt(i));
		   	Start_MajorityPercenatge_Slider();
			//parametersChanged();
		}
		else { // I don't recognize this command. Pass it on to the command handler in the MesquiteModule superclass.
			return  super.doCommand(commandName, arguments, checker);
		}
		// Neither I nor my superclass knows what to do
		return null;
	
	}//doCommand
    
   public void Start_MajorityPercenatge_Slider()
   {
		JFrame frame = new JFrame("Set Majority Percentage");
		JPanel panel = new JPanel();
		final JLabel label = new JLabel("Majority % = 50");
		slider=new JSlider(JSlider.HORIZONTAL,MIN,MAX,INIT);
		slider.setPaintTicks(true);
		slider.setMajorTickSpacing(10);
		slider.setMinorTickSpacing(5);
		slider.setLabelTable(slider.createStandardLabels(10));
		slider.setPaintLabels(true);   
		panel.add(slider);
		panel.add(label);
		frame.add(panel, BorderLayout.CENTER);
	    frame.setSize(400, 100);
	    frame.setVisible(true);
		slider.addChangeListener(new ChangeListener(){
			
			//executed when the slider is moved
			public void stateChanged(ChangeEvent e){
				
				if (! slider.getValueIsAdjusting()) {
					majorityPercentage = slider.getValue();
					String str="Majority %="+ Double.toString(majorityPercentage);	
					label.setText(str);
					majorityPercentage=majorityPercentage/100;
					if (majorityPercentage==1){
											}
					System.out.println("majority Percentage%="+majorityPercentage);
			        parametersChanged();	
			     }
			}
		});
		
   }
    /**
     * Gets the name for the module
     *@return The name of the module
     */
    public String getName()
    {
	return "Majority Rules Consensus [TSV]"; //WPM Oct05  renamed to match field's expectation
    }

    /**
     * Gets the version of the module
     *@return The version number
     */
    public String getVersion()
    {
	return "1.1";
    }

    /**
     * Gets the year the module was released
     *@return The year
     */
    public String getYearReleased()
    {
	return "2002";
    }

    /**
     * Indicate whether or not the module does substantive calculations and should be cited
     *@return true if the module is cited, false otherwise
     */
    public boolean showCitation()
    {
	return true;
    }
    

    /**
     * Get the name of the package the module resides in.
     *@return The package name
     */
    public String getPackageName()
    {
	return "Tree Comparison Package";
    }

    /**
     * Indicates if the module resides in a sub-menu where the user can choose directly.
     *@return true if the module can be selected directly from a sub-menu, false otherwise
     */
    public boolean getUserChoosable() //WPM 06 set to true
    {
	return true;
    }
	public boolean requestPrimaryChoice() { return true; } //WPM 06 set to true

	/*.................................................................................................................*/
 	/** returns the version number at which this module was first released.  If 0, then no version number is claimed.  If a POSITIVE integer
 	 * then the number refers to the Mesquite version.  This should be used only by modules part of the core release of Mesquite.
 	 * If a NEGATIVE integer, then the number refers to the local version of the package, e.g. a third party package*/
    public int getVersionOfFirstRelease(){
    		return 110;  
    	}
  
   /**
     * Indicates if the module is a pre-release version of the module.
     *@return true if the module is a pre-release version, false otherwise
     */
    public boolean isPrerelease()
    {
	return false;
    }

    /**
     * Indicates whether the module does substantive calculations affecting analysis
     *@return true if the module is substantive, false otherwise
     *
     */
    public boolean isSubstantive()
    {
	return true;
    }

    /**
     * Get the citation for the module
     *@return The citation
     */
    public String getCitation()
    {
	return "\n" + getYearReleased() + ". " + getAuthors() + "\n";
    }

    /**
     * Get the authors of the module
     *@return The authors
     */
    public String getAuthors()
    {
	return "Frederick Clarke, City University of New York";
    }

    /**
     * Gives an explanation of what the module does
     *@return An explanation of what the module does
     */
    public String getExplanation()
    {
	return "The Majority Tree contains those edges that\n" +
	    "appears in more than half the total number of input trees";
    }

    /**
     * This method is called to compute the Majority from a set of imput trees
     *@param list The list of selected trees.
     *@return The Majority tree.
     */
    public Tree consense(Trees list)
    {
    
	numLeaves = list.getTaxa().getNumTaxa();
	numTrees = list.size();

	boolean validMajorityTree = false;
	
	do {

	tempTree = new MesquiteTree( list.getTaxa() );
	tempTree.setAsDefined(true);

	// Nina - 7/06 - cut table size way down, so it is linear in 
	// number of leaves, rather than numLeaves*numTrees. We don't
	// expect many unique nodes to appear even when the set of trees
	// is large, and when the set of trees is large, we don't want to
	// waste a lot of space on the hash table; limits the size of the 
	// problems we can handle. 
	//int tableSize = getPrime( (numLeaves+numLeaves) * numTrees );
	int tableSize = getPrime(numLeaves+numLeaves*6);
    //System.out.println("numLeaves="+numLeaves +" numTrees="+numTrees+" tablesize="+tableSize);	
	h1 = new int[numLeaves+1];
	h2 = new int[numLeaves+1];

	for (int i = 0; i < h1.length; i++) {
	    h1[i] = -1;
	    h2[i] = -1;
	}

	majorityNodes = new Vector( numLeaves );
	parentOfLeaf = new HashMap( numLeaves );
	encoding = new Hashtable( numLeaves );
	bitSet = new BitSet( 10 * tableSize ); // to make sure taxon random hash values are unique

	/* pi - majority is .5 by default */
	

	if ( majorityPercentage == 0)
	{
		majorityPercentage = .5;
		//System.out.println(" majorityPercentage not set, setting to default of 50%" );
	}
	else{
		//System.out.println("Majority Percentage Set:"+majorityPercentage);
	}
	
	table = new RandomHashtable( tableSize, numTrees, majorityPercentage );


	boolean success = true;

       do
	{
	    bitSet.clear();
	    selectRandomHashes( tableSize ); // populate h1 and h2 with unique hashCodes for each taxon
	    //Nina - 7/20/06 - do not store PSWs between passes
	    table.clear();
        //System.out.println("traversing the trees");
	    for ( int i = 0; i < list.size(); ++i ) {
	    	CommandRecord.tick("Majority Rules Consensus: putting tree " + (i+1));
		t = list.getTree(i);
		// Nina - 7/20/06 - do not store PSWs between passes
		//if (! rememberedPSWs.containsKey( t ) ) {
			tempPSW = new MajPSWTree( t, tableSize, h1, h2 );
		    // Nina - 7/20/06 - do not store PSWs between passes
				    
		    success = table.traverseTree( tempPSW );

		    if ( !success )
			break;
		    	//Nina - 7/20/06 - do not store PSWs between passes		
	    }// end for
	    if (!success) System.out.println("Retry random hashcodes.");
	  } while ( !success );


	int firstNode = 0;
    //System.out.println("Consensing the trees");
	for ( int j = 0; j < list.size(); ++j ) {

		t = list.getTree(j);
		tempPSW = new MajPSWTree( t, tableSize, h1, h2);
	    // Nina - 7/20/06 - do not store PSWs between passes, to save memory

	    if ( tempPSW != null ) {

	    	firstNode = t.getRoot();
	    	CommandRecord.tick("Majority Rules Consensus: getting majority nodes for tree " + (j+1));
	    	getMajorityNodes( firstNode, null);

	    }// end if tempPSW
	}// end for

	insertMajorityNodes();//insert and rearrange majority nodes

	// Make a PSW tree for the new majority tree, which counts leaves of
	// each subtree among other things. 
	tempPSW = new MajPSWTree(tempTree,tableSize, h1, h2);
	// Check it against the hash table
	validMajorityTree = table.checkMajorityTree(tempPSW);
	if (!validMajorityTree) System.out.println("Retry random construction.");
	
	} while (!validMajorityTree);

	//System.out.println("before setTaxonNumber:" +tempTree.writeTree(0, true, true, false, true,":"));

	//WPM: added this
	//storeScores(tempTree, tempTree.getRoot(),list.size(), new MesquiteDouble());

	//System.out.println("before reroot:" +tempTree.writeTree(0, true, true, true, true,":"));
	//tempTree.reroot(tempTree.nodeOfTaxonNumber(0), tempTree.getRoot(), true);
	//System.out.println("Consense: after reroot");
	tempTree.standardize(tempTree.getRoot(), true);
	storeScores(tempTree, tempTree.getRoot(), list.size());
	return tempTree;
    }
    


    /**
     * by WPM.  This method is used to assign majority scores to nodes. Not sure if it works, because I don't know
     * if the bipartitions continue to be associated with the correct nodes as these manipulations happen
     */
	static final NameReference frequencyNameRef = NameReference.getNameReference("consensusFrequency");
   private void storeScores(MesquiteTree tree, int node, int total){
	    	for (int d = tree.firstDaughterOfNode(node); tree.nodeExists(d); d = tree.nextSisterOfNode(d))
	    		storeScores(tree, d, total);
	    	if (tree.nodeIsInternal(node)){
	    		Bipartition b = findBipartition(node);
	    		if (b != null){
	    			int c = b.getCount();
	    			tree.setAssociatedDouble(frequencyNameRef, node, c*100.0/total);
	    			tree.setNodeLabel(MesquiteDouble.toStringDigitsSpecified(c*100.0/total, 2), node);
	    		}
	    	}
    }
    
    /**
     * This method is used to get the Majority nodes of each tree. 
     * The parameter "last" is the last ancestor if the current node which was a 
     * majority node, that is, an ancestor in the majority tree. The actual parent of a 
     * majority node in the majority tree is the ancestor with the fewest leaves in its 
     * subtree. Every time we see a majority node, we check it's current guess as to what 
     * its parent is, and if the "last" ancestor in this tree has fewer leaves in its 
     * subtree, we update the guess for the parent. 
     * After traversing all trees, every node knows its parent.  
     *
     *@param a The ancestor of the current node during the traversal
     *@param v The current Node
     *@param last The last node majority node seen during the traversal
     */
    private void getMajorityNodes(int v, Bipartition last )
    {
	Bipartition b = null;

	if ( t.nodeIsTerminal(v) ) {
	    int taxonNum = t.taxonNumberOfNode( v );

	    if (! parentOfLeaf.containsKey( new Integer(taxonNum) ) ) {
	    	// First time we've seen this taxon

	    		if ( last != null ) {

		    last.addTaxonAsChild( taxonNum );
		    parentOfLeaf.put( new Integer(taxonNum), last );
            
	    		} else {
		    parentOfLeaf.put( new Integer(taxonNum), null );
		    // This shouldn't happen? Surely at least the root is a Majority node and 	
		    // has been seen? Added diagnostic comment below. Nina
		    System.out.println("Taxon with no majority ancestor?");
			}

	    	} else {
		Bipartition currentPar = (Bipartition)parentOfLeaf.get(new Integer(taxonNum));
		// We've seen this taxon before. Retrieve majority ancestor seen so far with 
		// fewest number of leaves. 

		if ( currentPar != null && last != null ) {
		    if (! currentPar.equals(last) ) {

			if ( currentPar.getNumOfLeaves() > last.getNumOfLeaves() ) { 
				// Last majority ancestor in this tree has fewer leaves than stored 
				// ancestor. Save this new ancestor as the parent of the taxon.
			    parentOfLeaf.remove( new Integer(taxonNum) );
			    last.addTaxonAsChild( taxonNum );
			    parentOfLeaf.put( new Integer(taxonNum), last );
			    currentPar.removeTaxonAsChild( taxonNum );

			}
		    }
		} else if ( currentPar == null && last != null ) {
			// This should not happen; if parentOfLeaf contains this taxon as a key, 
			// it should also have stored the last majority ancestor. Added diagnostic
			// print. 
		    parentOfLeaf.remove( new Integer(taxonNum) );
		    last.addTaxonAsChild( taxonNum );
		    parentOfLeaf.put( new Integer(taxonNum), last );
		    System.out.println("Stored taxon with no majority ancestor?");
		}
	    }
	} else {
		// An internal node. Identified by its hash codes. 
	    int h1 = tempPSW.getFirstHash(v);
	    int h2 = tempPSW.getSecondHash(v);

	    if ( table.isMajorityNode(h1, h2) ) { 
		 if ( ! table.nodeExist(h1, h2) ) {
			// First time we have seen this majority node. 
		    table.setExist( h1, h2 );
		    b = table.getBipartition( h1, h2 );
		    parent = last;

		    if ( parent != null ) {

			parent.addInternalAsChild(b);
			b.setParent( parent );

		    } else {
			b.setParent( null );
		    }
		    
		    majorityNodes.addElement(b);

		} else {
			// a majority node we have seen before. 
		    b = table.getBipartition( h1, h2 );
             
		    if ( last != null ) {
			Bipartition currentPar = b.getParent(); 
			// Retrieve majority ancestor with fewest leaves seen so far. 

			if ( currentPar != null && !currentPar.equals(last) ) {
			    if ( currentPar.getNumOfLeaves() > last.getNumOfLeaves() ) {
				currentPar.removeInternalAsChild(b);
				last.addInternalAsChild(b);
				b.setParent( last );

			    }
			} else if ( currentPar == null && last != null ) {

			    last.addInternalAsChild(b);
			    b.setParent(last);

			}// end currentPar != null
		    }// end last != null
		}// node Exist
		// Since this is a majority node, it becomes "last" for its descendants. 	
		last = b;
	    } // end isMajorityNode
	}// end node is internal

	for( int d = t.firstDaughterOfNode(v); d > 0; d = t.nextSisterOfNode(d) ) {
	    getMajorityNodes(d, last);
	}// end for
	// Recursive call on children of this node. 
    }// end getMajorityNodes()


    /**
     * Inserts the majority nodes into the majority tree
     */
    private void insertMajorityNodes()
    {
	Bipartition b,par;
//    System.out.println("Inside InsMaj node: maj size="+majorityNodes.size());

    for ( int i = 0; i < majorityNodes.size(); i++ ) {
    	  b = (Bipartition)majorityNodes.elementAt(i);
		  par=b.getParent();
		  if (par!=null){
			    int internal = tempTree.sproutDaughter( tempTree.getRoot(), false );
	            tempTree.setTaxonNumber(internal, -1, false);
	            encoding.put(b, new Integer(internal));
	       }
		   else{
	      //parent equal to null which means node is root. store root as root not as daughter of root.
	      //this is executed only once as root is node which is present only once.
			 int temp =tempTree.getRoot();
		   	 encoding.put(b, new Integer(temp));
		  }
	   }
	// This is building a tree in which all majority nodes are children of the root? 
	//System.out.println("before insert & rearrange:" +tempTree.writeTree(0, true, true, true, true,":"));
	insertAndRearrange();

   }



    /**
     * Rearranges the majority nodes in the tree, that is to assign nodes to their respective
     * parents.
     * What we'd like to do is re-adjust parent pointers.
     * But Mesquite functions for moving branches insert them into the middle of old
     * branches, creating new nodes, which we call clones...which eventually need to be
     * collapsed.   
     * So this gets quite hairy. 
     */
     
    private void insertAndRearrange() //double check this?
    //this is where the bug is
    {
	Bipartition b;
	int nodeInTree, motherInTree, parentNode, numDaughters = 0, sister = 0;
	for ( int i = 0; i < majorityNodes.size(); i++ ) {
	    b = (Bipartition)majorityNodes.elementAt(i);

	    if ( encoding.containsKey(b) ) {
		Bipartition parent = b.getParent();
		if ( parent != null ) { // if we have a parent (ie not the root?)
		    parentNode = ((Integer)encoding.get(parent)).intValue();
		    nodeInTree = ((Integer)encoding.get(b)).intValue();
		    motherInTree = tempTree.motherOfNode( nodeInTree );
		    numDaughters = tempTree.numberOfDaughtersOfNode( motherInTree );
		    if ( motherInTree != parentNode ) { // if we don't already have the right parent set
			if ( numDaughters == 2 ) // current mother has exactly 2 daughters
			    sister = getInternalSister( nodeInTree ); // sister of node in current tree

			if ( numDaughters == 1 ) { // current mother has exactly one daughter
				// create a new clone daughter for current mother
			    int newNode = tempTree.sproutDaughter( motherInTree, false ); 
			    tempTree.setTaxonNumber( newNode, -1, false );
			}

			if ( tempTree.nodeIsTerminal(parentNode) ) {
				// desired mother currently has no daughters
			    if ( tempTree.moveBranch(nodeInTree, parentNode, false) 
			    	// move branch detaches nodeInTree, and then reattaches it to branch 
			    	// from parentNodes's parent to parentNode! 
			    	// Won't do anything if parentNode is already nodeInTree's parent,
			    	// if they're sisters, or if nodeInTree is the only child of its 
			    	// current parent (motherInTree). 
			    ||
			    	numDaughters == 2 && tempTree.nodesAreSisters(nodeInTree, parentNode) 
			    ) {
			    	
				int newMother = tempTree.motherOfNode( nodeInTree );

				// Deal with the case in which desired mother is your childless sister.
				if ( sister != 0 )
				    moveSisterUp( motherInTree, sister );

				if ( numDaughters == 2 && sister == 0 ) { 
					// sister is first in list?

				    int newNode = tempTree.insertNode( motherInTree, false );
				    tempTree.setTaxonNumber( newNode, -1, false );
				    Bipartition m = findBipartition( motherInTree );
				    // add a new node above old mother, re-assign old mother's bipartiotion
				    // to this new node. Why? 

				    if ( m != null ) {
				    		encoding.remove(m);
				    		encoding.put(m, new Integer(newNode));
				    				}

				} // end ( numDaughters == 2 && sister == 0 )
				 
				
				if ( tempTree.snipClade(parentNode,false) ) { // pi added this -
					// desired parent is now sister; cut it out. 
					// nodeInTree's bipartition gets assigned to new mother. 
				    encoding.remove(b);
				    encoding.put(b, new Integer(newMother));

				    // Add a new clone between new mother and her mother, and 
				    // assign this to desired parent.
				    int newNode = tempTree.insertNode( newMother, false );
				    tempTree.setTaxonNumber( newNode, -1, false );
				    encoding.remove( parent );
				    encoding.put(parent, new Integer(newNode));

				} else {
				    System.err.println("DELETE FAILED");
				} // end ( tempTree.snipClade(parentNode,false) )


			    } // move when parentNode is terminal failed. 
			    		else {
					int branchFrom =nodeInTree ;
					int branchTo = parentNode;

							if (branchFrom==branchTo)
								System.err.println(" (branchFrom==branchTo)");
							else if (!tempTree.nodeExists(branchFrom) || !tempTree.nodeExists(branchTo))
								System.err.println(" (!nodeExists(branchFrom) || !nodeExists(branchTo))");
							else if  (tempTree.descendantOf(branchTo,branchFrom))
								System.err.println(" (descendantOf(branchTo,branchFrom))");
							else if  (branchTo == tempTree.motherOfNode(branchFrom) && !tempTree.nodeIsPolytomous(branchTo))
								System.err.println(" (branchTo == motherOfNode(branchFrom) && !nodeIsPolytomous(branchTo))");
							else if (tempTree.nodesAreSisters(branchTo, branchFrom) && (tempTree.numberOfDaughtersOfNode(tempTree.motherOfNode(branchFrom))==2))
								System.err.println(" (nodesAreSisters(branchTo, branchFrom) && (numberOfDaughtersOfNode(motherOfNode(branchFrom))==2))");
							else if (tempTree.numberOfDaughtersOfNode(tempTree.motherOfNode(branchFrom))==1) //TODO: NOTE that you can't move a branch with
							System.err.println(" (numberOfDaughtersOfNode(motherOfNode(branchFrom))==1)");
				System.err.println("MOVED FAILED: numDaughters="+numDaughters+ " nodeInTree="+nodeInTree +" parentNode="+parentNode);
				//System.exit(1);

				// print out more info
				// maybe stop doing everything?
			    }
			} else {
				// desired parent already has children. 
			    if (tempTree.moveBranch(nodeInTree, tempTree.firstDaughterOfNode(parentNode), false) ){
			    	// move branch detaches nodeInTree, and then reattaches it to branch 
			    	// from parentNode to existing daughter, at a new clone node. 
			    		if ( sister != 0 )
			    			moveSisterUp( motherInTree, sister );

			    		if ( numDaughters == 2 && sister == 0 ) {
			    			// other daughter of old mother has taken over old mother node

			    			int newNode = tempTree.insertNode( motherInTree, false );
			    			tempTree.setTaxonNumber( newNode, -1, false );
			    			Bipartition m = findBipartition( motherInTree );

			    			if ( m != null ) {
			    				encoding.remove(m);
			    				encoding.put(m, new Integer(newNode));
			    			}
			    		}
			    		// collapse clone parent into desired parent.
			    		tempTree.collapseBranch( tempTree.motherOfNode(nodeInTree), false );

			    } else {
				System.err.println("MOVE FAILED");
			    }
			}// end else (desired parent had children already). 
		    }//end motherInTree != parentNode
		}//end parent != null
	    }//end containsKey(b)
	}// end for
    	insertTaxons();
    }


    /**
     * Inserts the taxon nodes into the tree
     */
    private void insertTaxons()
    {
	Bipartition b = null;
	int nodeInTree, parentNode = 0, motherInTree;
	
	for( int i = 0; i < majorityNodes.size(); i++ ) {
	    b = (Bipartition)majorityNodes.elementAt(i);
	    if ( encoding.containsKey(b) ) {
		nodeInTree = ((Integer)encoding.get(b)).intValue();
    	if ( b.hasTaxonChildren() ) {
			int j = 0;
		    for ( int t = b.firstTaxonAsChild(); t != b.INVALID; t = b.nextTaxonAsChild() ) {
				int taxon = tempTree.sproutDaughter(nodeInTree, false);
	    		tempTree.setTaxonNumber(taxon, t, false);
  			    j++;
		    }//end for
		}//end hasTaxonChildren()
	    }// end containsKey(b)
	}//end for
}


    /**
     * This is used to move a node's sister up in the tree. This is done because moving a
     * branch can remove majority nodes from the tree by re-assigning the node's children
     * to the node's parent
     *@param motherInTree The node which will replace the node being removed
     *@param siste The node that is being removed
     */
    private void moveSisterUp( int motherInTree, int sister )
    {
	Bipartition sis = findBipartition(sister);

	if ( sis != null ) {
	    int newNode = tempTree.insertNode(motherInTree, false);
	    tempTree.setTaxonNumber(newNode, -1, false);

	    Bipartition mother = findBipartition(motherInTree);

	    if( mother != null ) {
		encoding.remove(mother);
		encoding.put(mother, new Integer(newNode));

	    }
	    encoding.remove(sis);
	    encoding.put(sis, new Integer(motherInTree));

	}
    }


    /**
     * This is used to get the sister of a node if the sister  is internal and if the parent of the node
     * has only two children
     *@return the number of the node or 0
     */
    private int getInternalSister( int nodeInTree )
    {
	int sister = 0;

	if ( tempTree.nodeIsFirstDaughter(nodeInTree) ) {
	    if ( tempTree.nodeIsInternal(tempTree.nextSisterOfNode(nodeInTree)) )
		sister = tempTree.nextSisterOfNode(nodeInTree);

	} else if ( tempTree.nodeIsInternal(tempTree.previousSisterOfNode(nodeInTree)) ) {
	    sister = tempTree.previousSisterOfNode(nodeInTree);
	}
	return sister;
    }

    /**
     * Used to finds the Bipartition that corresponds to this node
     *@param node The specified node
     *@return The Bipartition or null
     */
    private Bipartition findBipartition( int node )
    {
	Bipartition b = null;
	Map.Entry m;

	for ( Iterator i = encoding.entrySet().iterator(); i.hasNext(); ) {
	    m = (Map.Entry)i.next();

	    if ( ((Integer)m.getValue()).intValue() == node ) {
		b = (Bipartition)m.getKey();
		break;
	    } else {
		continue;
	    }
	}
	return b;
    }

    /* Random number generator */
	static Random r = new Random();

    /**
     * Used to select unique random hashCodes for each taxon.
     * Checks that all random numbers chosen are unique. 
     * Sadly, this does not guarantee that all hashcodes for internal
     * nodes (which are computed from the taxon hashcodes) are unique. 
     *@param tableSize The size of our Hashtable
     */
    private void selectRandomHashes(int tableSize )
    {
	//Random r = new Random();
	int rn = 0;
	int newTableSize = 10 * tableSize; // Hash2 values can be much bigger than hash1

	for ( int i = 0;  i < h1.length; i++ ) {
	    rn =  r.nextInt( tableSize );

	    while ( bitSet.get(rn) ) {
		rn = r.nextInt( tableSize );
	    }
	    bitSet.set(rn);
	    h1[i] = rn;
	}

        bitSet.clear();

	for ( int k = 0; k < h2.length; k++ ) {
	    int nr = r.nextInt( newTableSize );

	    while ( bitSet.get(nr) ) {
		nr = r.nextInt( newTableSize );
	    }
	    bitSet.set(nr);
	    h2[k] = nr;
	}
    }


    /**
     * This method is used to get the next biggest prime number relative to the given
     * number
     *@param num The number in which we are trying to find the next biggest prime
     *@return The next biggest prime.
     */
    public int getPrime( int num )
    {
	int number = num;

	while( ! isPrime( number ) ){
	    number++;
	}
	return number;
    }


    /**
     * This method checks to see if a given number is a prime number
     *@param num The number that we are checking
     *@return true if the number is a prime, false otherwise.
     */
    private boolean isPrime( int num )
    {
	if ( num <= 2 ){
	    return (num == 2);
	}

	if ( num % 2 == 0 ) {
	    return false;
	}

	for ( int i = 3; i <= (int)Math.sqrt( num ); i += 2 ) {

	    if ( num % i == 0 )
		return false;
	}
	return true;
    }
}
