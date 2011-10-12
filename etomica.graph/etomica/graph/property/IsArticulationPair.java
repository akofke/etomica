package etomica.graph.property;

import etomica.graph.model.Graph;
import etomica.graph.operations.DropOrphanNodes;
import etomica.graph.property.IsDisconnected;

public class IsArticulationPair {
	
	IsDisconnected m = new IsDisconnected();
	
	public boolean isArticulationPair(Graph g,int x, int y){

		Graph c = g.copy();
		DropOrphanNodes dropper = new DropOrphanNodes();
		//Deleting X
	//	System.out.println("Deleting the bonds of "+x);
		for(int i = c.getOutDegree((byte) x)-1;	i>=0;	i--){
           c.deleteEdge((byte) x,c.getOutNode((byte) x,(byte)  i));	//public void deleteEdge(byte fromNode, byte toNode);
		}
	
		//Deleting Y
	//	System.out.println("Deleting the bonds of "+y);
		for(int j = c.getOutDegree((byte) y)-1;	j>=0;	j--){
			if(c.hasEdge((byte) y,c.getOutNode((byte) y,(byte)  j)))	//public boolean hasEdge(byte fromNode, byte toNode);
				c.deleteEdge((byte) y,c.getOutNode((byte) y,(byte)  j));	//public void deleteEdge(byte fromNode, byte toNode);
		}
	
		c = dropper.apply(c);
		
		if(c.nodeCount()==0)return true;
	
	//	System.out.println("Articulation pair dropped");
		
		for(int i = 0;(i<c.nodeCount()); i++){       // To find all bonds
			for(int j=0;j<c.getOutDegree( (byte) i);j++){
	//			System.out.println(i+","+c.getOutNode( (byte) i,(byte) j )+" are the new bonds after dropping orphan bonds");
			}
		}
		
		if(c.nodeCount()<g.nodeCount()-2) {
	//			System.out.println("c.nodeCount()<g.nodeCount()-2");
				return true;
			}
		else if(m.isDisconnected(c)) {
	//		System.out.println("isDisconnected(c) is true");
				return true;
			}
		else {
	//		System.out.println("isDisconnected(c) is not true");
				return false;
			 }
	}

}