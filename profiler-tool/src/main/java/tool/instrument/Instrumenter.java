package tool.instrument;

import tool.model.CodeInsert;
import common.IO;
import tool.model.*;
import tool.model.BlockType;
import tool.model.JavaFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class Instrumenter {
  JavaFile[] javaFiles;
  int blockCounter;
  public String incRefAdd;
  boolean verboseOutput;

  public Instrumenter(boolean syncCounters, boolean verboseOutput, JavaFile... javaFiles) {
    assert javaFiles.length > 0;
    this.verboseOutput = verboseOutput;
    incRefAdd = (syncCounters) ? "Sync" : "";
    this.javaFiles = javaFiles;
  }

  public void analyzeFiles() {
    for (JavaFile javaFile : javaFiles) {
      analyze(javaFile);
    }
  }

  void analyze(JavaFile javaFile) {
    System.out.println("Parsing file: \"" + javaFile.sourceFile + "\"");
    Parser parser = new Parser(new Scanner(javaFile.sourceFile.toString()));
    parser.state.verbose = verboseOutput;
    parser.Parse();
    int errors = parser.errors.count;
    if (errors > 0) {
      throw new RuntimeException("Abort due to parse errors.");
    }
    javaFile.packageName = parser.state.packageName;
    javaFile.beginOfImports = parser.state.beginOfImports;
    javaFile.foundBlocks = parser.state.allBlocks;
    javaFile.topLevelClasses = parser.state.topLevelClasses;
  }

  public void instrumentFiles() {
    IO.clearDirectoryIfExists(IO.getInstrumentDir());
    blockCounter = 0;
    try {
      for (JavaFile javaFile : javaFiles) {
        instrument(javaFile);
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    copyAuxiliaryFiles();
    System.out.println();
    System.out.println("Total code block found: " + blockCounter);
  }

  void instrument(JavaFile javaFile) throws IOException {
    List<CodeInsert> codeInserts = getCodeInserts(javaFile);
    String fileContent = Files.readString(javaFile.sourceFile, StandardCharsets.ISO_8859_1);
    StringBuilder builder = new StringBuilder();
    int prevIdx = 0;
    for (CodeInsert codeInsert : codeInserts) {
      builder.append(fileContent, prevIdx, codeInsert.chPos());
      prevIdx = codeInsert.chPos();
      builder.append(codeInsert.code());
    }
    builder.append(fileContent.substring(prevIdx));
    Path instrumentedFilePath = IO.getInstrumentedFilePath(javaFile.relativePath);
    IO.createDirectoriesIfNotExists(instrumentedFilePath);
    Files.writeString(instrumentedFilePath, builder.toString());
  }

  List<CodeInsert> getCodeInserts(JavaFile javaFile) {
    List<CodeInsert> inserts = new ArrayList<>();
    inserts.add(new CodeInsert(javaFile.beginOfImports, "import auxiliary.__Counter;"));
    for (Block block : javaFile.foundBlocks) {
      if (block.blockType.isSwitch()) {
        continue;
      }
      // insert order is important, in case of same CodeInsert char positions
      if (block.isSingleStatement && block.blockType != BlockType.LAMBDA) {
        assert block.blockType != BlockType.METHOD;
        inserts.add(new CodeInsert(block.beg.pos(), "{"));
      }
      if (block.isSingleStatement && block.blockType == BlockType.LAMBDA) {
        inserts.add(new CodeInsert(block.getIncInsertPos(), String.format("__Counter.incLambda%s(%d, () -> ", incRefAdd, blockCounter++)));
        inserts.add(new CodeInsert(block.end.pos(), ")"));
      } else {
        inserts.add(new CodeInsert(block.getIncInsertPos(), String.format("__Counter.inc%s(%d);", incRefAdd, blockCounter++)));
      }
      if (block.isSingleStatement && block.isSwitchExpressionCase() && block.jumpStatement != JumpStatement.THROW) {
        inserts.add(new CodeInsert(block.getIncInsertPos(), "yield "));
      }
      if (block.isSingleStatement && block.blockType != BlockType.LAMBDA) {
        inserts.add(new CodeInsert(block.end.pos(), "}"));
      }
    }
    inserts.sort(Comparator.comparing(CodeInsert::chPos));
    return inserts;
  }

  public void exportMetadata() {
    new Metadata(blockCounter, javaFiles).exportMetadata();
  }


  public static void copyAuxiliaryFiles() {
    IO.copyResource(Instrumenter.class, "auxiliary/__Counter.class", IO.getAuxiliaryCounterClassPath());
  }
}