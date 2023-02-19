package common;

import instrument.Parser;

import java.nio.file.Path;
import java.util.List;

import static common.Constants.instrumentDir;

public class JavaFile {
  public Path sourceFile;
  public int beginOfImports = 0;
  public List<Parser.Class> foundClasses;
  public List<Parser.Block> foundBlocks;
  public Path instrumentedFile;

  public JavaFile(Path sourceFile, Path sourcesRoot) {
    this.sourceFile = sourceFile;
    Path relativePathToSources = sourcesRoot.relativize(sourceFile);
    this.instrumentedFile = instrumentDir.resolve(relativePathToSources);
  }
}
