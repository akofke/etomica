package etomica.modules.colloid;
import etomica.action.activity.ActivityIntegrate;
import etomica.api.IAtom;
import etomica.api.IAtomList;
import etomica.api.IAtomType;
import etomica.api.IBox;
import etomica.api.IVectorMutable;
import etomica.atom.AtomArrayList;
import etomica.atom.AtomLeafAgentManager;
import etomica.atom.AtomLeafAgentManager.AgentSource;
import etomica.box.Box;
import etomica.box.BoxAgentManager;
import etomica.exception.ConfigurationOverlapException;
import etomica.integrator.IntegratorHard;
import etomica.integrator.IntegratorMD.ThermostatType;
import etomica.nbr.CriterionPositionWall;
import etomica.nbr.NeighborCriterion;
import etomica.nbr.list.BoxAgentSourceCellManagerList;
import etomica.nbr.list.PotentialMasterList;
import etomica.potential.P2HardSphere;
import etomica.potential.P2HardWrapper;
import etomica.simulation.Simulation;
import etomica.space.Space;
import etomica.space3d.Space3D;
import etomica.species.SpeciesSpheresMono;
import etomica.util.RandomNumberGenerator;

/**
 * Colloid simulation.  Design by Alberto Striolo.
 *
 * @author Andrew Schultz
 */
public class ColloidSim extends Simulation {
    
    private static final long serialVersionUID = 1L;
    public PotentialMasterList potentialMaster;
    public SpeciesSpheresMono species, speciesColloid;
    public IBox box;
    public IntegratorHard integrator;
    public P2HardWrapper potentialWrapper;
    public ActivityIntegrate activityIntegrate;
    public ConfigurationColloid configuration;
    public AtomLeafAgentManager colloidMonomerBondManager;
    public AtomLeafAgentManager monomerMonomerBondManager;
    public P2SquareWellMonomer p2mm;
    public P2HardSphereMC p2mc;
    public P2HardSphere p2pseudo;
    public int nGraft;
    public int chainLength;
    public P1Wall p1WallMonomer, p1WallColloid;
    public CriterionPositionWall criterionWallMonomer;
    
    public ColloidSim(Space _space) {
        super(_space);
        setRandom(new RandomNumberGenerator(1));
        BoxAgentSourceCellManagerList boxAgentSource = new BoxAgentSourceCellManagerList(this, null, _space);
        potentialMaster = new PotentialMasterList(this, 6, boxAgentSource, new BoxAgentManager(boxAgentSource), new NeighborListManagerColloid.NeighborListAgentSourceColloid(6, _space), _space);
        
        int nColloid = 1;
        chainLength = 50;
        nGraft = 20;
        
        double sigma = 1.0;
        double sigmaColloid = 7.5;
        double lambda = 1.5;
        double boxSize = 150;
        
        //controller and integrator
	    integrator = new IntegratorHard(this, potentialMaster, space);
	    integrator.setTimeStep(0.02);
	    integrator.setTemperature(2);
	    integrator.setIsothermal(true);
        integrator.setThermostat(ThermostatType.ANDERSEN_SINGLE);
        integrator.setThermostatInterval(10);
        activityIntegrate = new ActivityIntegrate(integrator,0,true);
        getController().addAction(activityIntegrate);

	    //species and potentials
	    species = new SpeciesSpheresMono(this, space);
	    species.setIsDynamic(true);
        addSpecies(species);
        speciesColloid = new SpeciesSpheresMono(this, space);
        speciesColloid.setIsDynamic(true);
        addSpecies(speciesColloid);

        box = new Box(space);
        addBox(box);
        ((NeighborListManagerColloid)potentialMaster.getNeighborManager(box)).setSpeciesColloid(speciesColloid);
        ((NeighborListManagerColloid)potentialMaster.getNeighborManager(box)).setSpeciesMonomer(species);
        IVectorMutable dim = space.makeVector();
        dim.E(boxSize);
        box.getBoundary().setBoxSize(dim);
        box.setNMolecules(speciesColloid, nColloid);

        AgentSource bondAgentSource = new AgentSource() {
            public void releaseAgent(Object agent, IAtom atom) {}
            public Object makeAgent(IAtom a) {return new AtomArrayList();}
            public Class getAgentClass() {return AtomArrayList.class;}
        };
        colloidMonomerBondManager = new AtomLeafAgentManager(bondAgentSource, box);
        monomerMonomerBondManager = new AtomLeafAgentManager(bondAgentSource, box);

        //instantiate several potentials for selection in combo-box
	    p2mm = new P2SquareWellMonomer(space, monomerMonomerBondManager);
	    p2mm.setCoreDiameter(sigma);
	    p2mm.setLambda(lambda);
	    p2mm.setBondFac(0.85);
        potentialMaster.addPotential(p2mm,new IAtomType[]{species.getLeafType(), species.getLeafType()});
        p2mc = new P2HardSphereMC(space, colloidMonomerBondManager);
        p2mc.setCollisionDiameter(0.5*(sigma+sigmaColloid));
        p2mc.setBondFac(0.9);
        potentialMaster.addPotential(p2mc,new IAtomType[]{species.getLeafType(), speciesColloid.getLeafType()});
        potentialMaster.setCriterion(p2mc, new CriterionNone());
        ((NeighborListManagerColloid)potentialMaster.getNeighborManager(box)).setPotentialMC(p2mc);
        
        p1WallMonomer = new P1Wall(space, monomerMonomerBondManager);
        p1WallMonomer.setBox(box);
        p1WallMonomer.setRange(2);
        p1WallMonomer.setSigma(1);
        p1WallMonomer.setEpsilon(2);
        potentialMaster.addPotential(p1WallMonomer, new IAtomType[]{species.getLeafType()});
        criterionWallMonomer = new CriterionPositionWall(this);
        criterionWallMonomer.setBoundaryWall(true);
        criterionWallMonomer.setNeighborRange(3);
        criterionWallMonomer.setWallDim(1);
        potentialMaster.setCriterion(p1WallMonomer, criterionWallMonomer);
	    
        p1WallColloid = new P1Wall(space, null);
        p1WallColloid.setBox(box);
        p1WallColloid.setRange(15);
        p1WallColloid.setSigma(7.5);
        p1WallColloid.setEpsilon(10);
        potentialMaster.addPotential(p1WallColloid, new IAtomType[]{speciesColloid.getLeafType()});
        
        //construct box
        configuration = new ConfigurationColloid(space, species, speciesColloid, random);
        configuration.setNGraft(nGraft);
        configuration.setChainLength(chainLength);
        configuration.setSigmaColloid(sigmaColloid);
        configuration.setSigmaMonomer(sigma);
        configuration.setMonomerMonomerBondManager(monomerMonomerBondManager);
        configuration.setColloidMonomerBondManager(colloidMonomerBondManager);
        configuration.initializeCoordinates(box);
        integrator.setBox(box);
        
        p2pseudo = new P2HardSphere(space, 1, true);
        if (nGraft > 1) {
            IAtom atom1 = box.getMoleculeList(species).getMolecule(0).getChildList().getAtom(0);
            IAtom atom2 = box.getMoleculeList(species).getMolecule(chainLength).getChildList().getAtom(0);
            IVectorMutable dr = space.makeVector();
            dr.Ev1Mv2(atom1.getPosition(), atom2.getPosition());
            box.getBoundary().nearestImage(dr);
            p2pseudo.setCollisionDiameter(0.9*Math.sqrt(dr.squared()));
        }
        
        potentialMaster.addPotential(p2pseudo, new IAtomType[]{species.getLeafType(), species.getLeafType()});
        potentialMaster.setCriterion(p2pseudo, new CriterionNone());

        ((NeighborListManagerColloid)potentialMaster.getNeighborManager(box)).setPotentialPseudo(p2pseudo);
        ((NeighborListManagerColloid)potentialMaster.getNeighborManager(box)).setChainLength(chainLength);
        

        integrator.getEventManager().addListener(potentialMaster.getNeighborManager(box));
    }
    
    public void setNumGraft(int newNumGraft) {
        if (nGraft == newNumGraft) return;
        configuration.setNGraft(newNumGraft);
        nGraft = newNumGraft;
        configuration.initializeCoordinates(box);
        
        if (newNumGraft > 1) {

            IAtom atom1 = box.getMoleculeList(species).getMolecule(0).getChildList().getAtom(0);
            IAtom atom2 = box.getMoleculeList(species).getMolecule(chainLength).getChildList().getAtom(0);
            IVectorMutable dr = space.makeVector();
            dr.Ev1Mv2(atom1.getPosition(), atom2.getPosition());
            box.getBoundary().nearestImage(dr);
            
            p2pseudo.setCollisionDiameter(0.9*Math.sqrt(dr.squared()));
        }
        try {
            integrator.reset();
        }
        catch (ConfigurationOverlapException e) {}
    }
    
    public void setChainLength(int newChainLength) {
        if (chainLength == newChainLength) return;
        configuration.setChainLength(newChainLength);
        chainLength = newChainLength;
        configuration.initializeCoordinates(box);
        ((NeighborListManagerColloid)potentialMaster.getNeighborManager(box)).setChainLength(chainLength);
        
        try {
            integrator.reset();
        }
        catch (ConfigurationOverlapException e) {}
    }
    
    public int getChainLength() {
        return chainLength;
    }
    
    public static void main(String[] args) {
        Space space = Space3D.getInstance();
            
        ColloidSim sim = new ColloidSim(space);
        sim.getController().actionPerformed();
    }

    // reject everything.  we'll add them explicitly
    public static class CriterionNone implements NeighborCriterion {
        public boolean unsafe() {return false;}
        public void setBox(IBox box) {}
        public void reset(IAtom atom) {}
        public boolean needUpdate(IAtom atom) {return false;}
        public boolean accept(IAtomList pair) {return false;}
    }
}