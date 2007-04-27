package etomica.paracetamol;

import etomica.atom.AtomArrayList;
import etomica.atom.AtomLeaf;
import etomica.atom.iterator.AtomIteratorArrayListSimple;
import etomica.config.Conformation;
import etomica.space.Space;

/*
 *  Geometry of Paracetamol Molecule
 * 
 * @author Tai Tan
 */

public class ConformationParacetamol extends Conformation{
	
	/*
	 * Bond Length [unit in Amstrom]
	 */
	
	// Benzene Ring 
	private double bondLengthC1C2 = 1.395;
	private double bondLengthC2C3 = 1.385;
	private double bondLengthC3C4 = 1.395;
	private double bondLengthC4C5 = 1.395;
	private double bondLengthC5C6 = 1.385;
	private double bondLengthC6C1 = 1.395; 
	
	private double bondLengthC4O1 = 1.352; //Benzene - OH
	private double bondLengthC1N1 = 1.394; //Benzene - N
	
	private double bondLengthN1C7 = 1.366; //N - C
	private double bondLengthC7O2 = 1.226; //C = O
	private double bondLengthC7C8 = 1.503; //C - C
	
	/*
	 * Bond Angles [unit is radian]
	 */
	
	private double angleC6C1C2 = 120.0*Math.PI/180;
	private double angleC2C3C4 = 120.0*Math.PI/180;
	private double angleC3C4C5 = 120.0*Math.PI/180;
	private double angleC5C6C1 = 120.0*Math.PI/180;
	
	private double angleO1C4C5 = 123.0*Math.PI/180; //Benzene - OH
	private double angleC6C1N1 = 118.0*Math.PI/180; //Benzene -N
	private double angleC1N1C7 = 129.0*Math.PI/180;	
	
	private final AtomIteratorArrayListSimple iterator;
	
	public ConformationParacetamol(Space space) {
		super(space);
		iterator = new AtomIteratorArrayListSimple();	
	}
	
	public void initializePositions(AtomArrayList List){
		
		iterator.setList(List);
		double x = 0.0;
		double y = 0.0;
		
		iterator.reset();
		
		AtomLeaf c1 = (AtomLeaf)iterator.nextAtom();
		c1.getPosition().E(new double [] {x, y, 0.0});
		
		AtomLeaf c2 = (AtomLeaf)iterator.nextAtom();
		x = x-bondLengthC1C2*Math.cos((angleC6C1C2)/2);
		y = y-bondLengthC1C2*Math.sin((angleC6C1C2)/2);
		c2.getPosition().E(new double [] {x, y, 0.0});
		
		AtomLeaf c3 = (AtomLeaf)iterator.nextAtom();
		x = x-bondLengthC2C3;
		c3.getPosition().E(new double [] {x, y, 0.0});
		
		AtomLeaf c4 = (AtomLeaf)iterator.nextAtom();
		x = x-bondLengthC3C4*Math.cos((angleC2C3C4/2));
		y = y+bondLengthC3C4*Math.sin((angleC2C3C4/2));
		c4.getPosition().E(new double [] {x, y, 0.0});
		
		AtomLeaf o1 = (AtomLeaf)iterator.nextAtom();
		x = x-bondLengthC4O1*Math.cos((angleO1C4C5-120*Math.PI/180));
		y = y-bondLengthC4O1*Math.sin((angleO1C4C5-120*Math.PI/180));
		o1.getPosition().E(new double [] {x, y, 0.0});
		
		AtomLeaf c5 = (AtomLeaf)iterator.nextAtom();
		x = x+bondLengthC4O1*Math.cos((angleO1C4C5-120*Math.PI/180))+bondLengthC4C5*Math.cos((angleC3C4C5/2));
		y = y+bondLengthC4O1*Math.sin((angleO1C4C5-120*Math.PI/180))+bondLengthC4C5*Math.sin((angleC3C4C5/2));  
		c5.getPosition().E(new double [] {x, y, 0.0});
		
		AtomLeaf c6 = (AtomLeaf)iterator.nextAtom();
		x = x+bondLengthC5C6;
		c6.getPosition().E(new double [] {x, y, 0.0});
		
		AtomLeaf n1 = (AtomLeaf)iterator.nextAtom();
		x = x+bondLengthC6C1*Math.cos((angleC5C6C1/2))+bondLengthC1N1*Math.cos((120*Math.PI/180-angleC6C1N1));
		y = y-bondLengthC6C1*Math.sin((angleC5C6C1/2))+bondLengthC1N1*Math.sin((120*Math.PI/180-angleC6C1N1));
		n1.getPosition().E(new double [] {x, y, 0.0});
		
		AtomLeaf c7 = (AtomLeaf)iterator.nextAtom();
		x = x+bondLengthN1C7*Math.cos((180*Math.PI/180-angleC1N1C7-2*Math.PI/180));
		y = y-bondLengthN1C7*Math.sin((180*Math.PI/180-angleC1N1C7-2*Math.PI/180));
		c7.getPosition().E(new double [] {x, y, 0.0});
		
		AtomLeaf c8 = (AtomLeaf)iterator.nextAtom();
		x = x+bondLengthC7C8*Math.cos(17*Math.PI/180);
		y = y+bondLengthC7C8*Math.sin(17*Math.PI/180);
		c8.getPosition().E(new double [] {x, y, 0.0});
		
		AtomLeaf o2 = (AtomLeaf)iterator.nextAtom();
		x = x-bondLengthC7C8*Math.cos(17*Math.PI/180)-bondLengthC7O2*Math.cos(76*Math.PI/180);
		y = y-bondLengthC7C8*Math.sin(17*Math.PI/180)-bondLengthC7O2*Math.sin(76*Math.PI/180);
		o2.getPosition().E(new double [] {x, y, 0.0});
	
	}

	private static final long serialVersionUID = 1L;
}
