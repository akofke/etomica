package etomica.simulations;
import etomica.*;

/**
 * Simple hard-sphere Monte Carlo simulation in 2D
 */
 
public class HsMc2d extends Simulation {
    
    public IntegratorMC integrator;
    public MCMoveAtom mcMoveAtom;
    public SpeciesDisks species;
    public Phase phase;
    public P2HardSphere potential;
    public Controller controller;
    public DisplayPhase display;
    public MeterCycles meterCycles;
    public DisplayBox displayCycles;

    public HsMc2d() {
        super(new Space2D());
        Simulation.instance = this;
	    phase = new Phase(this);
	    integrator = new IntegratorMC(this);
	    mcMoveAtom = new MCMoveAtom();
	    integrator.add(mcMoveAtom);
	    species = new SpeciesDisks(this);
	    potential = new P2HardSphere(this);
	    controller = new Controller(this);
	    display = new DisplayPhase(this);
	    meterCycles = new MeterCycles(this);
	    displayCycles = new DisplayBox(this,meterCycles);
		setBackground(java.awt.Color.yellow);
		elementCoordinator.go();
        Potential2.Agent potentialAgent = (Potential2.Agent)potential.getAgent(phase);
        potentialAgent.setIterator(new AtomPairIterator(phase));
    }
    
    /**
     * Demonstrates how this class is implemented.
     */
    public static void main(String[] args) {
        HsMc2d sim = new HsMc2d();
		sim.elementCoordinator.go(); 
		Simulation.makeAndDisplayFrame(sim);
	//	sim.controller.start();
    }//end of main
    
}