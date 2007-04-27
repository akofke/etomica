package etomica.paracetamol;


import etomica.action.AtomsetAction;
import etomica.atom.AtomGroup;
import etomica.atom.AtomSet;
import etomica.atom.AtomsetArray;
import etomica.atom.IAtom;
import etomica.atom.IAtomGroup;
import etomica.atom.iterator.AtomsetIterator;
import etomica.atom.iterator.AtomsetIteratorBasisDependent;

/**
 * @author Tai Tan
 */


public class AtomsetIteratorIndexList implements AtomsetIteratorBasisDependent, AtomsetIterator {

	
	
	/**
     * Constructs iterator without defining set of atoms.
     */
    public AtomsetIteratorIndexList(int [][]index) {
    	atomset = new AtomsetArray(3);
    	atoms = atomset.getArray();
        this.index = index;
    }

    public int basisSize(){
    	return 1;
    }
    
    public boolean haveTarget(IAtom a){
    	for(int i =0; i < index.length; i++){   //index.length = number of sets
    		
    		if (a == parentGroup.getChildList().get(index[i][0])){
    			return true;
    		}
    		if (a == parentGroup.getChildList().get(index[i][1])){
    			return true;
    		}
    		if (a == parentGroup.getChildList().get(index[i][2])){
    			return true;
    		}
    	}
    	return false;
    }
    
    public void setTarget(IAtom a){
    	target = a;
    	unset();
    }
                                

	public void setBasis(AtomSet moleculeParacetamol) {
		parentGroup = (AtomGroup)moleculeParacetamol;		
		unset();
	}

    public int size() {
        return index.length;
    }

    /**
     * Performs the given action on an AtomPair containing the three atoms last
     * identified via setSet. Does nothing if either such atom is null.
     * Unaffected by and has no effect on the reset/unset state.
     */
    public void allAtoms(AtomsetAction action) {
    	
    	for(int i =0; i < index.length; i++){   //index.length = number of sets
    		
    		atoms[0] = parentGroup.getChildList().get(index[i][0]);
    		atoms[1] = parentGroup.getChildList().get(index[i][1]);
    		atoms[2] = parentGroup.getChildList().get(index[i][2]);
    		action.actionPerformed(atomset);
    	}
    }

    /**
     * Returns true if the given atom set has the same two atoms passed to the
     * last call to setAtom(Atom).  Returns false if any relevant atoms are null.
     */
    public boolean contains(AtomSet a) { //T: a equals the set
    	
    	for(int i =0; i < index.length; i++){   //index.length = number of pairs
    		atoms[0] = parentGroup.getChildList().get(index[i][0]);
    		atoms[1] = parentGroup.getChildList().get(index[i][1]);
    		atoms[2] = parentGroup.getChildList().get(index[i][2]);
    		if (atomset.equals(a)){
    			return true;
    		}
    	}
    	return false;
    }

    /**
     * Returns true if three non-null atoms have set and a call to reset() has
     * been performed, without any subsequent calls to next() or nextPair().
     */
    public boolean hasNext() {

    	if (target != null){
        	for(; cursor < index.length; cursor++){   //index.length = number of pairs
        		
        		if (target == parentGroup.getChildList().get(index[cursor][0])){
        			break;
        		}
        		if (target == parentGroup.getChildList().get(index[cursor][1])){
        			break;
        		}
        		if (target == parentGroup.getChildList().get(index[cursor][2])){
        			break;
        		}
        	}
    	}
        return (cursor < index.length);
    	
    }

    /**
     * Sets iterator to a state where hasNext() returns false.
     */
    public void unset() {
        cursor = index.length;
    }

    /**
     * Resets iterator to a state where hasNext is true, if atoms in pair are
     * not null.
     */
    public void reset() {
    	cursor = 0;
    }

    /**
     * Returns the iterator's pair and unsets iterator.
     */
    public AtomsetArray nextSet() {
        if (!hasNext())
            return null;
		atoms[0] = parentGroup.getChildList().get(index[cursor][0]);
		atoms[1] = parentGroup.getChildList().get(index[cursor][1]);
		atoms[2] = parentGroup.getChildList().get(index[cursor][2]);
		
		cursor++;
        return atomset;
    }

    /**
     * Same as nextSet().
     */
    public AtomSet next() {
        return nextSet();
    }

    /**
     * Returns the set last specified via setSet, if hasNext() is true. Does
     * not advance iterator.
     */
    public AtomSet peek() {
        if (!hasNext())
            return null;
		atoms[0] = parentGroup.getChildList().get(index[cursor][0]);
		atoms[1] = parentGroup.getChildList().get(index[cursor][1]);
		atoms[2] = parentGroup.getChildList().get(index[cursor][2]);
        return atomset;
    }

    /**
     * Returns 3, indicating that this is a set of three iterator.
     */
    public final int nBody() {
        return 3;
   }

    private int [][]index;
    private int cursor;
    private IAtomGroup parentGroup;
    private IAtom target;
    
    private AtomsetArray atomset;
    private IAtom[] atoms;
    
	private static final long serialVersionUID = 1L;

}

