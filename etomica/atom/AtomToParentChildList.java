package etomica.atom;

public class AtomToParentChildList implements AtomToArrayList, java.io.Serializable {

    public AtomArrayList getArrayList(Atom atom) {
        return atom.getNode().parentNode().childList;
    }

    private static final long serialVersionUID = 1L;
}
