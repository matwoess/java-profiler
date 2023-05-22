package model;

import misc.Constants;

import java.nio.file.Path;
import java.util.List;

import static misc.Constants.instrumentDir;

public class JavaFile {
  public Path sourceFile;
  public int beginOfImports = 0;
  public List<Class> foundClasses;
  public List<Block> foundBlocks;
  public Path instrumentedFile;

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

  public int getAggregatedMethodBlockCounts() {
    return foundBlocks.stream().filter(b -> b.blockType == BlockType.METHOD).mapToInt(b -> b.hits).sum();
  }
}
