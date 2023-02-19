package profile;

import common.JavaFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

import static common.Constants.*;
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
    try (Stream<Path> walk = Files.walk(auxiliarySourceDir)) {
      walk.filter(Files::isRegularFile).forEach(filePath -> {
            try {
              Files.copy(filePath, auxiliaryInstrumentDir.resolve(filePath.getFileName()), REPLACE_EXISTING);
            } catch (IOException e) {
              throw new RuntimeException(e);
            }
          }
      );
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
    throw new UnsupportedOperationException("not yet implemented");
  }
}
