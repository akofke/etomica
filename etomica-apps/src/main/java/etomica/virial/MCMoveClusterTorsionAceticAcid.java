/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package etomica.virial;

import etomica.atom.IAtom;
import etomica.atom.IAtomList;
import etomica.integrator.mcmove.MCMoveMolecule;
import etomica.integrator.mcmove.MCMoveStepTracker;
import etomica.molecule.IMoleculeList;
import etomica.molecule.MoleculeArrayList;
import etomica.potential.PotentialMaster;
import etomica.space.Space;
import etomica.space.Vector;
import etomica.util.random.IRandom;

/**
 * An MC Move for cluster simulations that performs torsion moves on acetic acid.
 * The move is performed on all molecules in the Box.  
 * The move needs the torsion potential in order to choose appropriate
 * torsion angles.
 * Only the hydrogen site is rotating by 180 degree. 
 * 
 * @author Hye Min Kim
 */
public class MCMoveClusterTorsionAceticAcid extends MCMoveMolecule {
   
    /**
     * Constructor for MCMoveAtomMulti.
     * @param parentIntegrator
     * @param nAtoms number of atoms to move in a trial.  Number of atoms in
     * box should be at least one greater than this value (greater
     * because first atom is never moved)
     */
    public MCMoveClusterTorsionAceticAcid(PotentialMaster potentialMaster, Space space,
                                          IRandom random) {
        super(potentialMaster,random,space,1,Double.POSITIVE_INFINITY);//we don't need stepsize-> put 1
        ((MCMoveStepTracker)getTracker()).setTunable(false);
        vCO = space.makeVector();
        vOH = space.makeVector();
        selectedMolecules = new MoleculeArrayList();
    }

    //note that total energy is calculated
    public boolean doTrial() {
        IMoleculeList allMolecules = box.getMoleculeList();
        uOld = energyMeter.getDataAsScalar();
        wOld = ((BoxCluster)box).getSampleCluster().value((BoxCluster)box);
        selectedMolecules.clear();
        for(int i=0; i<allMolecules.getMoleculeCount(); i++) {
        	if (random.nextInt(2) == 0)continue;//random # can be 0 or 1. 50% chance of torsion move
        	selectedMolecules.add(allMolecules.getMolecule(i));
            IAtomList childList = allMolecules.getMolecule(i).getChildList();
            int numChildren = childList.getAtomCount();//5

            IAtom c = childList.getAtom(1);
            IAtom sBO = childList.getAtom(3);
            IAtom h = childList.getAtom(4);
            vCO.Ev1Mv2(sBO.getPosition(), c.getPosition());//vector CO
            vOH.Ev1Mv2(h.getPosition(), sBO.getPosition());//vector OH
            double lengthdr13 = vCO.squared();
            
            Vector project = space.makeVector();
            Vector secondaryDirection = space.makeVector();
            project.E(vCO);
            project.TE(vCO.dot(vOH)/lengthdr13);
            secondaryDirection.Ev1Mv2(project,vOH);
            secondaryDirection.TE(2);
            h.getPosition().PE(secondaryDirection);
            for (int k=0; k<numChildren; k++) {
                // shift the whole molecule so that the center of mass (or whatever
                // the position definition uses) doesn't change
            	//newCenter is needed to be changed to oldCenter
                IAtom atomk = childList.getAtom(k);
                atomk.getPosition().PEa1Tv1(-0.2, secondaryDirection);
            }
        }
        ((BoxCluster)box).trialNotify();
        wNew = ((BoxCluster)box).getSampleCluster().value((BoxCluster)box);
        uNew = energyMeter.getDataAsScalar();
        return true;
    }

    public void rejectNotify() {//only for the molecules which were moved in doTrial
        for(int i=0; i<selectedMolecules.getMoleculeCount(); i++) {
            IAtomList childList = selectedMolecules.getMolecule(i).getChildList();
            int numChildren = childList.getAtomCount();//5
            IAtom c = childList.getAtom(1);
            IAtom sBO = childList.getAtom(3);
            IAtom h = childList.getAtom(4);
            vCO.Ev1Mv2(sBO.getPosition(), c.getPosition());//vector CO
            vOH.Ev1Mv2(h.getPosition(), sBO.getPosition());//vector OH
            double lengthdr13 = vCO.squared();
            
            Vector project = space.makeVector();
            Vector secondaryDirection = space.makeVector();
            project.E(vCO);
            project.TE(vCO.dot(vOH)/lengthdr13);
            secondaryDirection.Ev1Mv2(project,vOH);
            secondaryDirection.TE(2);
            h.getPosition().PE(secondaryDirection);
            for (int k=0; k<numChildren; k++) {
                IAtom atomk = childList.getAtom(k);
                atomk.getPosition().PEa1Tv1(-0.2, secondaryDirection);
            }
        }
        ((BoxCluster)box).rejectNotify();
    }

    public void acceptNotify() {
        ((BoxCluster)box).acceptNotify();
    }
    
    public double getB() {
        return -(uNew - uOld);
    }
    
    public double getA() {
    	return wNew/wOld;
    }
	
    private static final long serialVersionUID = 1L;
    protected final Vector vCO,vOH;
    protected double wOld, wNew;
    protected final MoleculeArrayList selectedMolecules;
}
