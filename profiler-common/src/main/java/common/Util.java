package common;

import tool.model.JavaFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.stream.Stream;

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

  public static JavaFile[] getJavaFilesInFolder(Path sourcesFolder, Path exceptFor) {
    try (Stream<Path> walk = Files.walk(sourcesFolder)) {
      return walk
          .filter(path -> Files.isRegularFile(path) && !path.equals(exceptFor) && isJavaFile(path))
          .map(sourceFile -> new JavaFile(sourceFile, sourcesFolder))
          .toArray(JavaFile[]::new);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public static int runCommand(Path cwd, String... command) {
    ProcessBuilder builder = new ProcessBuilder()
        .inheritIO()
        .directory(cwd.toFile())
        .command(command);
    builder.redirectOutput(ProcessBuilder.Redirect.PIPE);
    try {
      Process process = builder.start();
      String outputString;
      String errorString;
      try (InputStream processStdOut = process.getInputStream();
           InputStream processStdErr = process.getErrorStream()) {
        outputString = new String(processStdOut.readAllBytes());
        errorString = new String(processStdErr.readAllBytes());
        System.out.println(outputString);
        System.err.println(errorString);
      }
      return process.waitFor();
    } catch (IOException | InterruptedException e) {
      throw new RuntimeException(e);
    }
  }

  public enum OS {
    WINDOWS, LINUX, MAC, SOLARIS;

    public String lineSeparator() {
      if (this == WINDOWS) {
        return "\r\n";
      } else {
        return "\n";
      }
    }
  }

  private static OS os = null;

  public static OS getOS() {
    if (os == null) {
      String osName = System.getProperty("os.name").toLowerCase();
      if (osName.contains("win")) {
        os = OS.WINDOWS;
      } else if (osName.contains("nix") || osName.contains("nux") || osName.contains("aix")) {
        os = OS.LINUX;
      } else if (osName.contains("mac")) {
        os = OS.MAC;
      } else if (osName.contains("sunos")) {
        os = OS.SOLARIS;
      }
    }
    return os;
  }

}
