package etomica.conjugategradient;

import etomica.action.Activity;
import etomica.api.IAtomLeaf;
import etomica.api.IAtomList;
import etomica.api.IBox;
import etomica.api.IMolecule;
import etomica.api.IVector;
import etomica.atom.AtomLeafAgentManager;
import etomica.atom.AtomLeafAgentManager.AgentSource;
import etomica.atom.iterator.IteratorDirective;
import etomica.data.meter.MeterPotentialEnergy;
import etomica.integrator.IntegratorVelocityVerlet;
import etomica.normalmode.CoordinateDefinition;
import etomica.potential.PotentialCalculationForceSum;
import etomica.potential.PotentialMaster;
import etomica.space.ISpace;
import etomica.util.FunctionMultiDimensionalDifferentiable;

public class DerivativeEnergyFunction implements FunctionMultiDimensionalDifferentiable{

	/*
	 * This class is developed to calculate for 
	 * 	the first derivative of energy function ANALYTICALLY.
	 * 
	 * @author Tai Tan
	 */
	
	protected IBox box;
	protected MeterPotentialEnergy meterEnergy;
	protected PotentialMaster potentialMaster;
	protected IteratorDirective allAtoms;
	protected PotentialCalculationForceSum forceSum;
	protected AtomLeafAgentManager agentManager;
	protected Activity activity;
	protected CoordinateDefinition coordinateDefinition;
	protected double[] fPrime;
	protected IVector moleculeForce;
	protected FunctionMultiDimensionalDifferentiable fFunction;
	
	public DerivativeEnergyFunction(IBox box, PotentialMaster potentialMaster, ISpace space){
		this.box = box;
		this.potentialMaster = potentialMaster;
		meterEnergy = new MeterPotentialEnergy(potentialMaster);
		allAtoms = new IteratorDirective();
		forceSum = new PotentialCalculationForceSum();
		
		MyAgentSource source = new MyAgentSource(space);
		agentManager = new AtomLeafAgentManager(source, box);
		forceSum.setAgentManager(agentManager);
		moleculeForce = space.makeVector();
		
		/*
		 * Dimensions of a study system is three-times the number of atoms
		 */
	}
	

	
	public CoordinateDefinition getCoordinateDefinition(){
		return coordinateDefinition;
	}
	
	public void setCoordinateDefinition (CoordinateDefinition coordinateDefinition){
		this.coordinateDefinition = coordinateDefinition;
		fPrime = new double[coordinateDefinition.getCoordinateDim()];
	}
	
	public double f(double[] newU){
		for (int cell=0; cell<coordinateDefinition.getBasisCells().length; cell++){
			IAtomList molecules = coordinateDefinition.getBasisCells()[cell].molecules;
			coordinateDefinition.setToU(molecules, newU);
		}
		
		return meterEnergy.getDataAsScalar();
	}
	
	public double df(int[] d, double[] u){
		
		/*
		 * Index is assigned to be the number of molecules in a box
		 * fPrime[number]; number equals the degree of freedom, 
		 * 	where dx, dy, and dz to each molecule
		 */
		
		forceSum.reset();
		
		for (int cell=0; cell<coordinateDefinition.getBasisCells().length; cell++){
			IAtomList molecules = coordinateDefinition.getBasisCells()[cell].molecules;
			coordinateDefinition.setToU(molecules, u);
		}
		
		/*
		 * d only takes in array that compute first-order derivative w.r.t. to corresponding n-th dimension
		 *  for example, d=new double{1, 0, 0} or {0, 0, 1}, which means first-order differentiation to 
		 *  first- and third- dimension respectively. 
		 */
		
		int index =0;
		double check =0;
		
		for (int i =0; i <d.length; i++){
			check += d[i];
			
			if (d[i]==1){
				index = i;
			}
		} 
		
		if (check != 1){
			throw new IllegalArgumentException("The function MUST and CAN only compute first-order derivative!!");
		}
		
		int j=0;
		potentialMaster.calculate(box, allAtoms, forceSum);
		
		IAtomList molecules = coordinateDefinition.getBasisCells()[0].molecules;
		
		for (int m=0; m<molecules.getAtomCount(); m++){
				
			if (m==0){
				for (int k=0; k<3; k++){
					fPrime[j+k] = 0;
					
					if (index == j+k){
						return fPrime[j+k];
					}
				}
					
			} else {
				
				IAtomList childList = ((IMolecule)molecules.getAtom(m)).getChildList();
				
				moleculeForce.E(0); //initialize moleculeForce to zero
				
				for (int r=0; r<childList.getAtomCount(); r++){
					moleculeForce.PE(((IntegratorVelocityVerlet.MyAgent)agentManager.getAgent((IAtomLeaf)childList.getAtom(r)))
							   .force);
				}
					
				
				for (int k=0; k<3; k++){
					fPrime[j+k] = moleculeForce.x(k);
						
					if (index == j+k){
						return fPrime[j+k];
					}
				}
			}
			j += coordinateDefinition.getCoordinateDim() /molecules.getAtomCount();
		}
		
		return fPrime[index];
	}
	
	public int getDimension(){
		return fFunction.getDimension();
	}
	
	public void getScalarEnergy(){
		meterEnergy.setBox(box);
		System.out.println("The energy of the system is: "+meterEnergy.getDataAsScalar());
	}
	
	
	
	public static class MyAgentSource implements AgentSource{
		
		public MyAgentSource(ISpace space){
			this.space = space;
		}
		
		public void releaseAgent(Object agent, IAtomLeaf atom){}
		public Class getAgentClass(){
			return IntegratorVelocityVerlet.MyAgent.class;
		}
		public Object makeAgent(IAtomLeaf atom){
			
			return new IntegratorVelocityVerlet.MyAgent(space);
			}
		protected ISpace space;
	}

}
