package profile;

import common.Constants;
import common.JavaFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

import static common.Constants.auxiliaryInstrumentDir;
import static common.Constants.instrumentDir;
import static java.lang.System.exit;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

public class Profiler {
  JavaFile mainJavaFile;
  JavaFile[] additionalJavaFiles;

  public Profiler(JavaFile mainJavaFile, JavaFile... additionalJavaFiles) {
    this.mainJavaFile = mainJavaFile;
    this.additionalJavaFiles = additionalJavaFiles;
    copyAuxiliaryFiles();
  }

  void copyAuxiliaryFiles() {
    String counterClass = "__Counter.class";
    try (InputStream fileStream = getClass().getResourceAsStream("/auxiliary/" + counterClass)) {
      if (fileStream == null) {
        throw new RuntimeException("unable to locate auxiliary file: <" + counterClass + ">");
      }
      Files.copy(fileStream, auxiliaryInstrumentDir.resolve(Path.of(counterClass)), REPLACE_EXISTING);
    } catch (IOException | RuntimeException e) {
      throw new RuntimeException(e);
    }
  }

  public void compileInstrumented() {
    Path mainFile = mainJavaFile.instrumentedFile;
    ProcessBuilder builder = new ProcessBuilder()
        .inheritIO()
        .directory(instrumentDir.toFile())
        .command("javac", mainFile.getFileName().toString());
    try {
      int exitCode = builder.start().waitFor();
      if (exitCode != 0) {
        System.out.println("Error compiling instrumented files.");
        exit(exitCode);
      }
    } catch (IOException | InterruptedException e) {
      throw new RuntimeException(e);
    }
  }

  public void profile() {
    Path mainFile = mainJavaFile.instrumentedFile;
    String fileName = mainFile.getFileName().toString();
    String classFileName = fileName.substring(0, fileName.lastIndexOf("."));
    ProcessBuilder builder = new ProcessBuilder()
        .inheritIO()
        .directory(instrumentDir.toFile())
        .command("java", classFileName);
    try {
      int exitCode = builder.start().waitFor();
      if (exitCode != 0) {
        System.out.println("Error executing compiled classes.");
        exit(exitCode);
      }
    } catch (IOException | InterruptedException e) {
      throw new RuntimeException(e);
    }
  }

  public void generateReport() {
    ReportGenerator report = new ReportGenerator(mainJavaFile.foundBlocks);
    int[] blockCounts;
    try {
      blockCounts = Arrays.stream(Files.readString(Constants.resultsFile).split(" "))
          .mapToInt(Integer::parseInt).toArray();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    report.header(mainJavaFile.sourceFile.getFileName().toString());
    report.bodyStart();
    report.heading(mainJavaFile.sourceFile.getFileName().toString());
    int[] fileBlockCounts = Arrays.stream(blockCounts).limit(mainJavaFile.foundBlocks.size()).toArray();
    report.codeDiv(mainJavaFile, fileBlockCounts);
    report.bodyEnd();
    report.write(Path.of("./htmlReport.html"));
  }
}
