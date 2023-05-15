import common.JavaFile;
import instrument.Instrumenter;
import profile.Profiler;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.stream.Stream;

public class Main {

  public static void main(String[] args) {
    if (args.length == 0 || args[0].equals("-h") || args[0].equals("--help")) {
      printUsage();
      return;
    }
    if (args[0].equals("-i") || args[0].equals("--instrument-only")) {
      if (args.length != 2) {
        printUsage();
        return;
      }
      instrumentOnly(args[1]);
    } else if (args[0].equals("-d") || args[0].equals("--sources-directory")) {
      if (args.length < 3) {
        printUsage();
        return;
      }
      Path instrumentDir = Path.of(args[1]);
      Path mainFile = Path.of(args[2]);
      String[] programArgs = Arrays.copyOfRange(args, 2, args.length);
      instrumentFolderCompileAndRun(instrumentDir, mainFile, programArgs);
    } else {
      String[] programArgs = Arrays.copyOfRange(args, 1, args.length);
      Path mainFile = Path.of(args[1]);
      instrumentCompileAndRun(mainFile, programArgs);
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

  private static void instrumentSingleFile(Path file) {
    JavaFile mainJavaFile = new JavaFile(file, file.getParent());
    Instrumenter instrumenter = new Instrumenter(mainJavaFile);
    instrumenter.analyzeFiles();
    instrumenter.instrumentFiles();
    instrumenter.exportBlockData();
  }

  private static void instrumentFolder(Path folder) {
    JavaFile[] javaFiles = getJavaFilesInFolder(folder, null);
    Instrumenter instrumenter = new Instrumenter(null, javaFiles);
    instrumenter.analyzeFiles();
    instrumenter.instrumentFiles();
    instrumenter.exportBlockData();
  }

  private static void instrumentFolderCompileAndRun(Path instrumentDir, Path mainFile, String[] programArgs) {
    JavaFile mainJavaFile = new JavaFile(mainFile, instrumentDir);
    JavaFile[] additionalJavaFiles = getJavaFilesInFolder(instrumentDir, mainFile);
    Instrumenter instrumenter = new Instrumenter(mainJavaFile, additionalJavaFiles);
    instrumenter.analyzeFiles();
    instrumenter.instrumentFiles();
    instrumenter.exportBlockData();
    Profiler profiler = new Profiler(mainJavaFile, additionalJavaFiles);
    profiler.compileInstrumented();
    profiler.profile(programArgs);
    profiler.generateReport();
  }

  private static void instrumentCompileAndRun(Path mainFile, String[] programArgs) {
    JavaFile mainJavaFile = new JavaFile(mainFile, mainFile.getParent());
    Instrumenter instrumenter = new Instrumenter(mainJavaFile);
    instrumenter.analyzeFiles();
    instrumenter.instrumentFiles();
    instrumenter.exportBlockData();
    Profiler profiler = new Profiler(mainJavaFile);
    profiler.compileInstrumented();
    profiler.profile(programArgs);
    profiler.generateReport();
  }

  private static JavaFile[] getJavaFilesInFolder(Path sourcesFolder, Path mainFile) {
    try (Stream<Path> walk = Files.walk(sourcesFolder)) {
      return walk
          .filter(path -> Files.isRegularFile(path) && !path.equals(mainFile) && path.toString().endsWith(".java"))
          .map(sourceFile -> new JavaFile(sourceFile, sourcesFolder))
          .toArray(JavaFile[]::new);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }

  }

  static void printUsage() {  // TODO: update
    System.out.println("""
        Usage: <main class> [-h] FOLDER FILE
          FOLDER          Folder containing java source files. All java files in it will be instrumented.
          FILE            Main java class file (entry point). Will be instrumented and executed.
          -h,--help       Display this message and quit.
        """);
  }
}
