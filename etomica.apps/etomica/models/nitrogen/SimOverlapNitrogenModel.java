package etomica.models.nitrogen;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import etomica.action.activity.ActivityIntegrate;
import etomica.api.IBox;
import etomica.api.ISpecies;
import etomica.api.IVector;
import etomica.box.Box;
import etomica.data.AccumulatorAverage;
import etomica.data.DataPump;
import etomica.data.IEtomicaDataSource;
import etomica.data.meter.MeterPotentialEnergy;
import etomica.data.types.DataDoubleArray;
import etomica.data.types.DataGroup;
import etomica.graphics.SimulationGraphic;
import etomica.integrator.IntegratorBox;
import etomica.integrator.IntegratorMC;
import etomica.integrator.mcmove.MCMoveRotateMolecule3D;
import etomica.lattice.crystal.Basis;
import etomica.lattice.crystal.BasisCubicFcc;
import etomica.lattice.crystal.Primitive;
import etomica.lattice.crystal.PrimitiveCubic;
import etomica.listener.IntegratorListenerAction;
import etomica.normalmode.BasisBigCell;
import etomica.normalmode.CalcHarmonicA;
import etomica.normalmode.MCMoveHarmonic;
import etomica.normalmode.MCMoveMoleculeCoupled;
import etomica.normalmode.MeterBoltzmannHarmonic;
import etomica.normalmode.MeterBoltzmannTarget;
import etomica.normalmode.MeterHarmonicEnergy;
import etomica.normalmode.NormalModes;
import etomica.normalmode.NormalModesFromFile;
import etomica.normalmode.WaveVectorFactory;
import etomica.potential.PotentialMaster;
import etomica.simulation.Simulation;
import etomica.space.Boundary;
import etomica.space.BoundaryDeformablePeriodic;
import etomica.space.Space;
import etomica.units.Kelvin;
import etomica.units.Pascal;
import etomica.units.Pixel;
import etomica.util.ParameterBase;
import etomica.util.ReadParameters;
import etomica.virial.overlap.AccumulatorVirialOverlapSingleAverage;
import etomica.virial.overlap.DataSourceVirialOverlap;
import etomica.virial.overlap.IntegratorOverlap;

/**
 * Simulation to run sampling with the nitrogen model to find the free energy difference
 * 	between the harmonic reference system and the target system
 * 
 * The Bennett's Overlapping Sampling Simulation
 * 	- used to check for the computation time
 * 
 * @author Tai Boon Tan
 */
public class SimOverlapNitrogenModel extends Simulation {

    public SimOverlapNitrogenModel(Space _space, int numMolecules,double temperature, String filename) {
        super(_space);
        this.fname = filename;
        
        integrators = new IntegratorBox[2];
        accumulatorPumps = new DataPump[2];
        meters = new IEtomicaDataSource[2];
        accumulators = new AccumulatorVirialOverlapSingleAverage[2];

        double unitCellLength = 5.661;
        nCell = (int) Math.round(Math.pow((numMolecules/4), 1.0/3.0));
       
        potentialMasterTarget = new PotentialMaster();
        
    	Basis basisFCC = new BasisCubicFcc();
		Basis basis = new BasisBigCell(space, basisFCC, new int[]{nCell, nCell, nCell});
		
		ConformationNitrogen conformation = new ConformationNitrogen(space);
		species = new SpeciesN2(space);
		species.setConformation(conformation);
		addSpecies(species);
		
        // TARGET
        boxTarget = new Box(space);
        addBox(boxTarget);
        boxTarget.setNMolecules(species, numMolecules);

    	int [] nCells = new int[]{1,1,1};
		boundaryTarget = new BoundaryDeformablePeriodic(space,nCell*unitCellLength);
		primitive = new PrimitiveCubic(space, nCell*unitCellLength);
	
		CoordinateDefinitionNitrogen coordDefTarget = new CoordinateDefinitionNitrogen(this, boxTarget, primitive, basis, space);
		coordDefTarget.setIsAlpha();
		coordDefTarget.setOrientationVectorAlpha(space);
		coordDefTarget.initializeCoordinates(nCells);
        
		boxTarget.setBoundary(boundaryTarget);
		double rC =9.0;//box.getBoundary().getBoxSize().getX(0)*0.5;
		System.out.println("Truncation Radius: " + rC);
		P2Nitrogen potential = new P2Nitrogen(space, rC);
		potential.setBox(boxTarget);
		System.out.println("Box Dimension(before): " + boxTarget.getBoundary().getBoxSize().toString());
		final IVector initBox = space.makeVector(new double[]{boxTarget.getBoundary().getBoxSize().getX(0),
															  boxTarget.getBoundary().getBoxSize().getX(1),
															  boxTarget.getBoundary().getBoxSize().getX(2)});
		coordDefTarget.setInitVolume(initBox);
		potentialMasterTarget.addPotential(potential, new ISpecies[]{species,species});
		
        IntegratorMC integratorTarget = new IntegratorMC(potentialMasterTarget, getRandom(), Kelvin.UNIT.toSim(temperature));
		MCMoveMoleculeCoupled move = new MCMoveMoleculeCoupled(potentialMasterTarget,getRandom(),space);
		move.setBox(boxTarget);
		move.setPotential(potential);
		
		MCMoveRotateMolecule3D rotate = new MCMoveRotateMolecule3D(potentialMasterTarget, getRandom(), space);
		rotate.setBox(boxTarget);
		
		MCMoveVolumeN2 mcMoveVolume = new MCMoveVolumeN2(this, potentialMasterTarget, space);
		mcMoveVolume.setSpecies(species);
		mcMoveVolume.setBox(boxTarget);
		mcMoveVolume.setXYZChange();
		mcMoveVolume.setPressure(Pascal.UNIT.toSim(0.0e9));
		
		uLatticeCorrec = mcMoveVolume.getLatticeCorrec();
		
		integratorTarget = new IntegratorMC(this, potentialMasterTarget);
		integratorTarget.getMoveManager().addMCMove(move);
		integratorTarget.getMoveManager().addMCMove(rotate);
		//integratorTarget.getMoveManager().addMCMove(mcMoveVolume);

		integratorTarget.setBox(boxTarget);
        integrators[1] = integratorTarget;
     
        MeterPotentialEnergy meterPE = new MeterPotentialEnergy(potentialMasterTarget);
        meterPE.setBox(boxTarget);
        latticeEnergy =  Kelvin.UNIT.fromSim(meterPE.getDataAsScalar())+ uLatticeCorrec;
        System.out.println("lattice energy per molecule in K: " +latticeEnergy/numMolecules);
      
        // HARMONIC
        boundaryHarmonic =  new BoundaryDeformablePeriodic(space,nCell*unitCellLength);
        boxHarmonic = new Box(boundaryHarmonic, space);
        addBox(boxHarmonic);
        boxHarmonic.setNMolecules(species, numMolecules);
        boxHarmonic.setBoundary(boundaryHarmonic);
        
        IntegratorMC integratorHarmonic = new IntegratorMC(null, random, 1.0); //null changed on 11/20/2009

        moveHarmonic = new MCMoveHarmonic(getRandom());
        integratorHarmonic.getMoveManager().addMCMove(moveHarmonic);
        integrators[0] = integratorHarmonic;
       
        CoordinateDefinitionNitrogen coordDefHarmonic = new CoordinateDefinitionNitrogen(this, boxHarmonic, primitive, basis, space);
    	coordDefHarmonic.setIsAlpha();
		coordDefHarmonic.setOrientationVectorAlpha(space);
		coordDefHarmonic.initializeCoordinates(nCells);
		coordDefHarmonic.setInitVolume(initBox);
        
		normalModes = new NormalModesFromFile(filename, space.D());
        normalModes.setTemperature(Kelvin.UNIT.toSim(temperature));
        
        WaveVectorFactory waveVectorFactory = normalModes.getWaveVectorFactory();
        waveVectorFactory.makeWaveVectors(boxHarmonic);
        moveHarmonic.setOmegaSquared(normalModes.getOmegaSquared(), waveVectorFactory.getCoefficients());
        moveHarmonic.setEigenVectors(normalModes.getEigenvectors());
        moveHarmonic.setWaveVectors(waveVectorFactory.getWaveVectors());
        moveHarmonic.setWaveVectorCoefficients(waveVectorFactory.getCoefficients());
        moveHarmonic.setCoordinateDefinition(coordDefHarmonic);
        moveHarmonic.setTemperature(Kelvin.UNIT.toSim(temperature));
        
        moveHarmonic.setBox(boxHarmonic);
        
        integratorHarmonic.setBox(boxHarmonic);
   
        // OVERLAP
        integratorOverlap = new IntegratorOverlap(new IntegratorBox[]{integratorHarmonic, integratorTarget});
        meterHarmonicEnergy = new MeterHarmonicEnergy(coordDefTarget, normalModes);
        MeterBoltzmannTarget meterTarget = new MeterBoltzmannTarget(integratorTarget, meterHarmonicEnergy);
        meterTarget.setLatticeEnergy(latticeEnergy);
        meters[1] = meterTarget;
        setAccumulator(new AccumulatorVirialOverlapSingleAverage(10, 11, false), 1);
        
        MeterBoltzmannHarmonic meterHarmonic = new MeterBoltzmannHarmonic(moveHarmonic, potentialMasterTarget);
        meterHarmonic.setTemperature(Kelvin.UNIT.toSim(temperature));
        meterHarmonic.setLatticeEnergy(latticeEnergy);
        meters[0] = meterHarmonic;
        setAccumulator(new AccumulatorVirialOverlapSingleAverage(10, 11, true), 0);
        
        setRefPref(1.0, 30);
     
        activityIntegrate = new ActivityIntegrate(integratorOverlap);
        
        getController().addAction(activityIntegrate);
    }

    public void setRefPref(double refPrefCenter, double span) {
        refPref = refPrefCenter;
        accumulators[0].setBennetParam(refPrefCenter,span);
        accumulators[1].setBennetParam(refPrefCenter,span);

    }

    public void setAccumulator(AccumulatorVirialOverlapSingleAverage newAccumulator, int iBox) {

        accumulators[iBox] = newAccumulator;
    
        newAccumulator.setBlockSize(200); // setting the block size = 300
        
        if (accumulatorPumps[iBox] == null) {
            accumulatorPumps[iBox] = new DataPump(meters[iBox],newAccumulator);
            IntegratorListenerAction pumpListener = new IntegratorListenerAction(accumulatorPumps[iBox]);
            integrators[iBox].getEventManager().addListener(pumpListener);
            if (iBox == 1) {
            	if (boxTarget.getMoleculeList().getMoleculeCount()==32){
            		
            	    pumpListener.setInterval(100);
            	
            	} else if (boxTarget.getMoleculeList().getMoleculeCount()==108){
                
            	    pumpListener.setInterval(300);
            	} else 
            	    pumpListener.setInterval(500);
                    //pumpListener.setInterval(boxTarget.getMoleculeList().getMoleculeCount());
            }
        }
        else {
            accumulatorPumps[iBox].setDataSink(newAccumulator);
        }
        if (integratorOverlap != null && accumulators[0] != null && accumulators[1] != null) {
            dsvo = new DataSourceVirialOverlap(accumulators[0],accumulators[1]);
            integratorOverlap.setDSVO(dsvo);
        }
    }
    
    public void setRefPref(double newRefPref) {
        System.out.println("setting ref pref (explicitly) to "+newRefPref);
        setAccumulator(new AccumulatorVirialOverlapSingleAverage(1,true),0);
        setAccumulator(new AccumulatorVirialOverlapSingleAverage(1,false),1);
        setRefPref(newRefPref,1);
    }
    
    public void initRefPref(String fileName, long initSteps) {
        // refPref = -1 indicates we are searching for an appropriate value
        refPref = -1.0;
        if (fileName != null) {
            try { 
                FileReader fileReader = new FileReader(fileName);
                BufferedReader bufReader = new BufferedReader(fileReader);
                String refPrefString = bufReader.readLine();
                refPref = Double.parseDouble(refPrefString);
                bufReader.close();
                fileReader.close();
                System.out.println("setting ref pref (from file) to "+refPref);
                setAccumulator(new AccumulatorVirialOverlapSingleAverage(1,true),0);
                setAccumulator(new AccumulatorVirialOverlapSingleAverage(1,false),1);
                setRefPref(refPref,1);
            }
            catch (IOException e) {
                // file not there, which is ok.
            }
        }
        
        if (refPref == -1) {
            // equilibrate off the lattice to avoid anomolous contributions
            activityIntegrate.setMaxSteps(initSteps/2);
            getController().actionPerformed();
            getController().reset();
            System.out.println("target equilibration finished");

            setAccumulator(new AccumulatorVirialOverlapSingleAverage(41,true),0);
            setAccumulator(new AccumulatorVirialOverlapSingleAverage(41,false),1);
            setRefPref(1,200);
            activityIntegrate.setMaxSteps(initSteps);
            getController().actionPerformed();
            getController().reset();

            int newMinDiffLoc = dsvo.minDiffLocation();
            refPref = accumulators[0].getBennetAverage(newMinDiffLoc)
                /accumulators[1].getBennetAverage(newMinDiffLoc);
            if (Double.isNaN(refPref) || refPref == 0 || Double.isInfinite(refPref)) {
                throw new RuntimeException("Simulation failed to find a valid ref pref");
            }
            System.out.println("setting ref pref to "+refPref);
            
            setAccumulator(new AccumulatorVirialOverlapSingleAverage(11,true),0);
            setAccumulator(new AccumulatorVirialOverlapSingleAverage(11,false),1);
            setRefPref(refPref,5);

            // set refPref back to -1 so that later on we know that we've been looking for
            // the appropriate value
            refPref = -1;
            getController().reset();
        }

    }
    
    public void equilibrate(String fileName, long initSteps) {
        // run a short simulation to get reasonable MC Move step sizes and
        // (if needed) narrow in on a reference preference
        activityIntegrate.setMaxSteps(initSteps);

        for (int i=0; i<2; i++) {
            if (integrators[i] instanceof IntegratorMC) ((IntegratorMC)integrators[i]).getMoveManager().setEquilibrating(true);
        }
        getController().actionPerformed();
        getController().reset();
        for (int i=0; i<2; i++) {
            if (integrators[i] instanceof IntegratorMC) ((IntegratorMC)integrators[i]).getMoveManager().setEquilibrating(false);
        }

        if (refPref == -1) {
            int newMinDiffLoc = dsvo.minDiffLocation();
            refPref = accumulators[0].getBennetAverage(newMinDiffLoc)
                /accumulators[1].getBennetAverage(newMinDiffLoc);
            System.out.println("setting ref pref to "+refPref+" ("+newMinDiffLoc+")");
            setAccumulator(new AccumulatorVirialOverlapSingleAverage(1,true),0);
            
            System.out.println("block size (equilibrate) "+accumulators[0].getBlockSize());
            
            setAccumulator(new AccumulatorVirialOverlapSingleAverage(1,false),1);
            setRefPref(refPref,1);
            if (fileName != null) {
                try {
                    FileWriter fileWriter = new FileWriter(fileName);
                    BufferedWriter bufWriter = new BufferedWriter(fileWriter);
                    bufWriter.write(String.valueOf(refPref)+"\n");
                    bufWriter.close();
                    fileWriter.close();
                }
                catch (IOException e) {
                    throw new RuntimeException("couldn't write to refpref file");
                }
            }
        }
        else {
            dsvo.reset();
        }
    }

    /**
     * @param args filename containing simulation parameters
     * @see SimOverlapNitrogenModel.SimOverlapParam
     */
    public static void main(String[] args) {
        //set up simulation parameters
        SimOverlapParam params = new SimOverlapParam();
        String inputFilename = null;
        if (args.length > 0) {
            inputFilename = args[0];
        }
        if (inputFilename != null) {
            ReadParameters readParameters = new ReadParameters(inputFilename, params);
            readParameters.readParameters();
        }
        int D = 3;
        long numSteps = params.numSteps;
        final int numMolecules = params.numMolecules;
        double temperature = params.temperature;
        String filename = params.filename;
        if (filename.length() == 0) {
        	System.err.println("Need input files!!!");
        	if(temperature <1.0){
    			filename = "alphaN2_nA"+numMolecules+"_T0"+(int)(temperature*10);
    			
    		} else {
    			filename = "alphaN2_nA"+numMolecules+"_ConstCT"+Math.round(temperature);
    		}
        }
        //String refFileName = args.length > 0 ? filename+"_ref" : null;
        String refFileName = filename+"_ref";
        
    	System.out.println("Running alpha-N2 crystal structure overlap-sampling simulation with " + numSteps + " steps" );
		System.out.println("num Molecules: " + numMolecules+ " ; temperature: " + temperature+"K\n");
		System.out.println((numSteps/1000)+" total steps of 1000");
        System.out.println("output data to "+filename);

        //instantiate simulation
        SimOverlapNitrogenModel sim = new SimOverlapNitrogenModel(Space.getInstance(D), numMolecules, temperature, filename);
       
        //start simulation
        sim.integratorOverlap.setNumSubSteps(1000);
        numSteps /= 1000;

        sim.initRefPref(refFileName, numSteps/20);
        if (Double.isNaN(sim.refPref) || sim.refPref == 0 || Double.isInfinite(sim.refPref)) {
            throw new RuntimeException("Simulation failed to find a valid ref pref");
        }
        System.out.flush();
        
        sim.equilibrate(refFileName, numSteps/10);
        if (Double.isNaN(sim.refPref) || sim.refPref == 0 || Double.isInfinite(sim.refPref)) {
            throw new RuntimeException("Simulation failed to find a valid ref pref");
        }
       
        System.out.println("equilibration finished");
        System.out.flush();
     
        final long startTime = System.currentTimeMillis();
        System.out.println("Start Time: " + startTime);
       
        sim.activityIntegrate.setMaxSteps(numSteps);
        sim.getController().actionPerformed();
        
        int totalCells = 1;
        for (int i=0; i<D; i++) {
            totalCells *= sim.nCell;
        }
        double  AHarmonic = CalcHarmonicA.doit(sim.normalModes, D, Kelvin.UNIT.toSim(temperature), numMolecules);
        System.out.println("Harmonic-reference free energy in K, A: "+Kelvin.UNIT.fromSim(AHarmonic) + " " + Kelvin.UNIT.fromSim(AHarmonic)/numMolecules);
        System.out.println(" ");
        
        System.out.println("final reference optimal step frequency "+sim.integratorOverlap.getStepFreq0()
        		+" (actual: "+sim.integratorOverlap.getActualStepFreq0()+")");
              
        double ratio = sim.dsvo.getDataAsScalar();
        double error = sim.dsvo.getError();
        
        System.out.println("\nratio average: "+ratio+" ,error: "+error);
        System.out.println("free energy difference in K: "+(-temperature*Kelvin.UNIT.fromSim(Math.log(ratio)))
        												  +" ,error: "+temperature*Kelvin.UNIT.fromSim((error/ratio)));
        System.out.println("target free energy in K: "+(Kelvin.UNIT.fromSim(AHarmonic)-temperature*Kelvin.UNIT.fromSim(Math.log(ratio))));
        System.out.println("target free energy per particle in K: "+ (Kelvin.UNIT.fromSim(AHarmonic)-temperature*Kelvin.UNIT.fromSim(Math.log(ratio)))/numMolecules 
        		+" ;error: "+temperature*Kelvin.UNIT.fromSim((error/ratio))/numMolecules);
        DataGroup allYourBase = (DataGroup)sim.accumulators[0].getData(sim.dsvo.minDiffLocation());
        double betaFAW = -Math.log(((DataDoubleArray)allYourBase.getData(AccumulatorAverage.StatType.AVERAGE.index)).getData()[1]);
        System.out.println("harmonic ratio average: "+((DataDoubleArray)allYourBase.getData(AccumulatorAverage.StatType.AVERAGE.index)).getData()[1]
                          +" stdev: "+((DataDoubleArray)allYourBase.getData(AccumulatorAverage.StatType.STANDARD_DEVIATION.index)).getData()[1]
                          +" error: "+((DataDoubleArray)allYourBase.getData(AccumulatorAverage.StatType.ERROR.index)).getData()[1]);
        
        allYourBase = (DataGroup)sim.accumulators[1].getData(sim.dsvo.minDiffLocation());
        double betaFBW = -Math.log(((DataDoubleArray)allYourBase.getData(AccumulatorAverage.StatType.AVERAGE.index)).getData()[1]);
        System.out.println("target ratio average: "+((DataDoubleArray)allYourBase.getData(AccumulatorAverage.StatType.AVERAGE.index)).getData()[1]
                          +" stdev: "+((DataDoubleArray)allYourBase.getData(AccumulatorAverage.StatType.STANDARD_DEVIATION.index)).getData()[1]
                          +" error: "+((DataDoubleArray)allYourBase.getData(AccumulatorAverage.StatType.ERROR.index)).getData()[1]);
        
        
        long endTime = System.currentTimeMillis();
        System.out.println("End Time: " + endTime);
        System.out.println("Time taken: " + (endTime - startTime));
    
        
		if(false){
			SimulationGraphic simGraphic = new SimulationGraphic(sim, sim.space, sim.getController());
		    simGraphic.getDisplayBox(sim.boxHarmonic).setPixelUnit(new Pixel(50));
		    simGraphic.makeAndDisplayFrame("Alpha-Phase Nitrogen Crystal Structure");
			sim.activityIntegrate.setMaxSteps(numSteps);
			//sim.getController().actionPerformed();
		}
        
    }

    private static final long serialVersionUID = 1L;
    public IntegratorOverlap integratorOverlap;
    public DataSourceVirialOverlap dsvo;
    public IntegratorBox[] integrators;
    public ActivityIntegrate activityIntegrate;
    public IBox boxTarget, boxHarmonic;
    public Boundary boundaryTarget, boundaryHarmonic;
    public NormalModes normalModes;
    public Primitive primitive, primitiveUnitCell;
    public double refPref;
    public AccumulatorVirialOverlapSingleAverage[] accumulators;
    public DataPump[] accumulatorPumps;
    public IEtomicaDataSource[] meters;
    public String fname;
    protected MCMoveHarmonic moveHarmonic;
    protected PotentialMaster potentialMasterTarget;
    protected MeterHarmonicEnergy meterHarmonicEnergy;
    protected double latticeEnergy, uLatticeCorrec;
    protected SpeciesN2 species;
    protected int nCell;
    
    /**
     * Inner class for parameters understood by the HSMD3D constructor
     */
    public static class SimOverlapParam extends ParameterBase {
        public int numMolecules =32;
        public long numSteps = 1000000;
        public String filename = "alphaN2_nA32_ConstCT25";
        public double temperature =25;
    }
}
