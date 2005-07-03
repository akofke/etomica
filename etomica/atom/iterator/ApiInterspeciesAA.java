package etomica.atom.iterator;

import etomica.AtomTreeNodeGroup;
import etomica.Phase;
import etomica.Species;
import etomica.atom.AtomList;

/**
 * Gives pairs formed from the molecules of two different species in a phase.
 * Species are specified at construction and cannot be changed afterwards.
 */

/*
 * History
 * Created on Dec 30, 2004 by kofke
 */

public class ApiInterspeciesAA extends AtomPairIteratorAdapter implements
        AtomsetIteratorPhaseDependent {

    /**
     * Constructs iterator that provides iterates taken from the molecules of
     * two species. Atoms in iterate pairs will be such that species of atom0 is
     * species[0], and species of atom1 is species[1].
     * 
     * @param species
     *            array of two different, non-null species
     * @throws NullPointerException
     *             if species or one of its elements is null
     * @throws IllegalArgumentException
     *             if species.length != 2 or if species[0] == species[1]
     */
    public ApiInterspeciesAA(Species[] species) {
        super(new ApiInterList());
        apiInterList = (ApiInterList) iterator;
        if(species.length != 2) {
            throw new IllegalArgumentException("Incorrect array length; must be 2 but length is "+species.length);
        }
        species0 = species[0];
        species1 = species[1];
        if (species0 == null || species1 == null) {
            throw new NullPointerException(
                    "Constructor of ApiInterspeciesAA requires two non-null species");
        }
        if (species0 == species1) {
            throw new IllegalArgumentException(
                    "Constructor of ApiInterspeciesAA requires two different species");
        }
    }

    /**
     * Configures iterator to return molecules from the set species in the given
     * phase.  No iterates are given if phase is null.
     */
    public void setPhase(Phase phase) {
        if (phase == null) {
            emptyList.clear();
            apiInterList.setOuterList(emptyList);
        } else {
            apiInterList.setOuterList(((AtomTreeNodeGroup) phase
                    .getAgent(species0).node).childList);
            apiInterList.setInnerList(((AtomTreeNodeGroup) phase
                    .getAgent(species1).node).childList);
        }
    }

    private final ApiInterList apiInterList;
    private final Species species0, species1;
    private final AtomList emptyList = new AtomList();
}
