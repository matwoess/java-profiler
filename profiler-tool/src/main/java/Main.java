import instrument.Instrumenter;
import misc.IO;
import misc.Util;
import model.JavaFile;
import profile.Profiler;

import java.nio.file.Path;
import java.util.Arrays;

import static misc.IO.DEFAULT_OUT_DIR;
import static misc.Util.assertJavaSourceFile;

public class Main {
  public static void main(String[] args) throws IllegalArgumentException {
    if (args.length == 0) {
      invalidUsage();
    }
    IO.outputDir = DEFAULT_OUT_DIR;
    boolean instrumentOnly = false, reportOnly = false, syncCounters = false, verboseOutput = false;
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
        case "-i", "--instrument-only" -> instrumentOnly = true;
        case "-r", "--generate-report" -> reportOnly = true;
        case "-o", "--out-directory" -> {
          if (i + 1 >= args.length) invalidUsage();
          IO.outputDir = Path.of(args[++i]);
          assert IO.outputDir.toFile().isDirectory() : "not a directory: " + sourcesDir;
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
    if (instrumentOnly && reportOnly) invalidUsage();
    if (reportOnly) {
      if (args.length > 0) invalidUsage();
      generateReportOnly();
    } else if (instrumentOnly) {
      if (args.length != 1) invalidUsage();
      Path target = Path.of(args[0]);
      instrumentOnly(target, syncCounters, verboseOutput);
    } else {
      if (args.length == 0) invalidUsage();
      Path mainFile = Path.of(args[0]);
      assertJavaSourceFile(mainFile);
      String[] programArgs = Arrays.copyOfRange(args, 1, args.length);
      instrumentCompileAndRun(sourcesDir, mainFile, programArgs, syncCounters, verboseOutput);
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
      javaFiles = Util.getJavaFilesInFolder(target, null);
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
      additionalJavaFiles = Util.getJavaFilesInFolder(sourcesDir, mainFile);
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

  static void printUsage() {
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

  static void invalidUsage() throws IllegalArgumentException {
    printUsage();
    throw new IllegalArgumentException("invalid arguments");
  }
}
