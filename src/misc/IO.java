package misc;

import model.JavaFile;

import java.io.*;

import static misc.Constants.metadataFile;

public class IO {

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

  public record Metadata(int blocksCount, JavaFile[] javaFiles) {
  }
}
