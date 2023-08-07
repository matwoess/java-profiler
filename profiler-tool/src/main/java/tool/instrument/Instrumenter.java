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
    System.out.println("Reading File: \"" + javaFile.sourceFile + "\"");
    Parser parser = new Parser(new Scanner(javaFile.sourceFile.toString()));
    parser.state.verbose = verboseOutput;
    parser.Parse();
    int errors = parser.errors.count;
    System.out.printf("Errors found: %d\n\n", errors);
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
    IO.copyAuxiliaryFiles();
    System.out.println();
    System.out.println("Total block found: " + blockCounter);
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
      // insert order is important, in case of same CodeInsert char positions
      if (block.blockType.hasNoBraces() && block.blockType != BlockType.SS_LAMBDA) {
        assert block.blockType != BlockType.METHOD;
        inserts.add(new CodeInsert(block.begPos, "{"));
      }
      if (block.blockType == BlockType.SS_LAMBDA) {
        inserts.add(new CodeInsert(block.getIncInsertPos(), String.format("__Counter.incLambda%s(%d, () -> ", incRefAdd, blockCounter++)));
        inserts.add(new CodeInsert(block.endPos, ")"));
      } else {
        inserts.add(new CodeInsert(block.getIncInsertPos(), String.format("__Counter.inc%s(%d);", incRefAdd, blockCounter++)));
      }
      if (block.blockType == BlockType.SS_SWITCH_EXPR_ARROW_CASE && !block.startsWithThrow) {
        inserts.add(new CodeInsert(block.getIncInsertPos(), "yield "));
      }
      if (block.blockType.hasNoBraces() && block.blockType != BlockType.SS_LAMBDA) {
        inserts.add(new CodeInsert(block.endPos, "}"));
      }
    }
    inserts.sort(Comparator.comparing(CodeInsert::chPos));
    return inserts;
  }

  public void exportMetadata() {
    new Metadata(blockCounter, javaFiles).exportMetadata();
  }

}