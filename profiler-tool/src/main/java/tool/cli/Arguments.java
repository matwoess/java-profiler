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
          return new Arguments(RunMode.HELP, null, null, false, false, null);
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
}
