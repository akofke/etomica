package etomica.atom;

import java.lang.reflect.Array;

import etomica.atom.iterator.AtomIteratorTree;
import etomica.phase.Phase;
import etomica.phase.PhaseEvent;
import etomica.phase.PhaseListener;
import etomica.util.Arrays;

/**
 * AtomAgentManager acts on behalf of client classes (an AgentSource) to manage 
 * agents in every Atom in a phase.  When atoms are added or removed from the 
 * phase, the agents array (indexed by the atom's global index) is updated.  
 * The client should call getAgents() at any point where an atom might have 
 * have been added to the system because the old array would be stale at that
 * point. 
 * @author andrew
 */
public class AtomAgentManager implements PhaseListener, java.io.Serializable {

    public AtomAgentManager(AgentSource source, Phase phase) {
        this(source, phase, true);
    }
    
    public AtomAgentManager(AgentSource source, Phase phase, boolean isBackend) {
        agentSource = source;
        this.isBackend = isBackend;
        setPhase(phase);
    }        
    
    /**
     * Returns the array of Atom agents for the Phase, indexed by the Atom's
     * global index.  The array is of the type returned by the AgentSource's 
     * getAgentClass method.
     */
    public Object[] getAgents() {
        return agents;
    }
    
    /**
     * Convenience method to return the agent the given Atom.  The Atom must 
     * be from the Phase associated with this instance.  For repeated access to
     * the agents from multiple Atoms, it might be faster to use the above 
     * getAgents method.
     */
    public Object getAgent(Atom a) {
        return agents[a.getGlobalIndex()];
    }
    
    /**
     * Sets the Phase in which this AtomAgentManager will manage Atom agents.
     * Setting the Phase to null will notify the AtomAgentManager it should 
     * disconnect itself as a listener.
     */
    public void setPhase(Phase newPhase) {
        if (phase != null) {
            // remove ourselves as a listener to the old phase
            phase.getEventManager().removeListener(this);
            AtomIteratorTree iterator = new AtomIteratorTree(phase.getSpeciesMaster(),Integer.MAX_VALUE,true);
            iterator.reset();
            while (iterator.hasNext()) {
                Atom atom = iterator.nextAtom();
                // check if atom's spot in the array even exists yet
                if (atom.getGlobalIndex() < agents.length) {
                    Object agent = agents[atom.getGlobalIndex()];
                    if (agent != null) {
                        agentSource.releaseAgent(agent,atom);
                    }
                }
            }
        }
        phase = newPhase;
        if (phase == null) {
            agents = null;
            return;
        }
        phase.getEventManager().addListener(this, isBackend);
        SpeciesMaster speciesMaster = phase.getSpeciesMaster();
        
        agents = (Object[])Array.newInstance(agentSource.getAgentClass(),
                speciesMaster.getMaxGlobalIndex()+1+speciesMaster.getIndexReservoirSize());
        // fill in the array with agents from all the atoms
        AtomIteratorTree iterator = new AtomIteratorTree(speciesMaster,Integer.MAX_VALUE,true);
        iterator.reset();
        while (iterator.hasNext()) {
            addAgent(iterator.nextAtom());
        }
    }
    
    public void actionPerformed(PhaseEvent evt) {
        if (evt.type() == PhaseEvent.ATOM_ADDED) {
            Atom a = evt.atom();
            if (a.type.isLeaf()) {
                addAgent(a);
            }
            else {
                if (treeIterator == null) {
                    treeIterator = new AtomIteratorTree(Integer.MAX_VALUE);
                    treeIterator.setDoAllNodes(true);
                }
                // add all atoms below this atom
                treeIterator.setRoot(a);
                treeIterator.reset();
                while (treeIterator.hasNext()) {
                    addAgent(treeIterator.nextAtom());
                }
            }       
        }
        else if (evt.type() == PhaseEvent.ATOM_REMOVED) {
            Atom a = evt.atom();
            if (a.type.isLeaf()) {
                int index = a.getGlobalIndex();
                if (agents[index] != null) {
                    // Atom used to have an agent.  nuke it.
                    agentSource.releaseAgent(agents[index], a);
                    agents[index] = null;
                }
            }
            else {
                if (treeIterator == null) {
                    treeIterator = new AtomIteratorTree(Integer.MAX_VALUE);
                    treeIterator.setDoAllNodes(true);
                }
                // nuke all atoms below this atom
                treeIterator.setRoot(a);
                treeIterator.reset();
                while (treeIterator.hasNext()) {
                    Atom childAtom = treeIterator.nextAtom();
                    int index = childAtom.getGlobalIndex();
                    if (agents[index] != null) {
                        // Atom used to have an agent.  nuke it.
                        agentSource.releaseAgent(agents[index], childAtom);
                        agents[index] = null;
                    }
                }
            }
        }
        else if (evt.type() == PhaseEvent.ATOM_CHANGE_INDEX) {
            // the atom's index changed.  assume it would get the same agent
            agents[evt.atom().getGlobalIndex()] = agents[evt.getIndex()];
            agents[evt.getIndex()] = null;
        }
        else if (evt.type() == PhaseEvent.GLOBAL_INDEX) {
            SpeciesMaster speciesMaster = (SpeciesMaster)evt.getSource();
            int reservoirSize = speciesMaster.getIndexReservoirSize();
            if (agents.length > evt.getIndex()+reservoirSize || agents.length < evt.getIndex()) {
                // indices got compacted.  If our array is a lot bigger than it
                // needs to be, shrink it.
                // ... or we've been notified that atoms are about to get added to the 
                // system.  Make room for them
                agents = Arrays.resizeArray(agents,evt.getIndex()+1+reservoirSize);
            }
        }
    }
    
    protected void addAgent(Atom a) {
        if (agents.length < a.getGlobalIndex()+1) {
            // no room in the array.  reallocate the array with an extra cushion.
            agents = Arrays.resizeArray(agents,a.getGlobalIndex()+1+phase.getSpeciesMaster().getIndexReservoirSize());
        }
        agents[a.getGlobalIndex()] = agentSource.makeAgent(a);
    }
    
    /**
     * Interface for an object that wants an agent associated with each Atom in
     * a Phase.
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
        public Object makeAgent(Atom a);
        
        /**
         * This informs the agent source that the agent is going away and that 
         * the agent source should disconnect the agent from other elements
         */
        public void releaseAgent(Object agent, Atom atom);
    }

    private static final long serialVersionUID = 1L;
    private final AgentSource agentSource;
    protected Object[] agents;
    private AtomIteratorTree treeIterator;
    private Phase phase;
    private final boolean isBackend;
}
