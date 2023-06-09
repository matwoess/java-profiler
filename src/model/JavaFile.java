package model;

import misc.Constants;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static misc.Constants.instrumentDir;

public class JavaFile implements Serializable {
  public int beginOfImports = 0;
  public List<Class> foundClasses;
  public List<Block> foundBlocks;
  public transient Path sourceFile;
  public transient Path instrumentedFile;

  public JavaFile(Path sourceFile, Path sourcesRoot) {
    this.sourceFile = sourceFile;
    Path relativePathToSources = sourcesRoot.relativize(sourceFile);
    this.instrumentedFile = instrumentDir.resolve(relativePathToSources);
  }

  public JavaFile(Path sourceFile) {
    this.sourceFile = sourceFile;
    this.instrumentedFile = instrumentDir.resolve(sourceFile);
  }

  public Path getReportFile() {
    return Constants.reportDir.resolve(sourceFile);
  }

  public Path getReportHtmlFile() {
    Path reportFilePath = getReportFile();
    return reportFilePath.resolveSibling(reportFilePath.getFileName().toString().replace(".java", ".html"));
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
