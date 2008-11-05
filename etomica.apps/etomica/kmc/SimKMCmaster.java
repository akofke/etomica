package etomica.kmc;

import etomica.api.IVector;
import etomica.simulation.Simulation;
import etomica.space.ISpace;
import etomica.units.Kelvin;

public class SimKMCmaster extends Simulation{

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    public SimKMCmaster(ISpace space) {
        super(space);
    }

    public static void main(String[] args){
        double temp = Double.parseDouble(args[0]);
        int steps = Integer.parseInt(args[1]);
        int totalSearch = Integer.parseInt(args[2]);
        final String APP_NAME = "SimKMCmaster";

        final SimKMCMEAMadatom sim = new SimKMCMEAMadatom();
        IVector vect = sim.getSpace().makeVector();
        vect.setX(0, 9.8);
        vect.setX(1, -0.2);
        vect.setX(2, -0.2);
               
        sim.setMovableAtoms(100.0, vect);
        
        sim.setPotentialListAtoms();
        
        sim.initializeConfiguration("initialStart");
        
        sim.integratorKMCCluster(Kelvin.UNIT.toSim(temp), steps, totalSearch);
        
        //for sn energy: -3331480.584975273    Vib: 9.561284069712113E96
        sim.integratorKMCCluster.setInitialStateConditions(-3331480.584975273, 9.561284069712113E96);
        sim.getController().actionPerformed();
    }
}
