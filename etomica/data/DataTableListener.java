package etomica.data;



/**
 * Interface for a class that performs some action in response to
 * a change in a DataSinkTable instance.  Classes implementing this interface can
 * register with a DataSinkTable via the addTableListener method.  Typically
 * this interface is used to cause a display element to update when a
 * table entry changes.
 * 
 * @author David Kofke
 *
 */
public interface DataTableListener extends DataSetListener {

    /**
     * Method called to indicate that the row headers in the table have
     * changed.  Method is called after the change takes place.
     */
    public void tableRowHeadersChanged(DataSinkTable table);

    /**
     * Method called to indicate that the number of rows in the
     * table has changed.  Method is called after the change takes place.
     */
    public void tableRowCountChanged(DataSinkTable table);
}
