package etomica.normalmode;

import java.io.FileWriter;
import java.io.IOException;

import etomica.action.PDBWriter;
import etomica.action.activity.ActivityIntegrate;
import etomica.atom.AtomFactoryMono;
import etomica.atom.AtomType;
import etomica.data.types.DataDoubleArray;
import etomica.data.types.DataGroup;
import etomica.integrator.IntegratorMC;
import etomica.integrator.mcmove.MCMoveStepTracker;
import etomica.lattice.crystal.Basis;
import etomica.lattice.crystal.BasisCubicFcc;
import etomica.lattice.crystal.BasisMonatomic;
import etomica.lattice.crystal.Primitive;
import etomica.lattice.crystal.PrimitiveCubic;
import etomica.nbr.list.PotentialMasterList;
import etomica.phase.Phase;
import etomica.potential.P2LennardJones;
import etomica.potential.P2SoftSphericalTruncated;
import etomica.potential.Potential2SoftSpherical;
import etomica.potential.PotentialMaster;
import etomica.simulation.Simulation;
import etomica.space.Boundary;
import etomica.space.BoundaryRectangularPeriodic;
import etomica.space.IVector;
import etomica.space.Space;
import etomica.species.SpeciesSpheresMono;

/**
 * MD simulation of hard spheres in 1D or 3D with tabulation of the
 * collective-coordinate S-matrix. No graphic display of simulation.
 */
public class SimCalcSLJ extends Simulation {

    public SimCalcSLJ(Space space, int numAtoms, double density, double temperature) {
        super(space, true);

        PotentialMaster potentialMaster = new PotentialMaster(space);
        defaults.makeLJDefaults();
        defaults.atomSize = 1.0;

        SpeciesSpheresMono species = new SpeciesSpheresMono(this);
        getSpeciesManager().addSpecies(species);

        phase = new Phase(this);
        addPhase(phase);
        phase.getAgent(species).setNMolecules(numAtoms);

        integrator = new IntegratorMC(potentialMaster, getRandom(), temperature);
        MCMoveAtomCoupled move = new MCMoveAtomCoupled(potentialMaster, getRandom());
        move.setStepSize(0.1);
        move.setStepSizeMax(0.5);
        integrator.getMoveManager().addMCMove(move);
        ((MCMoveStepTracker)move.getTracker()).setNoisyAdjustment(true);
        
        activityIntegrate = new ActivityIntegrate(this, integrator);
        getController().addAction(activityIntegrate);
        // activityIntegrate.setMaxSteps(nSteps);

        int[] nCells;
        Basis basis;
        if (space.D() == 1) {
            primitive = new PrimitiveCubic(space, 1.0/density);
            boundary = new BoundaryRectangularPeriodic(space, getRandom(), numAtoms/density);
            nCells = new int[]{numAtoms};
            basis = new BasisMonatomic(space);
        } else {
            double L = Math.pow(4.0/density, 1.0/3.0);
            primitive = new PrimitiveCubic(space, L);
            int n = (int)Math.round(Math.pow(numAtoms/4, 1.0/3.0));
            nCells = new int[]{n,n,n};
            boundary = new BoundaryRectangularPeriodic(space, random, n * L);
            basis = new BasisCubicFcc();
        }

        Potential2SoftSpherical potential = new P2LennardJones(space, 1.0, 1.0);
        double truncationRadius = boundary.getDimensions().x(0) * 0.45;
        P2SoftSphericalTruncated pTruncated = new P2SoftSphericalTruncated(potential, truncationRadius);
        AtomType sphereType = ((AtomFactoryMono)species.moleculeFactory()).getType();
        potentialMaster.addPotential(pTruncated, new AtomType[] {sphereType, sphereType});
        move.setPotential(pTruncated);

        phase.setBoundary(boundary);

        if (potentialMaster instanceof PotentialMasterList) {
            double neighborRange = truncationRadius;
            int cellRange = 7;
            ((PotentialMasterList)potentialMaster).setRange(neighborRange);
            ((PotentialMasterList)potentialMaster).setCellRange(cellRange); // insanely high, this lets us have neighborRange close to dimensions/2
            // find neighbors now.  Don't hook up NeighborListManager (neighbors won't change)
            ((PotentialMasterList)potentialMaster).getNeighborManager(phase).reset();
            int potentialCells = ((PotentialMasterList)potentialMaster).getNbrCellManager(phase).getLattice().getSize()[0];
            if (potentialCells < cellRange*2+1) {
                throw new RuntimeException("oops ("+potentialCells+" < "+(cellRange*2+1)+")");
            }
            if (potentialCells > cellRange*2+1) {
                System.out.println("could probably use a larger truncation radius ("+potentialCells+" > "+(cellRange*2+1)+")");
            }
        }

        coordinateDefinition = new CoordinateDefinitionLeaf(phase, primitive, basis);
        coordinateDefinition.initializeCoordinates(nCells);
        
        integrator.setPhase(phase);
    }

    /**
     * @param args
     */
    public static void main(String[] args) {

        // defaults
        int D = 3;
        int nA = 32;
        double density = 1.3;
        double temperature = 1;
        if (D == 1) {
            nA = 3;
            density = 0.5;
        }
        long simSteps = 1000000;

        // parse arguments
        if (args.length > 1) {
            density = Double.parseDouble(args[1]);
        }
        if (args.length > 2) {
            simSteps = Long.parseLong(args[2]);
        }
        if (args.length > 3) {
            nA = Integer.parseInt(args[3]);
        }
        if (args.length > 4) {
            temperature = Double.parseDouble(args[4]);
        }
        String filename = "normal_modes_LJ_" + D + "D_"+nA;
        if (args.length > 0) {
            filename = args[0];
        }

        System.out.println("Running "
                + (D == 1 ? "1D" : (D == 3 ? "FCC" : "2D hexagonal"))
                + " hard sphere simulation");
        System.out.println(nA + " atoms at density " + density+" and temperature "+temperature);
        System.out.println(simSteps+ " steps");
        System.out.println("output data to " + filename);

        // construct simulation
        SimCalcSLJ sim = new SimCalcSLJ(Space.getInstance(D), nA, density, temperature);

        // set up initial configuration and save nominal positions
        Primitive primitive = sim.primitive;

        // set up normal-mode meter
        MeterNormalMode meterNormalMode = new MeterNormalMode();
        meterNormalMode.setCoordinateDefinition(sim.coordinateDefinition);
        WaveVectorFactory waveVectorFactory;
        if (D == 1) {
            waveVectorFactory = new WaveVectorFactory1D();
        } else if (D == 2) {
            waveVectorFactory = null;
        } else {
            waveVectorFactory = new WaveVectorFactorySimple(primitive);
        }
        meterNormalMode.setWaveVectorFactory(waveVectorFactory);
        meterNormalMode.setPhase(sim.phase);

        sim.integrator.addIntervalAction(meterNormalMode);
        sim.integrator.setActionInterval(meterNormalMode, nA);

        // MeterMomentumCOM meterCOM = new MeterMomentumCOM(sim.space);
        // MeterPositionCOM meterCOM = new MeterPositionCOM(sim.space);
        // DataSinkConsole console = new DataSinkConsole();
        // DataPump comPump = new DataPump(meterCOM,console);
        // IntervalActionAdapter comAdapter = new
        // IntervalActionAdapter(comPump);
        // sim.integrator.addListener(comAdapter);
        // meterCOM.setPhase(sim.phase);

        // start simulation
//        MeterEnergy m = new MeterEnergy(sim.getPotentialMaster());
//        m.setPhase(sim.phase);
//        DataLogger logger = new DataLogger();
//        logger.setAppending(true);
//        logger.setCloseFileEachTime(true);
//        DataTableWriter writer = new DataTableWriter();
//        writer.setIncludeHeader(false);
//        logger.setDataSink(writer);
//        logger.setFileName("LJ_energy.dat");
//        logger.setSameFileEachTime(true);
//        logger.setWriteInterval(1);
//        logger.setWriteOnInterval(true);
//        DataPump pump = new DataPump(m, logger);
//        sim.integrator.addListener(new IntervalActionAdapter(pump));
        sim.activityIntegrate.setMaxSteps(simSteps/10);
        sim.getController().actionPerformed();
        System.out.println("equilibrated");
        sim.integrator.getMoveManager().setEquilibrating(false);
        sim.getController().reset();
        sim.activityIntegrate.setMaxSteps(simSteps);
        sim.getController().actionPerformed();
        PDBWriter pdbWriter = new PDBWriter(sim.phase);
        pdbWriter.setFileName("calcS.pdb");
        pdbWriter.actionPerformed();
        
        // normalize averages
        DataGroup normalModeData = (DataGroup) meterNormalMode.getData();
        normalModeData.TE(1.0 / meterNormalMode.getCallCount());

        // write wave vectors (to filename.k) and simulation results (to
        // filename.S) to file
        IVector[] waveVectors = waveVectorFactory.getWaveVectors();
        double[] coefficients = waveVectorFactory.getCoefficients();

        try {
            int coordinateDim = meterNormalMode.getCoordinateDefinition()
                    .getCoordinateDim();
            FileWriter fileWriterK = new FileWriter(filename + ".k");
            FileWriter fileWriterS = new FileWriter(filename + ".S");
            for (int k = 0; k < waveVectors.length; k++) {
                // write the wavevector with its coefficient
                fileWriterK.write(Double.toString(coefficients[k]));
                for (int j = 0; j < waveVectors[k].getD(); j++) {
                    fileWriterK.write(" " + waveVectors[k].x(j));
                }
                fileWriterK.write("\n");
                if (D == 1) {
                    System.out.println(NormalModes1DHR.S1DHR(k + 1, nA / density, nA));
                }

                // write the (coordDim x coordDim) S array for the current
                // wavevector
                DataDoubleArray dataS = (DataDoubleArray) normalModeData.getData(k);
                for (int j = 0; j < coordinateDim; j++) {
                    fileWriterS.write(Double.toString(dataS.getValue(j * coordinateDim)));
                    for (int l = 1; l < coordinateDim; l++) {
                        fileWriterS.write(" " + dataS.getValue(j * coordinateDim + l));
                    }
                    fileWriterS.write("\n");
                }
            }
            fileWriterK.close();
            fileWriterS.close();
        } catch (IOException e) {
            throw new RuntimeException("Oops, failed to write data " + e);
        }

    }

    private static final long serialVersionUID = 1L;
    public IntegratorMC integrator;
    public ActivityIntegrate activityIntegrate;
    public Phase phase;
    public Boundary boundary;
    public Primitive primitive;
    public CoordinateDefinition coordinateDefinition;
}