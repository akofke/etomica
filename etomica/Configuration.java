package etomica;

/**
 * General class for assignment of coordinates to a group of atoms.
 * 
 * @author David Kofke
 */
public abstract class Configuration implements java.io.Serializable {

    protected final Space space;
    protected double[] dimensions;
    
    public Configuration(Space space) {
        this.space = space;
        dimensions = new double[space.D()];
        for(int i=0; i<dimensions.length; i++) {dimensions[i] = Default.BOX_SIZE;}
    }
        
    public abstract void initializePositions(AtomIterator[] iterator);
    
    public void initializePositions(AtomIterator iterator) {
        initializePositions(new AtomIterator[] {iterator});
    }
    
    public void setDimensions(double[] dimensions) {
        this.dimensions = dimensions;
    }
    public double[] getDimensions() {return dimensions;}
    
 /**   
  * All atom velocities are set such that all have the same total momentum (corresponding to
  * the default value of temperature), with the direction at random
  */
    public static void initializeMomenta(Atom atom) {
        initializeMomenta(atom, Default.TEMPERATURE);
    }
    public static void initializeMomenta(Atom atom, double temperature) {
        atom.coord.randomizeMomentum(temperature);
    }//end of initializeMomenta
    
    /**
     * Initializes positions and momenta of the given atom group.
     */
    public void initializeCoordinates(Atom group) {
        if(group.node.isLeaf()) throw new IllegalArgumentException("Error in Configuration.initializeCoordinates:  Attempt to initialize child coordinates of leaf atom");

        AtomIteratorList iterator = 
                AtomIteratorList.makeCopylistIterator(((AtomTreeNodeGroup)group.node).childList);
        initializePositions(iterator);
        initializeMomenta(group);
    }
    
    /**
     * Initializes positions of the given atom group.
     */
    public void initializePositions(Atom group) {
        if(group.node.isLeaf()) throw new IllegalArgumentException("Error in Configuration.initializeCoordinates:  Attempt to initialize child coordinates of leaf atom");
        initializePositions(new AtomIteratorList(((AtomTreeNodeGroup)group.node).childList));
    }
    /**
     * Initializes positions and momenta of the given atom groups.
     */
    public void initializeCoordinates(Atom[] group) {
        AtomIterator[] iterators = new AtomIterator[group.length];
        for(int i=0; i<group.length; i++) {
            iterators[i] = AtomIteratorList.makeCopylistIterator(((AtomTreeNodeGroup)group[i].node).childList);
            //new AtomIteratorSequential(group[i]);
            initializeMomenta(group[i]);
        }
        initializePositions(iterators);
    }

    public static Space1D.Vector[] lineLattice(int n, double Lx) {
        Space1D.Vector[] r = new Space1D.Vector[n];
        double delta = Lx/(double)n;
        for(int i=0; i<n; i++) {
            r[i] = new Space1D.Vector();
            r[i].x = (i+0.5)*delta;
        }
        return r;
    }
        
    /**
     * Returns a set of n coordinates filling a square lattice of sides Lx and Ly
     * If n is not suitable for square lattice, then last sites are left unfilled
     * Lattice is centered between (0,0) and (Lx, Ly).
     * The final argument should be passed one of the class variables VERTICAL or HORIZONTAL, indicating
     *   whether successive points fill the lattice across or down.
     */
    public final static Space2D.Vector[] squareLattice(int n, double Lx, double Ly, boolean fillVertical) {
        Space2D.Vector[] r = new Space2D.Vector[n];
        for(int i=0; i<n; i++) {r[i] = new Space2D.Vector();}

        int moleculeColumns, moleculeRows;
        double moleculeInitialSpacingX, moleculeInitialSpacingY;

    //  Number of molecules per row (moleculeColumns) and number of rows (moleculeRows)
    //  in initial configuration
        moleculeColumns = (int)Math.sqrt(Lx/Ly*(double)n);
        moleculeRows = (int)(n/moleculeColumns);

        if(moleculeRows*moleculeColumns < n) moleculeRows++;

    //moleculeColumns may be greater than the actual number of columns drawn
    //Need to center columns in the initial position.
        int columnsDrawn = (int)((double)n/(double)moleculeRows - 1.0E-10) + 1;
        
    //moleculeColumnsShift used to center initial coordinates
        double moleculeColumnsShift = Lx/columnsDrawn/2;
        double moleculeRowsShift = Ly/moleculeRows/2;
  
    //assign distance between molecule centers
        moleculeInitialSpacingX = Lx/columnsDrawn;
        moleculeInitialSpacingY = Ly/moleculeRows;
        int i = 0;
        int ix = 0;
        int iy = 0;
        while(i < n) {
            r[i].x = ix*moleculeInitialSpacingX + moleculeColumnsShift;
	        r[i].y = iy*moleculeInitialSpacingY + moleculeRowsShift;
	        i++;
	        if(fillVertical) {
	            iy++;
	            if(iy >= moleculeRows) {
	                iy = 0;
	                ix++;
	            }}
	        else {
	            ix++;
	            if(ix >= columnsDrawn) {
	                ix = 0;
	                iy++;
	            }
	        }
	    }
	    return r;
    }//end of squareLattice
}//end of Configuration
