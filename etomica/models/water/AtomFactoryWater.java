package etomica.models.water;

import etomica.Atom;
import etomica.AtomFactory;
import etomica.AtomFactoryMono;
import etomica.AtomSequencerFactory;
import etomica.AtomType;
import etomica.AtomTypeSphere;
import etomica.Space;

/**
 * Factory that constructs a water molecule, with three child atoms of two
 * Hydrogen and one Oxygen.
 * @author kofke
 *
 */
public class AtomFactoryWater extends AtomFactory {

	/**
	 * Constructor for AtomFactoryWater.
	 * @param sim
	 * @param sequencerFactory
	 */
	public AtomFactoryWater(Space space) {
        this(space, AtomSequencerFactory.SIMPLE);
    }
    
    public AtomFactoryWater(Space space, AtomSequencerFactory sequencerFactory) {
		super(space, sequencerFactory, AtomTreeNodeWater.FACTORY);

		hFactory = new AtomFactoryMono(space, simulation.getIteratorFactory().simpleSequencerFactory());
		oFactory = new AtomFactoryMono(space, simulation.getIteratorFactory().simpleSequencerFactory());
		AtomType hType = new AtomTypeSphere(hFactory, 1.0, /*Electron.UNIT.toSim(0.4238),*/ 2.0);
		AtomType oType = new AtomTypeSphere(oFactory, 16.0, /*Electron.UNIT.toSim(-0.8476),*/ 3.167);
        
		hFactory.setType(hType);
		oFactory.setType(oType);

		configuration = new ConfigurationWater(space); 
	}

	/**
	 * @see etomica.AtomFactory#build(etomica.Atom)
	 */
	public Atom build(Atom group) {
		AtomTreeNodeWater waterNode = (AtomTreeNodeWater)group.node;
		waterNode.O = oFactory.makeAtom(waterNode);
		waterNode.H1 = hFactory.makeAtom(waterNode);
		waterNode.H2 = hFactory.makeAtom(waterNode);
		configuration.initializeCoordinates(group);
		return group;
	}

	/**
	 * @see etomica.AtomFactory#isGroupFactory()
	 */
	public boolean isGroupFactory() {
		return true;
	}

	public final AtomFactoryMono hFactory, oFactory;
}
