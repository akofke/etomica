package etomica.models.nitrogen;
import etomica.action.MoleculeChildAtomAction;
import etomica.api.IAtom;
import etomica.api.IAtomList;
import etomica.api.IBox;
import etomica.api.IPotentialMaster;
import etomica.api.IRandom;
import etomica.api.IVectorMutable;
import etomica.atom.AtomPositionGeometricCenter;
import etomica.atom.IAtomPositionDefinition;
import etomica.integrator.mcmove.MCMoveMolecule;
import etomica.paracetamol.AtomActionTransformed;
import etomica.space.ISpace;
import etomica.space.RotationTensor;
import etomica.space3d.RotationTensor3D;
import etomica.space3d.Tensor3D;


/**
 * MCMoveRotate that moves the molecules according to pre-set constraint angle
 *  which is given by the parameter "angle"
 *  
 * This class does not consider rotational energy, so the variable "energyChange"
 *  always returns zero
 *  
 * getB() always returns positive one is to ensure the proposed move is always 
 *  accepted
 * 
 * @author Tai Boon Tan
 *
 */
public class MCMoveRotateMolecule3DConstraint extends MCMoveMolecule {
    
    private static final long serialVersionUID = 2L;
    protected transient IVectorMutable r0;
    protected transient RotationTensor rotationTensor;
    protected IAtomPositionDefinition positionDefinition;
    protected double constraintAngle;
    protected CoordinateDefinitionNitrogen coordinateDef;
    protected IVectorMutable[][] initMolecOrientation;
    protected IVectorMutable molecOrientation, rotationAxis, workVector;
	protected RotationTensor3D rotation;
	protected Tensor3D tensor;
    protected final MoleculeChildAtomAction atomGroupAction;
    
    public MCMoveRotateMolecule3DConstraint(IPotentialMaster potentialMaster, IRandom random,
    		                      ISpace _space, double angle, CoordinateDefinitionNitrogen coordinateDef, IBox box) {
        super(potentialMaster, random, _space, 0.5*Math.PI, Math.PI);
        rotationTensor = _space.makeRotationTensor();
        r0 = _space.makeVector();
        positionDefinition = new AtomPositionGeometricCenter(space);
        constraintAngle = angle;
        this.coordinateDef = coordinateDef;
        
        int numMolec = box.getMoleculeList().getMoleculeCount();
     	initMolecOrientation = new IVectorMutable[numMolec][3];
     	molecOrientation = space.makeVector();
     	rotationAxis = space.makeVector();
     	workVector = space.makeVector();
     	
    	tensor = new Tensor3D(new double[][]{{1.0, 0.0, 0.0},{0.0, 1.0, 0.0},{0.0, 0.0, 1.0}});
		rotation = new RotationTensor3D();
		rotation.E(tensor);
    	/*
		 * initializing the initial orientation of the molecule
		 */
		for (int i=0; i<numMolec; i++){
			initMolecOrientation[i] = space.makeVectorArray(3);
			initMolecOrientation[i] = coordinateDef.getMoleculeOrientation(box.getMoleculeList().getMolecule(i));
		}
		atomGroupAction = new MoleculeChildAtomAction(new AtomActionTransformed(space));
	        
    }
     
    public boolean doTrial() {
//        System.out.println("doTrial MCMoveRotateMolecule called");
        
        if(box.getMoleculeList().getMoleculeCount()==0) {molecule = null; return false;}
        int iMolecule = 0;//random.nextInt(box.getMoleculeList().getMoleculeCount());
        
        energyMeter.setTarget(molecule);
        uOld = energyMeter.getDataAsScalar();
        if(Double.isInfinite(uOld)) {
            throw new RuntimeException("Overlap in initial state");
        }
        
        molecule = coordinateDef.getBox().getMoleculeList().getMolecule(iMolecule);
        r0.E(positionDefinition.position(molecule));
        
        IVectorMutable leafPos0 = molecule.getChildList().getAtom(0).getPosition();
		IVectorMutable leaftPos1 = molecule.getChildList().getAtom(1).getPosition();
		
		molecOrientation.Ev1Mv2(leaftPos1, leafPos0);
		molecOrientation.normalize();
		
		workVector.Ev1Mv2(molecOrientation, initMolecOrientation[iMolecule][0]);
		
		double check = Math.sqrt(workVector.squared());
		double angleMol;
		if(check < 1e-7){
			angleMol = 0.0;
		} else {
			angleMol = Math.acos(molecOrientation.dot(initMolecOrientation[iMolecule][0]));
		}
		
		if(check !=0.0){
            doTransformToInitial(iMolecule, angleMol);
	        
		}

        double randomValue = (2*random.nextDouble() - 1.0);
        double constant = (constraintAngle/180.0) *Math.PI;
        double dTheta = randomValue*constant;
        //System.out.println("{MCMOVE} angle: " +Degree.UNIT.fromSim(dTheta));
        rotationTensor.setAxial(r0.getD() == 3 ? random.nextInt(3) : 2,dTheta);

        r0.E(positionDefinition.position(molecule));
		doTransform();
        
        energyMeter.setTarget(molecule);
        return true;
    }
    
    protected void doTransform() {
        IAtomList childList = molecule.getChildList();
        for (int iChild = 0; iChild<childList.getAtomCount(); iChild++) {
            IAtom a = childList.getAtom(iChild);
            IVectorMutable r = a.getPosition();
            r.ME(r0);
            box.getBoundary().nearestImage(r);
            rotationTensor.transform(r);
            r.PE(r0);
        }
    }
    
    protected void doTransformToInitial(int iMolecule, double angleMol) {
        IAtomList childList = molecule.getChildList();
        for (int iChild = 0; iChild<childList.getAtomCount(); iChild++) {
            IAtom a = childList.getAtom(iChild);
            IVectorMutable r = a.getPosition();
            r.ME(r0);
        }
        
		rotationAxis.E(molecOrientation);
		rotationAxis.XE(initMolecOrientation[iMolecule][0]);
		rotationAxis.normalize();
      
		rotation.setRotationAxis(rotationAxis, angleMol);
        ((AtomActionTransformed)atomGroupAction.getAtomAction()).setTransformationTensor(rotation);
        atomGroupAction.actionPerformed(molecule);
        
        for (int iChild = 0; iChild<childList.getAtomCount(); iChild++) {
            IAtom a = childList.getAtom(iChild);
            IVectorMutable r = a.getPosition();
            r.PE(r0);
        }
        
    }
    
    public void rejectNotify() {
        rotationTensor.invert();
        doTransform();
    }
    
    public double getB() {
//    	double energy = energyMeter.getDataAsScalar();
//    	if(Double.isInfinite(energy)){
//    		return -1.0;
//    	}
        return 1.0; //always accept the rotational move, See the acceptance criteria in IntegratorMC
    }
    
    public double energyChange() { return 0.0;}
}
