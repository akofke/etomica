package etomica.simulations;
import etomica.*;

/**
 * Simple hard-sphere molecular dynamics simulation in 2D
 */
 
public class HSMD2D extends Simulation {
    
    public IntegratorHard integrator;
    public SpeciesDisks species;
    public Phase phase;
    public P2HardSphere potential;
    public Controller controller;
    public DisplayPhase display;

    public HSMD2D() {
        super(new Space2D());
        Simulation.instance = this;
	    integrator = new IntegratorHard(this);
	    species = new SpeciesDisks(this);
	    species.setNMolecules(25);
	    phase = new Phase(this);
	    potential = new P2HardSphere(this);
	    controller = new Controller(this);
	    display = new DisplayPhase(this);
//	    IntegratorMD.Timer timer = integrator.new Timer(integrator.chronoMeter());
//	    timer.setUpdateInterval(10);
		setBackground(java.awt.Color.yellow);
		elementCoordinator.go();
        Potential2.Agent potentialAgent = (Potential2.Agent)potential.getAgent(phase);
        potentialAgent.setIterator(new AtomPairIterator(phase));
		
    }
    
    /**
     * Demonstrates how this class is implemented.
     */
    public static void main(String[] args) {
        javax.swing.JFrame f = new javax.swing.JFrame();   //create a window
        f.setSize(600,350);
        
        Simulation sim = new HSMD2D();
		sim.elementCoordinator.go(); 
		
        f.getContentPane().add(sim.panel());
        
        f.pack();
        f.show();
        f.addWindowListener(Simulation.WINDOW_CLOSER);
    }//end of main
    
}