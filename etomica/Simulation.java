package simulate;

import java.awt.*;
import java.awt.event.*;
import java.beans.Beans;
import java.util.*;

public class Simulation extends Container {

  public Controller controller;
  Phase firstPhase;
  Phase lastPhase;
  Display firstDisplay;
  Display lastDisplay;
  
  public Simulation() {
    setSize(400,300);
  }

  public void add(Controller c) {
    if(controller != null) {return;}  //already added a controller
    super.add(c);
    controller = c;
    c.parentSimulation = this;
  }
    
  public void add(Display d) {
    super.add(d);
    d.parentSimulation = this;
    if(lastDisplay != null) {lastDisplay.setNextDisplay(d);}
    else {firstDisplay = d;}
    lastDisplay = d;
    if(haveIntegrator()) {
        controller.integrator.addIntegrationIntervalListener(d);
    }
    if(d.displayTool != null) {super.add(d.displayTool);}
    for(Phase p=firstPhase; p!=null; p=p.getNextPhase()) {
        d.setPhase(p);
    }
    d.repaint();
  }
  
  public void add(Phase p) {
    super.add(p);
    p.parentSimulation = this;
    if(lastPhase != null) {lastPhase.setNextPhase(p);}
    else {firstPhase = p;}
    lastPhase = p;
    if(haveIntegrator()) {
        controller.integrator.registerPhase(p);
        p.gravity.addObserver(controller.integrator);
    }
    for(Display d=firstDisplay; d!=null; d=d.getNextDisplay()) {
        d.setPhase(p);
    }
  }
  
  public Phase firstPhase() {return firstPhase;}
  public Phase lastPhase() {return lastPhase;}
  
  public boolean haveIntegrator() {
    return (controller != null && controller.integrator != null);
  }
  
/*  public void paint(Graphics g) {
    if(Beans.isDesignTime()) {
      g.setColor(Color.red);
      g.drawRect(0,0,getSize().width-1,getSize().height-1);
      g.drawRect(1,1,getSize().width-3,getSize().height-3);
    } 
    g.setColor(getBackground());
    paintComponents(g);
  }
  */
}


