package etomica.models.nitrogen;

import etomica.api.IBox;
import etomica.api.IMoleculeList;
import etomica.api.ISpecies;
import etomica.api.IVectorMutable;
import etomica.box.Box;
import etomica.data.DataInfo;
import etomica.data.IData;
import etomica.data.IDataInfo;
import etomica.data.types.DataDouble;
import etomica.lattice.BravaisLatticeCrystal;
import etomica.lattice.crystal.Basis;
import etomica.lattice.crystal.BasisHcp;
import etomica.lattice.crystal.Primitive;
import etomica.lattice.crystal.PrimitiveTriclinic;
import etomica.models.nitrogen.LatticeSumCrystalMolecular.DataGroupLSC;
import etomica.normalmode.BasisBigCell;
import etomica.potential.PotentialMaster;
import etomica.simulation.Simulation;
import etomica.space.ISpace;
import etomica.space3d.Space3D;
import etomica.units.Degree;
import etomica.units.Energy;
import etomica.util.FunctionGeneral;



/**
 * 
 * @author Tai Boon Tan
 *
 */
public class HarmonicBetaNitrogenModelLatticeSum extends Simulation{

	
	public HarmonicBetaNitrogenModelLatticeSum(ISpace space, int numMolecule, double density, int iLayer) {
		super(space);
		this.space = space;
		
		potentialMaster = new PotentialMaster();
				
	  	double ratio = 1.631;
		double aDim = Math.pow(4.0/(Math.sqrt(3.0)*ratio*density), 1.0/3.0);
		double cDim = aDim*ratio;
		//System.out.println("aDim: " + aDim + " ;cDim: " + cDim);
		
		int [] nCells = new int[]{1,2,1};
		Basis basisHCP = new BasisHcp();
		BasisBigCell basis = new BasisBigCell(space, basisHCP, nCells);
        
		ConformationNitrogen conformation = new ConformationNitrogen(space);
		SpeciesN2 species = new SpeciesN2(space);
		species.setConformation(conformation);
		addSpecies(species);
		
		SpeciesN2B ghostSpecies = new SpeciesN2B(space);
		ghostSpecies.setConformation(conformation);
		addSpecies(ghostSpecies);
		
		box = new Box(space);
		addBox(box);
		box.setNMolecules(species, numMolecule);		
		
		IBox ghostBox = new Box(space);
		addBox(ghostBox);
		ghostBox.setNMolecules(ghostSpecies, 1);
		
		Primitive primitive = new PrimitiveTriclinic(space, aDim, 2*aDim, cDim, Math.PI*(90/180.0),Math.PI*(90/180.0),Math.PI*(120/180.0));

		coordinateDef = new CoordinateDefinitionNitrogen(this, box, primitive, basis, space);
		coordinateDef.setIsBetaLatticeSum();
		coordinateDef.setOrientationVectorBeta(space);
		coordinateDef.initializeCoordinates(new int[]{1,1,1});
		
	//	box.setBoundary(boundary);
		double rCScale = 0.475;
		double rC = 1000;//box.getBoundary().getBoxSize().getX(0)*rCScale;
		//System.out.println("Truncation Radius (" + rCScale +" Box Length): " + rC);
		
		final P2Nitrogen potential = new P2Nitrogen(space, rC);
		potential.setBox(box);
		potential.setEnablePBC(false);
		
		FunctionGeneral function = new FunctionGeneral() {
			public IData f(Object obj) {
				data.x = potential.energy((IMoleculeList)obj);
				return data;
			}
			public IDataInfo getDataInfo() {
				return dataInfo;
			}
			final DataInfo dataInfo = new DataDouble.DataInfoDouble("Lattice energy", Energy.DIMENSION);
			final DataDouble data = new DataDouble();
		};
		
		BravaisLatticeCrystal lattice = new BravaisLatticeCrystal(primitive, basis);
		LatticeSumCrystalMolecular latticeSum = new LatticeSumCrystalMolecular(lattice, coordinateDef, ghostBox);
		latticeSum.setMaxLatticeShell(iLayer);
		
		double sum = 0;
	    double basisDim = lattice.getBasis().getScaledCoordinates().length;
		DataGroupLSC data = (DataGroupLSC)latticeSum.calculateSum(function);
        for(int j=0; j<basisDim; j++) {
            for(int jp=0; jp<basisDim; jp++) {
                sum += ((DataDouble)data.getDataReal(j,jp)).x; 
            }
        }
        System.out.println("lattice:  " + 0.5*sum/basisDim);
		//System.exit(1);
		
		potentialMaster.addPotential(potential, new ISpecies[]{species, species});

	}
	
	public static void main (String[] args){
		
		int numMolecule =4;
		double density = 0.025;
		for (int i=2; i<30; i++){	
			System.out.print(i+" ");
			HarmonicBetaNitrogenModelLatticeSum test = new HarmonicBetaNitrogenModelLatticeSum(Space3D.getInstance(3), numMolecule, density, i);
		}
		System.exit(1);
		
//		CalcNumerical2ndDerivative cm2ndD = new CalcNumerical2ndDerivative(test.box, test.potentialMaster,test.coordinateDef);
//		
//		double[] newU = new double[test.coordinateDef.getCoordinateDim()];
//		
//		String fname = new String (numMolecule+"_2ndDer");
//		try {
//			FileWriter fileWriter = new FileWriter(fname);
//			
//			double value = 0;
//			for (int i=0; i<newU.length; i++){
//				for (int j=0; j<newU.length; j++){
//					value = cm2ndD.d2phi_du2(new int[]{i,j}, newU);
//					
//					if(Math.abs(value) < 1e-6){
//						value = 0.0;
//					}
//					fileWriter.write(value+ " ");
//				}
//				fileWriter.write("\n");
//			}
//			fileWriter.close();
//			
//		} catch (IOException e) {
//			
//		}
	
//		System.out.println("d2phi_du2: " + cm2ndD.d2phi_du2(new int[]{5,54}, newU));
//		System.out.println("d2phi_du2: " + cm2ndD.d2phi_du2(new int[]{54,5}, newU));

	}
	
	
	protected Box box;
	protected ISpace space;
	protected CoordinateDefinitionNitrogen coordinateDef;
	protected PotentialMaster potentialMaster;
	private static final long serialVersionUID = 1L;
}
