package etomica.kmc;

import etomica.api.IVector;
import etomica.dimer.SimDimerMEAMadatom;
import etomica.simulation.Simulation;
import etomica.space.ISpace;

public class SimKMCworker extends Simulation{

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    public SimKMCworker(ISpace space) {
        super(space);
    }

    public static void main(String[] args){
        String fileName = args[0];
        final String APP_NAME = "SimKMCworker";

        final SimKMCMEAMadatom sim = new SimKMCMEAMadatom();
        IVector vect = sim.getSpace().makeVector();
        vect.setX(0, 9.8);
        vect.setX(1, -0.2);
        vect.setX(2, -0.2);
        
        sim.setMovableAtoms(100.0, vect);
        
        sim.setPotentialListAtoms();
        
        sim.initializeConfiguration("searchStart");
        sim.randomizePositions();
        
        sim.enableDimerSearch(fileName, 1000);
        sim.integratorDimer.setRotNum(0);
        
        sim.getController().actionPerformed();
    }
}
