// This class includes a main method demonstrating its use.
package etomica;

/**
 * Base class for classes that perform some elementary action on an atom.
 * These classes see use in several ways:
 * <ul>
 * <li>they can be passed to the allAtoms method of atom iterators, which then performs
 * the specified action on all the atoms of the iterator.  
 * <li>they can be used in a DisplayPhase.AtomActionWrapper, to specify some action in response to 
 * selection of an atom by the mouse.  
 * <li>they may be used to generate a Monte Carlo trial in an MCMove object.
 * 
 * @author David Kofke
 * @see Atom.Iterator
 * @see DisplayPhase.AtomActionWrapper
 */
 
 /* History of changes
  * 7/31/02 (DAK) Added Null subclass and NULL static instance of it.
  */

public abstract class AtomAction implements etomica.Action {
    
    protected Atom atom;
    String label;

    public void setAtom(Atom a) {atom = a;}
    public Atom getAtom() {return atom;}

    /**
     * Performs the defined action using the atom most recently specified by setAtom or by the last call to actionPerformed(Atom a).
     * Performs no action if the atom is null.
     */
    public void actionPerformed() {if(atom != null) actionPerformed(atom);}
    
    /**
     * Method that defines the action to be performed on the atom
     * @param a Atom passed to method by iterator
     */
    public abstract void actionPerformed(Atom a);
        
    public String getLabel() {
    	return label;
    }
    public void setLabel(String label) {
    	this.label = label;
    }
    
    //***** end of Action methods; begin definition of subclasses *****//

    /**
     * Translates the atom by the amount it would move in free (ballistic) flight for a specified time interval.
     * Uses the atom's current momentum to determine this displacement.
     */
    public static class FreeFlight extends AtomAction {
        private double tStep = 0.0;
        public void actionPerformed(Atom a) {
            if(a.coord.isStationary()) {return;}  //skip if atom is stationary
            a.coord.freeFlight(tStep);  // r += tStep*p/m
        }
        public void actionPerformed(Atom a, double t) {
            tStep = t;
            actionPerformed(a);
        }
        public void setTStep(double t) {tStep = t;}
        public double getTStep() {return tStep;}
    } //end of FreeFlight
        
    public static class Translate extends AtomAction {
        protected Space.Vector displacement;
            
        public Translate(Space space) {
            super();
            displacement = space.makeVector();
        }
            
        public final void actionPerformed(Atom a) {a.coord.position().PE(displacement);}
        public void actionPerformed(Atom a, Space.Vector d) {a.coord.position().PE(d);}
        public final void setDisplacement(Space.Vector d) {displacement.E(d);}
    }//end of Translate
    
    /**
     * Action that does nothing.
     */
    public static class Null extends AtomAction {
        public final void actionPerformed(Atom a) {}
    }
    
    public static final AtomAction NULL = new Null();
    
//        public static class Displace extends Translate {
//            Atom atom;
//            public void actionPerformed(Atom a) {atom = a; a.r.PE(displacement);}
//            public void actionPerformed(Atom a, Space.Vector d) {
//                displacement.E(d);
//                a.r.PE(displacement);
//            }
//            public void retractAction() {a.r.ME(displacement);}
//        }//end of Displace

    /**
     * Demonstrates how this class can be implemented with a DisplayPhase event using a AtomActionWrapper.
     * Hold the 'a' key while pressing mouse button near an atom to change its color; relase of the 
     * mouse button reverts to original color.
     */
/*    public static void main(String[] args) {
        final Frame f = new Frame();   //create a window
        f.setSize(600,350);
        
      //make a simple simulation for this example
        etomica.simulations.HSMD2D sim = new etomica.simulations.HSMD2D();
        Simulation.instance = sim;
        
      //get a handle to the display in the simple simulation
        final DisplayPhase display = sim.display;
        display.setColorScheme(new ColorSchemeNull());
      //create an instance of the AtomAction that is being demonstrated here
        AtomAction.ChangeColor colorChanger = new AtomAction.ChangeColor();
      //wrap the action in a DisplayPhaseListener so it responds to MousePressed events
        DisplayPhaseListener.AtomActionWrapper wrapper = new DisplayPhaseListener.AtomActionWrapper(colorChanger);
      //set the wrapper so that release of the mouse button retracts the action
        wrapper.setUndoOnRelease(true);
      //make the wrapper a listener to the DisplayPhase, and add listener to repaint display
        display.addDisplayPhaseListener(wrapper);
        display.addDisplayPhaseListener(new DisplayPhaseListener() {
            public void displayPhaseAction(DisplayPhaseEvent e) {display.repaint();}
        });
      //add the simulation graphic elements to the frame
        sim.elementCoordinator.go();
        f.add(Simulation.instance.panel());
      //display the frame
        f.pack();
        f.show();
        f.addWindowListener(new WindowAdapter() {   //anonymous class to handle window closing
            public void windowClosing(WindowEvent e) {System.exit(0);}
        });
    }
    */
} //end of AtomAction   