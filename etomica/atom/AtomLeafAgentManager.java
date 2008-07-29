package etomica.atom;

import java.io.Serializable;
import java.lang.reflect.Array;

import etomica.api.IAtom;
import etomica.api.IAtomLeaf;
import etomica.api.IAtomSet;
import etomica.api.IBox;
import etomica.api.IMolecule;
import etomica.box.BoxAtomAddedEvent;
import etomica.box.BoxAtomEvent;
import etomica.box.BoxAtomLeafIndexChangedEvent;
import etomica.box.BoxAtomRemovedEvent;
import etomica.box.BoxEvent;
import etomica.box.BoxGlobalAtomLeafIndexEvent;
import etomica.box.BoxListener;
import etomica.util.Arrays;

/**
 * AtomAgentManager acts on behalf of client classes (an AgentSource) to
 * manage agents for every leaf Atom in a box.  When leaf atoms are added or
 * removed from the box, the agents array (indexed by the atom's global
 * index) is updated.  The client can access and modify the agents via getAgent
 * and setAgent.
 * 
 * @author Andrew Schultz
 */
public class AtomLeafAgentManager implements BoxListener, Serializable {

    public AtomLeafAgentManager(AgentSource source, IBox box) {
        this(source, box, true);
    }
    
    public AtomLeafAgentManager(AgentSource source, IBox box, boolean isBackend) {
        agentSource = source;
        this.isBackend = isBackend;
        this.box = box;
        setReservoirSize(30);
        setupBox();
    }
    
    /**
     * Sets the size of the manager's "reservoir".  When an atom is removed,
     * the agents array will only be trimmed if the number of holes in the
     * array exceeds the reservoir size.  Also, when the array has no holes and
     * another atom is added, the array will resized to be 
     * numAtoms+reservoirSize to avoid reallocating a new array every time an
     * atom is added.  reservoirSize=0 means the array will
     * always be the same size as the number of atoms (no holes).
     * 
     * The default reservoir size is 30.
     */
    public void setReservoirSize(int newReservoirSize) {
        reservoirSize = newReservoirSize;
    }

    public int getReservoirSize() {
        return reservoirSize;
    }

    /**
     * Returns an iterator that returns each non-null agent
     */
    public AgentIterator makeIterator() {
        return new AgentIterator(this);
    }
    
    /**
     * Returns the agent associated with the given IAtom.  The IAtom must be
     * from the Box associated with this instance.
     */
    public Object getAgent(IAtomLeaf a) {
        int idx = box.getLeafIndex(a);
        if (idx < agents.length) {
            return agents[idx];
        }
        return null;
    }
    
    /**
     * Sets the agent associated with the given atom to be the given agent.
     * The IAtom must be from the Box associated with this instance.  The
     * IAtom's old agent is not released.  This should be done manually if
     * needed.
     */
    public void setAgent(IAtomLeaf a, Object newAgent) {
        int idx = box.getLeafIndex(a);
        if (idx >= agents.length) {
            // no room in the array.  reallocate the array with an extra cushion.
            agents = Arrays.resizeArray(agents,idx+1+reservoirSize);
        }
        agents[box.getLeafIndex(a)] = newAgent;
    }
    
    /**
     * Convenience method to return the box the Manager is tracking.
     */
    public IBox getBox(){
        return box;
    }
    
    /**
     * Notifies the AtomAgentManager it should disconnect itself as a listener.
     */
    public void dispose() {
        // remove ourselves as a listener to the box
        box.getEventManager().removeListener(this);
        IAtomSet leafList = box.getLeafList();
        int nLeaf = leafList.getAtomCount();
        for (int i=0; i<nLeaf; i++) {
            // leaf index corresponds to the position in the leaf list
            Object agent = agents[i];
            if (agent != null) {
                agentSource.releaseAgent(agent, (IAtomLeaf)leafList.getAtom(i));
            }
        }
        agents = null;
    }
    
    /**
     * Sets the Box in which this AtomAgentManager will manage Atom agents.
     */
    protected void setupBox() {
        box.getEventManager().addListener(this, isBackend);
        
        agents = (Object[])Array.newInstance(agentSource.getAgentClass(),
                box.getLeafList().getAtomCount()+1+reservoirSize);
        // fill in the array with agents from all the atoms
        IAtomSet leafList = box.getLeafList();
        int nLeaf = leafList.getAtomCount();
        for (int i=0; i<nLeaf; i++) {
            // leaf list position is the leaf index, so don't bother looking
            // that up again.
           addAgent((IAtomLeaf)leafList.getAtom(i), i);
        }
    }
    
    public void actionPerformed(BoxEvent evt) {
        if (evt instanceof BoxAtomEvent) {
            IAtom a = ((BoxAtomEvent)evt).getAtom();
            if (evt instanceof BoxAtomAddedEvent) {
                if (a instanceof IMolecule) {
                    // add all leaf atoms below this atom
                    IAtomSet childList = ((IMolecule)a).getChildList();
                    for (int iChild = 0; iChild < childList.getAtomCount(); iChild++) {
                        addAgent((IAtomLeaf)childList.getAtom(iChild));
                    }
                }
                else {
                    // the atom itself is a leaf
                    addAgent((IAtomLeaf)a);
                }
            }
            else if (evt instanceof BoxAtomRemovedEvent) {
                if (a instanceof IMolecule) {
                    // IAtomGroups don't have agents, but nuke all atoms below this atom
                    IAtomSet childList = ((IMolecule)a).getChildList();
                    for (int iChild = 0; iChild < childList.getAtomCount(); iChild++) {
                        IAtomLeaf childAtom = (IAtomLeaf)childList.getAtom(iChild);
                        int index = box.getLeafIndex(childAtom);
                        if (agents[index] != null) {
                            // Atom used to have an agent.  nuke it.
                            agentSource.releaseAgent(agents[index], childAtom);
                            agents[index] = null;
                        }
                    }
                }
                else {
                    int index = box.getLeafIndex((IAtomLeaf)a);
                    if (agents[index] != null) {
                        // Atom used to have an agent.  nuke it.
                        agentSource.releaseAgent(agents[index], (IAtomLeaf)a);
                        agents[index] = null;
                    }
                }
            }
            else if (evt instanceof BoxAtomLeafIndexChangedEvent) {
                // the atom's index changed.  assume it would get the same agent
                int oldIndex = ((BoxAtomLeafIndexChangedEvent)evt).getOldIndex();
                agents[box.getLeafIndex((IAtomLeaf)a)] = agents[oldIndex];
                agents[oldIndex] = null;
            }
        }
        else if (evt instanceof BoxGlobalAtomLeafIndexEvent) {
            // don't use leafList.size() since the SpeciesMaster might be notifying
            // us that it's about to add leaf atoms
            int newMaxIndex = ((BoxGlobalAtomLeafIndexEvent)evt).getMaxIndex();
            if (agents.length > newMaxIndex+reservoirSize || agents.length < newMaxIndex) {
                // indices got compacted.  If our array is a lot bigger than it
                // needs to be, shrink it.
                // ... or we've been notified that atoms are about to get added to the 
                // system.  Make room for them
                agents = Arrays.resizeArray(agents,newMaxIndex+1+reservoirSize);
            }
        }
    }
    
    /**
     * Adds an agent for the given leaf atom to the agents array.
     */
    protected void addAgent(IAtomLeaf a) {
        addAgent(a, box.getLeafIndex(a));
    }
    
    /**
     * Adds an agent for the given leaf atom to the agents array at the given
     * index.
     */
    protected void addAgent(IAtomLeaf a, int index) {
        if (agents.length < index+1) {
            // no room in the array.  reallocate the array with an extra cushion.
            agents = Arrays.resizeArray(agents,index+1+reservoirSize);
        }
        agents[index] = agentSource.makeAgent(a);
    }        
    
    /**
     * Interface for an object that wants an agent associated with each Atom in
     * a Box.
     */
    public interface AgentSource {
        /**
         * Returns the Class of the agent.  This is used to create an array of 
         * the appropriate Class.
         */
        public Class getAgentClass();

        /**
         * Returns an agent for the given Atom.
         */
        public Object makeAgent(IAtomLeaf a);
        
        /**
         * This informs the agent source that the agent is going away and that 
         * the agent source should disconnect the agent from other elements
         */
        public void releaseAgent(Object agent, IAtomLeaf atom);
    }

    private static final long serialVersionUID = 1L;
    protected final AgentSource agentSource;
    protected Object[] agents;
    protected final IBox box;
    protected final boolean isBackend;
    protected int reservoirSize;
    
    /**
     * Iterator that loops over the agents, skipping null elements
     */
    public static class AgentIterator implements Serializable {

        protected AgentIterator(AtomLeafAgentManager agentManager) {
            this.agentManager = agentManager;
        }
        
        public void reset() {
            cursor = 0;
            agents = agentManager.agents;
        }
        
        public boolean hasNext() {
            while (cursor < agents.length) {
                if (agents[cursor] != null) {
                    return true;
                }
                cursor++;
            }
            return false;
        }
        
        public Object next() {
            cursor++;
            while (cursor-1 < agents.length) {
                if (agents[cursor-1] != null) {
                    return agents[cursor-1];
                }
                cursor++;
            }
            return null;
        }
        
        private static final long serialVersionUID = 1L;
        private final AtomLeafAgentManager agentManager;
        private int cursor;
        private Object[] agents;
    }
    
}
