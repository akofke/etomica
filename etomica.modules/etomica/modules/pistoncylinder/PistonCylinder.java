package etomica.modules.pistoncylinder;

import etomica.action.activity.ActivityIntegrate;
import etomica.action.activity.Controller;
import etomica.config.Configuration;
import etomica.config.ConfigurationLattice;
import etomica.config.ConfigurationSequential;
import etomica.data.AccumulatorAverage;
import etomica.data.DataPump;
import etomica.data.meter.MeterPressureHard;
import etomica.data.types.DataDouble;
import etomica.data.types.DataGroup;
import etomica.integrator.IntegratorMD;
import etomica.integrator.IntervalActionAdapter;
import etomica.lattice.LatticeCubicFcc;
import etomica.phase.Phase;
import etomica.potential.P1HardBoundary;
import etomica.potential.P1HardMovingBoundary;
import etomica.potential.P2SquareWell;
import etomica.potential.Potential2HardSphericalWrapper;
import etomica.potential.PotentialMaster;
import etomica.simulation.Simulation;
import etomica.space.BoundaryRectangularNonperiodic;
import etomica.space.Space;
import etomica.space.Vector;
import etomica.space2d.Vector2D;
import etomica.space3d.Vector3D;
import etomica.species.Species;
import etomica.species.SpeciesSpheresMono;
import etomica.units.Bar;
import etomica.util.Default;

/**
 * Simple hard-sphere MD in piston-cylinder apparatus
 */
public class PistonCylinder extends Simulation {
    
    public IntegratorHardPiston integrator;
    public SpeciesSpheresMono species;
    public Phase phase;
    public Controller controller;
    public Potential2HardSphericalWrapper potentialWrapper;
    public P1HardBoundary wallPotential;
    public P1HardMovingBoundary pistonPotential;
    public ActivityIntegrate ai;
    public double lambda;

    public PistonCylinder(int D) {
        this(D, new Default());
    }
    
    public PistonCylinder(int D, Default defaults) {
        super(Space.getInstance(D), true, new PotentialMaster(Space.getInstance(D)), Default.BIT_LENGTH, defaults);
        lambda = 1.5;
        controller = getController();
        defaults.atomMass = 16;
        species = new SpeciesSpheresMono(this);
        species.setNMolecules(112);
        phase = new Phase(this);
        phase.setBoundary(new BoundaryRectangularNonperiodic(space));
        Vector newDim;
        Configuration config;
        if (space.D() == 2) {
            config = new ConfigurationSequential(space);
            newDim = new Vector2D(80,150);
        }
        else {
            config = new ConfigurationLattice(new LatticeCubicFcc());
            newDim = new Vector3D(80,80,80);
        }
        phase.setDimensions(newDim);
        phase.makeMolecules();
        config.initializeCoordinates(phase);
        
        P2SquareWell potentialSW = new P2SquareWell(space,defaults.atomSize,lambda,31.875,defaults.ignoreOverlap);
        potentialWrapper = new Potential2HardSphericalWrapper(space,potentialSW);
//        potential = new P2HardSphere(space,Default.atomSize);
        potentialMaster.setSpecies(potentialWrapper,new Species[]{species,species});
        
        wallPotential = new P1HardBoundary(this);
        wallPotential.setCollisionRadius(defaults.atomSize*0.5); //potential.getCoreDiameter()*0.5);
        potentialMaster.setSpecies(wallPotential,new Species[]{species});
        wallPotential.setActive(0,true,true);  // left wall
        wallPotential.setActive(0,false,true); // right wall
        wallPotential.setActive(1,true,false); // top wall
        wallPotential.setActive(1,false,true); // bottom wall
        if (D==3) {
            wallPotential.setActive(2,true,true);  // front wall
            wallPotential.setActive(2,false,true); // back wall
        }

        pistonPotential = new P1HardMovingBoundary(space,phase.getBoundary(),1,defaults.atomMass*100, defaults.ignoreOverlap);
        pistonPotential.setCollisionRadius(defaults.atomSize*0.5);
        pistonPotential.setWallPosition(0.0);
        pistonPotential.setWallVelocity(0.5);
        if (D == 3) {
            pistonPotential.setPressure(Bar.UNIT.toSim(1.0));
        }
        else {
            pistonPotential.setPressure(Bar.UNIT.toSim(100.0));
        }
        pistonPotential.setThickness(1.0);
        potentialMaster.setSpecies(pistonPotential,new Species[]{species});
        
        integrator = new IntegratorHardPiston(this,pistonPotential);
        integrator.setPhase(phase);
        integrator.setIsothermal(true);
        integrator.setThermostatInterval(1);
        integrator.setThermostat(IntegratorMD.ANDERSEN_SINGLE);
        integrator.setTimeStep(1.0);
        ai = new ActivityIntegrate(this,integrator);
        getController().addAction(ai);
        
    }
    
    /**
     * Demonstrates how this class is implemented.
     */
    public static void main(String[] args) {
        PistonCylinder sim = new PistonCylinder(3);
        sim.ai.setMaxSteps(50000);
        sim.integrator.setTimeStep(20.0);
        sim.getDefaults().blockSize=1000;

        MeterPressureHard pMeter = new MeterPressureHard(sim.space,sim.integrator);
        pMeter.setPhase(sim.phase);
        AccumulatorAverage pAcc = new AccumulatorAverage(sim);
        DataPump pump = new DataPump(pMeter, pAcc);
        IntervalActionAdapter adapter = new IntervalActionAdapter(pump,sim.integrator);
        adapter.setActionInterval(10);
        
        MeterPistonDensity dMeter = new MeterPistonDensity(sim.pistonPotential,1,sim.getDefaults().atomSize);
        dMeter.setPhase(sim.phase);
        AccumulatorAverage dAcc = new AccumulatorAverage(sim);
        pump = new DataPump(dMeter, dAcc);
        adapter = new IntervalActionAdapter(pump,sim.integrator);
        adapter.setActionInterval(10);
        
        System.out.println("density (mol/L) = "+dMeter.getDataAsScalar()*10000/6.0221367);
      
        sim.getController().actionPerformed();
        
        System.out.println("density average "+((DataDouble)((DataGroup)dAcc.getData()).getData(AccumulatorAverage.AVERAGE.index)).x*10000/6.0221367+" +/- "
                +((DataDouble)((DataGroup)dAcc.getData()).getData(AccumulatorAverage.ERROR.index)).x*10000/6.0221367);
        System.out.println("Z="+((DataDouble)((DataGroup)pAcc.getData()).getData(AccumulatorAverage.AVERAGE.index)).x+" +/- "
                +((DataDouble)((DataGroup)pAcc.getData()).getData(AccumulatorAverage.ERROR.index)).x);

   }
       
}
