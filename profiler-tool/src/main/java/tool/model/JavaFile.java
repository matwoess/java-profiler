package tool.model;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class JavaFile implements Serializable {
  public int beginOfImports = 0;
  public String packageName;
  public List<JClass> topLevelClasses;
  public List<Block> foundBlocks;
  public transient Path sourceFile;
  public transient Path relativePath;

  public JavaFile(Path sourceFile, Path sourcesRoot) {
    this.sourceFile = sourceFile;
    this.relativePath = sourcesRoot.relativize(sourceFile);
  }

  public JavaFile(Path sourceFile) {
    this.sourceFile = sourceFile;
    this.relativePath = sourceFile.getFileName();
  }

  public List<JClass> getClassesRecursive() {
    List<JClass> allClasses = new ArrayList<>(topLevelClasses);
    for (JClass clazz : topLevelClasses) {
      allClasses.addAll(clazz.getClassesRecursive());
    }
    return allClasses;
  }

  @Serial
  private void writeObject(ObjectOutputStream oos) throws IOException {
    oos.defaultWriteObject();
    oos.writeUTF(sourceFile.toString());
    oos.writeUTF(relativePath.toString());
  }

  @Serial
  private void readObject(ObjectInputStream ois) throws IOException, ClassNotFoundException {
    ois.defaultReadObject();
    sourceFile = Paths.get(ois.readUTF());
    relativePath = Paths.get(ois.readUTF());
  }
}
