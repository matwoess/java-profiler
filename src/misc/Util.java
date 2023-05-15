package misc;

import java.util.Arrays;

public class Util {
  @SafeVarargs
  public static <T> T[] prependToArray(T[] programArgs, T... prependValues) {
    T[] extendedArray = Arrays.copyOf(prependValues, prependValues.length + programArgs.length);
    System.arraycopy(programArgs, 0, extendedArray, prependValues.length, programArgs.length);
    return extendedArray;
  }
}
