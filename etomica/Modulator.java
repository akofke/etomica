package etomica;
import etomica.units.Dimension;
import java.beans.*;
import java.lang.reflect.*;

/**
 * Implements the Modulator functionality using introspection to obtain the accessor methods for a property.
 * Capable of modulating the same property for several objects at once (e.g., temperature for two different
 * integrator objects can be set simultaneously).  In the case of multi-object modulation, calls to readValue
 * are taken from only the first object in the list, which assumes all would return the same value
 *
 * @author David Kofke
 * @author Jhumpa Adhikari
 */

public class Modulator extends ModulatorAbstract {

    protected Object[] object;
    protected String property;
    protected transient Method[] readMethod;
    protected transient Method[] writeMethod;
    private int nObjects;
    
    /**
     * Constructor connecting the modulator's getValue and setValue methods to the set/get accessor
     * methods of the object being modulated.  If the accessor methods of the object are, for
     * example getTime and setTime, then the string "time" should be passed in the constructor.
     * Note the adherance to the JavaBeans case conventions for property and accessor names.
     *
     * @param obj the object with the property to be modulated
     * @param property the name of the property being modulated
     */
    public Modulator(Object[] obj, String prop) {
        super(Dimension.NULL);
        nObjects = obj.length;
        object    = new Object[nObjects];
        property = prop;
        setLabel(prop);
        for(int j=0; j<nObjects; j++) {object[j] = obj[j];}
        initialize();
    }
    /**
     * Modulator for property of a single object (most common use)
     */
    public Modulator(Object obj, String prop) {
        this(new Object[] {obj}, prop);
    }
    
    private void readObject(java.io.ObjectInputStream s) throws java.io.IOException, java.lang.ClassNotFoundException {
        s.defaultReadObject();
        initialize();
    }
    
    private void initialize() {
        if(nObjects == 0) return;
        readMethod = new Method[nObjects];  //not sure if these need to be arrays, but doing this way to be safe
        writeMethod = new Method[nObjects]; //(might be able to use same readMethod and writeMethod objects for all objects
        for(int j=0; j<nObjects; j++) {
            Class c = object[j].getClass();
            //bits of this code are taken from Thinking in Java (1st edition), pages 708-713
            BeanInfo bi = null;
            try {
                bi = Introspector.getBeanInfo(c, java.lang.Object.class);
            }
            catch(IntrospectionException ex) {
                System.out.println("Couldn't introspect " + c.getName());
                System.exit(1);
            }
            PropertyDescriptor[] properties = bi.getPropertyDescriptors();
            for(int i=0; i<properties.length; i++) {
                String propertyName = properties[i].getName();
                if(propertyName.equals(property)) {
                    readMethod[j] = properties[i].getReadMethod();
                    writeMethod[j] = properties[i].getWriteMethod();
                    break;
                }
            }
            if(readMethod[j] == null || writeMethod[j] == null) {  //should define an exception for this
                System.out.println("Error in modulator construction");
                System.exit(1);
            }
            if(j == 0) {//discover dimension of modulated property by looking at getDimension method of first object
                dimension = Dimension.introspect(object[0],property,bi);
/*                MethodDescriptor[] methods = bi.getMethodDescriptors();
                for(int i=0; i<methods.length; i++) {
                    String methodName = methods[i].getMethod().getName();
                    if(methodName.equalsIgnoreCase("get"+property+"Dimension")) {
                        try {
                            dimension = (Dimension)methods[i].getMethod().invoke(object[0], null);
                        }
                        catch(InvocationTargetException ex) {
                            System.out.println("InvocationTargetException in setValue");
                            System.exit(1);
                        }
                        catch(IllegalAccessException ex) {
                            System.out.println("IllegalAccessException in setValue");
                            System.exit(1);
                        }
                    }//end of if(methodName...) block
                }//end of for loop
*/
            }//end of if(j==0)
        }//end of loop over objects
        
    }
    
    public void setValue(double d) {
        for(int j=0; j<nObjects; j++) {
            try {
                writeMethod[j].invoke(object[j], new Double[] {new Double(d)});
            }
            catch(InvocationTargetException ex) {
                System.out.println("InvocationTargetException in Modulator.setValue");
                ex.printStackTrace();
                System.exit(1);
            }
            catch(IllegalAccessException ex) {
                System.out.println("IllegalAccessException in Modulator.setValue");
                System.exit(1);
            }
        }//end of j-loop
    }
        
    
    public double getValue() {
        double value = Double.NaN;
        try {
            value = ((Double)readMethod[0].invoke(object[0], null)).doubleValue();
        }
        catch(InvocationTargetException ex) {
            System.out.println("InvocationTargetException in getValue");
            System.exit(1);
        }
        catch(IllegalAccessException ex) {
            System.out.println("IllegalAccessException in getValue");
            System.exit(1);
        }
        return value;
    }
    
    public Object[] getObject() {return object;}
    public String getProperty() {return property;}
    
    /**
     * Method to demonstrate how to implement and use a modulator
     */
/*    public static void main(String[] args) {
        
      //A typical class to demonstrate use of the Modulator.
      //Modulator will be used to set and get the pressure field of this class.
        IntegratorMC integrator = new IntegratorMC();
        MCMoveVolume target = new MCMoveVolume(integrator);
      
      //Pass the instance of the target object and a string indicating the name of the field
      // to be accessed with the modulator
        Modulator modulator = new Modulator(target, "pressure");
        
        System.out.println("Setting field to 10.5 using modulator");
        modulator.setValue(10.5);
        System.out.println("Value of field as obtained directly from object: "+target.getPressure());
        System.out.println("Value of field as obtained using modulator: "+modulator.getValue());
        System.out.println("Dimensions of field as obtained by modulator: "+modulator.getDimension().toString());
        boolean isMass = modulator.getDimension() instanceof Dimension.Mass;
        System.out.println("Does the field have dimensions of MASS? "+isMass);
        boolean isPressure = modulator.getDimension() instanceof Dimension.Pressure;
        System.out.println("Does the field have dimensions of PRESSURE? "+isPressure);
    }
    */
}
    
    