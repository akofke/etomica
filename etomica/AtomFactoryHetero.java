package etomica;

/**
 * Builds an atom group that comprises a set of differently formed atoms or atomgroups.
 * Each child atom is constructed by a different atom factory, which are set as an
 * array of atomfactories given in the constructor.
 *
 * @author David Kofke
 */
public class AtomFactoryHetero extends AtomFactory {
    
    private AtomFactory[] childFactory;
    private final AtomType.Group groupType = new AtomType.Group(this);
    
    /**
     * @param factory the factory that makes each of the identical children.
     */
    public AtomFactoryHetero(Simulation sim, Species species, AtomFactory[] factory) {
        this(sim, species, factory, new ConfigurationLinear(sim.space));
    }
    /**
     * @param factory the factory that makes each of the identical children.
     * @param atoms the number of identical children per group (default is 1).
     * @param config the configuration applied to each group that is built (default is Linear).
     */
    public AtomFactoryHetero(Simulation sim, Species species, AtomFactory[] factory, 
                            Configuration config) {    
        super(sim, species);
        childFactory = factory;
        configuration = config;
    }
    
    /**
     * Constructs a new group.
     */
    protected Atom build(AtomTreeNodeGroup parent) {
        AtomGroup group = new AtomGroup(parentSimulation.space, groupType, parent);
        for(int i=0; i<childFactory.length; i++) {
            childFactory[i].build((AtomTreeNodeGroup)group.node);//builds child atom with group as parent
        }
        bondInitializer.makeBonds(group);
        configuration.initializeCoordinates(group);
        return group;
    }
    
    /**
     * Returns the array of subfactories that produces each of the identical atoms
     * in the group made by this factory.
     */
    public AtomFactory[] childFactory() {return childFactory;}
    
    public boolean producesAtomGroups() {return true;}
    
    public boolean vetoAddition(Atom a) {return true;} 
        
/*    public void renew(Atom a) {//need an exception in the case a is unrenewable
        if(a.type != groupType) return;  //throw exception
        configuration.initializeCoordinates((AtomGroup)a);
    }       
*/        
}//end of AtomFactoryHomo
    
