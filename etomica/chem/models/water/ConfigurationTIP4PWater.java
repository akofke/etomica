
package etomica.chem.models.water;
import etomica.Atom;
import etomica.Conformation;
import etomica.Space;
import etomica.atom.AtomList;
import etomica.atom.iterator.AtomIteratorListSimple;

public class ConfigurationTIP4PWater extends Conformation {

    private double bondLengthOH = 0.9572;
    private double angleHOH = 104.52*Math.PI/180.;
    private double bondLengthOcharge = 0.15;
    private AtomIteratorListSimple iterator;
    
    public ConfigurationTIP4PWater(Space space) {
        super(space);
        iterator = new AtomIteratorListSimple();
    }
    
    public void initializePositions(AtomList list){
        
        iterator.setList(list);
        double x = 0.0;
        double y = 0.0;
        
        iterator.reset();
        
        Atom o = iterator.nextAtom();
        o.coord.position().E(new double[] {x, y, 0.0});
               
        Atom h1 = iterator.nextAtom();
        h1.coord.position().E(new double[] {x+bondLengthOH, y, 0.0});
                
        Atom h2 = iterator.nextAtom();
        h2.coord.position().E(new double[] {x+bondLengthOH*Math.cos(angleHOH), y+bondLengthOH*Math.sin(angleHOH), 0.0});

        Atom charge = iterator.nextAtom();
        charge.coord.position().E(new double[] {x+bondLengthOcharge*Math.cos(angleHOH/2), y+bondLengthOcharge*Math.sin(angleHOH/2), 0.0});
    }//end of initializePositions
    
    
}






