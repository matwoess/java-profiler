package model;

import misc.IO;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class JavaFile implements Serializable {
  public int beginOfImports = 0;
  public String packageName;
  public List<Class> topLevelClasses;
  public List<Block> foundBlocks;
  public transient Path sourceFile;
  public transient Path instrumentedFile;

  public JavaFile(Path sourceFile, Path sourcesRoot) {
    this.sourceFile = sourceFile;
    Path relativePathToSources = sourcesRoot.relativize(sourceFile);
    this.instrumentedFile = IO.getInstrumentDir().resolve(relativePathToSources);
  }

  public JavaFile(Path sourceFile) {
    this.sourceFile = sourceFile;
    this.instrumentedFile = IO.getInstrumentDir().resolve(sourceFile.getFileName());
  }

  public List<Class> getClassesRecursive() {
    List<Class> allClasses = new ArrayList<>(topLevelClasses);
    for (Class clazz : topLevelClasses) {
      allClasses.addAll(clazz.getClassesRecursive());
    }
    return allClasses;
  }

  @Serial
  private void writeObject(ObjectOutputStream oos) throws IOException {
    oos.defaultWriteObject();
    oos.writeUTF(sourceFile.toString());
    oos.writeUTF(instrumentedFile.toString());
  }

  @Serial
  private void readObject(ObjectInputStream ois) throws IOException, ClassNotFoundException {
    ois.defaultReadObject();
    sourceFile = Paths.get(ois.readUTF());
    instrumentedFile = Paths.get(ois.readUTF());
  }
}
