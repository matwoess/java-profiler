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
  /**
   * Represents the output directory for the profiler.
   * The output directory is used for storing instrumented files, compiled classes, and generated reports.
   * By default, the output directory is set to ".profiler" (of the current working directory).
   */
  public static Path outputDir = Path.of(".profiler");

  public static Path getOutputDir() {
    return outputDir;
  }

  /**
   * @return the <code>instrumented<code> directory relative to the <code>outputDir</code>.
   */
  public static Path getInstrumentDir() {
    return getOutputDir().resolve("instrumented");
  }

  /**
   * @return the <code>classes<code> directory relative to the <code>outputDir</code>.
   */
  public static Path getClassesDir() {
    return getOutputDir().resolve("classes");
  }

  /**
   * The path where the instrumented version of a Java source file is stored.
   * The tool will use this function to place instrumented copies in the same directory structure as from the sources
   * directory, but relative to the instrumented directory.
   *
   * @param relativePath the source file path relative to the sources directory.
   *                     Every <code>JavaFile</code> instance knows its relative location to the sources.
   * @return the relative path of an instrumented copy according to the instrumented directory.
   */
  public static Path getInstrumentedFilePath(Path relativePath) {
    return getInstrumentDir().resolve(relativePath);
  }

  /**
   * To successfully compile instrumented versions of source files, an import statement is added to all source files.
   * A <code>__Counter.class</code> file is placed in an auxiliary package directory to resolve these imports.
   *
   * @return where the pre-compiled <code>__Counter.class</code> class will be copied to for instrumentation.
   */
  public static Path getAuxiliaryCounterInstrumentPath() {
    return getInstrumentDir().resolve("auxiliary").resolve("__Counter.class");
  }

  /**
   * To successfully run instrumented versions of source files, an import statement is added to all source files.
   * A <code>__Counter.class</code> file is placed in an auxiliary package directory to resolve this dependency.
   *
   * @return where the pre-compiled <code>__Counter.class</code> class will be copied to for running the instrumented code.
   */
  public static Path getAuxiliaryCounterClassPath() {
    return getClassesDir().resolve("auxiliary").resolve("__Counter.class");
  }

  /**
   * @return the <code>metadata.dat<code> file relative to the <code>outputDir</code>.
   */
  public static Path getMetadataPath() {
    return getOutputDir().resolve("metadata.dat");
  }

  /**
   * @return the <code>counts.dat<code> file relative to the <code>outputDir</code>.
   */
  public static Path getCountsPath() {
    return getOutputDir().resolve("counts.dat");
  }

  /**
   * @return the <code>report<code> directory relative to the <code>outputDir</code>.
   */
  public static Path getReportDir() {
    return getOutputDir().resolve("report");
  }

  /**
   * @return the class-index file path of the generated report.
   */
  public static Path getReportIndexPath() {
    return getReportDir().resolve("index.html");
  }

  /**
   * @return the path of the jQuery <code>highlighter.js</code> helper script to visualize blocks and regions in the report.
   */
  public static Path getReportHighlighterPath() {
    return getReportDir().resolve("highlighter.js");
  }

  /**
   * @return the path for the symlink to the main report index file.
   */
  public static Path getReportIndexSymLinkPath() {
    return Path.of(".", "report.html");
  }

  /**
   * @param clasName the name of the class. Will be appended as "index_" + <code>className</code>.
   * @return the path to the methods-index for a class.
   */
  public static Path getReportMethodIndexPath(String clasName) {
    return IO.getReportDir().resolve("index_" + clasName + ".html");
  }

  /**
   * @param relativePath the relative path from the <code>JavaFile</code> class relative to the sources directory.
   *                     The ".java" suffix is replaced by ".html" to create the new file name.
   * @return the location of an annotated report source code file.
   */
  public static Path getReportSourceFilePath(Path relativePath) {
    Path reportFilePath = IO.getReportDir().resolve("source").resolve(relativePath);
    return reportFilePath.resolveSibling(reportFilePath.getFileName().toString().replace(".java", ".html"));
  }

  /**
   * @return the home directory of the current user.
   */
  public static Path getUserHomeDir() {
    return Path.of(System.getProperty("user.home"));
  }

  /**
   * @return where the configured FxUI parameters are persisted to inside the project directory's <code>outDir</code>.
   */
  public static Path getUIParametersPath() {
    return getOutputDir().resolve("parameters.dat");
  }

  /**
   * @return location of the file for storing the previously opened project directory path.
   */
  public static Path lastProjectPath() {
    return Path.of("lastProjectRootDirectory.txt");
  }

  public static boolean isChildPath(Path child, Path parent) {
    return child.toAbsolutePath().startsWith(parent.toAbsolutePath());
  }

  /**
   * Extract a copy of a resource file from the project directory or the .jar file.
   *
   * @param resourceClass the anchor class used to locate the resource.
   * @param resourceName  the name of the resource file to be extracted.
   * @param destination   path where to copy the resource is copied to.
   */
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

  /**
   * Deletes all files inside a directory.
   * Used to clean up output files before the next execution.
   * The root directory itself won't be removed to avoid locking errors.
   *
   * @param directory the root directory that should be cleared of contents recursively.
   */  public static void clearDirectoryContents(Path directory) {
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

  /**
   * Creates all parent directories in the path description to ensure successful creation of a new file in it.
   *
   * @param fileOrFolder if a file is specified, all the parent directories will be created.
   *                     If a directory is specified, all parent directories including the given path will be created.
   * @return whether the operation was successful.
   */
  @SuppressWarnings("UnusedReturnValue")
  public static boolean createDirectoriesIfNotExists(Path fileOrFolder) {
    if (fileOrFolder.toFile().isDirectory()) {
      return fileOrFolder.toFile().mkdirs();
    } else {
      return fileOrFolder.getParent().toFile().mkdirs();
    }
  }

  /**
   * Creates a symbolic link at a given path location pointing to another path.
   * Will work on Linux and macOS but might fail on Windows due to group policies.
   *
   * @param link   the location of the linking file shortcut.
   * @param target what the link should point to.
   */
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
