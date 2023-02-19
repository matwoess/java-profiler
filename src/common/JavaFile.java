package common;

import instrument.Parser;

import java.nio.file.Path;
import java.util.List;

import static common.Constants.outputDir;

public class JavaFile {
  public Path sourceFile;
  public List<Parser.Class> foundClasses;
  public List<Parser.Block> foundBlocks;
  public Path instrumentedFile;

  public JavaFile(String sourceFilePathString) {
    sourceFile = Path.of(sourceFilePathString);
    instrumentedFile = outputDir.resolve(sourceFile.getFileName());
  }
}
