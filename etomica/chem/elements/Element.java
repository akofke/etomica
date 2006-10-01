package etomica.chem.elements;

import etomica.units.Dimension;
import etomica.units.Mass;

/**
 * Abstract structure for a class defining an element.
 */
public abstract class Element implements java.io.Serializable {
	
	public Element(String symbol) {
        this.symbol = symbol;
	}
    
    public abstract double getMass();
    
    public Dimension getMassDimension() {
        return Mass.DIMENSION;
    }
    
    public abstract double rm();

    public String getSymbol() {
        return symbol;
    }
    
    protected final String symbol;
}
