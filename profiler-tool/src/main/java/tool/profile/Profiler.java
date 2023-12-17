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
  private final JavaFile mainJavaFile;
  private final JavaFile[] additionalJavaFiles;

  /**
   * Creates a new {@link Profiler} object with the given main file and an array of additional files to profile.
   *
   * @param mainJavaFile        the main file to profile
   * @param additionalJavaFiles the additional dependent project files to profile
   */
  public Profiler(JavaFile mainJavaFile, JavaFile... additionalJavaFiles) {
    this.mainJavaFile = mainJavaFile;
    this.additionalJavaFiles = additionalJavaFiles;
  }

  /**
   * Compiles the main instrumented file.
   * The <code>javac</code> tool will automatically compile all referenced instrumented java files.
   * The working directory is not changed during this process.
   */
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

  /**
   * Copies the <code>__Counter.class</code> file to the auxiliary directory inside the classes directory.
   */
  private static void copyAuxiliaryFiles() {
    IO.copyResource(Profiler.class, "auxiliary/__Counter.class", IO.getAuxiliaryCounterClassPath());
  }

  /**
   * Runs the instrumented and compiled main file with the given program arguments.
   * <p>
   * The name of the class file is determined by the relative path of the main file
   * and removing the <code>.java</code> extension.
   *
   * @param programArgs the program arguments to pass to the main method
   */
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

  /**
   * Generates the report from the metadata and counts files.
   * <p>
   * Before creation, the hit counts are added to the blocks from the counts file data.
   * <p>
   * First the report directory is cleared.
   * Then the following files are written:
   * <ul>
   *   <li>the index file</li>
   *   <li>the source files</li>
   *   <li>the method index files for each class in the source files</li>
   * </ul>
   * Finally, the highlighting JavaScript file is copied to the report directory using {@link #copyJavaScriptFiles}.
   */
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
        continue; // exclude files without code blocks
      }
      new ReportSourceWriter(jFile).write();
      for (JClass clazz : jFile.topLevelClasses) {
        new ReportMethodIndexWriter(clazz, jFile).write();
      }
    }
    copyJavaScriptFiles();
  }

  /**
   * Populate hit counts of blocks from the counts file data.
   * <p>
   * An error is thrown if the number of counts does not match the number of blocks.
   *
   * @param allJavaFiles the list of all java files contained in the project
   */
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

  /**
   * Copies the <code>highlighter.js</code> JavaScript file to the report directory.
   */
  private static void copyJavaScriptFiles() {
    IO.copyResource(Profiler.class, "highlighter.js", IO.getReportHighlighterPath());
  }

  /**
   * Creates a symbolic link to the main report index file in the current directory.
   * Should work on Linux and macOS but might not work on Windows, depending on group policy rules.
   */
  public void createSymLinkForReport() {
    IO.createSymbolicLink(IO.getReportIndexSymLinkPath(), IO.getReportIndexPath());
  }
}
