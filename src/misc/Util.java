package misc;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

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

  public static void copyResource(String resourceName, Path destination) {
    try (InputStream resource = Util.class.getResourceAsStream(resourceName);) {
      if (resource == null) {
        throw new RuntimeException("unable to locate resource: <" + resourceName + ">");
      }
      destination.toFile().mkdirs();
      Files.copy(resource, destination, REPLACE_EXISTING);
    } catch (IOException | RuntimeException e) {
      throw new RuntimeException(e);
    }
  }
}
