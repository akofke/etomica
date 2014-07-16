package etomica.virial;

import java.util.Arrays;

/**
 * An IntSet holds an array of integers and can act as a has key.
 * 
 * @author Andrew Schultz
 */
public class IntSet implements Comparable<IntSet> {
    public int[] v;
    public IntSet(int[] v) {
        this.v = v;
    }
    public int hashCode() {
        return Arrays.hashCode(v);
    }
    public boolean equals(Object o) {
        IntSet ois = (IntSet)o;
        return Arrays.equals(v, ois.v);
    }
    public IntSet copy() {
        return new IntSet(v.clone());
    }
    public int compareTo(IntSet o) {
        for (int i=0; i<v.length; i++) {
            if (v[i] == o.v[i]) continue;
            return v[i] > o.v[i] ? 1 : -1;
        }
        return 0;
    }
    public String toString() {
        return Arrays.toString(v);
    }
}