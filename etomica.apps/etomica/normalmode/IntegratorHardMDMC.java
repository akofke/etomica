package etomica.normalmode;

import java.util.ArrayList;
import java.util.List;

import etomica.action.IAction;
import etomica.api.IBox;
import etomica.api.IPotentialMaster;
import etomica.api.ISimulation;
import etomica.exception.ConfigurationOverlapException;
import etomica.integrator.IntegratorHard;
import etomica.integrator.mcmove.MCMoveInsertDeleteBiased;
import etomica.space.ISpace;

/**
 * Custom DMD integrator that handles hybrid simulations with
 * insertion/deletions.
 *  
 * @author Andrew Schultz
 */
public class IntegratorHardMDMC extends IntegratorHard {
    protected MCMoveInsertDeleteBiased mcMoveID;
    protected List<IAction> thermostatActions;

    public IntegratorHardMDMC(ISimulation sim, IPotentialMaster potentialMaster, ISpace _space) {
        super(sim, potentialMaster, _space);
        thermostatActions = new ArrayList<IAction>();
    }
    
    public void setBox(IBox box) {
        super.setBox(box);
    }
    
    public void addThermostatAction(IAction a) {
        thermostatActions.add(a);
    }
    
    public void setMCMoveID(MCMoveInsertDeleteBiased mcMoveID) {
        this.mcMoveID = mcMoveID;
    }

    public void reset() {
        if (!thermostatting) {
            super.reset();
            return;
        }

        // we get here because HYBRID_MC thermostat calls reset.
        // neighbors should be fine, we just need to update the potential energy
        // IntegratorHard would reset collision times.  that will happen anyway from randomizeMomenta
        currentPotentialEnergy = meterPE.getDataAsScalar();
        if (currentPotentialEnergy == Double.POSITIVE_INFINITY) {
            System.err.println("overlap in configuration for "+box+" when resetting integrator");
            throw new ConfigurationOverlapException(box);
        }
        currentKineticEnergy = meterKE.getDataAsScalar();

    }
    
    public void doThermostat() {
        thermostatting = true;
        for (IAction a : thermostatActions) {
            a.actionPerformed();
        }
        super.doThermostat();
        thermostatting = false;
    }
}