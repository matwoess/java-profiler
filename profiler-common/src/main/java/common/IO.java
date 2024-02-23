package common;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileSystemException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.stream.Stream;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

/**
 * Helper class containing methods related to relative path finding and filesystem operations.
 * <p>
 * The {@link #outputDir} is the most important member variable.
 * It specifies the root directory for all the tool's output files.
 * No other paths are statically defined.
 * All subdirectories and file locations can be retrieved by corresponding get methods,
 * that resolve their file path relative to the <code>outputDir</code> member.
 */
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
   * Returns the <code>instrumented<code> directory relative to the {@link #outputDir}.
   */
  public static Path getInstrumentDir() {
    return getOutputDir().resolve("instrumented");
  }

  /**
   * Returns the <code>classes<code> directory relative to the {@link #outputDir}.
   */
  public static Path getClassesDir() {
    return getOutputDir().resolve("classes");
  }

  /**
   * Returns the path where the instrumented version of a Java source file is stored.
   * The tool will use this method to replicate the original directory structure for instrumented copies.
   *
   * @param relativePath the relative path of the original file to the sources directory.
   *                     Every <code>JavaFile</code> instance knows its relative location to the sources.
   * @return the path of an instrumented copy relative to the instrumented directory
   */
  public static Path getInstrumentedFilePath(Path relativePath) {
    return getInstrumentDir().resolve(relativePath);
  }

  /**
   * To successfully <b>compile</b> instrumented versions of source files, an import statement is added to all source files.
   * A <code>__Counter.class</code> file is placed in an "auxiliary" package directory to resolve these imports.
   *
   * @return where the pre-compiled <code>__Counter.class</code> class will be copied to before compilation
   */
  public static Path getAuxiliaryCounterInstrumentPath() {
    return getInstrumentDir().resolve("auxiliary").resolve("__Counter.class");
  }

  /**
   * To successfully <b>run</b> instrumented versions of source files, an import statement is added to all source files.
   * A <code>__Counter.class</code> file is placed in an auxiliary package directory to resolve this dependency.
   *
   * @return where the pre-compiled <code>__Counter.class</code> class will be copied to before running the instrumented code.
   */
  public static Path getAuxiliaryCounterClassPath() {
    return getClassesDir().resolve("auxiliary").resolve("__Counter.class");
  }

  /**
   * Returns the path to the parsed project's metadata file.
   * This data is used for instrumentation and report generation.
   *
   * @return <code>metadata.dat<code> relative to the {@link #outputDir}
   */
  public static Path getMetadataPath() {
    return getOutputDir().resolve("metadata.dat");
  }

  /**
   * Returns the path to the resulting counts file.
   * This data is written as soon as the instrumented version is run and is used for report generation.
   *
   * @return <code>counts.dat<code> relative to the {@link #outputDir}
   */
  public static Path getCountsPath() {
    return getOutputDir().resolve("counts.dat");
  }

  /**
   * Returns the containing root directory for the generated report.
   *
   * @return <code>report<code> relative to the {@link #outputDir}
   */
  public static Path getReportDir() {
    return getOutputDir().resolve("report");
  }

  /**
   * Returns the file path of the main report classes overview.
   *
   * @return <code>index.html</code> relative to the report directory
   */
  public static Path getReportIndexPath() {
    return getReportDir().resolve("index.html");
  }

  /**
   * Returns the path of the jQuery helper script to visualize blocks and regions in the report.
   *
   * @return <code>highlighter.js</code> relative to the report directory
   */
  public static Path getReportHighlighterPath() {
    return getReportDir().resolve("highlighter.js");
  }

  /**
   * Returns the target path for the symlink pointing to the main report index file.
   *
   * @return <code>report.html</code> relative to the current directory
   */
  public static Path getReportIndexSymLinkPath() {
    return Path.of(".", "report.html");
  }

  /**
   * Returns the path to the report method index of a java class.
   * Every parsed class gets its own one during report generation.
   *
   * @param className the name of a parsed java class
   * @return "index_" + <code>className</code> relative to the report directory
   */
  public static Path getReportMethodIndexPath(String className) {
    return IO.getReportDir().resolve("index_" + className + ".html");
  }

  /**
   * Returns the file path of an annotated HTML source file.
   * The relative path of the original source file to its sources directory should be passed to the method.
   * This way the package structure is replicated inside the <code>sources</code> report subdirectory.
   * The ".java" suffix is replaced by ".html" to create the new file name.
   *
   * @param relativePath the relative path from a <code>JavaFile</code> class relative to its sources directory
   * @return the location of the annotated report source code file
   */
  public static Path getReportSourceFilePath(Path relativePath) {
    Path reportFilePath = IO.getReportDir().resolve("source").resolve(relativePath);
    return reportFilePath.resolveSibling(reportFilePath.getFileName().toString().replace(".java", ".html"));
  }

  /**
   * {@return the home directory of the current user}
   */
  public static Path getUserHomeDir() {
    return Path.of(System.getProperty("user.home"));
  }

  /**
   * Returns where the configured FxUI parameters are persisted to inside the output directory.
   * @return <code>parameters.dat</code> relative to the {@link #outputDir}
   */
  public static Path getUIParametersPath() {
    return getOutputDir().resolve("parameters.dat");
  }

  /**
   * Returns the path of the file for storing the previously opened project directory.
   * This is read when re-starting the FxUI application and pre-filled as the project to open.
   * @return <code>lastProjectRootDirectory.txt</code> relative to the current directory
   */
  public static Path lastProjectPath() {
    return Path.of("lastProjectRootDirectory.txt");
  }

  /**
   * Extract a copy of a resource file from the project directory or the .jar file.
   *
   * @param resourceClass the anchor class used to locate the resource
   * @param resourceName  the name of the resource file to be extracted
   * @param destination   path where the resource is copied to
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
   * @param directory the root directory that should be cleared of contents recursively
   */
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

  /**
   * Creates all parent directories in the path description to ensure successful creation of a new file in it.
   *
   * @param fileOrFolder if a file is specified, all the parent directories will be created.
   *                     If a directory is specified, all parent directories including the given one will be created.
   * @return whether the operation was successful
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
   * Creates a symbolic link at a given path pointing to another file path.
   * Will work on Linux and macOS but might fail on Windows due to group policies.
   *
   * @param link   the location of the linking file shortcut
   * @param target what the link should point to
   */
  public static void createSymbolicLink(Path link, Path target) {
    try {
      if (Files.exists(link) && Files.isSymbolicLink(link)) {
        Files.delete(link);
      }
      Files.createSymbolicLink(link, target);
    } catch (FileSystemException e) {
      System.err.println(e.getMessage());
      System.out.println("Unable to create report symlink. Not supported or allowed by file system.");
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
