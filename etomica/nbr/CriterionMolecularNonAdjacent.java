package etomica.nbr;

import etomica.atom.AtomPair;

/**
 * 
 * Pair criterion that judges whether two atoms are or are not in the same
 * molecule, and whether or not they are separated by a minimum number of bonds.
 * 
 * 
 * @author nancycribbin
 *  
 */
public final class CriterionMolecularNonAdjacent extends
        CriterionMolecular {

    public CriterionMolecularNonAdjacent(NeighborCriterion criterion) {
        super(criterion);
        this.setIntraMolecular(true);
    }

    /*
     * (non-Javadoc)
     * 
     * @see etomica.atom.AtomPairFilter#accept(etomica.AtomPair)
     */
    public boolean accept(AtomPair pair) {

        int a0 = pair.atom0.getNode().getIndex();
        int a1 = pair.atom1.getNode().getIndex();
        int temp;

        //we do not need to consider a0 = a1, because then temp will be 0, and
        // we will fail the test anyway
        if (a0 > a1) {
            temp = a1 - a0;
        } else {
            temp = a0 - a1;
        }

        //seq does not keep track of numbers, but could be used to count.
        //pair.atom1.seq;
        if (2 < temp && super.accept(pair)) {
            return true;
        }

        return false;
    }

    private static final long serialVersionUID = 1L;
}