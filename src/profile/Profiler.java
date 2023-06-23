package profile;

import misc.IO;
import misc.Util;
import model.Block;
import model.Class;
import model.JavaFile;

import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.PrimitiveIterator;

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
        .directory(IO.instrumentDir.toFile())
        .command("javac", mainFile.getFileName().toString());
    try {
      int exitCode = builder.start().waitFor();
      if (exitCode != 0) {
        throw new RuntimeException("Error compiling instrumented file: " + mainFile);
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
        .directory(IO.instrumentDir.toFile())
        .command(Util.prependToArray(programArgs, "java", classFileName));
    try {
      int exitCode = builder.start().waitFor();
      if (exitCode != 0) {
        throw new RuntimeException("Error executing compiled class: " + classFileName);
      }
    } catch (IOException | InterruptedException e) {
      throw new RuntimeException(e);
    }
  }

  public void generateReport() {
    JavaFile[] allJavaFiles;
    if (mainJavaFile != null) {
      allJavaFiles = Util.prependToArray(additionalJavaFiles, mainJavaFile);
    } else {
      allJavaFiles = IO.importMetadata().javaFiles();
    }
    addHitCountToJavaFileBlocks(allJavaFiles);
    new ReportClassIndexWriter(allJavaFiles).write();
    for (JavaFile jFile : allJavaFiles) {
      new ReportSourceWriter(jFile).write();
      for (Class clazz : jFile.topLevelClasses) {
        new ReportMethodIndexWriter(clazz, jFile).write();
      }
    }
    IO.copyJavaScriptFiles();
  }

  private static void addHitCountToJavaFileBlocks(JavaFile[] allJavaFiles) {
    int[] counts;
    try (DataInputStream dis = new DataInputStream(new FileInputStream(IO.resultsFile.toString()))) {
      int nCounts = dis.readInt();
      counts = new int[nCounts];
      for (int i = 0; i < nCounts; i++) {
        counts[i] = dis.readInt();
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    PrimitiveIterator.OfInt allBlockCounts = Arrays.stream(counts).iterator();
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

  public void createSymLinkForReport() {
    IO.createSymbolicLink(IO.reportIndexSymLink, IO.reportIndexFile);
  }
}
