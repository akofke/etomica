package etomica;
import etomica.utility.HashMap;

import etomica.lattice.*;
import etomica.utility.Iterator;

/**
 * Iterator factory that uses a cell-based neighbor list.  Constructed
 * and functions as follows.<br>
 * Factory is attached to a simulation by invoking the setIteratorFactory method
 * (in Simulation) with an instance of this factory as an argument.
 * Mediator is placed in simulation when this factory is constructed.  This
 * mediator causes a cell lattices to be attached to each phase.
 * Atom factories decide whether each atom it constructs is given a simple
 * sequencer or a neighbor sequencer; this specification is made by passing
 * the to the atom's constructor the corresponding sequencer factory given
 * by the make*SequencerFactory methods of this iterator factory.  Usually
 * neighbor sequencers are given to molecules, and simple sequencers are
 * used for the atoms and groups they comprise.  In large molecules (e.g. polymers)
 * it may be the subgroups of each molecule that are organized into the cells.<br>
 * Atom is first assigned to its cell when it is constructed in the AtomFactory.makeAtom
 * method.  Its cell assignment is updated every time a translate method is
 * called in its coordinate class (translateTo, translateBy, etc. all perform a call
 * to moveNotify method of the atom's sequencer after doing the move).  The assignment
 * is also updated whenever the setParent method of the node is called (sequencer
 * is informed via its setParentNotify method) or if the [to be implemented] reset
 * method of the iterator factory is called (either directly, or by the registered observer
 * of the lattice when it is informed of a change in the lattice constant or dimensions).
 * The childList of the parent of cell-listed atoms organizes the atoms via their
 * sequencers, which are subclasses of AtomLinker.  The childList contains Tabs that 
 * demark the set of sequencers contained in a given cell.  As an atom moves from one
 * cell to another, it is shuffled in the childList into the segment corresponding
 * to its new cell.  Each cell references separately a fixed set of neighbor cells, and atoms within
 * those cells are accessed for each neighbor cell through the tab it has in the
 * childList.  (actually, the NeighborSequencer contains an "inner" atomlinker that
 * is used to maintain the cell structure; the "main" sequence generally does not
 * change and doesn't keep the cell ordering.  It was found necessary to use two sequences
 * for the times when looping through the atoms and tranlating each in space; each
 * translation potentially modifies the cell-list sequence, so to get through the 
 * list properly the fixed-sequence option is needed).  The neighbor and sequential
 * iterators generated by this factory adhere to the cell-list sequencing; list iterators
 * generated directly (externally to this factory) will loop through the fixed (not
 * cell-ordered) sequence when given the parent's childList as its basis.
 *
 * @author David Kofke
 * 02.02.05
 * 
 */

public class IteratorFactoryCell implements IteratorFactory {
    
    private Primitive primitive;
    private Simulation simulation;
    private int[] dimensions;
    private BravaisLattice[] deployedLattices = new BravaisLattice[0];
    private double neighborRange;
    
    /**
     * Constructs a new iterator factory for the given simulation, using
     * cubic cells for the neighbor listing.  Does not automatically
     * register the factory with the simulation; this must be done
     * separately using the simulation's setIteratorFactory method.
     */
    public IteratorFactoryCell(Simulation sim) {
        this(sim, new PrimitiveCubic(sim), 4);
    }
    
    /**
     * Constructs a new iterator factory for the given simulation, using
     * cells based on the given primitive.  Does not automatically
     * register the factory with the simulation; this must be done
     * separately using the simulation's setIteratorFactory method.
     *
     * @sim          The simulation in which this factory is being used
     * @primitive    The primitive class that defines the type of unit cell used to construct the neighbor-cell lattice
     * @param nCells the number of cell in each dimension; total number of cells is then nCells^D.
     */
    public IteratorFactoryCell(Simulation sim, Primitive primitive, int nCells) {
        this.simulation = sim;
        this.primitive = primitive;
        neighborRange = Default.ATOM_SIZE;
        dimensions = new int[sim.space.D()];
        for(int i=0; i<sim.space.D(); i++) dimensions[i] = nCells;
        
        //Add mediator that places a cell lattice in each phase.
        sim.mediator().addMediatorPair( new Mediator.PhaseNull(sim.mediator()) {
            public void add(Phase phase) {makeCellLattice(phase);}
        });        
    }
    
    /**
     * Constructs the cell lattice used to organize all cell-listed atoms
     * in the given phase.  Each new lattice is set up with its own instance
     * of the primitive, formed by copying the instance associated with this factory.  
     * Note that the phase does not contain any reference
     * to the lattice.  Its association with the phase is made through the 
     * deployedLattices array kept by this iterator factory class, and by 
     * the reference in each neighbor sequencer to the cell containing its atom.
     */
    public BravaisLattice makeCellLattice(final Phase phase) {
        if(phase.parentSimulation() != simulation) throw new IllegalArgumentException("Attempt to apply iterator factory to a phase from a different simulation"); 
        //make the unit cell factory and set it to produce cells of the appropriate size
        final PrimitiveCubic primitiveCopy = (PrimitiveCubic)primitive.copy();//each new lattice works with its own primitive
        AtomFactory cellFactory = primitiveCopy.unitCellFactory();
        ((PrimitiveCubic)primitiveCopy).setSize(phase.boundary().dimensions().component(0)/(double)dimensions[0]);//this needs work
        //construct the lattice
        AtomFactory latticeFactory = new BravaisLattice.Factory(simulation, cellFactory, dimensions, primitiveCopy);
        final BravaisLattice lattice = (BravaisLattice)latticeFactory.makeAtom();
        lattice.shiftFirstToOrigin();
        
        //set up the neighbor lists for each cell in the lattice
        NeighborManager.Criterion neighborCriterion = new NeighborManager.Criterion() {
            public boolean areNeighbors(Site s1, Site s2) {
                return ((AbstractCell)s1).r2NearestVertex((AbstractCell)s2, phase.boundary()) < neighborRange;
            }
        };
        lattice.setupNeighbors(neighborCriterion);
        
        //instantiate the hashmap that will hold the index Integers that
        //are keyed to the the parent of cell-listed set of children
        lattice.agents = new Object[1];
        lattice.agents[0] = new HashMap();
        
        //resize and update array that enables lookup of the lattice in any given phase
        int latticeCountOld = deployedLattices.length;
        if(phase.index >= latticeCountOld) {
            BravaisLattice[] newArray = new BravaisLattice[phase.index+1];
            for(int i=0; i<latticeCountOld; i++) newArray[i] = deployedLattices[i];
            deployedLattices = newArray;
        }
        deployedLattices[phase.index] = lattice;
        
        //add listener to notify all sequencers of any lattice events (resizing of lattice, for example)
        lattice.eventManager.addListener(new LatticeListener() {
            public void actionPerformed(LatticeEvent evt) {
                if(evt.type() == LatticeEvent.REBUILD || evt.type() == LatticeEvent.ALL_SITE) {
                    Phase p = getPhase((BravaisLattice)evt.lattice());
                    if(p == null) return;
                    AtomIteratorMolecule iterator = new AtomIteratorMolecule(p);
                    while(iterator.hasNext()) ((CellSequencer)iterator.next().seq).latticeChangeNotify();
                }//end if
            }
        });
        
        //add listener to phase to update the size and placement of the lattice
        //cells if the phase undergoes an inflation of its boundary
        phase.boundaryEventManager.addListener(new PhaseListener() {
            public void actionPerformed(PhaseEvent evt) {
                if(!evt.type().equals(PhaseEvent.BOUNDARY_INFLATE)) return;
                if(!evt.isotropic) throw new RuntimeException("Cannot handle anisotropic inflate in IteratorFactoryCell");
                    //we expect that primitive.lattice() == null, so change of size doesn't cause replacement of atoms in cells
                primitiveCopy.setSize(evt.isoScale * primitiveCopy.getSize());
                AtomIteratorListSimple cellIterator = new AtomIteratorListSimple(lattice.siteList());
                while(cellIterator.hasNext()) cellIterator.next().coord.inflate(evt.isoScale);
            }
        });
        return lattice;
    }
    
    /**
     * Returns the cell lattice corresponding to the given phase.
     */
    public BravaisLattice getLattice(Phase phase) {
        if(phase == null) return null;
        else return deployedLattices[phase.index];
    }
    
    /**
     * Returns the phase corresponding to the given cell lattice.
     */
    public Phase getPhase(BravaisLattice lattice) {
        if(lattice == null) return null;
        //check all phases that have been added to the simulation
        for(Iterator ip=simulation.phaseList().iterator(); ip.hasNext(); ) {
            Phase phase = (Phase)ip.next();
            if(getLattice(phase) == lattice) return phase;
        }
        return null;//no phase corresponding to given lattice found in simulation
    }
    
    /**
     * Sets the maximum range of interaction for which the cells must keep neighbors.
     */
    public void setNeighborRange(double r) {
        neighborRange = r;
        for(int i=0; i<deployedLattices.length; i++) {
            final Phase phase = getPhase(deployedLattices[i]);
            NeighborManager.Criterion neighborCriterion = new NeighborManager.Criterion() {
                public boolean areNeighbors(Site s1, Site s2) {
                    return ((AbstractCell)s1).r2NearestVertex((AbstractCell)s2, phase.boundary()) < neighborRange;
                }
            };
            deployedLattices[i].setupNeighbors(neighborCriterion);
        }
    }
    public double getNeighborRange() {return neighborRange;}

    
 //   public AtomIterator makeGroupIteratorSimple() {return new AtomIteratorListSimple();}

    public AtomIterator makeGroupIteratorSequential() {
        return new SequentialIterator(this);
    }
        
    public AtomIterator makeIntragroupIterator() {return new IntragroupIterator(this);}
    public AtomIterator makeIntergroupIterator() {
        throw new RuntimeException("IteratorFactoryCell.makeIntergroupIterator not yet implemented");
    }
    
    public AtomSequencer makeAtomSequencer(Atom atom) {return new SimpleSequencer(atom);}
    
    public AtomSequencer makeNeighborSequencer(Atom atom) {return new NbrSequencer(atom);}
    //maybe need an "AboveNbrLayerSequencer" and "BelowNbrLayerSequencer"
    
    public Class atomSequencerClass() {return SimpleSequencer.class;}
    
    public Class neighborSequencerClass() {return NbrSequencer.class;}
    
    public AtomSequencer.Factory atomSequencerFactory() {return SimpleSequencer.FACTORY;}
    
    public AtomSequencer.Factory neighborSequencerFactory() {return NbrSequencer.FACTORY;}
    
/////////////////////////////////////////////////////////////////////////////////////////////

/**
 * Iterates without reference to an atom, returning all the child atoms
 * of a basis in an order consistent with neighborlist order.
 */
public static final class SequentialIterator implements AtomIterator {
    
    private AtomIteratorList listIterator = new AtomIteratorList();
    private AtomLinker.Tab[] tabs;
    private boolean iterateCells;
    private int tabIndex;
    private IteratorFactoryCell factory;
    
    public SequentialIterator(IteratorFactoryCell f) {
        factory = f;
    }
    
    public void setBasis(Atom a) {
        AtomTreeNodeGroup basis = (AtomTreeNodeGroup)a.node;
        iterateCells = basis.childSequencerClass().equals(NbrSequencer.class);
        if(iterateCells) {
            BravaisLattice lattice = factory.getLattice(basis.parentPhase());
            Atom cell = lattice.siteList().getFirst();
            tabs = (AtomLinker.Tab[])cell.agents[0];
            HashMap hash = (HashMap)lattice.agents[0];
            tabIndex = ((Integer)hash.get(basis)).intValue();
        } else {
            listIterator.setBasis(basis.childList);
        }
    }
    
    public Atom reset() {
        if(iterateCells) {
            //set iterator to begin at first cell's tab and circle through 
            //until it is encountered again
            return listIterator.reset(tabs[tabIndex], tabs[tabIndex], IteratorDirective.UP);
        } else {
            return listIterator.reset();
        }
    }
    
    public boolean hasNext() {return listIterator.hasNext();}
    
    public boolean contains(Atom atom) {
        throw new RuntimeException("IteratorFactoryCell.SequentialIterator.contains() not yet implemented");
    }
    
    public Atom reset(IteratorDirective id) {
        throw new RuntimeException("IteratorFactoryCell.SequentialIterator.reset(IteratorDirective) not yet implemented");
    }
    
    /**
     * Returns the next atom in the iteration sequence.  Assumes that hasNext is
     * true; calling when hasNext is false can lead to unpredictable results, and
     * may or may not cause an error or exception.
     */
    public Atom next() {return listIterator.next();}
    
    public void allAtoms(AtomAction act) {
        listIterator.allAtoms(act);
    }
    
        
    public Atom getBasis() {
        throw new RuntimeException("IteratorFactoryCell.SequentialIterator.getBasis() not  implemented");
    }
    
    public int size() {
        throw new RuntimeException("IteratorFactoryCell.SequentialIterator.size() not yet implemented");
    }
    
}
/////////////////////////////////////////////////////////////////////////////////////////////

/**
 * Iterates among the children of a given basis, those atoms
 * that are cell-list neighbors of a specified atom that is
 * a child of the same basis.
 */
//would like to modify so that central atom can be any descendant of the basis.
public static final class IntragroupIterator implements AtomIterator {
    
    public IntragroupIterator(IteratorFactoryCell factory) {
        iteratorFactory = factory;
    }
    
    /**
     * Indicates if another iterate is forthcoming.
     */
    public boolean hasNext() {return listIterator.hasNext();}
    
    /**
     * True if the parent group of the given atom is the current basis for the iterator.
     * False otherwise, or if atom or basis is null.
     */
    public boolean contains(Atom atom) {
        return atom != null && basis != null && atom.node.parentNode() == basis;
    }
    
    /**
     * Does reset if atom in iterator directive is child of the current basis.  
     * Sets hasNext false if given atom does is not child of basis.  Throws
     * an IllegalArgumentException if directive does not specify an atom.
     */
    public Atom reset(IteratorDirective id) {
        direction = id.direction();
        return reset(id.atom1());
    }
    
    //we assume that the only Tab links in the list are those demarking
    //the beginning of each cell's sequence; thus we reset the list iterator
    //using null as the terminator
    
    public Atom reset(Atom atom) {
        referenceAtom = atom;
        upListNow = direction.doUp();
        doGoDown = direction.doDown();
        nextAtom = null;
        if(atom == null) {
            throw new IllegalArgumentException("Cannot reset IteratorFactoryCell.IntragroupIterator without referencing an atom");
        //probably need isDescendedFrom instead of parentGroup here
        } 
        if(atom.node.parentNode() != basis) {
            throw new IllegalArgumentException("Cannot return IteratorFactoryCell.IntragroupIterator referencing an atom not in group of basis");
        }
        if(iterateCells) {
            referenceCell = (AbstractCell)((NbrSequencer)atom.seq).site();
            cellIterator.setBasis(referenceCell);
            listIterator.unset();
            if(upListNow) {
                cellIterator.reset(IteratorDirective.UP);//set cell iterator to return first up-neighbor of reference cell
                listIterator.reset(((NbrSequencer)referenceAtom.seq).nbrLink, null, IteratorDirective.UP);
                listIterator.next();//advance so not to return reference atom
            }
            if(!listIterator.hasNext()) advanceCell();
        } else if(upListNow) {//no cell iteration
            listIterator.reset(referenceAtom.seq, IteratorDirective.UP);
            listIterator.next();//advance so not to return reference atom
            if(!listIterator.hasNext() && doGoDown) {
                listIterator.reset(referenceAtom.seq, IteratorDirective.DOWN);
                listIterator.next();//advance so not to return reference atom
                upListNow = false;
                doGoDown = false;
            }
        } else if(doGoDown) {//no cell iteration
            listIterator.reset(referenceAtom.seq, IteratorDirective.DOWN);
            listIterator.next();//advance so not to return reference atom
            doGoDown = false;
        }
        return listIterator.peek();
    }
                
    // Moves to next cell that has an iterate
    private void advanceCell() {
        do {
            if(cellIterator.hasNext()) {
                Atom cell = cellIterator.next();
                AtomLinker.Tab[] tabs = (AtomLinker.Tab[])cell.agents[0];
                if(upListNow) {
                    listIterator.reset(tabs[tabIndex], null, IteratorDirective.UP);
                } else {
                    listIterator.reset(tabs[tabIndex].nextTab, null, IteratorDirective.DOWN);
                }
            } else if(doGoDown) {//no more cells that way; see if should now reset to look at down-cells
                cellIterator.reset(IteratorDirective.DOWN);//set cell iterator to return first down neighbor of reference cell
                listIterator.reset(((NbrSequencer)referenceAtom.seq).nbrLink, null, IteratorDirective.DOWN);
                listIterator.next();//advance so not to return reference atom
                upListNow = false;
                doGoDown = false;
            } else {//no more cells at all
                break;
            }
        } while(!listIterator.hasNext());
    }
            
    public Atom next() {
        Atom atom = listIterator.next();
        if(!listIterator.hasNext() && iterateCells) advanceCell();
        return atom;
    }
    
    /**
     * Throws RuntimeException because this is a neighbor iterator, and must
     * be reset with reference to an atom.
     */
    public Atom reset() {
        throw new RuntimeException("Cannot reset IteratorFactoryCell.IntragroupIterator without referencing an atom");
    }
    
    
    /**
     * Performs given action for each child atom of basis.
     */
    public void allAtoms(AtomAction act) {
        throw new RuntimeException("AtomIteratorNbrCellIntra.allAtoms not implemented");
/*        if(basis == null) return;
        last = basis.node.lastChildAtom();
        for(Atom atom = basis.node.firstChildAtom(); atom != null; atom=atom.nextAtom()) {
            act.actionPerformed(atom);
            if(atom == last) break;
        }*/
    }
        
    /**
     * Sets the given atom as the basis, so that child atoms of the
     * given atom will be returned upon iteration.  If given atom is
     * a leaf atom, a class-cast exception will be thrown.
     */
    public void setBasis(Atom atom) {
        setBasis((AtomTreeNodeGroup)atom.node);
    }
    
    //may be room for efficiency here
    public void setBasis(AtomTreeNodeGroup node) {
        basis = node;
        //can base decision whether to iterate over cells on type of sequencer
        //for given atom, because it is in the group of atoms being iterated
        iterateCells = basis.childSequencerClass().equals(NbrSequencer.class);
        BravaisLattice lattice = iteratorFactory.getLattice(basis.parentPhase());
        listIterator.setBasis(node.childList);
        if(iterateCells) {
            HashMap hash = (HashMap)lattice.agents[0];
            tabIndex = ((Integer)hash.get(node)).intValue();
        }
    }
    
    /**
     * Returns the current iteration basis.
     */
    public Atom getBasis() {return basis.atom();}
    
    /**
     * The number of atoms returned on a full iteration, using the current basis.
     */
    public int size() {return (basis != null) ? basis.childAtomCount() : 0;}   

    private AtomTreeNodeGroup basis;
    private Atom next;
    private Atom referenceAtom, nextAtom;
    private boolean upListNow, doGoDown;
    private IteratorDirective.Direction direction, currentDirection;
    private AbstractCell referenceCell;
    private boolean iterateCells;
    private int tabIndex;
    private BravaisLattice lattice;
    private final SiteIteratorNeighbor cellIterator = new SiteIteratorNeighbor();
    private final AtomIteratorList listIterator = new AtomIteratorList();
    private final IteratorFactoryCell iteratorFactory;

}//end of IntragroupIterator class

/////////////////////////////////////////////////////////////////////////////////////////////

public interface CellSequencer {
    
    /**
     * Method called to notify sequencer that the phase has a new cell lattice.
     */
    public void latticeChangeNotify();
    
}//end of CellSequencer interface
   
/////////////////////////////////////////////////////////////////////////////////////////////

public static final class SimpleSequencer extends AtomSequencer implements CellSequencer {
    
    public SimpleSequencer(Atom a) {super(a);}
    
    /**
     * CellSequencer interface method; performs no action except to pass event
     * on to sequencers of child atoms.
     */
    public void latticeChangeNotify() {
        if(atom.node.isLeaf()) return;
        else {
            AtomIteratorListSimple iterator = new AtomIteratorListSimple(((AtomTreeNodeGroup)atom.node).childList);
            while(iterator.hasNext()) ((CellSequencer)atom.seq).latticeChangeNotify();
        }
    }

    /**
     * Performs no action.
     */
    public void moveNotify() {}
    
    /**
     * Performs no action.
     */
    public void setParentNotify(AtomTreeNodeGroup newParent) {}
    
    /**
     * Returns true if this atom preceeds the given atom in the atom sequence.
     * Returns false if the given atom is this atom, or (of course) if the
     * given atom instead preceeds this one.
     */
    public boolean preceeds(Atom a) {
        throw new RuntimeException("IteratorFactoryCell.Sequencer.preceeds method should be checked for correctness before using");
        //want to return false if atoms are the same atoms
   /*     if(a == null) return true;
        if(atom.node.parentGroup() == a.node.parentGroup()) return atom.node.index() < a.node.index();//works also if both parentGroups are null
        int thisDepth = atom.node.depth();
        int atomDepth = a.node.depth();
        if(thisDepth == atomDepth) return atom.node.parentGroup().seq.preceeds(a.node.parentGroup());
        else if(thisDepth < atomDepth) return this.preceeds(a.node.parentGroup());
        else /*if(this.depth > atom.depth)* / return atom.node.parentGroup().seq.preceeds(a);
        */
    }
    
    public static final AtomSequencer.Factory FACTORY = new AtomSequencer.Factory() {
        public AtomSequencer makeSequencer(Atom atom) {
            return new SimpleSequencer(atom);
        }
    };
}

/////////////////////////////////////////////////////////////////////////////////////////////

public static final class NbrSequencer extends AtomSequencer implements CellSequencer {
    
    public AbstractCell cell;         //cell currently occupied by this coordinate
    public BravaisLattice lattice;    //cell lattice in the phase occupied by this coordinate
    private int listIndex;
    public final AtomLinker nbrLink;  //linker used to arrange atom in sequence according to cells
    
    public NbrSequencer(Atom a) {
        super(a);
        nbrLink = new AtomLinker(a);
    }
    
    //CellSequencer interface method
    public void latticeChangeNotify() {
        this.assignCell();
        if(atom.node.isLeaf()) return;
        else {
            AtomIteratorListSimple iterator = new AtomIteratorListSimple(((AtomTreeNodeGroup)atom.node).childList);
            while(iterator.hasNext()) ((CellSequencer)atom.seq).latticeChangeNotify();
        }
    }

    public Site site() {return cell;}   //Lattice.Occupant interface method

    public int listIndex() {return listIndex;}

    public void remove() {
        super.remove();
        nbrLink.remove();
    }
        
    public void addBefore(AtomLinker newNext) {
        //newNext will never be one of the cell tabs
        super.addBefore(newNext);
        if(lattice != null) assignCell();
        else nbrLink.remove();
        
    /*    while(newNext.atom == null && newNext != this) newNext = newNext.next;
        if(newNext == this) {//this is the first and only non-tab entry in the list
            nextFixed = previousFixed = this;
            return;
        }
        nextFixed = (Sequencer)newNext;
        previousFixed = nextFixed.previousFixed;
        previousFixed.nextFixed = this;
        nextFixed.previousFixed = this;*/
	}
	/**
	 * Reshuffles position of "neighbor" links without altering the regular links.
	 */
	public void moveBefore(AtomLinker newNext) {
	    nbrLink.moveBefore(newNext);
	}


    /**
     * Returns true if this atom preceeds the given atom in the atom sequence.
     * Returns false if the given atom is this atom, or (of course) if the
     * given atom instead preceeds this one.
     */
     //this method needs to be fixed
    public boolean preceeds(Atom a) {
        throw new RuntimeException("IteratorFactoryCell.CellSequencer.preceeds method not yet implemented");
        //want to return false if atoms are the same atoms
      /*  if(a == null) return true;
        if(atom.node.parentGroup() == a.node.parentGroup()) {
            if(((Sequencer)atom.seq).site().equals(cell)) {
                //this isn't correct
                return atom.node.index() < a.node.index();//works also if both parentGroups are null
            }
            else return ((Sequencer)atom.seq).site().preceeds(cell);
        }
        int thisDepth = atom.node.depth();
        int atomDepth = a.node.depth();
        if(thisDepth == atomDepth) return atom.node.parentGroup().seq.preceeds(a.node.parentGroup());
        else if(thisDepth < atomDepth) return this.preceeds(a.node.parentGroup());
        else /*if(this.depth > atom.depth)* / return atom.node.parentGroup().seq.preceeds(a);
        */
    }
    
    /**
     * Method called when a translate method of coordinate is invoked.
     */
    public void moveNotify() {
        if(!cell.inCell(atom.coord.position())) assignCell();
    }
    
    /**
     * Method called when the parent of the atom is changed.
     * By the time this method is called, the atom has been placed
     * in the childList of the given parent (if it is not null).
     */
    public void setParentNotify(AtomTreeNodeGroup newParent) {
        if(newParent == null) {
            cell = null;
            lattice = null;
            return;
        }
        //get cell lattice for the phase containing the parent
        lattice = ((IteratorFactoryCell)newParent.parentSimulation().iteratorFactory).getLattice(newParent.parentPhase());
        //determine the index used by the cells for their tabs in the parent's childList
        HashMap hash = (HashMap)lattice.agents[0];
        Integer index = (Integer)hash.get(newParent);
        if(index == null) {//parent's childList isn't yet represented by cells
            index = new Integer(hash.size());
            hash.put(newParent, index);
            setupListTabs(lattice/*, newParent.childList*/);
        }
        listIndex = index.intValue();
        assignCell();
    }

//Determines appropriate cell and assigns it
    public void assignCell() {
        int[] latticeIndex = lattice.getPrimitive().latticeIndex(atom.coord.position(), lattice.getDimensions());
        AbstractCell newCell = (AbstractCell)lattice.site(latticeIndex);
        if(newCell != cell) {assignCell(newCell);}
    }
//Assigns atom to given cell
    public void assignCell(AbstractCell newCell) {
        cell = newCell;
        if(cell == null) return;
        this.moveBefore(((AtomLinker.Tab[])newCell.agents[0])[listIndex].nextTab);
    }//end of assignCell
    
    public static final AtomSequencer.Factory FACTORY = new AtomSequencer.Factory() {
        public AtomSequencer makeSequencer(Atom atom) {return new NbrSequencer(atom);}
    };
}//end of Sequencer

/////////////////////////////////////////////////////////////////////////////////////////////

/**
 * A factory that makes Sites of type AtomCell
 */
/*private static final class AtomCellFactory extends AtomFactory {
    public AtomCellFactory(Space space) {
        super(space);
    }
    public Atom build() {
        return new AtomCell();
    }
}//end of AtomCellFactory
    
/////////////////////////////////////////////////////////////////////////////////////////////

/**
 * Wraps a lattice cell and holds a reference to a sequence of atoms.
 */
/*private static final class AtomCell extends AbstractCell {
    public final AbstractCell cell;
    private AtomLinker.Tab[] firstTab, lastTab;
    public AtomCell() {
//        this.cell = cell;
//        color = Constants.RandomColor();
//            position = (Space2D.Vector)coord.position();
    }
    public AtomLinker.Tab first(int speciesIndex) {return firstTab[speciesIndex];}
    public AtomLinker.Tab last(int speciesIndex) {return lastTab[speciesIndex];}
}//end of AtomCell
*/

/////////////////////////////////////////////////////////////////////////////////////////////

/**
 * Iterates through the cells of the given lattice, and adds a tab to the 
 * given list for each cell, and extends the tablist in each cell to reference
 * its new tab.
 */
private static void setupListTabs(BravaisLattice lattice/*, AtomList list*/) {
    AtomIteratorList iterator = new AtomIteratorList(lattice.siteList());
    iterator.reset();
    AtomLinker.Tab[] headerList = null;
    while(iterator.hasNext()) {//loop over cells
        Site site = (Site)iterator.next();
        if(site.agents == null || site.agents[0] == null) {
            site.agents = new Object[1];
            site.agents[0] = new AtomLinker.Tab[0];
        }
        AtomLinker.Tab[] tabList = (AtomLinker.Tab[])site.agents[0];
        AtomLinker.Tab[] newTabList = new AtomLinker.Tab[tabList.length+1];
        for(int i=0; i<tabList.length; i++) newTabList[i] = tabList[i];
        AtomLinker.Tab newTab = new AtomLinker.Tab();
        newTabList[tabList.length] = newTab;
        if(headerList == null) headerList = newTabList;
        else newTab.addBefore(headerList[tabList.length]);
 //       list.add(newTab);
        site.agents[0] = newTabList;
    }
}//end of setupListTabs

    /**
     * Demonstrates how this class is implemented.
     */
    public static void main(String[] args) {
        Default.ATOM_SIZE = 1.0;
        etomica.graphics.SimulationGraphic sim = new etomica.graphics.SimulationGraphic(new Space2D());
        Simulation.instance = sim;

        sim.setIteratorFactory(new IteratorFactoryCell(sim));
        
	    //IntegratorHard integrator = new IntegratorHard();
        //integrator.setTimeStep(0.01);
        IntegratorMC integrator = new IntegratorMC();
        MCMoveAtom mcMoveAtom = new MCMoveAtom(integrator);
        MCMoveVolume mcMoveVolume = new MCMoveVolume(integrator);
        
	    SpeciesSpheresMono speciesSpheres = new SpeciesSpheresMono();
	    speciesSpheres.setNMolecules(300);
	    
	    Phase phase = new Phase();
	    
	    Potential2 potential = new P2HardSphere();
	    Controller controller = new Controller();
	    etomica.graphics.DisplayPhase displayPhase = new etomica.graphics.DisplayPhase();
	    integrator.setSleepPeriod(1);
	    
        //this method call invokes the mediator to tie together all the assembled components.
		Simulation.instance.elementCoordinator.go();
		                                    
	    BravaisLattice lattice = ((IteratorFactoryCell)sim.getIteratorFactory()).getLattice(phase);
	    etomica.graphics.LatticeRenderer latticeRenderer = 
	            new etomica.graphics.LatticeRenderer(lattice);
	    displayPhase.addDrawable(latticeRenderer);
        
        etomica.graphics.SimulationGraphic.makeAndDisplayFrame(sim);
        
     //   controller.start();
    }//end of main
    
    
   
}//end of IteratorFactoryCell