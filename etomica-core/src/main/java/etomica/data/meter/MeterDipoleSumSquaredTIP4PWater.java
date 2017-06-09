package etomica.data.meter;

import etomica.atom.IAtom;
import etomica.atom.IAtomList;
import etomica.atom.IMoleculeList;
import etomica.box.Box;
import etomica.data.DataSourceScalar;
import etomica.space.Vector;
import etomica.space.Space;
import etomica.space3d.Vector3D;
import etomica.units.CompoundDimension;
import etomica.units.Dimension;
import etomica.units.Dipole;
import etomica.units.Electron;

/**
 * meter for (sum dipole)^2
 * used for dielectric constant calculation
 * 
 * @author shu
 *
 */
public class MeterDipoleSumSquaredTIP4PWater extends DataSourceScalar{
	 
    private Box box;
    private Vector dipole, dipoleSum;
    
	public MeterDipoleSumSquaredTIP4PWater(Space space, Box box) {
		super("TIP4P water, dipoleSum^2", new CompoundDimension(new Dimension[]{Dipole.DIMENSION},new double[]{2.0}));
		this.box=box;
		dipole = space.makeVector();
		dipoleSum = space.makeVector();
	}
	public double getDataAsScalar() {
		dipoleSum = new Vector3D();
		if (box == null) throw new IllegalStateException("no box");
		IMoleculeList moleculeList = box.getMoleculeList();
		int numMolecule = moleculeList.getMoleculeCount();
		for (int i=0;i<numMolecule; i++){
			IAtomList childList = moleculeList.getMolecule(i).getChildList();
			IAtom atomH1 = childList.getAtom(0);
			IAtom atomH2 = childList.getAtom(1);
			IAtom atomO = childList.getAtom(2);
			IAtom atomM = childList.getAtom(3);
			double chargeH = Electron.UNIT.toSim(+0.52);
			double chargeM = Electron.UNIT.toSim(-1.04);
			
			dipole.Ea1Tv1(chargeH, atomH1.getPosition());
			dipole.PEa1Tv1(chargeH, atomH2.getPosition());
			dipole.PEa1Tv1(chargeM, atomM.getPosition());// now this is the current dipole
//			System.out.println("in meter class, dipole:"+dipole);			
			dipoleSum.PE(dipole);
//			System.out.println("in meter class, dipoleSum:"+dipoleSum);
//			System.out.println("in meter class, squared of dipoleSum:"+dipoleSum.squared());
		}
        return dipoleSum.squared();
	}
    public Box getBox() {
    	return box;
    }
    public void setBox(Box _box) {
    	box = _box;
    }

}
