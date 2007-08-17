package etomica.models.hexane;

/**
 * @author cribbin
 */
import etomica.action.activity.ActivityIntegrate;
import etomica.atom.AtomGroup;
import etomica.atom.AtomLeaf;
import etomica.atom.AtomSet;
import etomica.atom.AtomType;
import etomica.atom.AtomTypeGroup;
import etomica.atom.AtomTypeSphere;
import etomica.atom.AtomsetArrayList;
import etomica.atom.iterator.AtomIteratorLeafAtoms;
import etomica.box.Box;
import etomica.graphics.SimulationGraphic;
import etomica.integrator.IntegratorMC;
import etomica.integrator.mcmove.MCMoveMolecule;
import etomica.integrator.mcmove.MCMoveRotateMolecule3D;
import etomica.integrator.mcmove.MCMoveStepTracker;
import etomica.lattice.BravaisLattice;
import etomica.normalmode.MCMoveMoleculeCoupled;
import etomica.potential.P2HardSphere;
import etomica.potential.Potential;
import etomica.potential.PotentialMaster;
import etomica.simulation.ISimulation;
import etomica.simulation.Simulation;
import etomica.space.BoundaryDeformableLattice;
import etomica.space.BoundaryDeformablePeriodic;
import etomica.space.Space;
import etomica.space3d.Space3D;
import etomica.space3d.Vector3D;

public class TestSetToUHexane extends Simulation {

    CoordinateDefinitionHexane cdHex;
    PrimitiveHexane prim;    
    public BoundaryDeformablePeriodic bdry;
    public BravaisLattice lattice;
    SpeciesHexane species;
    Box box;
    
    public ActivityIntegrate activityIntegrate;
    public IntegratorMC integrator;
//    public MCMoveVolume moveVolume;
////    public MCMoveCrankshaft crank; 
//    public MCMoveReptate snake;
    public MCMoveMolecule moveMolecule;
    public CBMCGrowSolidHexane growMolecule;
    public MCMoveRotateMolecule3D rot;
    public MCMoveMoleculeCoupled coupledMove;
    public MCMoveCombinedCbmcTranslation cctMove;
    
    
    Vector3D[] oldX;
    Vector3D[] newX;
    double[] oldUs;
    int chainLength;
    AtomIteratorLeafAtoms iterator;
    
    TestSetToUHexane(Space space){
        super(space, false);
        
        //Set up a system; this is largely chunked over from TestHexane, but
        // the integrator and MC moves and potentials are all cut out.
        int xCells = 1; 
        int yCells = 1;
        int zCells = 1;
        chainLength = 6;
        int nA = xCells * yCells * zCells * chainLength;
        double dens = 0.373773507616;
        prim = new PrimitiveHexane(this.getSpace());
        prim.scaleSize(20.0);
        lattice = new BravaisLattice(prim);
        species = new SpeciesHexane((ISimulation)this);
        
        getSpeciesManager().addSpecies(species);
        int[] nCells = new int[]{xCells, yCells, zCells};
        bdry = new BoundaryDeformableLattice(prim, getRandom(), nCells);
        box = new Box(bdry);
        addBox(box);
        box.setNMolecules(species, xCells * yCells * zCells);
        cdHex = new CoordinateDefinitionHexane(box, prim, species);
        cdHex = new CoordinateDefinitionHexane(box, prim, species);
        cdHex.initializeCoordinates(nCells);
        oldUs = new double[cdHex.getCoordinateDim()];
        
        oldX = new Vector3D[chainLength];
        newX = new Vector3D[chainLength];
        for(int i = 0; i < chainLength; i++){
            oldX[i] = new Vector3D();
            newX[i] = new Vector3D();
        }
    
        iterator = new AtomIteratorLeafAtoms(box);
        iterator.reset();
        
        PotentialMaster potentialMaster = new PotentialMaster(space);
        integrator = new IntegratorMC(potentialMaster, getRandom(), 1.0);
        
        moveMolecule = new MCMoveMolecule(potentialMaster, getRandom(),
                0.1, 1, false);
        // 0.025 for translate, 0.042 for rotate for rho=0.3737735
        moveMolecule.setStepSize(0.024);        
        integrator.getMoveManager().addMCMove(moveMolecule);
        ((MCMoveStepTracker)moveMolecule.getTracker()).setNoisyAdjustment(true);
        
//         moveVolume = new MCMoveVolume(potentialMaster, box.space(),
//         sim.getDefaults().pressure);
//         moveVolume.setBox(box);
//         integrator.getMoveManager().addMCMove(moveVolume);
////         crank = new MCMoveCrankshaft();
//         snake = new MCMoveReptate(this);
//         snake.setBox(box);
//         integrator.getMoveManager().addMCMove(snake);
        
        rot = new MCMoveRotateMolecule3D(potentialMaster, getRandom());
        rot.setBox(box);
        rot.setStepSize(0.042);
        integrator.getMoveManager().addMCMove(rot);
        ((MCMoveStepTracker)rot.getTracker()).setNoisyAdjustment(true);
        
        growMolecule = new CBMCGrowSolidHexane(potentialMaster,
                getRandom(), integrator, box, species, 20);
        growMolecule.setBox(box);
        integrator.getMoveManager().addMCMove(growMolecule);

        coupledMove = new MCMoveMoleculeCoupled(potentialMaster, getRandom());
        integrator.getMoveManager().addMCMove(coupledMove);
        
        cctMove = new MCMoveCombinedCbmcTranslation(potentialMaster, growMolecule, getRandom());
        cctMove.setBox(box);
        integrator.getMoveManager().addMCMove(cctMove);
        
        // nan we're going to need some stuff in there to set the step sizes and
        // other stuff like that.

        integrator.setIsothermal(true);
        activityIntegrate = new ActivityIntegrate(integrator);
        activityIntegrate.setMaxSteps(2000000);
        getController().addAction(activityIntegrate);

        //INTERMOLECULAR POTENTIAL STUFF

        //This potential is the intermolecular potential between atoms on
        // different molecules. We use the class "Potential" because we are
        // reusing the instance as we define each potential.
        Potential potential = new P2HardSphere(space);
        
        //here, we add the species to the PotentialMaster, using types.
        //The PotentialMaster generates a group potential and automatically
        // does a lot of the stuff which we have to do for the intramolecular
        // potential manually.
        AtomTypeSphere sphereType = (AtomTypeSphere) ((AtomTypeGroup) species
                .getMoleculeType()).getChildTypes()[0];

        //Add the Potential to the PotentialMaster
        potentialMaster.addPotential(potential, new AtomType[] { sphereType,
                sphereType });
        
        coupledMove.setPotential(potentialMaster.getPotential(new AtomType[] {
                species.getMoleculeType(), species.getMoleculeType() }  ));

        integrator.setBox(box);
    }
    
    
    public void runit(){

        int nsteps = chainLength * 100;
            
        //Store old positions
        AtomSet aal = ((AtomGroup)box.molecule(0)).getChildList();
        AtomsetArrayList list = new AtomsetArrayList();
        list.setAtoms(aal);
        
        for(int i = 0; i < chainLength; i++){
            oldX[i].E((Vector3D)((AtomLeaf)list.getAtom(i)).getPosition());
        }
           
        for(int counter = 0; counter < nsteps; counter++){    
           //Calculate the u's that correspond to the old positions. 
           oldUs = cdHex.calcU(box.getAgent(species).getChildList());
            
            //nan MAKE A BUNCH OF MOVES HERE
            for(int i = 0; i < chainLength*2; i++){
                integrator.doStepInternal();
            }
            
            //Store the current positions for later use.
            for(int i = 0; i < chainLength; i++){
                newX[i].E((Vector3D)((AtomLeaf)list.getAtom(i)).getPosition());
            }
            
            //Move the molecules to the old positions, using u's
//            cdHex.setToU(box.getAgent(species).getAtomManager().getLeafList(), 
//                    oldUs);
        
            cdHex.setToU(((AtomGroup)box.getAgent(species).getChildList().getAtom(0)).getChildList(), oldUs);
            
            //Compare the old and new positions.
            double tol = 0.0000005;
            for(int i = 0; i < chainLength; i++) {
                newX[i].ME(oldX[i]);
                newX[i].squared();
                for(int j = 0; j < 3; j++){
                    if(newX[i].x(j) > tol){
                        System.out.println("Not in the tolerance!!");
                    }
                }
            }
            
            //Move the new positions to the old positions and repeat. 
            for(int i = 0; i < chainLength; i++){
                newX[i].E(oldX[i]);
            } 
        }
                
        System.out.println("Zoinks!");
    }
    
    public static void main(String[] args) {

        TestSetToUHexane sim = new TestSetToUHexane(Space3D.getInstance());
        boolean graphic = false;
        
        if (graphic) {
            SimulationGraphic simGraphic = new SimulationGraphic(sim);
            simGraphic.makeAndDisplayFrame();
        } else {
            sim.runit();
        }
    }
    
}
    
//    
//
//    
//    
//    
//
//class Compare implements etomica.action.Action {
//    
//    Compare(){
//        
//    }
//    
//    public void actionPerformed(){
//        
//    }
//}
