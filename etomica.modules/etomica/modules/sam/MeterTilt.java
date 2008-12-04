package etomica.modules.sam;

import etomica.api.IAtomPositioned;
import etomica.api.IAtomList;
import etomica.api.IBox;
import etomica.api.IData;
import etomica.api.IMolecule;
import etomica.api.ISpecies;
import etomica.api.IVector;
import etomica.data.DataTag;
import etomica.data.IEtomicaDataInfo;
import etomica.data.IEtomicaDataSource;
import etomica.data.types.DataDouble;
import etomica.data.types.DataGroup;
import etomica.space.ISpace;
import etomica.units.Angle;

/**
 * Meter that measures the average tilt angle (not the angle of average tilt!)
 *
 * @author Andrew Schultz
 */
public class MeterTilt implements IEtomicaDataSource {

    public MeterTilt(ISpace space, ISpecies species) {
        this.species = species;
        dr = space.makeVector();
        drSum = space.makeVector();
        tag = new DataTag();
        data = new DataGroup(new IData[]{new DataDouble(), new DataDouble()});
        dataInfo = new DataGroup.DataInfoGroup("Tilt", Angle.DIMENSION, new IEtomicaDataInfo[]{
                new DataDouble.DataInfoDouble("Tilt", Angle.DIMENSION),
                new DataDouble.DataInfoDouble("Tilt", Angle.DIMENSION)});
    }
    
    public void setBox(IBox newBox) {
        box = newBox;
    }

    public IData getData() {
        IAtomList molecules = box.getMoleculeList(species);
        int nMolecules = molecules.getAtomCount();
        int leafCount = species.getNumLeafAtoms();
        drSum.E(0);
        double thetaSum = 0;
        for (int i=0; i<nMolecules; i++) {
            IMolecule molecule = (IMolecule)molecules.getAtom(i);
            dr.E(((IAtomPositioned)molecule.getChildList().getAtom(leafCount-1)).getPosition());
            dr.ME(((IAtomPositioned)molecule.getChildList().getAtom(1)).getPosition());
            thetaSum += Math.acos(dr.x(1)/Math.sqrt(dr.squared()));
            drSum.PE(dr);
        }
        ((DataDouble)data.getData(0)).x = Math.acos(drSum.x(1)/Math.sqrt(drSum.squared()));
        ((DataDouble)data.getData(1)).x = thetaSum / nMolecules;
        return data;
    }

    public IEtomicaDataInfo getDataInfo() {
        return dataInfo;
    }

    public DataTag getTag() {
        return tag;
    }

    private static final long serialVersionUID = 1L;
    protected final ISpecies species;
    protected IBox box;
    protected final IVector dr, drSum;
    protected final DataTag tag;
    protected final DataGroup data;
    protected final IEtomicaDataInfo dataInfo;
}
