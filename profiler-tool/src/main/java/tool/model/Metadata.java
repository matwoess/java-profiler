package tool.model;

import java.io.*;

import static common.IO.getMetadataPath;

public record Metadata(int blocksCount, JavaFile[] javaFiles) {
  public void exportMetadata() {
    try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(getMetadataPath().toFile()))) {
      oos.writeInt(blocksCount);
      oos.writeObject(javaFiles);
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
}
