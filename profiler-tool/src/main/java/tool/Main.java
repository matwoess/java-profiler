package tool;

import common.RunMode;
import common.Util;
import tool.instrument.Instrumenter;
import tool.model.JavaFile;
import tool.profile.Profiler;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.stream.Stream;

import static common.Util.assertJavaSourceFile;
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
  public static void main(String[] args) throws IllegalArgumentException {
    if (args.length == 0) {
      invalidUsage();
    }
    RunMode runMode = RunMode.DEFAULT;
    boolean syncCounters = false, verboseOutput = false;
    Path sourcesDir = null;
    int i = 0;
    for (; i < args.length; i++) {
      if (!args[i].startsWith("-")) {
        break;
      }
      switch (args[i]) {
        case "-h", "--help" -> {
          printUsage();
          return;
        }
        case "-s", "--synchronized" -> syncCounters = true;
        case "-v", "--verbose" -> verboseOutput = true;
        case "-i", "--instrument-only" -> {
          if (runMode != RunMode.DEFAULT) invalidUsage();
          runMode = RunMode.INSTRUMENT_ONLY;
        }
        case "-r", "--generate-report" -> {
          if (runMode != RunMode.DEFAULT) invalidUsage();
          runMode = RunMode.REPORT_ONLY;
        }
        case "-d", "--sources-directory" -> {
          if (i + 1 >= args.length) invalidUsage();
          sourcesDir = Path.of(args[++i]);
          assert sourcesDir.toFile().isDirectory() : "not a directory: " + sourcesDir;
        }
        default -> {
          System.out.println("unknown option: " + args[i]);
          invalidUsage();
        }
      }
    }
    args = Arrays.copyOfRange(args, i, args.length);
    Path targetPath = null;
    if (args.length > 0) {
      targetPath = Path.of(args[0]);
    }
    switch (runMode) {
      case REPORT_ONLY -> {
        if (args.length > 0) invalidUsage();
        generateReportOnly();
      }
      case INSTRUMENT_ONLY -> {
        if (args.length != 1) invalidUsage();
        assert targetPath != null;
        instrumentOnly(targetPath, syncCounters, verboseOutput);
      }
      case DEFAULT ->  {
        if (args.length == 0) invalidUsage();
        assertJavaSourceFile(targetPath);
        String[] programArgs = Arrays.copyOfRange(args, 1, args.length);
        instrumentCompileAndRun(sourcesDir, targetPath, programArgs, syncCounters, verboseOutput);
      }
    }
  }

  private static void generateReportOnly() {
    Profiler profiler = new Profiler(null);
    profiler.generateReport();
    profiler.createSymLinkForReport();
  }

  private static void instrumentOnly(Path target, boolean sync, boolean verbose) {
    JavaFile[] javaFiles;
    if (target.toFile().isFile()) {
      javaFiles = new JavaFile[]{new JavaFile(target)};
    } else {
      javaFiles = getJavaFilesInFolder(target, null);
    }
    Instrumenter instrumenter = new Instrumenter(sync, verbose, javaFiles);
    instrumenter.analyzeFiles();
    instrumenter.instrumentFiles();
    instrumenter.exportMetadata();
  }

  private static void instrumentCompileAndRun(Path sourcesDir, Path mainFile, String[] programArgs, boolean sync, boolean verbose) {
    JavaFile mainJavaFile;
    JavaFile[] additionalJavaFiles = new JavaFile[0];
    if (sourcesDir != null) {
      mainJavaFile = new JavaFile(mainFile, sourcesDir);
      additionalJavaFiles = getJavaFilesInFolder(sourcesDir, mainFile);
    } else {
      mainJavaFile = new JavaFile(mainFile);
    }
    Instrumenter instrumenter = new Instrumenter(sync, verbose, Util.prependToArray(additionalJavaFiles, mainJavaFile));
    instrumenter.analyzeFiles();
    instrumenter.instrumentFiles();
    instrumenter.exportMetadata();
    Profiler profiler = new Profiler(mainJavaFile, additionalJavaFiles);
    profiler.compileInstrumented();
    profiler.profile(programArgs);
    profiler.generateReport();
    profiler.createSymLinkForReport();
  }

  private static JavaFile[] getJavaFilesInFolder(Path sourcesFolder, Path exceptFor) {
    try (Stream<Path> walk = Files.walk(sourcesFolder)) {
      return walk
          .filter(path -> Files.isRegularFile(path) && !path.equals(exceptFor) && isJavaFile(path))
          .map(sourceFile -> new JavaFile(sourceFile, sourcesFolder))
          .toArray(JavaFile[]::new);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private static void printUsage() {
    System.out.println("""
        Usage: profiler [options] <main file> [program args]
        Or   : profiler [options] <run mode>
        Options:
          -h, --help                        display this message and quit
          -s, --synchronized                instrument using synchronized counters increments
          -v, --verbose                     output verbose info about instrumentation of each file
          -d, --sources-directory <dir>     directory containing java files to additionally instrument
        Run mode (exclusive):
          -i, --instrument-only <file|dir>  only instrument a single file or directory of java files and exit
          -r, --generate-report             only generate the report from existing metadata and counts
        Main file:
          The path to the main java file. After instrumentation *it* will be compiled and and executed.
          (Must not be specified for the generate-report run mode)
        Program args:
          Will be passed to the main java class if given
        """);
  }

  private static void invalidUsage() throws IllegalArgumentException {
    printUsage();
    throw new IllegalArgumentException("invalid arguments");
  }
}
