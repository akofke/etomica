/*
 * History
 * Created on Aug 26, 2004 by kofke
 */
package etomica;

import etomica.action.AtomsetAction;

/**
 * Iterator for looping through the sequence list relative to a
 * specified atom.
 */
public final class AtomIteratorSequencerList extends AtomIteratorAdapter implements AtomIteratorAtomDependent {

	/**
	 * Constructs new class with hasNext as false.  Must
	 * invoke setAtom and reset before beginning iteration.
	 */
	public AtomIteratorSequencerList() {
		super(new AtomIteratorList());
		listIterator = (AtomIteratorList)iterator;
		listIterator.unset();
	}
	
	/**
	 * Overrides superclass reset to ensure no reset is performed
	 * if a firstAtom has not been identified.  Otherwise readies
	 * for iteration beginning with firstAtom, or the one following
	 * it if skippingFirst is true.
	 */
	public void reset() {
		if(firstAtom == null) return;
		listIterator.reset();
        int n = numToSkip;
		while (n-- != 0 && listIterator.hasNext()) listIterator.next();
	}

	/* (non-Javadoc)
	 * @see etomica.AtomIterator#allAtoms(etomica.AtomActive)
	 */
	public void allAtoms(AtomsetAction action) {
		reset();
        while (listIterator.hasNext()) {
            action.actionPerformed(listIterator.next());
        }
	}

	public boolean contains(Atom[] atom) {
        return (listIterator.getList().indexOf(atom[0]) >= numToSkip);
	}

	public int size() {
        int s = listIterator.size();
        if (s > numToSkip) return s - numToSkip;
        return 0;
	}

	public void setAtom(Atom atom) {
		firstAtom = atom;
		if(atom != null) listIterator.setFirst(atom.seq);
		else listIterator.unset();
	}

	/**
	 * @return Returns the number of atoms to skip.
	 */
	public int getNumToSkip() {
		return numToSkip;
	}
	/**
	 * @param numToSkip: the number of atoms to skip.
	 */
	public void setNumToSkip(int numToSkip) {
		this.numToSkip = numToSkip;
	}
	
	public void setIterationDirection(IteratorDirective.Direction direction) {
		listIterator.setDirection(direction);
	}
	public IteratorDirective.Direction getIterationDirection() {
		return listIterator.getIterationDirection();
	}
	
	private final AtomIteratorList listIterator;
	private int numToSkip = 0;
	private Atom firstAtom = null;

}
