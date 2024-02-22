package tool.cli;

import common.Util;
import tool.instrument.Instrumenter;
import tool.model.JavaFile;
import tool.profile.Profiler;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

import static common.Util.isJavaFile;

/**
 * Main class of the profiler tool.
 * <p>
 * Parses the command line arguments and calls the appropriate methods.
 */
public class Main {
  /**
   * Main entry point of the profiler tool.
   *
   * @param args the command line arguments specifying options and the main file to profile
   */
  public static void main(String[] args) {
    Arguments arguments = null;
    try {
      arguments = Arguments.parse(args);
    } catch (IllegalArgumentException e) {
      System.out.println(e.getMessage());
      System.out.println("Use -h for help.");
      System.exit(1);
    }
    if (arguments == null) return;
    switch (arguments.runMode()) {
      case REPORT_ONLY -> generateReportOnly();
      case INSTRUMENT_ONLY -> instrumentOnly(arguments);
      case DEFAULT -> instrumentCompileAndRun(arguments);
    }
  }

  private static void generateReportOnly() {
    Profiler profiler = new Profiler(null);
    profiler.generateReport();
    profiler.createSymLinkForReport();
  }

  private static void instrumentOnly(Arguments arguments) {
    JavaFile[] javaFiles;
    if (arguments.targetPath().toFile().isFile()) {
      javaFiles = new JavaFile[]{new JavaFile(arguments.targetPath())};
    } else {
      javaFiles = getJavaFilesInFolder(arguments.targetPath(), null);
    }
    Instrumenter instrumenter = new Instrumenter(javaFiles, arguments);
    instrumenter.analyzeFiles();
    instrumenter.instrumentFiles();
    instrumenter.exportMetadata();
  }

  private static void instrumentCompileAndRun(Arguments arguments) {
    JavaFile mainJavaFile;
    JavaFile[] additionalJavaFiles = new JavaFile[0];
    if (arguments.sourcesDir() != null) {
      mainJavaFile = new JavaFile(arguments.targetPath(), arguments.sourcesDir());
      additionalJavaFiles = getJavaFilesInFolder(arguments.sourcesDir(), arguments.targetPath());
    } else {
      mainJavaFile = new JavaFile(arguments.targetPath());
    }
    JavaFile[] allJavaFiles = Util.prependToArray(additionalJavaFiles, mainJavaFile);
    Instrumenter instrumenter = new Instrumenter(allJavaFiles, arguments);
    instrumenter.analyzeFiles();
    instrumenter.instrumentFiles();
    instrumenter.exportMetadata();
    Profiler profiler = new Profiler(mainJavaFile, additionalJavaFiles);
    profiler.compileInstrumented();
    profiler.profile(arguments.programArgs());
    profiler.generateReport();
    profiler.createSymLinkForReport();
  }

  private static JavaFile[] getJavaFilesInFolder(Path sourcesFolder, Path exceptFor) {
    try (Stream<Path> walk = Files.walk(sourcesFolder)) {
      return walk
          .filter(path -> !path.equals(exceptFor) && isJavaFile(path))
          .filter(path -> !path.getFileName().toString().equals("package-info.java"))
          .filter(path -> !path.getFileName().toString().equals("module-info.java"))
          .map(sourceFile -> new JavaFile(sourceFile, sourcesFolder))
          .toArray(JavaFile[]::new);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
