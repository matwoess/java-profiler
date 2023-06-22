package misc;

import model.JavaFile;

import java.io.*;
import java.nio.file.FileSystemException;
import java.nio.file.Files;
import java.nio.file.Path;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

public class IO {
  public static final Path outputDir = Path.of("out", "profiler");
  public static final Path instrumentDir = outputDir.resolve("instrumented");
  public static final Path auxiliaryInstrumentDir = instrumentDir.resolve("auxiliary");
  public static final Path metadataFile = outputDir.resolve("metadata.dat");
  public static final Path resultsFile = outputDir.resolve("counts.dat");

  public static final Path reportDir = outputDir.resolve("report");
  public static final Path reportIndexFile = reportDir.resolve("index.html");
  public static final Path reportHighlighter = reportDir.resolve("highlighter.js");
  public static final Path reportIndexSymLink = Path.of(".", "report.html");

  public record Metadata(int blocksCount, JavaFile[] javaFiles) {
  }

  public static void exportMetadata(Metadata metadata) {
    try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(metadataFile.toFile()))) {
      oos.writeInt(metadata.blocksCount);
      oos.writeObject(metadata.javaFiles);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public static Metadata importMetadata() {
    Metadata metadata;
    try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(metadataFile.toFile()))) {
      int blocksCount = ois.readInt();
      JavaFile[] javaFiles = (JavaFile[]) ois.readObject();
      metadata = new Metadata(blocksCount, javaFiles);
    } catch (IOException | ClassNotFoundException e) {
      throw new RuntimeException(e);
    }
    return metadata;
  }

  public static void copyResource(String resourceName, Path destination) {
    try (InputStream resource = Util.class.getResourceAsStream(resourceName);) {
      if (resource == null) {
        throw new RuntimeException("unable to locate resource: <" + resourceName + ">");
      }
      IO.createDirectoriesIfNotExists(destination);
      Files.copy(resource, destination, REPLACE_EXISTING);
    } catch (IOException | RuntimeException e) {
      throw new RuntimeException(e);
    }
  }

  public static void copyAuxiliaryFiles() {
    String counterClass = "__Counter.class";
    copyResource("/auxiliary/" + counterClass, auxiliaryInstrumentDir.resolve(Path.of(counterClass)));
  }

  public static void copyJavaScriptFiles() {
    String highlighter = "highlighter.js";
    copyResource("/js/" + highlighter, reportDir.resolve(highlighter));
  }

  @SuppressWarnings("UnusedReturnValue")
  public static boolean createDirectoriesIfNotExists(Path fileOrFolder) {
    if (fileOrFolder.toFile().isDirectory()) {
      return fileOrFolder.toFile().mkdirs();
    } else {
      return fileOrFolder.getParent().toFile().mkdirs();
    }
  }

  public static void createSymbolicLink(Path link, Path target) {
    try {
      if (Files.exists(link) && Files.isSymbolicLink(link)) {
        Files.delete(link);
      }
      Files.createSymbolicLink(link, target);
    } catch (FileSystemException e) {
      System.out.println(e.getMessage());
      System.out.println("Unable to create report symlink. Not supported or allowed by file system.");
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public static Path getReportMethodIndexPath(model.Class clazz) {
    return IO.reportDir.resolve("index_" + clazz.name + ".html");
  }

  public static Path getReportSourceFilePath(JavaFile javaFile) {
    Path reportFilePath = IO.reportDir.resolve(javaFile.sourceFile);
    return reportFilePath.resolveSibling(reportFilePath.getFileName().toString().replace(".java", ".html"));
  }
}
