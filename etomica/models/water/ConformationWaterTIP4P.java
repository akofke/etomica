package etomica.models.water;
import etomica.atom.AtomArrayList;
import etomica.atom.IAtomPositioned;
import etomica.config.Conformation;
import etomica.space.Space;

/**
 * Conformation for 4-point water molecule.
 */
public class ConformationWaterTIP4P extends Conformation {

    private double bondLengthOH = 0.9572;
    private double angleHOH = 104.52*Math.PI/180.;
    private double rOM=0.15;

    public ConformationWaterTIP4P(Space space) {
        super(space);
    }
    
    public void initializePositions(AtomArrayList list){
        
        double x = 0.0;
        double y = 0.0;
        
        IAtomPositioned o = (IAtomPositioned)list.getAtom(0);
        o.getPosition().E(new double[] {x, y, 0.0});
               
        IAtomPositioned h1 = (IAtomPositioned)list.getAtom(1);
        h1.getPosition().E(new double[] {x+bondLengthOH, y, 0.0});
                
        IAtomPositioned h2 = (IAtomPositioned)list.getAtom(2);
        h2.getPosition().E(new double[] {x+bondLengthOH*Math.cos(angleHOH), y+bondLengthOH*Math.sin(angleHOH), 0.0});
        
        IAtomPositioned m = (IAtomPositioned)list.getAtom(3);
        m.getPosition().E(new double[] {x+rOM*Math.cos(angleHOH/2.0), y+rOM*Math.sin(angleHOH/2.0), 0.0});

    }
    
    private static final long serialVersionUID = 1L;
}
