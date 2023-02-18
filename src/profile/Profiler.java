package profile;

import common.JavaFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static common.Constants.profilerRoot;
import static java.lang.System.exit;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

public class Profiler {
  JavaFile[] javaFiles;
  Path __counterFile;

  public Profiler(JavaFile... javaFiles) {
    this.javaFiles = javaFiles;
    __counterFile = profilerRoot.resolve("__Counter.java");
    Path __counterPath = Path.of("src/profile/__Counter.java");
    try {
      Files.copy(__counterPath, __counterFile, REPLACE_EXISTING);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public void compileInstrumented() {
    Path mainFile = javaFiles[0].instrumentedFile;
    ProcessBuilder builder = new ProcessBuilder()
        .inheritIO()
        .directory(mainFile.getParent().toFile())
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
    Path mainFile = javaFiles[0].instrumentedFile;
    String fileName = mainFile.getFileName().toString();
    String classFileName = fileName.substring(0, fileName.lastIndexOf("."));
    ProcessBuilder builder = new ProcessBuilder()
        .inheritIO()
        .directory(mainFile.getParent().toFile())
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
    throw new UnsupportedOperationException("not yet implemented");
  }
}
