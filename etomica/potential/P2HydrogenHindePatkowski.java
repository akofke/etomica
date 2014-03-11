package etomica.potential;

import etomica.api.IAtom;
import etomica.api.IAtomList;
import etomica.api.IBoundary;
import etomica.api.IBox;
import etomica.api.IMolecule;
import etomica.api.IMoleculeList;
import etomica.api.IPotential;
import etomica.api.IPotentialAtomic;
import etomica.api.IPotentialMolecular;
import etomica.api.IVector;
import etomica.api.IVectorMutable;
import etomica.atom.AtomHydrogen;
import etomica.potential.P2HydrogenHinde.P2HydrogenHindeMolecular;
import etomica.space.ISpace;
import etomica.units.BohrRadius;

public class P2HydrogenHindePatkowski implements IPotential {
    protected IBoundary boundary;
    protected IVectorMutable dr,com0,com1,hh0,hh1,n0,n1;
    protected P2HydrogenHinde p2Hinde;
    protected P2HydrogenPatkowski p2Patkowski;
    protected static final double r0 = BohrRadius.UNIT.toSim(1.448736);
    public P2HydrogenHindePatkowski(ISpace space) {
        p2Hinde = new P2HydrogenHinde(space);
        p2Patkowski = new P2HydrogenPatkowski(space);        
        dr = space.makeVector();
        com0 = space.makeVector();
        com1 = space.makeVector();  
        hh0 = space.makeVector();
        hh1 = space.makeVector();  
        n0 = space.makeVector();
        n1 = space.makeVector();
    }

    public double getRange() {    
        return Double.POSITIVE_INFINITY;
    }


    public void setBox(IBox box) {    
        boundary = box.getBoundary();
        p2Patkowski.setBox(box);
        p2Hinde.setBox(box);
    }


    public int nBody() {
        return 2;
    }
    public static class P2HydrogenHindePatkowskiMolecular extends P2HydrogenHindePatkowski implements IPotential {
        public P2HydrogenHindePatkowskiMolecular(ISpace space) {
            super(space);
        }
        public double energy(IMoleculeList molecules) {
            IMolecule m0 = molecules.getMolecule(0);
            IMolecule m1 = molecules.getMolecule(1);
            IAtom a00 = m0.getChildList().getAtom(0);
            IAtom a01 = m0.getChildList().getAtom(1);
            IAtom a10 = m1.getChildList().getAtom(0);
            IAtom a11 = m1.getChildList().getAtom(1);
            IVector orient00 = a00.getPosition();
            IVector orient01 = a01.getPosition();
            IVector orient10 = a10.getPosition();
            IVector orient11 = a11.getPosition();

            com0.Ev1Pv2(orient00, orient01);
            com0.TE(0.5);        
            com1.Ev1Pv2(orient10, orient11);
            com1.TE(0.5);
            dr.Ev1Mv2(com1, com0);    
            boundary.nearestImage(dr);    
            double r01 = Math.sqrt(dr.squared());
            dr.normalize();
            hh0.Ev1Mv2(orient01, orient00);
            hh1.Ev1Mv2(orient11, orient10);
            double r = Math.sqrt(hh0.squared());            
            double s = Math.sqrt(hh1.squared());        
            hh0.normalize();            
            hh1.normalize();
            double th1 = Math.acos(dr.dot(hh0));
            double th2 = Math.acos(dr.dot(hh1));
            n0.E(hh0);
            n1.E(hh1);
            n0.XE(dr);
            n1.XE(dr);
            double phi = Math.acos(n0.dot(n1));
            if (th1 > (Math.PI/2.0)) {
                th1 = Math.PI - th1;
                phi = Math.PI - phi;
            } 
            if (th2 > (Math.PI/2.0)) {
                th2 = Math.PI - th2;
                phi = Math.PI - phi;
            }
            double E = p2Patkowski.vH2H2(r01,th1,th2,phi) + p2Hinde.vH2H2(r01, r, s, th1, th2, phi) - p2Hinde.vH2H2(r01, r0, r0, th1, th2, phi);
            return E;
        }
    }
    public static class P2HydrogenHindePatkowskiAtomic extends P2HydrogenHindePatkowski implements IPotentialAtomic {
        public P2HydrogenHindePatkowskiAtomic(ISpace space) {
            super(space);     
        }

        public double energy(IAtomList atoms) {
            AtomHydrogen m0 = (AtomHydrogen) atoms.getAtom(0);
            AtomHydrogen m1 = (AtomHydrogen) atoms.getAtom(1);            
            IVector hh0 = m0.getOrientation().getDirection();
            IVector hh1 = m1.getOrientation().getDirection();        
            IVector com0 = m0.getPosition();               
            IVector com1 = m1.getPosition();        
            //            if (com1.squared() > 0) {
                //                throw new RuntimeException("oops");
                //            }
            dr.Ev1Mv2(com1, com0);    
            boundary.nearestImage(dr);    
            double r01 = Math.sqrt(dr.squared());        
            if (r01 != 0) {
                dr.normalize();
            }
            else {
                dr.E(0);
                dr.setX(2, 1);
            }
            double th1 = Math.acos(dr.dot(hh0));
            double th2 = Math.acos(dr.dot(hh1));
            n0.E(hh0);
            n1.E(hh1);
            n0.XE(dr);
            n1.XE(dr);
            double phi = Math.acos(n0.dot(n1));
            if (th1 > (Math.PI/2.0)) {
                th1 = Math.PI - th1;
                phi = Math.PI - phi;
            } 
            if (th2 > (Math.PI/2.0)) {
                th2 = Math.PI - th2;
                phi = Math.PI - phi;
            }
            if (th2 == 0) phi = 0;
            double r = m0.getBondLength();
            double s = m1.getBondLength();            
            double E = p2Patkowski.vH2H2(r01,th1,th2,phi) + p2Hinde.vH2H2(r01, r, s, th1, th2, phi) - p2Hinde.vH2H2(r01, r0, r0, th1, th2, phi);            
            return E;
        } 
    }

}