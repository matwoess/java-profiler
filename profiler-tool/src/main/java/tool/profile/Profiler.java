package tool.profile;

import common.IO;
import common.JCompilerCommandBuilder;
import common.JavaCommandBuilder;
import common.Util;
import tool.model.Block;
import tool.model.JClass;
import tool.model.JavaFile;
import tool.model.Metadata;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.PrimitiveIterator;

public class Profiler {
  final JavaFile mainJavaFile;
  final JavaFile[] additionalJavaFiles;

  public Profiler(JavaFile mainJavaFile, JavaFile... additionalJavaFiles) {
    this.mainJavaFile = mainJavaFile;
    this.additionalJavaFiles = additionalJavaFiles;
  }

  public void compileInstrumented() {
    copyAuxiliaryFiles();
    Path mainFile = IO.getInstrumentedFilePath(mainJavaFile.relativePath);
    int exitCode = Util.runCommand(new JCompilerCommandBuilder()
        .setClassPath(IO.getInstrumentDir())
        .setDirectory(IO.getClassesDir())
        .addSourceFile(mainFile)
        .build());
    if (exitCode != 0) {
      throw new RuntimeException("Error compiling instrumented file: " + mainFile);
    }
  }

  public static void copyAuxiliaryFiles() {
    IO.copyResource(Profiler.class, "auxiliary/__Counter.class", IO.getAuxiliaryCounterClassPath());
  }

  public void profile(String[] programArgs) {
    Path mainFile = IO.getInstrumentDir().relativize(IO.getInstrumentedFilePath(mainJavaFile.relativePath));
    String filePath = mainFile.toString();
    String classFilePath = filePath.substring(0, filePath.lastIndexOf("."));
    if (File.separatorChar == '\\') {
      classFilePath = classFilePath.replace("\\", "/");
    }
    System.out.println("Program output:");
    int exitCode = Util.runCommand(new JavaCommandBuilder()
        .setClassPath(IO.getClassesDir())
        .setMainClass(classFilePath)
        .addArgs(programArgs)
        .build());
    if (exitCode != 0) {
      throw new RuntimeException("Error executing compiled class: " + classFilePath);
    }
  }

  public void generateReport() {
    JavaFile[] allJavaFiles;
    if (mainJavaFile != null) {
      allJavaFiles = Util.prependToArray(additionalJavaFiles, mainJavaFile);
    } else {
      allJavaFiles = Metadata.importMetadata(IO.getMetadataPath()).javaFiles();
    }
    addHitCountToJavaFileBlocks(allJavaFiles);
    IO.clearDirectoryContents(IO.getReportDir());
    new ReportClassIndexWriter(allJavaFiles).write();
    for (JavaFile jFile : allJavaFiles) {
      if (jFile.foundBlocks.isEmpty()) {
        continue;
      }
      new ReportSourceWriter(jFile).write();
      for (JClass clazz : jFile.topLevelClasses) {
        new ReportMethodIndexWriter(clazz, jFile).write();
      }
    }
    copyJavaScriptFiles();
  }

  private static void addHitCountToJavaFileBlocks(JavaFile[] allJavaFiles) {
    int[] counts;
    try (DataInputStream dis = new DataInputStream(new FileInputStream(IO.getCountsPath().toString()))) {
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
        if (block.blockType.hasNoCounter()) {
          continue;
        }
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

  public static void copyJavaScriptFiles() {
    IO.copyResource(Profiler.class, "highlighter.js", IO.getReportHighlighterPath());
  }

  public void createSymLinkForReport() {
    IO.createSymbolicLink(IO.getReportIndexSymLinkPath(), IO.getReportIndexPath());
  }
}
