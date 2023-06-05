import model.JavaFile;
import instrument.Instrumenter;
import misc.Util;
import profile.Profiler;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.stream.Stream;

import static misc.Util.assertJavaSourceFile;

public class Main {

  public static void main(String[] args) {
    if (args.length == 0 || args[0].equals("-h") || args[0].equals("--help")) {
      printUsage();
      return;
    }
    switch (args[0]) {
      case "-i", "--instrument-only" -> {
        if (args.length != 2) {
          printUsage();
          return;
        }
        instrumentOnly(args[1]);
      }
      case "-r", "--generate-report" -> {
        if (args.length != 1) {
          printUsage();
          return;
        }
        generateReportOnly();
      }
      case "-d", "--sources-directory" -> {
        if (args.length < 3) {
          printUsage();
          return;
        }
        Path instrumentDir = Path.of(args[1]);
        Path mainFile = Path.of(args[2]);
        String[] programArgs = Arrays.copyOfRange(args, 2, args.length);
        instrumentFolderCompileAndRun(instrumentDir, mainFile, programArgs);
      }
      default -> {
        String[] programArgs = Arrays.copyOfRange(args, 1, args.length);
        Path mainFile = Path.of(args[1]);
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
  }

  private static void instrumentSingleFile(Path file) {
    assertJavaSourceFile(file);
    JavaFile mainJavaFile = new JavaFile(file);
    Instrumenter instrumenter = new Instrumenter(mainJavaFile);
    instrumenter.analyzeFiles();
    instrumenter.instrumentFiles();
    instrumenter.exportMetadata();
  }

  private static void instrumentFolder(Path folder) {
    JavaFile[] javaFiles = getJavaFilesInFolder(folder, null);
    Instrumenter instrumenter = new Instrumenter(javaFiles);
    instrumenter.analyzeFiles();
    instrumenter.instrumentFiles();
    instrumenter.exportMetadata();
  }

  private static void instrumentFolderCompileAndRun(Path instrumentDir, Path mainFile, String[] programArgs) {
    JavaFile mainJavaFile = new JavaFile(mainFile, instrumentDir);
    JavaFile[] additionalJavaFiles = getJavaFilesInFolder(instrumentDir, mainFile);
    Instrumenter instrumenter = new Instrumenter(Util.prependToArray(additionalJavaFiles, mainJavaFile));
    instrumenter.analyzeFiles();
    instrumenter.instrumentFiles();
    instrumenter.exportMetadata();
    Profiler profiler = new Profiler(mainJavaFile, additionalJavaFiles);
    profiler.compileInstrumented();
    profiler.profile(programArgs);
    profiler.generateReport();
  }

  private static void instrumentCompileAndRun(Path mainFile, String[] programArgs) {
    assertJavaSourceFile(mainFile);
    JavaFile mainJavaFile = new JavaFile(mainFile);
    Instrumenter instrumenter = new Instrumenter(mainJavaFile);
    instrumenter.analyzeFiles();
    instrumenter.instrumentFiles();
    instrumenter.exportMetadata();
    Profiler profiler = new Profiler(mainJavaFile);
    profiler.compileInstrumented();
    profiler.profile(programArgs);
    profiler.generateReport();
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
        Usage: <main class> [option] [<main-file>] [arguments]
        Options:
          -h, --help                        Display this message and quit
          -d, --sources-directory <dir>     directory containing java files to additionally instrument
          -i, --instrument-only <file|dir>  only instrument a single file or directory of java files
          -r, --generate-report             only generate a report from metadata and counts
        Main file:    the path to the main java file; after instrumentation it will be compiled and run
        Arguments:    will be passed to the main java class if given
        """);
  }
}
