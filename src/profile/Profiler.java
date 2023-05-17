package profile;

import misc.Constants;
import misc.Util;
import model.JavaFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

import static java.lang.System.exit;
import static misc.Constants.*;

public class Profiler {
  JavaFile mainJavaFile;
  JavaFile[] additionalJavaFiles;

  public Profiler(JavaFile mainJavaFile, JavaFile... additionalJavaFiles) {
    this.mainJavaFile = mainJavaFile;
    this.additionalJavaFiles = additionalJavaFiles;
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

  public void profile(String[] programArgs) {
    Path mainFile = mainJavaFile.instrumentedFile;
    String fileName = mainFile.getFileName().toString();
    String classFileName = fileName.substring(0, fileName.lastIndexOf("."));
    ProcessBuilder builder = new ProcessBuilder()
        .inheritIO()
        .directory(instrumentDir.toFile())
        .command(Util.prependToArray(programArgs, "java", classFileName));
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
    report.write(reportIndexFile); // TODO: generate by file and index
    copyJavaScriptFiles();
  }

  private void copyJavaScriptFiles() {
    String highlighter = "highlighter.js";
    Util.copyResource("/js/" + highlighter, reportDir.resolve(highlighter));
  }
}
