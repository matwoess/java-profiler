import instrument.Instrumenter;
import misc.IO;
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
  public static void main(String[] args) throws IllegalArgumentException {
    if (args.length == 0) {
      invalidUsage();
    }
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
    }
    else if (instrumentOnly) {
      if (args.length != 1) invalidUsage();
      Path target = Path.of(args[0]);
      instrumentOnly(target, syncCounters, verboseOutput);
    }
    else {
      if (args.length == 0) invalidUsage();
      Path mainFile = Path.of(args[0]);
      assertJavaSourceFile(mainFile);
      String[] programArgs = Arrays.copyOfRange(args, 1, args.length);
      if (sourcesDir != null) {
        instrumentFolderCompileAndRun(sourcesDir, mainFile, programArgs, syncCounters, verboseOutput);
      } else {
        instrumentCompileAndRun(mainFile, programArgs, syncCounters, verboseOutput);
      }
    }
  }

  private static void instrumentOnly(Path targetPath, boolean sync, boolean verbose) {
    boolean targetIsFile = targetPath.toFile().isFile();
    if (targetIsFile) {
      instrumentSingleFile(targetPath, sync, verbose);
    } else {
      instrumentFolder(targetPath, sync, verbose);
    }
  }

  private static void generateReportOnly() {
    Profiler profiler = new Profiler(null);
    profiler.generateReport();
    profiler.createSymLinkForReport();
  }

  private static void instrumentSingleFile(Path file, boolean sync, boolean verbose) {
    assertJavaSourceFile(file);
    JavaFile mainJavaFile = new JavaFile(file);
    Instrumenter instrumenter = new Instrumenter(sync, verbose, mainJavaFile);
    instrumenter.analyzeFiles();
    instrumenter.instrumentFiles();
    instrumenter.exportMetadata();
  }

  private static void instrumentFolder(Path folder, boolean sync, boolean verbose) {
    JavaFile[] javaFiles = getJavaFilesInFolder(folder, null);
    Instrumenter instrumenter = new Instrumenter(sync, verbose, javaFiles);
    instrumenter.analyzeFiles();
    instrumenter.instrumentFiles();
    instrumenter.exportMetadata();
  }

  private static void instrumentFolderCompileAndRun(Path instrumentDir, Path mainFile, String[] programArgs, boolean sync, boolean verbose) {
    assertJavaSourceFile(mainFile);
    JavaFile mainJavaFile = new JavaFile(mainFile, instrumentDir);
    JavaFile[] additionalJavaFiles = getJavaFilesInFolder(instrumentDir, mainFile);
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

  private static void instrumentCompileAndRun(Path mainFile, String[] programArgs, boolean sync, boolean verbose) {
    assertJavaSourceFile(mainFile);
    JavaFile mainJavaFile = new JavaFile(mainFile);
    Instrumenter instrumenter = new Instrumenter(sync, verbose, mainJavaFile);
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
