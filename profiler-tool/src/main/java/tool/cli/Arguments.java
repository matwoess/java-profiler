package tool.cli;

import common.RunMode;

import java.nio.file.Path;
import java.util.Arrays;

import static common.Util.assertJavaSourceFile;

public record Arguments(
    RunMode runMode,
    Path targetPath,
    Path sourcesDir,
    boolean syncCounters,
    boolean verboseOutput,
    String[] programArgs) {

  public static Arguments fromArguments(String[] args) throws IllegalArgumentException {
    if (args.length == 0) {
      invalidArguments();
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
          return null;
        }
        case "-s", "--synchronized" -> syncCounters = true;
        case "-v", "--verbose" -> verboseOutput = true;
        case "-i", "--instrument-only" -> {
          if (runMode != RunMode.DEFAULT) invalidArguments();
          runMode = RunMode.INSTRUMENT_ONLY;
        }
        case "-r", "--generate-report" -> {
          if (runMode != RunMode.DEFAULT) invalidArguments();
          runMode = RunMode.REPORT_ONLY;
        }
        case "-d", "--sources-directory" -> {
          if (i + 1 >= args.length) invalidArguments();
          sourcesDir = Path.of(args[++i]);
          assert sourcesDir.toFile().isDirectory() : "not a directory: " + sourcesDir;
        }
        default -> {
          System.out.println("unknown option: " + args[i]);
          invalidArguments();
        }
      }
    }
    args = Arrays.copyOfRange(args, i, args.length);
    Path targetPath = null;
    String[] programArgs = null;
    if (args.length > 0) {
      targetPath = Path.of(args[0]);
    }
    switch (runMode) {
      case REPORT_ONLY -> {
        if (args.length > 0) invalidArguments();
      }
      case INSTRUMENT_ONLY -> {
        if (args.length != 1) invalidArguments();
      }
      case DEFAULT -> {
        if (args.length == 0) invalidArguments();
        assertJavaSourceFile(targetPath);
        programArgs = Arrays.copyOfRange(args, 1, args.length);
      }
    }
    return new Arguments(runMode, targetPath, sourcesDir, syncCounters, verboseOutput, programArgs);
  }

  public static void invalidArguments() throws IllegalArgumentException {
    throw new IllegalArgumentException("invalid arguments. Use -h for help.");
  }

  public static void printUsage() {
    System.out.println("""
        Usage: profiler [options] <main file> [program args]
        Or   : profiler [options] <run mode>
        Options:
          -h, --help                        display this message and quit
          -s, --synchronized                instrument using synchronized counter increments
          -v, --verbose                     output verbose info about instrumentation of files
          -d, --sources-directory <dir>     directory with additional Java files to instrument
        Run mode (exclusive):
          -i, --instrument-only <file|dir>  only instrument a single file or directory and exit
          -r, --generate-report             only generate the report from metadata and counts
        Main file:
          The path to the main Java file. It will be compiled and and executed after instrumentation.
          (Must not be specified for the generate-report run mode)
        Program args:
          Will be passed to the main method if given
        """);
  }
}
