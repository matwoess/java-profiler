package common;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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

  public static int runCommand(String... command) {
    return runCommandInDir(null, command);
  }

  public static int runCommandInDir(Path cwd, String... command) {
    ProcessBuilder builder = new ProcessBuilder()
        .inheritIO()
        .command(command);
    if (cwd != null) {
      builder.directory(cwd.toFile());
    }
    try {
      Process process = builder.start();
      return process.waitFor();
    } catch (IOException | InterruptedException e) {
      throw new RuntimeException(e);
    }
  }

  public static String[] runCommandAndGetOutput(Path cwd, String... command) {
    ProcessBuilder builder = new ProcessBuilder().directory(cwd.toFile()).command(command);
    List<String> output = new ArrayList<>();
    try {
      Process process = builder.start();
      BufferedReader stdIn = new BufferedReader(new InputStreamReader(process.getInputStream()));
      BufferedReader stdErr = new BufferedReader(new InputStreamReader(process.getErrorStream()));
      process.waitFor();
      stdIn.lines().forEach(output::add);
      stdErr.lines().forEach(output::add);
    } catch (IOException | InterruptedException e) {
      throw new RuntimeException(e);
    }
    return output.toArray(String[]::new);
  }

}
