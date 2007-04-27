package etomica.graphics;

import etomica.action.Action;
import etomica.action.ActionGroupSeries;
import etomica.action.SimulationRestart;
import etomica.action.activity.Controller;
import etomica.atom.SpeciesAgent;
import etomica.modifier.ModifierNMolecule;
import etomica.simulation.prototypes.HSMD2D;

/**
 * Slider that selects the number of atoms of a given species in a phase.
 *
 * @author David Kofke
 */
public class DeviceNSelector extends DeviceSlider {
    
    public DeviceNSelector() {
        this(null);
    }
    
    public DeviceNSelector(Controller controller) {
        super(controller);
    }

    public void setResetAction(Action newResetAction) {
        resetAction = newResetAction;
        if (modifyAction != null) {
            targetAction = new ActionGroupSeries(new Action[]{modifyAction,resetAction});
        }
    }

    /**
     * Returns the action used to "reset" the simulation after changing the 
     * number of molecules, SimulationRestart by default.
     */
    public Action getResetAction() {
        return resetAction;
    }
    
    public void setSpeciesAgent(SpeciesAgent agent) {
	    setMinimum(0);
        int max = 60;
        if (agent.getNMolecules() > max) max = agent.getNMolecules();
	    setMaximum(max);
	    slider.setSnapToTicks(false);
	    slider.setMajorTickSpacing(10);
	    graphic(null).setSize(new java.awt.Dimension(40,30));
        setModifier(new ModifierNMolecule(agent));
        if (resetAction != null) {
            targetAction = new ActionGroupSeries(new Action[]{modifyAction,resetAction});
        }
	    
	    if(agent.getType().getSpecies().getName() == "") {
	        setLabel("Number of molecules");
	    } else {
	        setLabel(agent.getType().getSpecies().getName() + " molecules");
	    }
    }
    
    protected Action resetAction;
    
    //main method to demonstrate and test class
    public static void main(String[] args) {
        
        HSMD2D sim = new HSMD2D();
        SimulationGraphic graphic = new SimulationGraphic(sim);
        sim.register(sim.integrator);
        
        DeviceNSelector nSelector = new DeviceNSelector(sim.getController());
        nSelector.setResetAction(new SimulationRestart(sim));
        nSelector.setSpeciesAgent(sim.phase.getAgent(sim.species));

        graphic.makeAndDisplayFrame();
        graphic.add(nSelector);
    }
    

} //end of DeviceNSelector
  