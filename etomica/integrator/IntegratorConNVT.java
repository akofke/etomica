package etomica.integrator;

import etomica.api.IAtomLeaf;
import etomica.api.IAtomSet;
import etomica.api.IAtomTypeLeaf;
import etomica.api.IBox;
import etomica.api.IPotentialMaster;
import etomica.api.IRandom;
import etomica.api.ISimulation;
import etomica.api.IVector;
import etomica.atom.AtomLeafAgentManager;
import etomica.atom.IAtomKinetic;
import etomica.atom.AtomLeafAgentManager.AgentSource;
import etomica.atom.iterator.IteratorDirective;
import etomica.potential.PotentialCalculationForceSum;
import etomica.space.ISpace;

/**
 * Constant NVT Molecular Dynamics Integrator-Constraint Method
 * 
 * Uses a modified version of the Leap Frog Algorithm. 
 * ke is calculated from the unconstrained velocities at time T.
 * A ratio of the setTemperature to the unconstrained temp (as solved from ke),
 * is used to calculate the new constrained velocities at T+Dt/2.  
 * The positions at T+Dt are solved for from the constrained velocities calculated at T+Dt/2.
 *  
 * @author Chris Iacovella
 * @author David Kofke
 */
public final class IntegratorConNVT extends IntegratorMD implements AgentSource {

    private static final long serialVersionUID = 1L;
    public final PotentialCalculationForceSum forceSum;
    private final IteratorDirective allAtoms;
    IVector work, work1, work2, work3, work4;
    double halfTime, mass;

    protected AtomLeafAgentManager agentManager;

    public IntegratorConNVT(ISimulation sim, IPotentialMaster potentialMaster, ISpace space) {
        this(potentialMaster, sim.getRandom(), 0.05, 1.0, space);
    }
    
    public IntegratorConNVT(IPotentialMaster potentialMaster, IRandom random, 
            double timeStep, double temperature, ISpace space) {
        super(potentialMaster,random,timeStep,temperature, space);
        forceSum = new PotentialCalculationForceSum();
        allAtoms = new IteratorDirective();
        // allAtoms is used only for the force calculation, which has no LRC
        allAtoms.setIncludeLrc(false);
        work = space.makeVector();
        work1 = space.makeVector();
        work2 = space.makeVector();
        work3 = space.makeVector();
       	work4 = space.makeVector();
    }

	
    public void setBox(IBox p) {
        if (box != null) {
            agentManager.dispose();
        }
        super.setBox(p);
        agentManager = new AtomLeafAgentManager(this,p);
        forceSum.setAgentManager(agentManager);
    }
    
  	public final void setTimeStep(double t) {
    	super.setTimeStep(t);
    	halfTime = timeStep/2.0;
  	}
  	
  	private double Temper;
  	public void setTemp(double temperature) {
  	    Temper=temperature;
	}
          
//--------------------------------------------------------------
// steps all particles across time interval tStep

  	public void doStepInternal() {
        super.doStepInternal();

        double dim = space.D();  //get the dimension

  	    //Compute forces on each atom
  	    forceSum.reset();
  	    potential.calculate(box, allAtoms, forceSum);

  	    //MoveA
        //Advance velocities from T-Dt/2 to T without constraint
        double Free=0.0;
        //degrees of freedom
        Free=((box.getMoleculeList().getAtomCount()-1)*dim); 

        double k=0.0;
        double chi;
        IAtomSet leafList = box.getLeafList();
        int nLeaf = leafList.getAtomCount();
        for (int iLeaf=0; iLeaf<nLeaf; iLeaf++) {
            IAtomKinetic a = (IAtomKinetic)leafList.getAtom(iLeaf);
            IVector v = a.getVelocity();

            work1.E(v); //work1 = v
            work2.E(((Agent)agentManager.getAgent((IAtomLeaf)a)).force);	//work2=F
            work1.PEa1Tv1(halfTime*((IAtomTypeLeaf)a.getType()).rm(),work2); //work1= p/m + F*Dt2/m = v + F*Dt2/m

            k+=work1.squared();
        }   

        //calculate scaling factor chi
        chi= Math.sqrt(Temper*Free/(mass*k));

        //calculate constrained velbox.getSpace()ocities at T+Dt/2
        for (int iLeaf=0; iLeaf<nLeaf; iLeaf++) {
            IAtomKinetic a = (IAtomKinetic)leafList.getAtom(iLeaf);
            Agent agent = (Agent)agentManager.getAgent((IAtomLeaf)a);
            IVector v = a.getVelocity();

            double scale = (2.0*chi-1.0); 
            work3.Ea1Tv1(scale,v); 
            work4.Ea1Tv1(chi*((IAtomTypeLeaf)a.getType()).rm(),agent.force);
            work4.TE(timeStep);
            work3.PE(work4);
            v.E(work3);
        } 

        for (int iLeaf=0; iLeaf<nLeaf; iLeaf++) {
            IAtomKinetic a = (IAtomKinetic)leafList.getAtom(iLeaf);
            IVector r = a.getPosition();
            IVector v = a.getVelocity();

            work.Ea1Tv1(timeStep,v);
            work.PE(r);
            r.E(work);
        }
  	}
    

    public Class getAgentClass() {
        return Agent.class;
    }
    
    public final Object makeAgent(IAtomLeaf a) {
        return new Agent(space);
    }
    
    public void releaseAgent(Object agent, IAtomLeaf atom) {}
            
	public final static class Agent implements IntegratorBox.Forcible {  //need public so to use with instanceof
        public IVector force;

        public Agent(ISpace space) {
            force = space.makeVector();
        }
        
        public IVector force() {return force;}
    }
    
}
