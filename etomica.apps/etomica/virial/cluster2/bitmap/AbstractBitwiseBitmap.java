package etomica.virial.cluster2.bitmap;

/*
 * Abstract implementation of a bitmap that implements every operation
 * in a bitwise fashion. This means that all operations require access
 * to every bit of the bitmap in the worst case. 
 * 
 * ADVANTAGE: fast implementation of child classes. 
 * 
 * DISADVANTAGE: not efficient if the physical representation of the
 * map of bits supports operations in blocks of bits.
 * 
 */
public abstract class AbstractBitwiseBitmap implements Bitmap {

  // ***********************
  // * PUBLIC METHODS
  // ***********************

  @Override
  public void and(final Bitmap other) {

    Bitmap bm = comparableInstance(other);
    for (int i = 0; i < bitSize(); i++) {
      defBit(i, testBit(i) & bm.testBit(i));
    }
  }

  @Override
  public int bitCount() {

    int result = 0;
    for (int i = 0; i < bitSize(); i++) {
      if (testBit(i)) {
        result++;
      }
    }
    return result;
  }

  @Override
  public Bitmap comparableInstance(Bitmap other) {

    if (bitSize() <= other.bitSize()) {
      return other;
    } else {
      return other.copyLowest(bitSize());
    }
  }

  @Override
  public int compareTo(final Bitmap other) {

    int thisHSB = hsb();
    int thisSize = bitSize();
    int otherHSB = other.hsb();
    int otherSize = other.bitSize();
    if (thisHSB < 0) {
      return (otherHSB < 0 ? 0 : -1);
    }
    if ((otherHSB < 0) && (thisHSB >= 0)) {
      return 1;
    }
    if ((thisSize - thisHSB) > (otherSize - otherHSB)) {
      return 1;
    }
    if ((thisSize - thisHSB) < (otherSize - otherHSB)) {
      return -1;
    }
    thisHSB++;
    otherHSB++;
    while ((thisHSB < thisSize) && (testBit(thisHSB) == testBit(otherHSB))) {
      thisHSB++;
      otherHSB++;
    }
    if (thisHSB == thisSize) {
      return 0;
    }
    if (testBit(thisHSB)) {
      return 1;
    }
    return -1;
  }

  @Override
  public Bitmap copy() {

    return createInstance(this);
  }

  @Override
  public Bitmap copyHighest(int numBits) {

    Bitmap copy = createInstance(numBits);
    for (int i = 0; i < bitSize() - numBits; i++) {
      copy.setBit(i);
    }
    return copy;
  }

  @Override
  public Bitmap copyLowest(int numBits) {

    Bitmap copy = createInstance(numBits);
    for (int i = numBits; i < bitSize(); i++) {
      copy.setBit(i);
    }
    return copy;
  }

  @Override
  public void dec() {

    int bit = lsb();
    if (bit >= 0) {
      clearBit(bit);
      if (bit < bitSize()) {
        for (int i = bit + 1; i < bitSize(); i++) {
          setBit(i);
        }
      }
    } else {
      setBits(true);
    }
  }

  @Override
  public void defBit(final int bitIndex, final boolean bitValue) {

    boolean oldValue = testBit(bitIndex);
    if (oldValue != bitValue) {
      if (oldValue) {
        clearBit(bitIndex);
      } else {
        setBit(bitIndex);
      }
    }
  }

  @Override
  public boolean equals(Object other) {

    boolean result = false;
    if (other instanceof Bitmap) {
      Bitmap bm = (Bitmap) other;
      result = (bitSize() == bm.bitSize());
      if (bitSize() == bm.bitSize()) {
        int i = 0;
        while ((i < bitSize()) && (testBit(i) == bm.testBit(i))) {
          i++;
        }
        result = (i == bitSize());
      }
    }
    return result;
  }

  @Override
  public void flipBit(final int bitIndex) {

    if (testBit(bitIndex)) {
      clearBit(bitIndex);
    } else {
      setBit(bitIndex);
    }
  }

  @Override
  public int hsb() {

    // find the highest set bit
    int j = 0;
    while ((j < bitSize()) && !testBit(j)) {
      j++;
    }
    return (j == bitSize() ? -1 : j);
  }

  @Override
  public int hub() {

    // find the highest unset bit
    int j = 0;
    while ((j < bitSize()) && testBit(j)) {
      j++;
    }
    return (j == bitSize() ? -1 : j);
  }

  @Override
  public void inc() {

    int bit = lub();
    if (bit >= 0) {
      setBit(bit);
      if (bit < bitSize()) {
        for (int i = bit + 1; i < bitSize(); i++) {
          clearBit(i);
        }
      }
    } else {
      setBits(false);
    }
  }

  @Override
  public int lsb() {

    // find the lowest set bit
    int j = bitSize() - 1;
    while ((j >= 0) && !testBit(j)) {
      j--;
    }
    return j;
  }

  @Override
  public int lub() {

    // find the lowest unset bit
    int j = bitSize() - 1;
    while ((j >= 0) && testBit(j)) {
      j--;
    }
    return j;
  }

  @Override
  public void nand(final Bitmap other) {

    Bitmap bm = comparableInstance(other);
    for (int i = 0; i < bitSize(); i++) {
      defBit(i, !(testBit(i) & bm.testBit(i)));
    }
  }

  @Override
  public void not() {

    for (int i = 0; i < bitSize(); i++) {
      flipBit(i);
    }
  }

  @Override
  public void or(final Bitmap other) {

    Bitmap bm = comparableInstance(other);
    for (int i = 0; i < bitSize(); i++) {
      defBit(i, (testBit(i) | bm.testBit(i)));
    }
  }

  @Override
  public void setBits(final boolean value) {

    for (int i = 0; i < bitSize(); i++) {
      defBit(i, value);
    }
  }

  @Override
  public String toString() {

    String result = "";
    for (int i = 0; i < bitSize(); i++) {
      result += (testBit(i) ? Bitmap.CH_ONE : Bitmap.CH_ZERO);
    }
    return result;
  }

  @Override
  public void xor(final Bitmap other) {

    Bitmap bm = comparableInstance(other);
    for (int i = 0; i < bitSize(); i++) {
      defBit(i, (testBit(i) ^ bm.testBit(i)));
    }
  }

  // ***********************
  // * PROTECTED METHODS
  // ***********************

  /**
   * Allocates the storage space for bitSize() bits.
   * 
   */
  protected abstract void allocateBitmap();

  protected void copyFrom(final Bitmap other) {

    for (int i = 0; i < bitSize(); i++) {
      defBit(i, other.testBit(i));
    }
  }

  protected void copyFrom(final String strBitmap) {

    for (int i = 0; i < bitSize(); i++) {
      defBit(i, strBitmap.charAt(i) == Bitmap.CH_ONE);
    }
  }

  /**
   * Creates an instance of the implementing class based on the given
   * parameters.
   * 
   */
  protected abstract Bitmap createInstance(final Bitmap other);

  protected abstract Bitmap createInstance(final int capacity);

  protected abstract Bitmap createInstance(final int capacity,
      final boolean isSet);

  protected abstract Bitmap createInstance(final String strBitmap);
}