package misc;

import java.nio.file.Path;
import java.util.Arrays;

public class Util {
  @SafeVarargs
  public static <T> T[] prependToArray(T[] array, T... prependValues) {
    T[] extendedArray = Arrays.copyOf(prependValues, prependValues.length + array.length);
    System.arraycopy(array, 0, extendedArray, prependValues.length, array.length);
    return extendedArray;
  }

  public static boolean isJavaFile(Path path) {
    return path.toString().endsWith(".java") && path.toFile().isFile();
  }

  public static void assertJavaSourceFile(Path filePath) {
    if (!Util.isJavaFile(filePath)) {
      throw new RuntimeException(String.format("'%s' is not a java source file!", filePath));
    }
  }
}
