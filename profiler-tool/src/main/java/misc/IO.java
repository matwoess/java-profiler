package misc;

import model.JavaFile;

import java.io.*;
import java.nio.file.FileSystemException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.Objects;
import java.util.stream.Stream;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

public class IO {
  public static final Path DEFAULT_OUT_DIR = Path.of("out", "profiler");
  public static Path outputDir;

  public record Metadata(int blocksCount, JavaFile[] javaFiles) {
  }

  public static Path getOutputDir() {
    return Objects.requireNonNullElse(outputDir, DEFAULT_OUT_DIR);
  }

  public static Path getInstrumentDir() {
    return getOutputDir().resolve("instrumented");
  }

  public static Path getInstrumentedFilePath(JavaFile javaFile) {
    return getInstrumentDir().resolve(javaFile.relativePath);
  }

  public static Path getAuxiliaryInstrumentDir() {
    return getInstrumentDir().resolve("auxiliary");
  }

  public static Path getMetadataPath() {
    return getOutputDir().resolve("metadata.dat");
  }

  public static Path getCountsPath() {
    return getOutputDir().resolve("counts.dat");
  }

  public static Path getReportDir() {
    return getOutputDir().resolve("report");
  }

  public static Path getReportIndexPath() {
    return getReportDir().resolve("index.html");
  }

  public static Path getReportHighlighterPath() {
    return getReportDir().resolve("highlighter.js");
  }

  public static Path getReportIndexSymLinkPath() {
    return Path.of(".", "report.html");
  }

  public static Path getReportMethodIndexPath(model.Class clazz) {
    return IO.getReportDir().resolve("index_" + clazz.name + ".html");
  }

  public static Path getReportSourceFilePath(JavaFile javaFile) {
    Path reportFilePath = IO.getReportDir().resolve("source").resolve(javaFile.relativePath);
    return reportFilePath.resolveSibling(reportFilePath.getFileName().toString().replace(".java", ".html"));
  }

  public static void exportMetadata(Metadata metadata) {
    try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(getMetadataPath().toFile()))) {
      oos.writeInt(metadata.blocksCount);
      oos.writeObject(metadata.javaFiles);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public static Metadata importMetadata() {
    Metadata metadata;
    try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(getMetadataPath().toFile()))) {
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
    copyResource("/auxiliary/" + counterClass, getAuxiliaryInstrumentDir().resolve(Path.of(counterClass)));
  }

  public static void copyJavaScriptFiles() {
    String highlighter = "highlighter.js";
    copyResource("/js/" + highlighter, getReportDir().resolve(highlighter));
  }

  public static void clearDirectoryIfExists(Path directory) {
    if (Files.exists(directory)) {
      try (Stream<Path> walk = Files.walk(directory)) {
        walk.sorted(Comparator.reverseOrder()).forEach(file -> {
          try {
            Files.delete(file);
          } catch (IOException e) {
            throw new RuntimeException(e);
          }
        });
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }
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

}
