package profile;

import misc.Constants;
import misc.Util;
import model.Block;
import model.JavaFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.PrimitiveIterator;

import static java.lang.System.exit;
import static misc.Constants.instrumentDir;
import static misc.Constants.reportDir;

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
    // TODO: read metadata and create JavaFile list instead
    JavaFile[] allJavaFiles = Util.prependToArray(additionalJavaFiles, mainJavaFile);
    addHitCountToJavaFileBlocks(allJavaFiles);
    for (JavaFile jFile : allJavaFiles) {
      generateReportFile(jFile);
    }
    ReportIndexWriter index = new ReportIndexWriter();
    index.header();
    index.bodyStart();
    index.sortedFileTable(allJavaFiles);
    index.write(Constants.reportIndexFile);
    index.bodyEnd();
    copyJavaScriptFiles();
  }

  private static void addHitCountToJavaFileBlocks(JavaFile[] allJavaFiles) {
    PrimitiveIterator.OfInt allBlockCounts;
    try {
      allBlockCounts = Arrays.stream(Files.readString(Constants.resultsFile).split(" "))
          .mapToInt(Integer::parseInt).iterator();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    for (JavaFile jFile : allJavaFiles) {
      for (Block block : jFile.foundBlocks) {
        if (!allBlockCounts.hasNext()) {
          throw new RuntimeException("Ran out of block counts. Mismatching entry counts");
        }
        block.hits = allBlockCounts.next();
      }
    }
    if (allBlockCounts.hasNext()) {
      throw new RuntimeException("Too many block counts. Mismatching entry counts!");
    }
  }

  private void generateReportFile(JavaFile jFile) {
    String fileName = jFile.sourceFile.getFileName().toString();
    ReportSourceWriter report = new ReportSourceWriter(jFile, fileName);
    report.header();
    report.bodyStart();
    report.heading(fileName);
    report.codeDiv();
    report.bodyEnd();
    report.write(jFile.getReportHtmlFile());
  }

  private void copyJavaScriptFiles() {
    String highlighter = "highlighter.js";
    Util.copyResource("/js/" + highlighter, reportDir.resolve(highlighter));
  }
}
