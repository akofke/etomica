package etomica.potential;

import etomica.AtomSet;
import etomica.AtomType;
import etomica.Default;
import etomica.Space;


/**
 * Wraps a soft-spherical potential to apply a truncation to it.  Energy and
 * its derivatives are set to zero at a specified cutoff.  (No accounting is
 * made of the infinite force existing at the cutoff point).  Lrc potential
 * is based on integration of energy from cutoff to infinity, assuming no
 * pair correlations beyond the cutoff.
 */

/*
 * History
 * Created on Mar 28, 2005
 */
public class P2SoftSphericalTruncated extends Potential2SoftSpherical
               implements PotentialTruncated {

    public P2SoftSphericalTruncated(Potential2SoftSpherical potential) {
        this(potential, Default.POTENTIAL_CUTOFF_FACTOR * Default.ATOM_SIZE);
    }
    
    public P2SoftSphericalTruncated(Potential2SoftSpherical potential, double truncationRadius) {
        super(potential.getSpace());
        this.potential = potential;
        setTruncationRadius(truncationRadius);
    }

    /**
     * Returns the energy of the wrapped potential if the separation
     * is less than the cutoff value
     * @param r2 the squared distance between the atoms
     */
    public double u(double r2) {
        return (r2 < r2Cutoff) ? potential.u(r2) : 0.0;
    }

    /**
     * Returns the derivative (r du/dr) of the wrapped potential if the separation
     * is less than the cutoff value
     * @param r2 the squared distance between the atoms
     */
    public double du(double r2) {
        return (r2 < r2Cutoff) ? potential.du(r2) : 0.0;
    }

    /**
     * Returns the 2nd derivative (r^2 d^2u/dr^2) of the wrapped potential if the separation
     * is less than the cutoff value
     * @param r2 the squared distance between the atoms
     */
    public double d2u(double r2) {
        return (r2 < r2Cutoff) ? potential.d2u(r2) : 0.0;
    }

    /**
     * Returns the value of uInt for the wrapped potential.
     */
    public double uInt(double rC) {
        return potential.uInt(rC);
    }
    
    /**
     * Mutator method for the radial cutoff distance.
     */
    public final void setTruncationRadius(double rCut) {
        rCutoff = rCut;
        r2Cutoff = rCut*rCut;
    }
    /**
     * Accessor method for the radial cutoff distance.
     */
    public double getTruncationRadius() {return rCutoff;}
    
    /**
     * Returns the truncation radius.
     */
    public double getRange() {
        return rCutoff;
    }
    
    /**
     * Returns the dimension (length) of the radial cutoff distance.
     */
    public etomica.units.Dimension getTruncationRadiusDimension() {return etomica.units.Dimension.LENGTH;}
    
    /**
     * Returns the zero-body potential that evaluates the contribution to the
     * energy and its derivatives from pairs that are separated by a distance
     * exceeding the truncation radius.
     */
    public Potential0Lrc makeLrcPotential(AtomType[] types) {
        return new P0Lrc(space, types);
    }
    
    /**
     * Inner class that implements the long-range correction for this truncation scheme.
     */
    private class P0Lrc extends Potential0Lrc {
        
        private final double A;
        private final int D;
        
        public P0Lrc(Space space, AtomType[] types) {
            super(space, types, potential);
            A = space.sphereArea(1.0);  //multiplier for differential surface element
            D = space.D();              //spatial dimension
        }
 
        public double energy(AtomSet atoms) {
            return uCorrection(nPairs()/phase.volume());
        }
        
        /**
         * Long-range correction to the energy.
         * @param pairDensity average pairs-per-volume affected by the potential.
         */
        public double uCorrection(double pairDensity) {
            double integral = potential.integral(rCutoff);
            return pairDensity*integral;
        }
        
        /**
         * Uses result from integration-by-parts to evaluate integral of
         * r du/dr using integral of u.
         * @param pairDensity average pairs-per-volume affected by the potential.
         */
            //not checked carefully
        public double duCorrection(double pairDensity) {
            double integral = potential.integral(rCutoff);
            integral = A*space.powerD(rCutoff)*potential.u(r2Cutoff) - D*integral;//need potential to be spherical to apply here
            return pairDensity*integral;
        }

        /**
         * Uses result from integration-by-parts to evaluate integral of
         * r^2 d2u/dr2 using integral of u.
         * @param pairDensity average pairs-per-volume affected by the potential.
         *
         * Not implemented: throws RuntimeException.
         */
        public double d2uCorrection(double pairDensity) {
            throw new etomica.exception.MethodNotImplementedException();
        }
    }//end of P0lrc
    
    /*   public static void main(String[] args) {
    
    Simulation.instance = new etomica.graphics.SimulationGraphic();
    IntegratorMC integrator = new IntegratorMC();
    MCMoveAtom mcMove = new MCMoveAtom(integrator);//comment this line to examine LRC by itself
    SpeciesSpheresMono species = new SpeciesSpheresMono();
    species.setNMolecules(72);
    final Phase phase = new Phase();
    P2LennardJones potential = new P2LennardJones(3.0, 2000.);
    Controller controller = new Controller();
    etomica.graphics.DisplayPhase display = new etomica.graphics.DisplayPhase();

    MeterEnergy energy = new MeterEnergy();
    energy.setPhase(phase);
    energy.setHistorying(true);
    energy.setActive(true);     
    energy.getHistory().setNValues(500);        
    etomica.graphics.DisplayPlot plot = new etomica.graphics.DisplayPlot();
    plot.setLabel("Energy");
    plot.setDataSources(energy.getHistory());
    
    integrator.setSleepPeriod(2);
    
    etomica.graphics.DeviceToggleButton lrcToggle = new etomica.graphics.DeviceToggleButton(Simulation.instance,
        new ModifierBoolean() {
            public void setBoolean(boolean b) {phase.setLrcEnabled(b);}
            public boolean getBoolean() {return phase.isLrcEnabled();}
        },"LRC enabled", "LRC disabled" );
    
    Simulation.instance.elementCoordinator.go();
    
//    potential.setIterator(new AtomPairIteratorGeneral(phase));
//    potential.set(species.getAgent(phase));
    
    etomica.graphics.SimulationGraphic.makeAndDisplayFrame(Simulation.instance);
}//end of main
// */   


    private double rCutoff, r2Cutoff;
    private final Potential2SoftSpherical potential;
}
