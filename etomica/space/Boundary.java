package etomica.space;

import etomica.NearestImageTransformer;
import etomica.Space;
import etomica.math.geometry.Polytope;

/**
 * Parent class of boundary objects that describe the size and periodic nature
 * of the phase boundaries.  Each Phase has its own instance of this class. 
 * It may be referenced by a coordinate pair when computing distances between
 * atoms, or by a cell iterator when generating neighbor lists.  It is also
 * used by objects that enforce periodic images.
 * 
 */
public abstract class Boundary implements NearestImageTransformer,
        java.io.Serializable {

    public Boundary(Space space, Polytope shape) {
        this.space = space;
        this.shape = shape;
    }
    
    public Polytope getShape() {
        return shape;
    }
    
    /**
     * @return the volume enclosed by the boundary
     */
    public double volume() {
        return shape.getVolume();
    }



    /**
     * Determines the translation vector needed to apply a periodic-image
     * transformation that moves the given point to an image point within the
     * boundary (if it lies outside, in a direction subject to periodic
     * imaging).
     * 
     * @param r
     *            vector position of untransformed point; r is not changed by
     *            this method
     * @return the displacement that must be applied to r to move it to its
     *         central-image location
     */
    public abstract Vector centralImage(Vector r);

    /**
     * Transforms the given vector to replace it with a minimum-image distance
     * vector consistent with the periodic boundaries.
     * 
     * @param dr
     *            a distance vector between two points in the volume; upon
     *            return this vector is replaced with the minimum-image vector
     */
    public abstract void nearestImage(Vector dr);

    /**
     * Returns a copy of the dimensions, as a Vector. Manipulation of this copy
     * will not cause any change to the boundary's dimensions.
     * 
     * @return a vector giving the nominal length of the boundary in each
     *         direction. This has an obvious interpretation for rectangular
     *         boundaries, while for others (e.g., octahedral) the definition is
     *         particular to the boundary.
     */
    public abstract Vector dimensions();

    /**
     * Sets the nominal length of the boundary in each direction.
     * 
     * @param v
     */
    public abstract void setDimensions(Vector v);

    /**
     * @return a point selected uniformly within the volume enclosed by the
     *         boundary.
     */
    public abstract Vector randomPosition();

    /**
     * Provides information needed so that drawing can be done of portions of
     * the periodic images of an atom that overflow into the volume (because the
     * periodic image is just outside the volume).
     * 
     * @param r
     *            position of the atom in the central image
     * @param distance
     *            size of the atom, indicating how far its image extends
     * @return all displacements needed to draw all overflow images; first index
     *         indicates each displacement, second index is the xyz translation
     *         needed to the overflow image
     */
    public abstract float[][] getOverflowShifts(Vector r, double distance);

    /**
     * Set of vectors describing the displacements needed to translate the
     * central image to all of the periodic images. The first index specifies
     * each perioidic image, while the second index indicates the xyz components
     * of the translation vector.
     * 
     * @param nShells
     *            the number of shells of images to be computed
     */
    public abstract double[][] imageOrigins(int nShells);

    protected final Space space;
    protected final Polytope shape;

}