/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package etomica.spin.heisenberg;

import etomica.api.IAtom;
import etomica.api.IAtomList;
import etomica.api.IBox;
import etomica.api.IPotentialMaster;
import etomica.api.IRandom;
import etomica.api.IVectorMutable;
import etomica.atom.iterator.AtomIterator;
import etomica.atom.iterator.AtomIteratorSinglet;
import etomica.data.meter.MeterPotentialEnergy;
import etomica.integrator.mcmove.MCMoveBox;
import etomica.space.ISpace;
import etomica.space.IVectorRandom;


/**
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 *
 * @author David Kofke
 *
 */
public class MCMoveSpinFlip extends MCMoveBox {

    /**
     * @param potentialMaster
     * @param random
     * @param space
     *
     */
    public MCMoveSpinFlip(IPotentialMaster potentialMaster, IRandom random, ISpace space) {
        super(potentialMaster);
        this.random = random;
        energyMeter = new MeterPotentialEnergy(potentialMaster);
        perParticleFrequency = true;
        energyMeter.setIncludeLrc(false);
        rOld = space.makeVector(); 
    }

    public void setBox(IBox p) {
        super.setBox(p);
        energyMeter.setBox(p);
    }
    
    /* (non-Javadoc)
     * @see etomica.integrator.MCMove#doTrial()
     */
    public boolean doTrial() {
        IAtomList leafList = box.getLeafList();
        atom = leafList.getAtom(random.nextInt(leafList.getAtomCount()));
        energyMeter.setTarget(atom);
        uOld = energyMeter.getDataAsScalar();
        rOld.E(atom.getPosition());
        
        ((IVectorRandom)atom.getPosition()).setRandomSphere(random);
        
        
        uNew = Double.NaN;
        return true;
    }

    /* (non-Javadoc)
     * @see etomica.integrator.MCMove#lnTrialRatio()
     */
    
    public double getA() {
        return 1.0;
    }

    /* (non-Javadoc)
     * @see etomica.integrator.MCMove#lnProbabilityRatio()
     */
    public double getB() {
        uNew = energyMeter.getDataAsScalar();
        return -(uNew - uOld);
    }

    /* (non-Javadoc)
     * @see etomica.integrator.MCMove#acceptNotify()
     */
    public void acceptNotify() {
        //nothing to do
    }

    /* (non-Javadoc)
     * @see etomica.integrator.MCMove#rejectNotify()
     */
    public void rejectNotify() {
        atom.getPosition().E(rOld);
    }

    /* (non-Javadoc)
     * @see etomica.integrator.MCMove#affectedAtoms(etomica.Box)
     */
    public AtomIterator affectedAtoms() {
        affectedAtomIterator.setAtom(atom);
        return affectedAtomIterator;
    }

    /* (non-Javadoc)
     * @see etomica.integrator.MCMove#energyChange(etomica.Box)
     */
    public double energyChange() {
        return uNew - uOld;
    }

    private static final long serialVersionUID = 1L;
    protected final IRandom random;
    protected final AtomIteratorSinglet affectedAtomIterator = new AtomIteratorSinglet();
    protected final MeterPotentialEnergy energyMeter;
    protected IAtom atom;
    protected double uOld;
    protected double uNew = Double.NaN;
    protected final IVectorMutable rOld;
}