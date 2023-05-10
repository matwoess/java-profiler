import common.JavaFile;
import instrument.Instrumenter;
import profile.Profiler;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

import static java.lang.System.exit;

public class Main {

  public static void main(String[] args) {
    if (args.length == 0 || args.length > 2 || args[0].equals("-h") || args[0].equals("--help")) {
      printUsage();
      exit(0);
    }
    Path sourcesFolder = Path.of(args[0]);
    Path mainFile = Path.of(args[1]);
    JavaFile mainJavaFile = new JavaFile(mainFile, sourcesFolder);
    JavaFile[] additionalJavaFiles = getAdditionalFilesToInstrument(sourcesFolder, mainFile);
    Instrumenter instrumenter = new Instrumenter(mainJavaFile, additionalJavaFiles);
    instrumenter.analyzeFiles();
    instrumenter.instrumentFiles();
    instrumenter.exportBlockData();
    Profiler profiler = new Profiler(mainJavaFile, additionalJavaFiles);
    profiler.compileInstrumented();
    profiler.profile();
    profiler.generateReport();
  }

  private static JavaFile[] getAdditionalFilesToInstrument(Path sourcesFolder, Path mainFile) {
    try (Stream<Path> walk = Files.walk(sourcesFolder)) {
      return walk
          .filter(path -> Files.isRegularFile(path) && !path.equals(mainFile) && path.toString().endsWith(".java"))
          .map(sourceFile -> new JavaFile(sourceFile, sourcesFolder))
          .toArray(JavaFile[]::new);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }

  }

  static void printUsage() {
    System.out.println("""
        Usage: <main class> [-h] FOLDER FILE
          FOLDER          Folder containing java source files. All java files in it will be instrumented.
          FILE            Main java class file (entry point). Will be instrumented and executed.
          -h,--help       Display this message and quit.
        """);
  }
}
