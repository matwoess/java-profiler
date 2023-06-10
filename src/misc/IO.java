package misc;

import model.JavaFile;

import java.io.*;
import java.nio.file.Path;

public class IO {
  public static final Path outputDir = Path.of("out", "profiler");
  public static final Path instrumentDir = outputDir.resolve("instrumented");
  public static final Path auxiliaryInstrumentDir = instrumentDir.resolve("auxiliary");
  public static final Path metadataFile = outputDir.resolve("metadata.dat");
  public static final Path resultsFile = outputDir.resolve("counts.dat");

  public static final Path reportDir = outputDir.resolve("report");
  public static final Path reportIndexFile = reportDir.resolve("index.html");
  public static final Path reportHighlighter = reportDir.resolve("highlighter.js");

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

  public static void copyAuxiliaryFiles() {
    String counterClass = "__Counter.class";
    Util.copyResource("/auxiliary/" + counterClass, auxiliaryInstrumentDir.resolve(Path.of(counterClass)));
  }

  public static void copyJavaScriptFiles() {
    String highlighter = "highlighter.js";
    Util.copyResource("/js/" + highlighter, reportDir.resolve(highlighter));
  }

  @SuppressWarnings("UnusedReturnValue")
  public static boolean createDirectoriesIfNotExists(Path fileOrFolder) {
    if (fileOrFolder.toFile().isDirectory()) {
      return fileOrFolder.toFile().mkdirs();
    } else {
      return fileOrFolder.getParent().toFile().mkdirs();
    }
  }
}
