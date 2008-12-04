package etomica.virial;

import etomica.action.AtomAction;
import etomica.api.IAtomPositioned;
import etomica.api.IAtomList;
import etomica.api.IBox;
import etomica.api.IMolecule;
import etomica.api.IPotentialMaster;
import etomica.api.IRandom;
import etomica.api.ISpecies;
import etomica.api.IVector;
import etomica.integrator.mcmove.MCMoveRotateMolecule3D;
import etomica.space.ISpace;

/**
 * MCMove for use in a Mayer sampling simulation that rotates all molecules in
 * a Box except the first molecule, which is never moved.  The angle of
 * rotation is the step size and can be tuned for some acceptance rate.
 */
public class MCMoveClusterRotateMoleculeMulti extends MCMoveRotateMolecule3D {

    /**
     * @param potentialMaster
     * @param space
     */
    public MCMoveClusterRotateMoleculeMulti(IPotentialMaster potentialMaster,
            IRandom random, ISpace _space) {
        super(potentialMaster, random, _space);
        this.space = _space;
        weightMeter = new MeterClusterWeight(potential);
    }

    public void setBox(IBox p) {
        super.setBox(p);
        weightMeter.setBox(p);
        IAtomList moleculeList = box.getMoleculeList();
        oldPositions = new IVector[moleculeList.getAtomCount()][];
        for (int i=0; i<moleculeList.getAtomCount(); i++) {
            molecule = (IMolecule)moleculeList.getAtom(i);
            oldPositions[i] = new IVector[molecule.getChildList().getAtomCount()];
            for (int j=0; j<oldPositions[i].length; j++) {
                oldPositions[i][j] = space.makeVector();
            }
        }
    }

    public boolean doTrial() {
        uOld = weightMeter.getDataAsScalar();
        boolean doRelax = false;
        if (trialCount-- == 0) {
            doRelax = true;
            trialCount = relaxInterval;
        }
        IAtomList moleculeList = box.getMoleculeList();
        for (int i=0; i<moleculeList.getAtomCount(); i++) {
            molecule = (IMolecule)moleculeList.getAtom(i);
            IAtomList leafAtoms = molecule.getChildList();
            r0.E(((ISpecies)molecule.getType()).getPositionDefinition().position(molecule));

            double dTheta = (2*random.nextDouble() - 1.0)*stepSize;
            rotationTensor.setAxial(random.nextInt(3),dTheta);

            for (int j=0; j<leafAtoms.getAtomCount(); j++) {
                oldPositions[i][j].E(((IAtomPositioned)leafAtoms.getAtom(j)).getPosition());
            }
            doTransform();

            if (doRelax && relaxAction != null) {
                relaxAction.actionPerformed(molecule);
            }
        }

        ((BoxCluster)box).trialNotify();
        uNew = weightMeter.getDataAsScalar();
        return true;
    }

    public double getB() {
        return 0.0;
    }
    
    public double getA() {
        return uNew/uOld;
    }
    
    public void acceptNotify() {
        super.acceptNotify();
        if (uNew == 0) {
            throw new RuntimeException("oops, accepted illegal configuration");
        }
        ((BoxCluster)box).acceptNotify();
        if (weightMeter.getDataAsScalar() == 0) {
            throw new RuntimeException("oops oops, accepted illegal configuration");
        }
    }
    
    public void rejectNotify() {
        IAtomList moleculeList = box.getMoleculeList();
        for (int i=0; i<moleculeList.getAtomCount(); i++) {
            molecule = (IMolecule)moleculeList.getAtom(i);
            IAtomList leafAtoms = molecule.getChildList();
            for (int j=0; j<leafAtoms.getAtomCount(); j++) {
                ((IAtomPositioned)leafAtoms.getAtom(j)).getPosition().E(oldPositions[i][j]);
            }
        }
        ((BoxCluster)box).rejectNotify();
        if (weightMeter.getDataAsScalar() == 0) {
            throw new RuntimeException("oops oops, reverted to illegal configuration");
        }
    }
    
    public void setRelaxAction(AtomAction action) {
        relaxAction = action;
    }
    
    private static final long serialVersionUID = 1L;
    private final MeterClusterWeight weightMeter;
    private IVector[][] oldPositions;
    private int trialCount, relaxInterval = 100;
    private AtomAction relaxAction;
    private final ISpace space;
}
