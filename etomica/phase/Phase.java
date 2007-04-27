package etomica.phase;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import etomica.EtomicaElement;
import etomica.action.PhaseInflate;
import etomica.atom.AtomArrayList;
import etomica.atom.AtomLeaf;
import etomica.atom.IAtom;
import etomica.atom.IAtomGroup;
import etomica.atom.SpeciesAgent;
import etomica.atom.SpeciesMaster;
import etomica.simulation.Simulation;
import etomica.space.Boundary;
import etomica.space.BoundaryRectangularPeriodic;
import etomica.space.IVector;
import etomica.space.Space;
import etomica.species.Species;
import etomica.species.SpeciesResolver;
import etomica.species.SpeciesSignature;
import etomica.units.Dimension;
import etomica.units.DimensionRatio;
import etomica.units.Quantity;
import etomica.units.Volume;

/**
 * A Phase collects all atoms that interact with one another; atoms in different
 * phases do not interact. These are the important features of a Phase:
 * <p>
 * <ol>
 * <li>It holds a SpeciesMaster instance, which provides the root of a
 * hierarchy of atoms that represent the physical objects that interact.
 * <li>It holds a Boundary object, obtained from the governing Space, that
 * defines the volume of the phase and the behavior of atoms as they move into or
 * across the boundary of the phase.
 * <li>It maintains a list of listeners that are informed when significant
 * events happen in the phase (such as a change in its boundary).
 * <li>Each Phase has a unique index assigned when it is constructed.
 * The index assignment begins at 0 and is incremented after each Phase
 * construction. This index is useful when collecting things in reference to the
 * phase.
 * </ol>
 * A phase is acted upon by an Integrator instance to move its atoms around and
 * generate configurations. Properties of a phase are measured by MeterAbstract
 * instances which are simply DataSource objects that require a phase to
 * generate their data. <br>
 * A simulation may involve more than one phase. All Phase instances are
 * registered with the simulation specified upon their construction, and
 * may be accessed via the simulation's getPhases method.
 * 
 * @author David Kofke, Andrew Schultz
 * @see Boundary
 */
public class Phase implements EtomicaElement, java.io.Serializable {
        
    /**
     * Constructs phase with default rectangular periodic boundary.
     */
    public Phase(Simulation sim) {
        this.sim = sim;
        space = sim.getSpace();
        eventManager = new PhaseEventManager();
        speciesMaster = new SpeciesMaster(this, eventManager);
        setBoundary(new BoundaryRectangularPeriodic(sim));
        sim.addPhase(this);
        setName(null);
        
        inflateEvent = new PhaseInflateEvent(this);
    }
    
    /**
     * Returns the Simulation holding this Phase.  No, you shouldn't be calling
     * this method.  It's here for SpeciesAgent.getNMolecules, which shouldn't
     * be calling this method either!
     */
    public Simulation getSimulation() {
        return sim;
    }
    
    /**
     * Resets the Phase's index.  This should only need to be called from the
     * Simulation when another Phase is removed.
     */
    public void resetIndex() {
        Phase[] phases = sim.getPhases();
        for (int i=0; i<phases.length; i++) {
            if (phases[i] == this) {
                index = i;
                return;
            }
        }
        index = 0;
    }

    public int getIndex() {
        return index;
    }
    
    /**
     * Accessor method of the name of this phase
     * 
     * @return The given name of this phase
     */
    public final String getName() {
        if (name == null) {
            return "Phase"+getIndex();
        }
        return name;
    }
    
    /**
     * Method to set the name of this simulation element. The element's name
     * provides a convenient way to label output data that is associated with
     * it.  This method might be used, for example, to place a heading on a
     * column of data. Default name is the base class followed by the integer
     * index of this element.
     * 
     * @param name The name string to be associated with this element
     */
    public void setName(String name) {this.name = name;}

    /**
     * Overrides the Object class toString method to have it return the output of getName
     * 
     * @return The name given to the phase
     */
    public String toString() {return getName();}
    
    /**
     * Mutator method for flag that enables or disables application of long-range
     * correction to truncated potentials.  Enabled by default.
     */
    public void setLrcEnabled(boolean b) {lrcEnabled = b;}
    /**
     * Accessor method for flag that enables or disables application of long-range
     * correction to truncated potentials.  Enabled by default.
     */
    public boolean isLrcEnabled() {return lrcEnabled;}
    
    public final Space getSpace() {return space;}
    
    /**
     * Returns the ith molecule in the linked list of molecules.
     * 0 returns the first molecule, and moleculeCount-1 returns the last.
     * An argument outside this range throws an IndexOutOfBoundsException
     */
    //could make more efficient by starting from first or last molecule, as appropriate
    public IAtom molecule(int i) {
        if(i >= moleculeCount() || i < 0) 
            throw new IndexOutOfBoundsException("Index: "+i+
                                                ", Number of molecules: "+moleculeCount());
        AtomArrayList agentList = speciesMaster.getAgentList();
        for (int agentIndex=0; agentIndex<agentList.size(); agentIndex++) {
            AtomArrayList moleculeList = ((IAtomGroup)agentList.get(agentIndex)).getChildList();
            int count = moleculeList.size();
            if (i < count) {
                return moleculeList.get(i);
            }
            i -= count;
        }
        throw new IllegalStateException("how can this be?!?!?!");
    }
    
    /**
     * Finds and returns the atom nearest to each of one or more given
     * positions, using the boundary associated with this phase.
     * @param r a positions in the phase.  Point may be outside the phase, as
     * minimum-image separations are used in accordance with phase's boundary.
     * @return Atom the (leaf) atoms in the phase nearest to each position. A
     * given atom can appear more than once in this list, if it is nearest to
     * more than one of the positions.
     */
    public IAtom[] nearestAtom(IVector[] r) {
        AtomArrayList leafList = speciesMaster.getLeafList();
        int nLeaf = leafList.size();
    	IAtom[] nearest = new IAtom[r.length];
    	for(int i=0; i<r.length; i++) {
	    	double r2Min = Double.MAX_VALUE;
            for (int iLeaf=0; iLeaf<nLeaf; iLeaf++) {
                AtomLeaf a = (AtomLeaf)leafList.get(iLeaf);
	    		double r2 = Space.r2(a.getPosition(), r[i], boundary);
	    		if(r2 < r2Min) {
	    			r2Min = r2;
	    			nearest[i] = a;
	    		}
	    	}
    	}
    	return nearest;
    }
    
    /**
     * Sets the boundary object of the phase.
     */
     public void setBoundary(Boundary b) {
        boundary = b;
     }
     
    /**
     * Returns the current boundary instance.
     * 
     * @return The current instance of the boundary class
     */
    public final Boundary getBoundary() {return boundary;}
    
    public final void setDimensions(IVector d) {
        boundary.setDimensions(d);
        eventManager.fireEvent(inflateEvent);
    }
    
    /**
     * Returns the agent of the given species in this phase.
     */
    public final SpeciesAgent getAgent(Species s) {
        //brute force it
        AtomArrayList agentList = speciesMaster.getAgentList();
        for (int i=0; i<agentList.size(); i++) {
            if (((SpeciesAgent)agentList.get(i)).getType().getSpecies() == s) {
                return (SpeciesAgent)agentList.get(i);
            }
        }
        return null;
    }

    public final double volume() {return boundary.volume();}  //infinite volume unless using PBC
    
    public void setDensity(double rho) {
        double vNew = moleculeCount()/rho;
        double scale = Math.pow(vNew/boundary.volume(), 1.0/space.D());
        PhaseInflate inflater = new PhaseInflate(this);
        inflater.setScale(scale);
        inflater.actionPerformed();
    }
    
    public double getDensity() {return moleculeCount()/boundary.volume();}
    
    public Dimension getDensityDimension() {
        return new DimensionRatio("Density",Quantity.DIMENSION,Volume.DIMENSION);
    }

    /**
     * returns the number of molecules in the phase
     */
    public int moleculeCount() {return speciesMaster.moleculeCount();}
    
    /**
     * returns the number of leaf atoms in the phase
     */
    public int atomCount() {return speciesMaster.getLeafList().size();}

    /**
     * @return Returns the speciesMaster.
     */
    public SpeciesMaster getSpeciesMaster() {
        return speciesMaster;
    }

    public void writePhase(ObjectOutputStream out) throws IOException {
        out.writeObject(boundary);
        AtomArrayList agents = speciesMaster.getAgentList();
        out.writeInt(agents.size());
        for (int i=0; i<agents.size(); i++) {
            Species species = agents.get(i).getType().getSpecies();
            out.writeObject(species.getSpeciesSignature());
            out.writeInt(((SpeciesAgent)agents.get(i)).getNMolecules());
        }
        AtomArrayList leafList = speciesMaster.getLeafList();
        int nLeaf = leafList.size();
        for (int iLeaf=0; iLeaf<nLeaf; iLeaf++) {
            AtomLeaf a = (AtomLeaf)leafList.get(iLeaf);
            out.writeObject(a.getPosition());
        }
    }
    
    public static Phase readPhase(ObjectInputStream in, Simulation sim, SpeciesResolver resolver) throws IOException, ClassNotFoundException {
        Boundary newBoundary = (Boundary)in.readObject();
        Phase newPhase = new Phase(sim);
        newPhase.setBoundary(newBoundary);
        
        Species[] mySpecies = sim.getSpeciesManager().getSpecies();
        int numSpecies = in.readInt();
        for (int i = 0; i<numSpecies; i++) {
            SpeciesSignature speciesSignature = (SpeciesSignature)in.readObject();
            Species newSpecies = null;
            Species[] candidates = new Species[0];
            for (int j=0; j<mySpecies.length; j++) {
                Species candidate = mySpecies[j];
                if (speciesSignature.equals(candidate.getSpeciesSignature())) {
                    candidates = (Species[])etomica.util.Arrays.addObject(candidates,candidate);
                }
            }
            if (candidates.length > 0) {
                newSpecies = resolver.whichOneDoYouLike(candidates,speciesSignature.name);
            }
            if (newSpecies == null) {
                Constructor constructor = speciesSignature.constructor;
                Object[] parameters = new Object[speciesSignature.parameters.length+1];
                parameters[0] = sim;
                System.arraycopy(speciesSignature.parameters,0,parameters,1,parameters.length-1);
                try {
                    newSpecies = (Species)constructor.newInstance(parameters);
                }
                catch (IllegalAccessException e) {}
                catch (InstantiationException e) {}
                catch (InvocationTargetException e) {}
            }
            int nMolecules = in.readInt();
            newPhase.getAgent(newSpecies).setNMolecules(nMolecules);
        }

            //XXX broken
        // loop over the atoms
//            ((AtomLeaf)iterator.nextAtom()).getCoord().E((ICoordinate)in.readObject());
        return newPhase;
    }
    
    public PhaseEventManager getEventManager() {
        return eventManager;
    }

    private static final long serialVersionUID = 1L;
    private Boundary boundary;
    private SpeciesMaster speciesMaster;
    private boolean lrcEnabled = true;
    private String name;
    protected final Space space;
    private final PhaseEventManager eventManager;
    private final PhaseEvent inflateEvent;
    private Simulation sim;
    private int index;
}
        
