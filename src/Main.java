import instrument.Instrumenter;
import misc.Util;
import model.JavaFile;
import profile.Profiler;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.stream.Stream;

import static misc.Util.assertJavaSourceFile;

public class Main {
  static boolean syncCounters = false;

  public static void main(String[] args) throws IllegalArgumentException {
    if (args.length == 0) {
      invalidUsage();
    }
    if (args[0].equals("-h") || args[0].equals("--help")) {
      printUsage();
      return;
    }
    if (args[0].equals("-s") || args[0].equals("--synchronized")) {
      syncCounters = true;
      args = Arrays.copyOfRange(args, 1, args.length);
      if (args.length == 0) invalidUsage();
    }
    switch (args[0]) {
      case "-i", "--instrument-only" -> {
        if (args.length != 2) invalidUsage();
        instrumentOnly(args[1]);
      }
      case "-r", "--generate-report" -> {
        if (args.length != 1) invalidUsage();
        generateReportOnly();
      }
      case "-d", "--sources-directory" -> {
        if (args.length < 3) invalidUsage();
        Path instrumentDir = Path.of(args[1]);
        Path mainFile = Path.of(args[2]);
        String[] programArgs = Arrays.copyOfRange(args, 2, args.length);
        instrumentFolderCompileAndRun(instrumentDir, mainFile, programArgs);
      }
      default -> {
        String[] programArgs = Arrays.copyOfRange(args, 1, args.length);
        Path mainFile = Path.of(args[0]);
        instrumentCompileAndRun(mainFile, programArgs);
      }
    }
  }

  private static void instrumentOnly(String target) {
    Path targetPath = Path.of(target);
    boolean targetIsFile = targetPath.toFile().isFile();
    if (targetIsFile) {
      instrumentSingleFile(targetPath);
    } else {
      instrumentFolder(targetPath);
    }
  }

  private static void generateReportOnly() {
    Profiler profiler = new Profiler(null);
    profiler.generateReport();
    profiler.createSymLinkForReport();
  }

  private static void instrumentSingleFile(Path file) {
    assertJavaSourceFile(file);
    JavaFile mainJavaFile = new JavaFile(file);
    Instrumenter instrumenter = new Instrumenter(syncCounters, mainJavaFile);
    instrumenter.analyzeFiles();
    instrumenter.instrumentFiles();
    instrumenter.exportMetadata();
  }

  private static void instrumentFolder(Path folder) {
    JavaFile[] javaFiles = getJavaFilesInFolder(folder, null);
    Instrumenter instrumenter = new Instrumenter(syncCounters, javaFiles);
    instrumenter.analyzeFiles();
    instrumenter.instrumentFiles();
    instrumenter.exportMetadata();
  }

  private static void instrumentFolderCompileAndRun(Path instrumentDir, Path mainFile, String[] programArgs) {
    assertJavaSourceFile(mainFile);
    JavaFile mainJavaFile = new JavaFile(mainFile, instrumentDir);
    JavaFile[] additionalJavaFiles = getJavaFilesInFolder(instrumentDir, mainFile);
    Instrumenter instrumenter = new Instrumenter(syncCounters, Util.prependToArray(additionalJavaFiles, mainJavaFile));
    instrumenter.analyzeFiles();
    instrumenter.instrumentFiles();
    instrumenter.exportMetadata();
    Profiler profiler = new Profiler(mainJavaFile, additionalJavaFiles);
    profiler.compileInstrumented();
    profiler.profile(programArgs);
    profiler.generateReport();
    profiler.createSymLinkForReport();
  }

  private static void instrumentCompileAndRun(Path mainFile, String[] programArgs) {
    assertJavaSourceFile(mainFile);
    JavaFile mainJavaFile = new JavaFile(mainFile);
    Instrumenter instrumenter = new Instrumenter(syncCounters, mainJavaFile);
    instrumenter.analyzeFiles();
    instrumenter.instrumentFiles();
    instrumenter.exportMetadata();
    Profiler profiler = new Profiler(mainJavaFile);
    profiler.compileInstrumented();
    profiler.profile(programArgs);
    profiler.generateReport();
    profiler.createSymLinkForReport();
  }

  private static JavaFile[] getJavaFilesInFolder(Path sourcesFolder, Path mainFile) {
    try (Stream<Path> walk = Files.walk(sourcesFolder)) {
      return walk
          .filter(path -> Files.isRegularFile(path) && !path.equals(mainFile) && Util.isJavaFile(path))
          .map(sourceFile -> new JavaFile(sourceFile, sourcesFolder))
          .toArray(JavaFile[]::new);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }

  }

  static void printUsage() {
    System.out.println("""
        Usage: <main class> [sync] [option] [<main-file>] [arguments]
        Sync:
           -s, --synchronized               instrument using synchronized counters
        Options:
          -h, --help                        display this message and quit
          -d, --sources-directory <dir>     directory containing java files to additionally instrument
          -i, --instrument-only <file|dir>  only instrument a single file or directory of java files
          -r, --generate-report             only generate a report from metadata and counts
        Main file:    the path to the main java file; after instrumentation it will be compiled and run
        Arguments:    will be passed to the main java class if given
        """);
  }

  static void invalidUsage() throws IllegalArgumentException {
    printUsage();
    throw new IllegalArgumentException("invalid arguments");
  }
}
