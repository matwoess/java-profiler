package common;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileSystemException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.stream.Stream;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

public class IO {
  public static Path outputDir = Path.of(".profiler");

  public static Path getOutputDir() {
    return outputDir;
  }

  public static Path getInstrumentDir() {
    return getOutputDir().resolve("instrumented");
  }

  public static Path getClassesDir() {
    return getOutputDir().resolve("classes");
  }

  public static Path getInstrumentedFilePath(Path relativePath) {
    return getInstrumentDir().resolve(relativePath);
  }

  public static Path getAuxiliaryCounterInstrumentPath() {
    return getInstrumentDir().resolve("auxiliary").resolve("__Counter.class");
  }

  public static Path getAuxiliaryCounterClassPath() {
    return getClassesDir().resolve("auxiliary").resolve("__Counter.class");
  }

  public static Path getMetadataPath() {
    return getOutputDir().resolve("metadata.dat");
  }

  public static Path getCountsPath() {
    return getOutputDir().resolve("counts.dat");
  }

  public static Path getReportDir() {
    return getOutputDir().resolve("report");
  }

  public static Path getReportIndexPath() {
    return getReportDir().resolve("index.html");
  }

  public static Path getReportHighlighterPath() {
    return getReportDir().resolve("highlighter.js");
  }

  public static Path getReportIndexSymLinkPath() {
    return Path.of(".", "report.html");
  }

  public static Path getReportMethodIndexPath(String clasName) {
    return IO.getReportDir().resolve("index_" + clasName + ".html");
  }

  public static Path getReportSourceFilePath(Path relativePath) {
    Path reportFilePath = IO.getReportDir().resolve("source").resolve(relativePath);
    return reportFilePath.resolveSibling(reportFilePath.getFileName().toString().replace(".java", ".html"));
  }

  public static Path getUserHomeDir() {
    return Path.of(System.getProperty("user.home"));
  }

  public static Path getUIParametersPath() {
    return getOutputDir().resolve("parameters.dat");
  }

  public static Path lastProjectPath() {
    return Path.of("lastProjectRootDirectory.txt");
  }

  public static boolean isChildPath(Path child, Path parent) {
    return child.toAbsolutePath().startsWith(parent.toAbsolutePath());
  }

  public static <T> void copyResource(Class<T> resourceClass, String resourceName, Path destination) {
    try (InputStream resource = resourceClass.getClassLoader().getResourceAsStream(resourceName)) {
      if (resource == null) {
        throw new RuntimeException("unable to locate resource: <" + resourceName + ">");
      }
      IO.createDirectoriesIfNotExists(destination);
      Files.copy(resource, destination, REPLACE_EXISTING);
    } catch (IOException | RuntimeException e) {
      throw new RuntimeException(e);
    }
  }

  public static void clearDirectoryContents(Path directory) {
    if (Files.exists(directory)) {
      try (Stream<Path> walk = Files.walk(directory)) {
        walk.sorted(Comparator.reverseOrder())
            .filter(p -> !p.equals(directory))
            .forEach(file -> {
              try {
                Files.delete(file);
              } catch (IOException e) {
                throw new RuntimeException(e);
              }
            });
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }
  }

  @SuppressWarnings("UnusedReturnValue")
  public static boolean createDirectoriesIfNotExists(Path fileOrFolder) {
    if (fileOrFolder.toFile().isDirectory()) {
      return fileOrFolder.toFile().mkdirs();
    } else {
      return fileOrFolder.getParent().toFile().mkdirs();
    }
  }

  public static void createSymbolicLink(Path link, Path target) {
    try {
      if (Files.exists(link) && Files.isSymbolicLink(link)) {
        Files.delete(link);
      }
      Files.createSymbolicLink(link, target);
    } catch (FileSystemException e) {
      System.out.println(e.getMessage());
      System.out.println("Unable to create report symlink. Not supported or allowed by file system.");
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
