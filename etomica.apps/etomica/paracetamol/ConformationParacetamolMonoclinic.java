package etomica.paracetamol;

import etomica.api.IAtomPositioned;
import etomica.api.IAtomList;
import etomica.api.IConformation;
import etomica.space.ISpace;

/*
 *  Geometry of Published Paracetamol Molecule (Monoclinic)
 * 
 * @author Tai Tan
 */

public class ConformationParacetamolMonoclinic implements IConformation {
	
	public ConformationParacetamolMonoclinic(ISpace space) {
	    this.space = space;
	}
	
	public void initializePositions(IAtomList list){
		double x = 0.0;
		double y = 0.0;
		double z = 0.0;
		
		IAtomPositioned c1 = (IAtomPositioned)list.getAtom(SpeciesParacetamol.indexC[0]);
		x =   0.02112;
		y = - 0.05209;
		z =   0.13276;
		c1.getPosition().E(new double [] {x, y, z});
		
		IAtomPositioned c2 = (IAtomPositioned)list.getAtom(SpeciesParacetamol.indexC[1]);
		x =   0.90043;
		y = - 0.07109;
		z = - 1.11054;
		c2.getPosition().E(new double [] {x, y, z});
		
		IAtomPositioned h1 = (IAtomPositioned)list.getAtom(SpeciesParacetamol.indexH[0]);
		x =   0.66461;
		y =   0.49952;
		z = - 1.94367;
		h1.getPosition().E(new double [] {x, y, z});
		
		IAtomPositioned c3 = (IAtomPositioned)list.getAtom(SpeciesParacetamol.indexC[2]);
		x =   2.06673;
		y = - 0.83632;
		z = - 1.27335;
		c3.getPosition().E(new double [] {x, y, z});
		
		IAtomPositioned h2 = (IAtomPositioned)list.getAtom(SpeciesParacetamol.indexH[1]);
		x =   2.74509;
		y = - 0.85319;
		z = - 2.23162;
		h2.getPosition().E(new double [] {x, y, z});
		
		IAtomPositioned c4 = (IAtomPositioned)list.getAtom(SpeciesParacetamol.indexC[3]);
		x =   2.38797;
		y = - 1.58811;
		z = - 0.20768;
		c4.getPosition().E(new double [] {x, y, z});
		
		IAtomPositioned o1 = (IAtomPositioned)list.getAtom(SpeciesParacetamol.indexO[0]);
		x =   3.55058;
		y = - 2.30947;
		z = - 0.43417;
		o1.getPosition().E(new double [] {x, y, z});
		
		IAtomPositioned h5 = (IAtomPositioned)list.getAtom(SpeciesParacetamol.indexHp[0]);
		x =   3.67175;
		y = - 2.77967;
		z =   0.35244;
		h5.getPosition().E(new double [] {x, y, z});
		
		IAtomPositioned c5 = (IAtomPositioned)list.getAtom(SpeciesParacetamol.indexC[4]);
		x =   1.51987;
		y = - 1.57233;
		z =   1.02946;
		c5.getPosition().E(new double [] {x, y, z});
		
		IAtomPositioned h3 = (IAtomPositioned)list.getAtom(SpeciesParacetamol.indexH[2]);
		x =   1.74805;
		y = - 2.15126;
		z =   1.86983;
		h3.getPosition().E(new double [] {x, y, z});
		
		IAtomPositioned c6 = (IAtomPositioned)list.getAtom(SpeciesParacetamol.indexC[5]);
		x =   0.34498;
		y = - 0.81206;
		z =   1.19075;
		c6.getPosition().E(new double [] {x, y, z});
		
		IAtomPositioned h4 = (IAtomPositioned)list.getAtom(SpeciesParacetamol.indexH[3]);
		x = - 0.32286;
		y = - 0.81763;
		z =   2.15972;
		h4.getPosition().E(new double [] {x, y, z});
		
		IAtomPositioned n1 = (IAtomPositioned)list.getAtom(SpeciesParacetamol.indexN[0]);
		x = - 1.17784;
		y =   0.72361;
		z =   0.39062;
		n1.getPosition().E(new double [] {x, y, z});
		
		IAtomPositioned h6 = (IAtomPositioned)list.getAtom(SpeciesParacetamol.indexHp[1]);
		x = - 1.59813;
		y =   0.80096;
		z =   1.36267;
		h6.getPosition().E(new double [] {x, y, z});
		
		IAtomPositioned c7 = (IAtomPositioned)list.getAtom(SpeciesParacetamol.indexC[6]);
		x = - 1.83628;
		y =   1.34805;
		z = - 0.51418;
		c7.getPosition().E(new double [] {x, y, z});
		
		IAtomPositioned o2 = (IAtomPositioned)list.getAtom(SpeciesParacetamol.indexO[1]);
		x = - 1.46888;
		y =   1.32871;
		z = - 1.72035;
		o2.getPosition().E(new double [] {x, y, z});
		
		IAtomPositioned c8 = (IAtomPositioned)list.getAtom(SpeciesParacetamol.indexC[7]);
		x = - 3.08023;
		y =   2.12930;
		z =   0.12800;
		c8.getPosition().E(new double [] {x, y, z});
		
		IAtomPositioned h7 = (IAtomPositioned)list.getAtom(SpeciesParacetamol.indexH[4]);
		x = - 3.60051;
		y =   1.68308;
		z =   1.06275;
		h7.getPosition().E(new double [] {x, y, z});
		
		IAtomPositioned h8 = (IAtomPositioned)list.getAtom(SpeciesParacetamol.indexH[5]);
		x = - 3.76528;
		y =   2.19509;
		z = - 0.59402;
		h8.getPosition().E(new double [] {x, y, z});
	
		IAtomPositioned h9 = (IAtomPositioned)list.getAtom(SpeciesParacetamol.indexH[6]);
		x = - 2.77116;
		y =   3.13489;
		z =   0.35058;
		h9.getPosition().E(new double [] {x, y, z});
	
	}

	private static final long serialVersionUID = 1L;
    protected final ISpace space;
}
