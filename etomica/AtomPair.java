package etomica;
/**
 * An association of two atoms.  Each AtomPair holds one CoordinatePair (obtained from a Space class),
 * which has all the methods needed to compute atom distance and momentum-difference vectors, dot products, etc.
 * Each AtomPair instance must be associated with a phase CoordinatePair requires a phase.boundary
 * for its complete definition
 */
public final class AtomPair implements java.io.Serializable {
    public static String getVersion() {return "01.06.25";}
    public Atom atom1, atom2;
    public final Space.CoordinatePair cPair;
//    public Potential potential;
   /**
    * Constructs an AtomPair for the given phase, but with no designated atoms.
    */
    public AtomPair(Phase phase) {
        cPair = phase.parentSimulation().space().makeCoordinatePair(phase);
    }
    /**
     * Constructs an AtomPair using the given atoms.  Assumes that the atoms are in the same phase.
     * The method atom1() will return the first atom in the argument list here, and atom2() the second.
     */
    public AtomPair(Atom a1, Atom a2) {  //Assumes a1 and a2 are in same phase
        cPair = a1.parentSimulation().space().makeCoordinatePair(a1.parentPhase());
        reset(a1, a2);
    }
    /**
     * Constructs an AtomPair using the given atoms and coordinate pair.  The coordinate pair
     * is assumed to correspond to the given atoms.  Passing it here can save on the overhead
     * of making it if it is already in place
     * The method atom1() will return the first atom in the argument list here, and atom2() the second.
     */
    public AtomPair(Atom a1, Atom a2, Space.CoordinatePair c) {atom1 = a1; atom2 = a2; cPair = c;}
    
    /**
     * Clones this atomPair without cloning the atoms or their coordinates.
     * The returned atomPair refers to the same pair of atoms as the original.
     * This can be used to make a working copy of an atomPair that is returned by an atomPair iterator.
     * Method is called "copy" instead of "clone" because whole object tree isn't cloned.
     */
    public AtomPair copy() {
        return new AtomPair(atom1, atom2, cPair.copy());  //cannot use super.clone() because cPair (being final) cannot then be changed to a clone of cPair
    }

   /**
    * Redefines the atom pair to correspond to the given atoms
    */
    public void reset(Atom a1, Atom a2) {
        atom1 = a1; 
        atom2 = a2;
        if(a2 != null) reset();
    }
    /**
     * Resets the coordinate pair for the current values of the atoms
     */
    public void reset() {
        cPair.reset(atom1.coord, atom2.coord);
    }
    /**
     * @return the square of the distance between the atoms, |r1 - r2|^2
     */
    public final double r2() {return cPair.r2();}
    
    /**
     * @return the square of the velocity-difference between the atoms, |p1/m1 - p2/m2|^2
     */
    public final double v2() {return cPair.v2();}
    
    /**
     * @return the dot product of the distance and the velocity difference, (r1 - r2).(p1/m1 - p2/m2)
     */
    public final double vDotr() {return cPair.vDotr();}
    
    /**
     * @return the vector distance between the atoms, r2 - r1
     */
    public final Space.Vector dr() {return cPair.dr();}
    
    /**
     * @return the i<sup>th</sup> component of the distance vector r2 - r1, where i=0 is the first component
     */
    public final double dr(int i) {return cPair.dr(i);}
    
    /**
     * @return the i<sup>th</sup> component of the velocity-difference vector p2/m2 - p2/m1, where i=0 is the first component
     */
    public final double dv(int i) {return cPair.dv(i);}
    
    /**
     * @return the first atom of the atom pair (as set when invoking the constructor or the reset method)
     */
    public final Atom atom1() {return atom1;}
    
    /**
     * @return the second atom of the atom pair (as set when invoking the constructor or the reset method)
     */
    public final Atom atom2() {return atom2;}
    
    /**
     * Sorts by separation distance all the atom pairs produced by an atomPair iterator
     * Returns the first element of a linked list of atomPair(Linker)s, sorted by increasing distance
     * Perhaps better to do this using java.util.Collections (in java 1.2 API)
     */
    public static AtomPairLinker distanceSort(AtomPairIterator api) {
        if(!api.hasNext()) return null;
        AtomPairLinker firstLink = new AtomPairLinker(api.next().copy());
        while(api.hasNext()) {                      //loop through all pairs generated by api
            AtomPair nextPair = api.next().copy();  //make a copy of pair for use in ordered list
            //Insert pair into ordered list in proper location
            AtomPairLinker previous = null;  //holds value from previous iteration of this for-loop
            boolean inserted = false;
            for(AtomPairLinker link=firstLink; link!=null; link=link.next()) {
                if(nextPair.r2() < link.pair().r2()) {  //insert nextPair before current pair
                    if(previous == null) {firstLink = new AtomPairLinker(nextPair,firstLink);} //nextPair is new firstPair, to be followed by old firstPair
                    else {previous.setNext(new AtomPairLinker(nextPair,link));}  //place nextPair between last and pair
                    inserted = true;
                    break;  //break out of for-loop
                }
                previous = link;
            }  //end of for loop
            if(!inserted) //reached end of list without inserting;
                previous.setNext(new AtomPairLinker(nextPair));   //insert after last link
        }
        return firstLink;
    }//end of distanceSort
    
}  //end of  AtomPair