/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package etomica.spin;

import etomica.atom.IAtom;
import etomica.box.Box;
import etomica.space.Vector;
import etomica.atom.iterator.AtomIteratorLeafAtoms;
import etomica.data.DataSourceScalar;
import etomica.data.IEtomicaDataSource;
import etomica.space.Space;
import etomica.units.dimensions.Undefined;


/**
 * Meter that provides the x-component of the vector average of
 * spin values (which is represented by the atom's position
 * vector).
 *
 * @author David Kofke
 *
 */
public class MeterSpin extends DataSourceScalar implements IEtomicaDataSource {

    /**
     * 
     */
    public MeterSpin(Space space) {
        super("Spin",Undefined.DIMENSION);
        sum = space.makeVector();
    }

    /* (non-Javadoc)
     * @see etomica.data.meter.MeterScalar#getDataAsScalar(etomica.Box)
     */
    public double getDataAsScalar() {
        sum.E(0.0);
        int count = 0;
        iterator.setBox(box);
        iterator.reset();
        for (IAtom atom = iterator.nextAtom(); atom != null;
             atom = iterator.nextAtom()) {
            sum.PE(atom.getPosition());
            count++;
        }
        return sum.getX(0)/count;
    }

    /**
     * @return Returns the box.
     */
    public Box getBox() {
        return box;
    }
    /**
     * @param box The box to set.
     */
    public void setBox(Box box) {
        this.box = box;
    }

    private static final long serialVersionUID = 1L;
    private Box box;
    private final AtomIteratorLeafAtoms iterator = new AtomIteratorLeafAtoms();
    private final Vector sum;
}
